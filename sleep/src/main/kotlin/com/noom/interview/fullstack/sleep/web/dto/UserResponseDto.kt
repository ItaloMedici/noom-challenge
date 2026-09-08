package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.domain.User
import java.util.UUID

data class UserResponseDto(val id: UUID) {
    companion object {
        fun from(user: User): UserResponseDto = UserResponseDto(id = user.id)
    }
}
