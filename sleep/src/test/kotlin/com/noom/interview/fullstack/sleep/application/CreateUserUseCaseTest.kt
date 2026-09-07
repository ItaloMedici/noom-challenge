package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.exception.DuplicateUsernameException
import com.noom.interview.fullstack.sleep.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CreateUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val useCase = CreateUserUseCase(userRepository)

    @Test
    fun `should create user with valid username`() {
        val username = "Italo"
        val command = CreateUserCommand(username)
        val expectedUser = User.create(username)

        every { userRepository.existsByUsername("Italo") } returns false

        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } returns expectedUser

        val result = useCase.execute(command)

        assertThat(userSlot.isCaptured).isTrue()
        assertThat(userSlot.captured.username).isEqualTo(username)
        assertThat(result).isSameAs(expectedUser)
        verify(exactly = 1) { userRepository.existsByUsername("Italo") }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `should sanitize and check sanitized username`() {
        val username = "  Italo  "
        val command = CreateUserCommand(username)

        every { userRepository.existsByUsername("Italo") } returns false

        val userSlot = slot<User>()
        val expectedUser = User.create("Italo")
        every { userRepository.save(capture(userSlot)) } returns expectedUser

        val result = useCase.execute(command)

        assertThat(userSlot.captured.username).isEqualTo("Italo")
        assertThat(result.username).isEqualTo("Italo")
        verify(exactly = 1) { userRepository.existsByUsername("Italo") }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `should throw DuplicateUsernameException when sanitized username exists`() {
        val command = CreateUserCommand("Italo")

        every { userRepository.existsByUsername("Italo") } returns true

        val exception = assertThrows(DuplicateUsernameException::class.java) {
            useCase.execute(command)
        }

        assertThat(exception.username).isEqualTo("Italo")
        verify(exactly = 1) { userRepository.existsByUsername("Italo") }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should throw DuplicateUsernameException for sanitized HTML username`() {
        val command = CreateUserCommand("<script>Italo</script>")

        every { userRepository.existsByUsername("Italo") } returns true

        val exception = assertThrows(DuplicateUsernameException::class.java) {
            useCase.execute(command)
        }

        assertThat(exception.username).isEqualTo("Italo")
        verify(exactly = 1) { userRepository.existsByUsername("Italo") }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should propagate validation error for blank username`() {
        val username = "   "
        val command = CreateUserCommand(username)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(command)
        }

        assertThat(exception.message).isEqualTo("Username cannot be blank")
        verify(exactly = 0) { userRepository.existsByUsername(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should propagate validation error for too short username`() {
        val username = "I"
        val command = CreateUserCommand(username)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(command)
        }

        assertThat(exception.message).isEqualTo("Username must be at least 2 characters")
        verify(exactly = 0) { userRepository.existsByUsername(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should propagate validation error for too long username`() {
        val username = "I".repeat(101)
        val command = CreateUserCommand(username)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(command)
        }

        assertThat(exception.message).isEqualTo("Username must be at most 100 characters")
        verify(exactly = 0) { userRepository.existsByUsername(any()) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `should create user with sanitized HTML tags`() {
        val command = CreateUserCommand("<script>Italo</script>")

        every { userRepository.existsByUsername("Italo") } returns false

        val userSlot = slot<User>()
        val expectedUser = User.create("Italo")
        every { userRepository.save(capture(userSlot)) } returns expectedUser

        useCase.execute(command)

        assertThat(userSlot.captured.username).isEqualTo("Italo")
        assertThat(userSlot.captured.username).doesNotContain("<script>")
        assertThat(userSlot.captured.username).doesNotContain("</script>")
        verify(exactly = 1) { userRepository.existsByUsername("Italo") }
        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `should create user with removed control characters`() {
        val command = CreateUserCommand("It\u0001alo")

        every { userRepository.existsByUsername("Italo") } returns false

        val userSlot = slot<User>()
        val expectedUser = User.create("Italo")
        every { userRepository.save(capture(userSlot)) } returns expectedUser

        useCase.execute(command)

        assertThat(userSlot.captured.username).isEqualTo("Italo")
        assertThat(userSlot.captured.username).doesNotContain("\u0001")
        verify(exactly = 1) { userRepository.existsByUsername("Italo") }
        verify(exactly = 1) { userRepository.save(any()) }
    }
}
