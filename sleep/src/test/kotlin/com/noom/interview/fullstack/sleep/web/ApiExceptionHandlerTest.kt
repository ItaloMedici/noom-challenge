package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.exception.DuplicateSleepLogException
import com.noom.interview.fullstack.sleep.domain.exception.InvalidDateRangeException
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.web.dto.CreateSleepLogRequestDto
import com.noom.interview.fullstack.sleep.web.dto.FieldErrorDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.MockHttpInputMessage
import org.springframework.validation.MapBindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import java.util.*

class ApiExceptionHandlerTest {

    private val handler = ApiExceptionHandler()

    @Test
    fun `should map InvalidDateRangeException to 400`() {
        val response = handler.handleInvalidDateRange(InvalidDateRangeException())

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message).isEqualTo("End date cannot be before start date")
    }

    @Test
    fun `should map UserNotFoundException to 404`() {
        val userId = UUID.randomUUID()
        val response = handler.handleUserNotFound(UserNotFoundException(userId))

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body!!.status).isEqualTo(404)
        assertThat(response.body!!.errors).isEmpty()
    }

    @Test
    fun `should map DuplicateSleepLogException to 409 with sleepDate field error`() {
        val userId = UUID.randomUUID()
        val sleepDate = java.time.LocalDate.now().minusDays(1)
        val response = handler.handleDuplicateSleepLog(DuplicateSleepLogException(sleepDate))

        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body!!.status).isEqualTo(409)
        assertThat(response.body!!.message).contains(sleepDate.toString())
        assertThat(response.body!!.errors)
            .containsExactly(FieldErrorDto("sleepDate", "Sleep log already exists on this date"))
    }

    @Test
    fun `should map IllegalArgumentException to 400`() {
        val response =
            handler.handleIllegalArgument(IllegalArgumentException("Bed time and wake time cannot be identical"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message).isEqualTo("Bed time and wake time cannot be identical")
        assertThat(response.body!!.errors).isEmpty()
    }

    @Test
    fun `should map MethodArgumentNotValidException to 400 with field-level errors`() {
        val bindingResult = MapBindingResult(mapOf<String, Any>(), "createSleepLogRequest")
        bindingResult.rejectValue("mood", "NotNull", "Mood is required")
        bindingResult.rejectValue("sleepDate", "NotNull", "Sleep date is required")

        val response = handler.handleMethodArgumentNotValid(
            MethodArgumentNotValidException(methodParameter(), bindingResult)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message).isEqualTo("Validation failed")
        assertThat(response.body!!.errors).hasSize(2)
        assertThat(response.body!!.errors.map { it.field })
            .containsExactlyInAnyOrder("mood", "sleepDate")
        assertThat(response.body!!.errors.map { it.message })
            .containsExactlyInAnyOrder("Mood is required", "Sleep date is required")
    }

    @Test
    fun `should map HttpMessageNotReadableException to 400 without leaking internals`() {
        val response = handler.handleUnreadableBody(
            HttpMessageNotReadableException("internal jackson detail", MockHttpInputMessage(ByteArray(0)))
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message).isEqualTo("Malformed request body")
        assertThat(response.body!!.message).doesNotContain("internal jackson detail")
    }

    @Test
    fun `should provide field and message for each validation error`() {
        val bindingResult = MapBindingResult(mapOf<String, Any>(), "createUserRequest")
        bindingResult.rejectValue("username", "NotBlank", "Username is required")

        val response = handler.handleMethodArgumentNotValid(
            MethodArgumentNotValidException(methodParameter(), bindingResult)
        )

        val fieldError = response.body!!.errors.first()
        assertThat(fieldError).isInstanceOf(FieldErrorDto::class.java)
        assertThat(fieldError.field).isEqualTo("username")
        assertThat(fieldError.message).isEqualTo("Username is required")
    }

    private fun methodParameter(): MethodParameter =
        MethodParameter(ApiExceptionHandlerTest::class.java.declaredMethods.first(), -1)
}
