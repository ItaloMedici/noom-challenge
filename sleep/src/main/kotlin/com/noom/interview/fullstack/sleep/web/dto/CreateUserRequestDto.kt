package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.application.CreateUserCommand
import javax.validation.constraints.NotBlank

data class CreateUserRequestDto(
    @field:NotBlank(message = "Username is required")
    val username: String?
) {
    fun toCommand(): CreateUserCommand =
        CreateUserCommand(username!!.trim())
}
