package com.noom.interview.fullstack.sleep.infrastructure.persistence

import com.noom.interview.fullstack.sleep.domain.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Tag("integration")
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    lateinit var repository: JpaUserRepository

    @Test
    fun `save should persist and find by id`() {
        val user = User.create("italo")

        val saved = repository.save(user)
        val found = repository.findById(saved.id)

        assertThat(found).isNotNull
        assertThat(found!!.username).isEqualTo("italo")
    }

    @Test
    fun `findById should return null when absent`() {
        val found = repository.findById(UUID.randomUUID())

        assertThat(found).isNull()
    }

    @Test
    fun `existsByUsername should return true when user exists`() {
        val user = User.create("Italo")
        repository.save(user)

        val exists = repository.existsByUsername("Italo")

        assertThat(exists).isTrue()
    }

    @Test
    fun `existsByUsername should return false when user does not exist`() {
        val exists = repository.existsByUsername("NonExistentUser")

        assertThat(exists).isFalse()
    }

    @Test
    fun `existsByUsername should be case sensitive`() {
        val user = User.create("Italo")
        repository.save(user)

        val existsLower = repository.existsByUsername("italo")
        val existsUpper = repository.existsByUsername("ITALO")

        assertThat(existsLower).isFalse()
        assertThat(existsUpper).isFalse()
    }
}
