package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.domain.repository.UserNotFoundException
import com.noom.interview.fullstack.sleep.domain.repository.UserRepository
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class CreateSleepLogUseCase(
    private val sleepLogRepository: SleepLogRepository,
    private val userRepository: UserRepository,
) {
    fun execute(command: CreateSleepLogCommand): SleepLog {
        val user = userRepository.findById(command.userId)
            ?: throw UserNotFoundException(command.userId)

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