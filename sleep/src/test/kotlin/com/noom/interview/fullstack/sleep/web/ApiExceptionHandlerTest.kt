package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.domain.exception.InvalidDateRangeException
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

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
    }

    @Test
    fun `should map IllegalArgumentException to 400`() {
        val response = handler.handleIllegalArgument(IllegalArgumentException("Bed time and wake time cannot be identical"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.status).isEqualTo(400)
        assertThat(response.body!!.message).isEqualTo("Bed time and wake time cannot be identical")
    }
}
