package com.noom.interview.fullstack.sleep.domain

import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
) {
    init {
        val sanitized = username.sanitize()
        require(sanitized.isNotBlank()) { "Username cannot be blank" }
        require(sanitized.length >= 2) { "Username must be at least 2 characters" }
        require(sanitized.length <= 100) { "Username must be at most 100 characters" }
    }

    companion object {
        fun create(username: String): User =
            User(
                id = UUID.randomUUID(),
                username = username.sanitize(),
            )
    }
}

private fun String.sanitize(): String = this
    .trim()
    .replace(Regex("<[^>]*>"), "")
    .replace(Regex("[\\x00-\\x1f]"), "")
