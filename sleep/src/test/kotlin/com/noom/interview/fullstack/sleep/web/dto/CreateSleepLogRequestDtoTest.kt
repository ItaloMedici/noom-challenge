package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.domain.SleepLog
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.validation.Validation
import javax.validation.Validator

class CreateSleepLogRequestDtoTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `should pass validation when every field is present`() {
        val request = logRequestDto()

        val violations = validator.validate(request)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when sleep date is null`() {
        val request = logRequestDto(sleepDate = null)

        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("sleepDate")
        assertThat(violation.message).isEqualTo("Sleep date is required")
    }

    @Test
    fun `should fail validation when bed time is null`() {
        val request = logRequestDto(bedTime = null)

        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("bedTime")
        assertThat(violation.message).isEqualTo("Bed time is required")
    }

    @Test
    fun `should fail validation when wake time is null`() {
        val request = logRequestDto(wakeTime = null)

        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("wakeTime")
        assertThat(violation.message).isEqualTo("Wake time is required")
    }

    @Test
    fun `should fail validation when mood is null`() {
        val request = logRequestDto(mood = null)

        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("mood")
        assertThat(violation.message).isEqualTo("Mood is required")
    }

    @Test
    fun `should report one violation per field when every field is null`() {
        val request = logRequestDto(sleepDate = null, bedTime = null, wakeTime = null, mood = null)

        val violations = validator.validate(request)

        assertThat(violations).hasSize(4)
        assertThat(violations.map { it.message }).containsExactlyInAnyOrder(
            "Sleep date is required",
            "Bed time is required",
            "Wake time is required",
            "Mood is required",
        )
    }

    @Test
    fun `should accept every supported wake up mood`() {
        SleepLog.WakeUpMood.values().forEach { mood ->
            val request = logRequestDto(mood = mood)

            val violations = validator.validate(request)

            assertThat(violations).isEmpty()
        }
    }

    @Test
    fun `should map to a create sleep log command carrying the given user id`() {
        val userId = UUID.randomUUID()
        val sleepDate = LocalDate.of(2026, 9, 6)
        val bedTime = LocalTime.of(23, 0)
        val wakeTime = LocalTime.of(7, 0)
        val request = logRequestDto(
            sleepDate = sleepDate,
            bedTime = bedTime,
            wakeTime = wakeTime,
            mood = SleepLog.WakeUpMood.GOOD,
        )

        val command = request.toCommand(userId)

        assertThat(command.userId).isEqualTo(userId)
        assertThat(command.sleepDate).isEqualTo(sleepDate)
        assertThat(command.bedTime).isEqualTo(bedTime)
        assertThat(command.wakeTime).isEqualTo(wakeTime)
        assertThat(command.mood).isEqualTo(SleepLog.WakeUpMood.GOOD)
    }

    @Test
    fun `should throw when mapping without validating and a field is null`() {
        val request = logRequestDto(mood = null)

        assertThatThrownBy { request.toCommand(UUID.randomUUID()) }
            .isInstanceOf(NullPointerException::class.java)
    }

    private fun logRequestDto(
        sleepDate: LocalDate? = LocalDate.of(2026, 9, 6),
        bedTime: LocalTime? = LocalTime.of(23, 0),
        wakeTime: LocalTime? = LocalTime.of(7, 0),
        mood: SleepLog.WakeUpMood? = SleepLog.WakeUpMood.GOOD,
    ) = CreateSleepLogRequestDto(
        sleepDate = sleepDate,
        bedTime = bedTime,
        wakeTime = wakeTime,
        mood = mood,
    )
}
