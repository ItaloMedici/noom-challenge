package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.application.CreateUserCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.Validator

class CreateUserRequestDtoTest {

    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `should pass validation when username is valid`() {
        val request = CreateUserRequestDto(username = "Italo")

        val violations = validator.validate(request)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should fail validation when username is null`() {
        val request = CreateUserRequestDto(username = null)

        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("username")
        assertThat(violation.message).isEqualTo("Username is required")
    }

    @Test
    fun `should fail validation when username is blank`() {
        val request = CreateUserRequestDto(username = "   ")

        val violations = validator.validate(request)

        assertThat(violations).hasSize(1)
        val violation = violations.first()
        assertThat(violation.propertyPath.toString()).isEqualTo("username")
        assertThat(violation.message).isEqualTo("Username is required")
    }

    @Test
    fun `should map to create user command`() {
        val request = CreateUserRequestDto(username = "Italo")

        val command = request.toCommand()

        assertThat(command).isInstanceOf(CreateUserCommand::class.java)
        assertThat(command.username).isEqualTo("Italo")
    }

    @Test
    fun `should trim username when mapping to command`() {
        val request = CreateUserRequestDto(username = "  Italo  ")

        val command = request.toCommand()

        assertThat(command.username).isEqualTo("Italo")
    }

    @Test
    fun `should throw when mapping without validating and username is null`() {
        val request = CreateUserRequestDto(username = null)

        assertThatThrownBy { request.toCommand() }
            .isInstanceOf(NullPointerException::class.java)
    }
}
