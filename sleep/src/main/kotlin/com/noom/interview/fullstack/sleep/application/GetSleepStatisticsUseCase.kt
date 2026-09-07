package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import com.noom.interview.fullstack.sleep.domain.exception.InvalidDateRangeException
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import java.time.LocalDate
import java.util.*

class GetSleepStatisticsUseCase(
    private val sleepLogRepository: SleepLogRepository,
) {
    fun execute(command: GetStatsCommand): SleepStatistics.SleepStatisticsCalculation? {
        if (command.rangeStart.isAfter(command.rangeEnd)) {
            throw InvalidDateRangeException()
        }

        val sleepLogs =
            sleepLogRepository.findByUserIdAndSleepDateBetween(command.userId, command.rangeStart, command.rangeEnd)

        if (sleepLogs.isEmpty()) return null

        return SleepStatistics.calculate(sleepLogs, command.rangeStart..command.rangeEnd)
    }
}

data class GetStatsCommand(
    val userId: UUID,
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
)
