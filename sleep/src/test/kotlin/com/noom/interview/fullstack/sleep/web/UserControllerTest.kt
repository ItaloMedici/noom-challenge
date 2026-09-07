package com.noom.interview.fullstack.sleep.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.noom.interview.fullstack.sleep.application.CreateUserCommand
import com.noom.interview.fullstack.sleep.application.CreateUserUseCase
import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.exception.DuplicateUsernameException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

class UserControllerTest {

    private val createUserUseCase = mockk<CreateUserUseCase>()
    private val controller = UserController(createUserUseCase)
    private val mockMvc = MockMvcBuilders
        .standaloneSetup(controller)
        .setControllerAdvice(ApiExceptionHandler())
        .build()
    private val objectMapper = ObjectMapper()

    @Test
    fun `should return 201 with created user id`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId, username = "Italo")
        every { createUserUseCase.execute(any()) } returns user

        val body = """{"username": "Italo"}"""

        mockMvc.post("/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(userId.toString()) }
        }

        verify(exactly = 1) {
            createUserUseCase.execute(CreateUserCommand(username = "Italo"))
        }
    }

    @Test
    fun `should return 400 for missing username`() {
        val body = "{}"

        mockMvc.post("/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Validation failed") }
            jsonPath("$.errors") { value(hasItem("Username is required")) }
        }

        verify(exactly = 0) { createUserUseCase.execute(any()) }
    }

    @Test
    fun `should return 400 for blank username`() {
        val body = """{"username": "   "}"""

        mockMvc.post("/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Validation failed") }
            jsonPath("$.errors") { value(hasItem("Username is required")) }
        }

        verify(exactly = 0) { createUserUseCase.execute(any()) }
    }

    @Test
    fun `should return 400 for too short username`() {
        val body = """{"username": "I"}"""

        every { createUserUseCase.execute(any()) } throws
                IllegalArgumentException("Username must be at least 2 characters")

        mockMvc.post("/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Username must be at least 2 characters") }
        }

        verify(exactly = 1) { createUserUseCase.execute(any()) }
    }

    @Test
    fun `should return 400 for too long username`() {
        val longUsername = "a".repeat(101)
        val body = """{"username": "$longUsername"}"""

        every { createUserUseCase.execute(any()) } throws
                IllegalArgumentException("Username must be at most 100 characters")

        mockMvc.post("/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
            jsonPath("$.message") { value("Username must be at most 100 characters") }
        }

        verify(exactly = 1) { createUserUseCase.execute(any()) }
    }

    @Test
    fun `should return 409 for duplicate username`() {
        every { createUserUseCase.execute(any()) } throws DuplicateUsernameException("Italo")

        val body = """{"username": "Italo"}"""

        mockMvc.post("/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isConflict() }
            jsonPath("$.status") { value(409) }
            jsonPath("$.message") { value("Username 'Italo' is already taken") }
        }

        verify(exactly = 1) {
            createUserUseCase.execute(CreateUserCommand(username = "Italo"))
        }
    }

    @Test
    fun `should sanitize and create user`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId, username = "Italo Arucho")
        every { createUserUseCase.execute(any()) } returns user

        val body = """{"username": "  Italo Arucho  "}"""

        mockMvc.post("/v1/users") {
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(userId.toString()) }
        }

        verify(exactly = 1) {
            createUserUseCase.execute(CreateUserCommand(username = "Italo Arucho"))
        }
    }
}
