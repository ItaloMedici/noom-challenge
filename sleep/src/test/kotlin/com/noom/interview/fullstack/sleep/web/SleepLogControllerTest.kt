package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.application.CreateSleepLogCommand
import com.noom.interview.fullstack.sleep.application.CreateSleepLogUseCase
import com.noom.interview.fullstack.sleep.application.GetLastNightSleepUseCase
import com.noom.interview.fullstack.sleep.application.GetSleepStatisticsUseCase
import com.noom.interview.fullstack.sleep.application.GetStatsCommand
import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.domain.model.DateRange
import com.noom.interview.fullstack.sleep.web.dto.CreateSleepLogRequestDto
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class SleepLogControllerTest {

    private val getSleepStatisticsUseCase = mockk<GetSleepStatisticsUseCase>()
    private val createSleepLogUseCase = mockk<CreateSleepLogUseCase>()
    private val getLastNightSleepUseCase = mockk<GetLastNightSleepUseCase>()
    private val controller = SleepLogController(
        getSleepStatisticsUseCase,
        createSleepLogUseCase,
        getLastNightSleepUseCase,
    )
    private val mockMvc = MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(ApiExceptionHandler())
        .build()

    @Test
    fun `should return 200 with mapped statistics from the use case`() {
        val userId = UUID.randomUUID()
        val calculation = SleepStatistics.SleepStatisticsCalculation(
            range = DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6)),
            averageTimeInBed = Duration.ofHours(8),
            averageBedTime = LocalTime.of(22, 0),
            averageWakeTime = LocalTime.of(6, 0),
            moodFrequencies = mapOf(SleepLog.WakeUpMood.GOOD to 1),
        )

        every { getSleepStatisticsUseCase.execute(any()) } returns calculation

        val response = controller.getStatistics(userId, days = 30)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body.rangeStart).isEqualTo(LocalDate.of(2026, 8, 8))
        assertThat(body.rangeEnd).isEqualTo(LocalDate.of(2026, 9, 6))
        assertThat(body.averageDurationInSeconds).isEqualTo(Duration.ofHours(8).seconds)
        assertThat(body.averageBedTime).isEqualTo(LocalTime.of(22, 0))
        assertThat(body.averageWakeTime).isEqualTo(LocalTime.of(6, 0))
        assertThat(body.moodFrequencies).containsEntry("GOOD", 1)

        verify(exactly = 1) {
            getSleepStatisticsUseCase.execute(GetStatsCommand(userId = userId, days = 30))
        }
    }

    @Test
    fun `should return 200 with empty stats when the use case reports no data in range`() {
        val userId = UUID.randomUUID()
        val emptyCalculation = SleepStatistics.empty(
            DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))
        )

        every { getSleepStatisticsUseCase.execute(any()) } returns emptyCalculation

        val response = controller.getStatistics(userId, days = 30)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body.rangeStart).isEqualTo(LocalDate.of(2026, 8, 8))
        assertThat(body.rangeEnd).isEqualTo(LocalDate.of(2026, 9, 6))
        assertThat(body.averageDurationInSeconds).isNull()
        assertThat(body.averageBedTime).isNull()
        assertThat(body.averageWakeTime).isNull()
        assertThat(body.moodFrequencies).isEmpty()
    }

    @Test
    fun `should forward an absent days param to the use case`() {
        val userId = UUID.randomUUID()

        every { getSleepStatisticsUseCase.execute(any()) } returns SleepStatistics.empty(
            DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))
        )

        controller.getStatistics(userId)

        verify(exactly = 1) {
            getSleepStatisticsUseCase.execute(GetStatsCommand(userId = userId, days = null))
        }
    }

    @Test
    fun `should propagate exceptions from the use case`() {
        val userId = UUID.randomUUID()

        every {
            getSleepStatisticsUseCase.execute(any())
        } throws IllegalArgumentException("Number of days must be positive, but was 0")

        assertThrows(IllegalArgumentException::class.java) {
            controller.getStatistics(userId, days = 0)
        }
    }

    @Test
    fun `should return 201 with created sleep log`() {
        val userId = UUID.randomUUID()
        val requestDto = validCreateRequest()

        val createdSleepLog = SleepLog(
            id = UUID.randomUUID(),
            userId = userId,
            sleepDate = requestDto.sleepDate!!,
            bedTime = requestDto.bedTime!!,
            wakeTime = requestDto.wakeTime!!,
            mood = requestDto.mood!!
        )

        every { createSleepLogUseCase.execute(any()) } returns createdSleepLog

        val response = controller.createSleepLog(userId, requestDto)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        val body = response.body!!
        assertThat(body.id).isEqualTo(createdSleepLog.id)
        assertThat(body.userId).isEqualTo(userId)
        assertThat(body.sleepDate).isEqualTo(requestDto.sleepDate)
        assertThat(body.bedTime).isEqualTo(requestDto.bedTime)
        assertThat(body.wakeTime).isEqualTo(requestDto.wakeTime)
        assertThat(body.mood).isEqualTo(requestDto.mood)
        assertThat(body.durationInSeconds).isEqualTo(28800)

        verify(exactly = 1) {
            createSleepLogUseCase.execute(
                CreateSleepLogCommand(
                    userId = userId,
                    sleepDate = LocalDate.of(2026, 8, 8),
                    bedTime = LocalTime.of(22, 0),
                    wakeTime = LocalTime.of(6, 0),
                    mood = SleepLog.WakeUpMood.GOOD,
                )
            )
        }
    }

    @Test
    fun `should propagate user not found from the create use case`() {
        val userId = UUID.randomUUID()
        every { createSleepLogUseCase.execute(any()) } throws UserNotFoundException(userId)

        assertThrows(UserNotFoundException::class.java) {
            controller.createSleepLog(userId, validCreateRequest())
        }
    }

    @Test
    fun `should propagate domain rule violations from the create use case`() {
        every {
            createSleepLogUseCase.execute(any())
        } throws IllegalArgumentException("Bed time and wake time cannot be identical")

        assertThrows(IllegalArgumentException::class.java) {
            controller.createSleepLog(UUID.randomUUID(), validCreateRequest())
        }
    }

    private fun validCreateRequest() = CreateSleepLogRequestDto(
        sleepDate = LocalDate.of(2026, 8, 8),
        bedTime = LocalTime.of(22, 0),
        wakeTime = LocalTime.of(6, 0),
        mood = SleepLog.WakeUpMood.GOOD
    )

    @Test
    fun `should return 200 with the last night sleep log`() {
        val userId = UUID.randomUUID()
        val lastNightSleepLog = SleepLog(
            id = UUID.randomUUID(),
            userId = userId,
            sleepDate = LocalDate.now().minusDays(1),
            bedTime = LocalTime.of(22, 0),
            wakeTime = LocalTime.of(6, 0),
            mood = SleepLog.WakeUpMood.GOOD
        )

        every { getLastNightSleepUseCase.execute(userId) } returns lastNightSleepLog

        val response = controller.getLastNightSleep(userId)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body.id).isEqualTo(lastNightSleepLog.id)
        assertThat(body.userId).isEqualTo(userId)
        assertThat(body.sleepDate).isEqualTo(LocalDate.now().minusDays(1))
        assertThat(body.bedTime).isEqualTo(LocalTime.of(22, 0))
        assertThat(body.wakeTime).isEqualTo(LocalTime.of(6, 0))
        assertThat(body.mood).isEqualTo(SleepLog.WakeUpMood.GOOD)
        assertThat(body.durationInSeconds).isEqualTo(28800)

        verify(exactly = 1) { getLastNightSleepUseCase.execute(userId) }
    }

    @Test
    fun `should return 404 when the user has no sleep log for last night`() {
        val userId = UUID.randomUUID()

        every { getLastNightSleepUseCase.execute(userId) } returns null

        val response = controller.getLastNightSleep(userId)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body).isNull()
    }

    @Test
    fun `should propagate exceptions from the last night use case`() {
        val userId = UUID.randomUUID()

        every {
            getLastNightSleepUseCase.execute(any())
        } throws IllegalArgumentException("boom")

        assertThrows(IllegalArgumentException::class.java) {
            controller.getLastNightSleep(userId)
        }
    }

    @Test
    fun `should reject with 400 and the error contract when a required field is missing`() {
        val userId = UUID.randomUUID()
        val body = """
            {
              "sleepDate": "2026-09-06",
              "bedTime": "22:00",
              "wakeTime": "06:00"
            }
        """.trimIndent()

        mockMvc.post("/v1/users/$userId/sleep-logs") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Validation failed") }
            jsonPath("$.errors[0].field") { value("mood") }
            jsonPath("$.errors[0].message") { value("Mood is required") }
        }

        verify { createSleepLogUseCase wasNot Called }
    }

    @Test
    fun `should reject with 400 listing every missing field when the body is empty`() {
        val userId = UUID.randomUUID()

        mockMvc.post("/v1/users/$userId/sleep-logs") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Validation failed") }
            jsonPath("$.errors.length()") { value(4) }
        }

        verify { createSleepLogUseCase wasNot Called }
    }

    @Test
    fun `should reject with 400 when the body cannot be deserialized`() {
        val userId = UUID.randomUUID()
        val body = """
            {
              "sleepDate": "2026-09-06",
              "bedTime": "22:00",
              "wakeTime": "06:00",
              "mood": "AMAZING"
            }
        """.trimIndent()

        mockMvc.post("/v1/users/$userId/sleep-logs") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Malformed request body") }
            jsonPath("$.errors.length()") { value(0) }
        }

        verify { createSleepLogUseCase wasNot Called }
    }
}
