package com.noom.interview.fullstack.sleep.domain.repository

import com.noom.interview.fullstack.sleep.domain.User
import java.util.UUID

interface UserRepository {
    fun save(user: User): User

    fun findById(id: UUID): User?
}