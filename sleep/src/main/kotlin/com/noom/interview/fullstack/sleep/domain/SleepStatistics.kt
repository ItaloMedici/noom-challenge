package com.noom.interview.fullstack.sleep.domain

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

object SleepStatistics {

    fun calculate(sleepLogs: List<SleepLog>, range: ClosedRange<LocalDate>): SleepStatisticsCalculation {
        require(sleepLogs.isNotEmpty()) { "Cannot calculate statistics from an empty log set" }
        require(range.start <= range.endInclusive) { "Invalid date range" }

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

    data class SleepStatisticsCalculation(
        val range: ClosedRange<LocalDate>,
        val averageTimeInBed: Duration,
        val averageBedTime: LocalTime,
        val averageWakeTime: LocalTime,
        val moodFrequencies: Map<SleepLog.WakeUpMood, Int>
    )
}