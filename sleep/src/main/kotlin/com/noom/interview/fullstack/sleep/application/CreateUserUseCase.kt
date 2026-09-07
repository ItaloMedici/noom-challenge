package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.common.annotation.UseCase
import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.exception.DuplicateUsernameException
import com.noom.interview.fullstack.sleep.domain.repository.UserRepository

@UseCase
class CreateUserUseCase(
    private val userRepository: UserRepository
) {
    fun execute(command: CreateUserCommand): User {
        val user = User.create(command.username)

        if (userRepository.existsByUsername(user.username)) {
            throw DuplicateUsernameException(user.username)
        }

        return userRepository.save(user)
    }

}

data class CreateUserCommand(val username: String)
