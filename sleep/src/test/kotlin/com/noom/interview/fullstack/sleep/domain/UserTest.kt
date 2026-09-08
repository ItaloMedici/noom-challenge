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
    fun `should create successfully with valid username`() {
        val user = User.create("Italo")

        assertEquals("Italo", user.username)
        assertNotEquals(UUID(0, 0), user.id)
    }

    @Test
    fun `should create successfully with spaces within username`() {
        val user = User.create("Italo Medici")

        assertEquals("Italo Medici", user.username)
    }

    @Test
    fun `should reject username shorter than 2 characters`() {
        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                User.create("I")
            }

        assertEquals("Username must be at least 2 characters", execution.message)
    }

    @Test
    fun `should reject username longer than 100 characters`() {
        val longUsername = "a".repeat(101)

        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                User.create(longUsername)
            }

        assertEquals("Username must be at most 100 characters", execution.message)
    }

    @Test
    fun `should accept username with exactly 2 characters`() {
        val user = User.create("It")

        assertEquals("It", user.username)
    }

    @Test
    fun `should accept username with exactly 100 characters`() {
        val username = "a".repeat(100)

        val user = User.create(username)

        assertEquals(100, user.username.length)
    }

    @Test
    fun `should sanitize HTML tags from username`() {
        val user = User.create("<script>Italo</script>")

        assertEquals("Italo", user.username)
    }

    @Test
    fun `should sanitize control characters from username`() {
        val user = User.create("Italo\u0000")

        assertEquals("Italo", user.username)
    }

    @Test
    fun `should sanitize multiple control characters`() {
        val user = User.create("It\u0001al\u0002o")

        assertEquals("Italo", user.username)
    }

    @Test
    fun `should trim whitespace and sanitize HTML tags`() {
        val user = User.create("  <b>Italo</b>  ")

        assertEquals("Italo", user.username)
    }

    @Test
    fun `should handle complex HTML sanitization`() {
        val user = User.create("<div><p>Italo</p></div>")

        assertEquals("Italo", user.username)
    }

    @Test
    fun `should reject blank username after sanitization`() {
        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                User.create("<script></script>")
            }

        assertEquals("Username cannot be blank", execution.message)
    }

    @Test
    fun `should reject too short username after sanitization`() {
        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                User.create("<b>I</b>")
            }

        assertEquals("Username must be at least 2 characters", execution.message)
    }
}
