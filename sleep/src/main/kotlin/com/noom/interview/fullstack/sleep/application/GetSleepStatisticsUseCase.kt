package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.common.annotation.UseCase
import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.domain.model.DateRange
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.domain.repository.UserRepository
import java.time.Clock
import java.util.*

@UseCase
class GetSleepStatisticsUseCase(
    private val userRepository: UserRepository,
    private val sleepLogRepository: SleepLogRepository,
    private val clock: Clock,
) {
    fun execute(command: GetStatsCommand): SleepStatistics.SleepStatisticsCalculation {
        userRepository.findById(command.userId) ?: throw UserNotFoundException(command.userId)

        val dateRange = DateRange.lastNDays(command.days ?: SleepStatistics.DEFAULT_RANGE_DAYS, clock)

        val sleepLogs =
            sleepLogRepository.findByUserIdAndSleepDateBetween(command.userId, dateRange.start, dateRange.end)

        return if (sleepLogs.isEmpty()) {
            SleepStatistics.empty(dateRange)
        } else {
            SleepStatistics.calculate(sleepLogs, dateRange)
        }
    }
}

data class GetStatsCommand(
    val userId: UUID,
    val days: Long? = null,
)
