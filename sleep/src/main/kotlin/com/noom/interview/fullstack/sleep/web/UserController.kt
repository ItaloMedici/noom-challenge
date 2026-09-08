package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.application.CreateUserUseCase
import com.noom.interview.fullstack.sleep.web.dto.CreateUserRequestDto
import com.noom.interview.fullstack.sleep.web.dto.UserResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/v1/users")
class UserController(
    private val createUserUseCase: CreateUserUseCase
) {
    @PostMapping
    fun createUser(
        @Valid @RequestBody request: CreateUserRequestDto
    ): ResponseEntity<UserResponseDto> {
        val user = createUserUseCase.execute(request.toCommand())
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(UserResponseDto.from(user))
    }
}
