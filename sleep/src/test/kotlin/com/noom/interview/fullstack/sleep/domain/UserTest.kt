package com.noom.interview.fullstack.sleep.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class UserTest {
    @Test
    fun `create should generate unique ids`() {
        val u1 = User.create("italo")
        val u2 = User.create("medici")

        assertNotEquals(u1.id, u2.id)
    }

    @Test
    fun `create should trim whitespace from username`() {
        val user = User.create("  italo  ")

        assertEquals("italo", user.username)
    }

    @Test
    fun `should not allow blank username`() {
        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                User.create("   ")
            }

        assertEquals("Username cannot be blank", execution.message)
    }

    @Test
    fun `should create successfully`() {
        val user = User.create("italo medici")

        assertEquals("italo medici", user.username)
        assertNotEquals(UUID(0, 0), user.id)
    }
}