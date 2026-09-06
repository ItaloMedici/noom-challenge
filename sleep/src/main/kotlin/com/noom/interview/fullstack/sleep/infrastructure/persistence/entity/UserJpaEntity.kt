package com.noom.interview.fullstack.sleep.infrastructure.persistence.entity

import com.noom.interview.fullstack.sleep.domain.User
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "users")
data class UserJpaEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    val username: String,
) {
    companion object {
        fun fromDomain(user: User): UserJpaEntity =
            UserJpaEntity(
                id = user.id,
                username = user.username,
            )

        fun toDomain(entity: UserJpaEntity): User =
            User(
                id = entity.id,
                username = entity.username,
            )
    }
}