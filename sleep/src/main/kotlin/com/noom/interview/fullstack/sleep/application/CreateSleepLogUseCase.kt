package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.common.annotation.UseCase
import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.exception.DuplicateSleepLogException
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.domain.repository.UserRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@UseCase
class CreateSleepLogUseCase(
    private val sleepLogRepository: SleepLogRepository,
    private val userRepository: UserRepository,
) {
    fun execute(command: CreateSleepLogCommand): SleepLog {
        val user = userRepository.findById(command.userId)
            ?: throw UserNotFoundException(command.userId)

        if (sleepLogRepository.existsByUserIdAndSleepDate(command.userId, command.sleepDate)) {
            throw DuplicateSleepLogException(command.userId, command.sleepDate)
        }

        val sleepLog =
            SleepLog.create(
                userId = user.id,
                sleepDate = command.sleepDate,
                bedTime = command.bedTime,
                wakeTime = command.wakeTime,
                mood = command.mood,
            )

        return sleepLogRepository.save(sleepLog)
    }
}

data class CreateSleepLogCommand(
    val userId: UUID,
    val sleepDate: LocalDate,
    val bedTime: LocalTime,
    val wakeTime: LocalTime,
    val mood: SleepLog.WakeUpMood,
)