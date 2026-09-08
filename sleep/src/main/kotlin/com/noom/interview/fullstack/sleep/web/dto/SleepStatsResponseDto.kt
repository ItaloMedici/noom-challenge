package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import java.time.LocalDate
import java.time.LocalTime

data class SleepStatsResponseDto(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val averageDurationInSeconds: Long?,
    val averageBedTime: LocalTime?,
    val averageWakeTime: LocalTime?,
    val moodFrequencies: Map<String, Int>,
) {
    companion object {
        fun from(calculation: SleepStatistics.SleepStatisticsCalculation): SleepStatsResponseDto =
            SleepStatsResponseDto(
                rangeStart = calculation.range.start,
                rangeEnd = calculation.range.end,
                averageDurationInSeconds = calculation.averageTimeInBed?.seconds,
                averageBedTime = calculation.averageBedTime,
                averageWakeTime = calculation.averageWakeTime,
                moodFrequencies = calculation.moodFrequencies.mapKeys { it.key.name },
            )

        fun empty(rangeStart: LocalDate, rangeEnd: LocalDate): SleepStatsResponseDto =
            SleepStatsResponseDto(
                rangeStart = rangeStart,
                rangeEnd = rangeEnd,
                averageDurationInSeconds = null,
                averageBedTime = null,
                averageWakeTime = null,
                moodFrequencies = emptyMap(),
            )
    }
}
