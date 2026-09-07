package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class SleepStatsResponseDto(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val averageTimeInBed: Duration?,
    val averageBedTime: LocalTime?,
    val averageWakeTime: LocalTime?,
    val moodFrequencies: Map<String, Int>,
) {
    companion object {
        fun from(calculation: SleepStatistics.SleepStatisticsCalculation): SleepStatsResponseDto =
            SleepStatsResponseDto(
                rangeStart = calculation.range.start,
                rangeEnd = calculation.range.end,
                averageTimeInBed = calculation.averageTimeInBed,
                averageBedTime = calculation.averageBedTime,
                averageWakeTime = calculation.averageWakeTime,
                moodFrequencies = calculation.moodFrequencies.mapKeys { it.key.name },
            )

        fun empty(rangeStart: LocalDate, rangeEnd: LocalDate): SleepStatsResponseDto =
            SleepStatsResponseDto(
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
                averageTimeInBed = null,
                averageBedTime = null,
                averageWakeTime = null,
                moodFrequencies = emptyMap(),
            )
    }
}
