package com.noom.interview.fullstack.sleep.domain

import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
) {
    init {
        require(username.isNotBlank()) { "Username cannot be blank" }
    }

    companion object {
        fun create(username: String): User =
            User(
                id = UUID.randomUUID(),
                username = username.trim(),
            )
    }
}