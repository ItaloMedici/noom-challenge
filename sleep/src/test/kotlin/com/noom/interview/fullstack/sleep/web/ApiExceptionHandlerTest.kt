package com.noom.interview.fullstack.sleep.web

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.noom.interview.fullstack.sleep.application.CreateSleepLogUseCase
import com.noom.interview.fullstack.sleep.application.CreateUserUseCase
import com.noom.interview.fullstack.sleep.application.GetLastNightSleepUseCase
import com.noom.interview.fullstack.sleep.application.GetSleepStatisticsUseCase
import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.exception.DuplicateSleepLogException
import com.noom.interview.fullstack.sleep.domain.exception.InvalidDateRangeException
import com.noom.interview.fullstack.sleep.domain.exception.StatisticsRangeExceededException
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.web.dto.CreateSleepLogRequestDto
import com.noom.interview.fullstack.sleep.web.dto.FieldErrorDto
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.MockHttpInputMessage
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.MapBindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.*

class ApiExceptionHandlerTest {

    private val handler = ApiExceptionHandler()

    private val createUserUseCase = mockk<CreateUserUseCase>()
    private val userMockMvc = MockMvcBuilders
        .standaloneSetup(UserController(createUserUseCase))
        .setControllerAdvice(handler)
        .build()

    private val sleepLogMockMvc = MockMvcBuilders
        .standaloneSetup(
            SleepLogController(
                mockk<GetSleepStatisticsUseCase>(),
                mockk<CreateSleepLogUseCase>(),
                mockk<GetLastNightSleepUseCase>(),
            )
        )
        .setControllerAdvice(handler)
        .build()

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
    fun `should map DataIntegrityViolationException to 409 with standard envelope`() {
        val response = handler.handleDataIntegrityViolation(
            DataIntegrityViolationException("unique constraint users_username_key")
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body!!.status).isEqualTo(409)
        assertThat(response.body!!.message).isEqualTo("Resource already exists")
        assertThat(response.body!!.errors).isEmpty()
    }

    @Test
    fun `should map MethodArgumentTypeMismatchException to 400 without leaking NumberFormatException message`() {
        val response = handler.handleTypeMismatch(
            MethodArgumentTypeMismatchException(
                "abc",
                Long::class.javaObjectType,
                "days",
                methodParameter(),
                NumberFormatException("For input string: \"abc\""),
            )
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message).isEqualTo("Invalid query parameter")
        assertThat(response.body!!.message).doesNotContain("For input string")
        assertThat(response.body!!.errors)
            .containsExactly(FieldErrorDto("days", "days must be a positive integer"))
    }

    @Test
    fun `should map type mismatch of non-numeric type to 400 with generic field message`() {
        val response = handler.handleTypeMismatch(
            MethodArgumentTypeMismatchException(
                "not-a-uuid",
                UUID::class.java,
                "userId",
                methodParameter(),
                IllegalArgumentException("Invalid UUID string"),
            )
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.message).isEqualTo("Invalid query parameter")
        assertThat(response.body!!.errors)
            .containsExactly(FieldErrorDto("userId", "userId has an invalid value"))
    }

    @Test
    fun `should map raw NumberFormatException to 400 without leaking internals`() {
        val response = handler.handleNumberFormat(NumberFormatException("For input string: \"abc\""))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message).isEqualTo("Invalid query parameter")
        assertThat(response.body!!.message).doesNotContain("For input string")
        assertThat(response.body!!.errors).isEmpty()
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
        assertThat(response.body!!.errors).isEmpty()
    }

    @Test
    fun `should report offending field when body is unreadable because of a field value`() {
        val cause = InvalidFormatException.from(
            null,
            "internal jackson detail",
            "AMAZING",
            SleepLog.WakeUpMood::class.java,
        )
        cause.prependPath(CreateSleepLogRequestDto::class.java, "mood")
        val response = handler.handleUnreadableBody(
            HttpMessageNotReadableException("JSON parse error", cause, MockHttpInputMessage(ByteArray(0)))
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message).isEqualTo("Malformed request body")
        assertThat(response.body!!.message).doesNotContain("internal jackson detail")
        assertThat(response.body!!.errors)
            .containsExactly(FieldErrorDto("mood", "Malformed value"))
    }

    @Test
    fun `should report most specific nested field when body is unreadable`() {
        val cause = InvalidFormatException.from(
            null,
            "internal jackson detail",
            "AMAZING",
            SleepLog.WakeUpMood::class.java,
        )
        cause.prependPath(CreateSleepLogRequestDto::class.java, "mood")
        cause.prependPath(Any(), "log")
        val response = handler.handleUnreadableBody(
            HttpMessageNotReadableException("JSON parse error", cause, MockHttpInputMessage(ByteArray(0)))
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.errors)
            .containsExactly(FieldErrorDto("mood", "Malformed value"))
    }

    @Test
    fun `should return empty errors when unreadable body has no field information`() {
        val cause = JsonMappingException.from(null as com.fasterxml.jackson.core.JsonParser?, "unexpected token")
        val response = handler.handleUnreadableBody(
            HttpMessageNotReadableException("JSON parse error", cause, MockHttpInputMessage(ByteArray(0)))
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.message).isEqualTo("Malformed request body")
        assertThat(response.body!!.errors).isEmpty()
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

    @Test
    fun `should map DataIntegrityViolationException to 409 with the standard error envelope`() {
        every { createUserUseCase.execute(any()) } throws
            DataIntegrityViolationException("unique constraint users_username_key")

        userMockMvc.post("/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"username": "race-${UUID.randomUUID()}"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.status") { value(409) }
            jsonPath("$.message") { value("Resource already exists") }
            jsonPath("$.errors") { isEmpty() }
        }
    }

    @Test
    fun `should map type mismatch on days param to 400 with clean message and days field error`() {
        val userId = UUID.randomUUID()

        sleepLogMockMvc.get("/v1/users/$userId/sleep-logs/stats") {
            param("days", "abc")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Invalid query parameter") }
            jsonPath("$.message", Matchers.not(Matchers.containsString("For input string")))
            jsonPath("$.errors[0].field") { value("days") }
            jsonPath("$.errors[0].message") { value("days must be a positive integer") }
        }
    }

    @Test
    fun `should map StatisticsRangeExceededException to 400 with the custom message`() {
        val response = handler.handleStatisticsRangeExceeded(
            StatisticsRangeExceededException(requestedDays = 91, maxDays = 90)
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message)
            .isEqualTo("Statistics range cannot exceed 90 days, but 91 days was requested")
        assertThat(response.body!!.errors).isEmpty()
    }

    private fun methodParameter(): MethodParameter =
        MethodParameter(ApiExceptionHandlerTest::class.java.declaredMethods.first(), -1)
}
