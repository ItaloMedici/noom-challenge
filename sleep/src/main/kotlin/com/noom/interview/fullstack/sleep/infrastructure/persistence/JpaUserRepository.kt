package com.noom.interview.fullstack.sleep.infrastructure.persistence

import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.repository.UserRepository
import com.noom.interview.fullstack.sleep.infrastructure.persistence.entity.UserJpaEntity
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JpaUserRepository(
    private val springDataUserRepository: SpringDataUserRepository,
) : UserRepository {
    override fun save(user: User): User {
        return UserJpaEntity.toDomain(springDataUserRepository.save(UserJpaEntity.fromDomain(user)))
    }

    override fun findById(id: UUID): User? {
        return springDataUserRepository.findById(id).map(UserJpaEntity::toDomain).orElse(null)
    }
}