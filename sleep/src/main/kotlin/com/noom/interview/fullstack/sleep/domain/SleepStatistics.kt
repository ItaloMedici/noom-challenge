package com.noom.interview.fullstack.sleep.domain

import com.noom.interview.fullstack.sleep.domain.model.DateRange
import java.time.Duration
import java.time.LocalTime

object SleepStatistics {

    const val DEFAULT_RANGE_DAYS: Long = 30

    fun calculate(sleepLogs: List<SleepLog>, range: DateRange): SleepStatisticsCalculation {
        require(sleepLogs.isNotEmpty()) { "Cannot calculate statistics from an empty log set" }

        val averageTimeInBed = sleepLogs
            .map { it.timeInBed }
            .fold(Duration.ZERO, Duration::plus)
            .dividedBy(sleepLogs.size.toLong())

        val averageBedTime = sleepLogs
            .map { it.bedTime.toSecondOfDay().toLong() }
            .average()
            .let { LocalTime.ofSecondOfDay(it.toLong()) }

        val averageWakeTime = sleepLogs
            .map { it.wakeTime.toSecondOfDay().toLong() }
            .average()
            .let { LocalTime.ofSecondOfDay(it.toLong()) }

        val moodFrequencies = sleepLogs.groupBy { it.mood }.mapValues { it.value.size }

        return SleepStatisticsCalculation(
            range = range,
            averageTimeInBed = averageTimeInBed,
            averageBedTime = averageBedTime,
            averageWakeTime = averageWakeTime,
            moodFrequencies = moodFrequencies
        )
    }

    fun empty(dateRange: DateRange): SleepStatisticsCalculation =
        SleepStatisticsCalculation(
            range = dateRange,
            averageTimeInBed = null,
            averageBedTime = null,
            averageWakeTime = null,
            moodFrequencies = emptyMap()
        )

    data class SleepStatisticsCalculation(
        val range: DateRange,
        val averageTimeInBed: Duration?,
        val averageBedTime: LocalTime?,
        val averageWakeTime: LocalTime?,
        val moodFrequencies: Map<SleepLog.WakeUpMood, Int>
    )
}
