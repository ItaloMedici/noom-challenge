package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import com.noom.interview.fullstack.sleep.domain.model.DateRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class SleepStatsResponseDtoTest {

    @Test
    fun `should map a SleepStatisticsCalculation into a response dto`() {
        val rangeStart = LocalDate.of(2026, 1, 1)
        val rangeEnd = LocalDate.of(2026, 1, 30)

        val calculation = SleepStatistics.SleepStatisticsCalculation(
            range = DateRange(rangeStart, rangeEnd),
            averageTimeInBed = Duration.ofHours(8),
            averageBedTime = LocalTime.of(22, 0),
            averageWakeTime = LocalTime.of(6, 0),
            moodFrequencies = mapOf(SleepLog.WakeUpMood.GOOD to 1),
        )

        val dto = SleepStatsResponseDto.from(calculation)

        assertThat(dto.rangeStart).isEqualTo(rangeStart)
        assertThat(dto.rangeEnd).isEqualTo(rangeEnd)
        assertThat(dto.averageDurationInSeconds).isEqualTo(Duration.ofHours(8).seconds)
        assertThat(dto.averageBedTime).isEqualTo(LocalTime.of(22, 0))
        assertThat(dto.averageWakeTime).isEqualTo(LocalTime.of(6, 0))
        assertThat(dto.moodFrequencies).containsEntry("GOOD", 1)
    }

    @Test
    fun `should map an empty calculation into null averages and no mood frequencies`() {
        val emptyCalculation = SleepStatistics.empty(
            DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))
        )

        val dto = SleepStatsResponseDto.from(emptyCalculation)

        assertThat(dto.rangeStart).isEqualTo(LocalDate.of(2026, 8, 8))
        assertThat(dto.rangeEnd).isEqualTo(LocalDate.of(2026, 9, 6))
        assertThat(dto.averageDurationInSeconds).isNull()
        assertThat(dto.averageBedTime).isNull()
        assertThat(dto.averageWakeTime).isNull()
        assertThat(dto.moodFrequencies).isEmpty()
    }

    @Test
    fun `should build an empty response dto with the requested range and no averages`() {
        val rangeStart = LocalDate.of(2026, 1, 1)
        val rangeEnd = LocalDate.of(2026, 1, 30)

        val dto = SleepStatsResponseDto.empty(rangeStart, rangeEnd)

        assertThat(dto.rangeStart).isEqualTo(rangeStart)
        assertThat(dto.rangeEnd).isEqualTo(rangeEnd)
        assertThat(dto.averageDurationInSeconds).isNull()
        assertThat(dto.averageBedTime).isNull()
        assertThat(dto.averageWakeTime).isNull()
        assertThat(dto.moodFrequencies).isEmpty()
    }
}
