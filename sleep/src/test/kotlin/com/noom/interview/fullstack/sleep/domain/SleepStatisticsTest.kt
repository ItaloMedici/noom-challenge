package com.noom.interview.fullstack.sleep.domain

import com.noom.interview.fullstack.sleep.domain.model.DateRange
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class SleepStatisticsTest {

    @Test
    fun `should not allow empty logs`() {
        val range = DateRange(LocalDate.now(), LocalDate.now())

        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                SleepStatistics.calculate(emptyList(), range)
            }

        assertThat(execution.message).isEqualTo("Cannot calculate statistics from an empty log set")
    }

    @Test
    fun `standard reporting window should be the rolling last 30 days`() {
        assertThat(SleepStatistics.DEFAULT_RANGE_DAYS).isEqualTo(30L)
    }

    @Test
    fun `empty should return a valid calculation bounded by the given date range`() {
        val range = DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))

        val empty = SleepStatistics.empty(range)

        assertThat(empty.range).isEqualTo(range)
        assertThat(empty.averageTimeInBed).isNull()
        assertThat(empty.averageBedTime).isNull()
        assertThat(empty.averageWakeTime).isNull()
        assertThat(empty.moodFrequencies).isEmpty()
    }

    @Test
    fun `empty and calculated results should be distinguishable for the same range`() {
        val range = DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))
        val log = SleepLog.create(
            userId = UUID.randomUUID(),
            sleepDate = LocalDate.of(2026, 9, 6),
            bedTime = LocalTime.of(22, 0),
            wakeTime = LocalTime.of(6, 0),
            mood = SleepLog.WakeUpMood.GOOD,
        )

        val empty = SleepStatistics.empty(range)
        val calculated = SleepStatistics.calculate(listOf(log), range)

        assertThat(empty).isNotEqualTo(calculated)
        assertThat(calculated.averageTimeInBed).isNotNull
    }

    @Test
    fun `average bedtime for times crossing midnight`() {
        val logs = listOf(
            SleepLog.create(
                userId = UUID.randomUUID(),
                bedTime = LocalTime.of(23, 30),
                wakeTime = LocalTime.of(7, 0),
                mood = SleepLog.WakeUpMood.OK,
            ),
            SleepLog.create(
                userId = UUID.randomUUID(),
                bedTime = LocalTime.of(0, 30),
                wakeTime = LocalTime.of(8, 0),
                mood = SleepLog.WakeUpMood.GOOD,
            ),
        )
        val range = DateRange(LocalDate.now().minusDays(1), LocalDate.now())

        val stats = SleepStatistics.calculate(logs, range)

        // TODO: linear average gives noon here, not the midnight. The average should be calculated in circular
        assertThat(stats.averageBedTime).isEqualTo(LocalTime.of(12, 0))
    }

    @Test
    fun `should calculate statistics happy path`() {
        val logs = listOf(
            SleepLog.create(
                userId = UUID.randomUUID(),
                bedTime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 0),
                mood = SleepLog.WakeUpMood.GOOD,
            ),
            SleepLog.create(
                userId = UUID.randomUUID(),
                bedTime = LocalTime.of(23, 0),
                wakeTime = LocalTime.of(7, 0),
                mood = SleepLog.WakeUpMood.OK,
            ),
        )
        val range = DateRange(LocalDate.now().minusDays(1), LocalDate.now())

        val stats = SleepStatistics.calculate(logs, range)

        // both durations are 8 hours
        assertThat(stats.averageTimeInBed).isEqualTo(Duration.ofHours(8))

        // average bed time between 22:00 and 23:00 -> 22:30
        assertThat(stats.averageBedTime).isEqualTo(LocalTime.of(22, 30))

        // average wake time between 06:00 and 07:00 -> 06:30
        assertThat(stats.averageWakeTime).isEqualTo(LocalTime.of(6, 30))

        // mood counts
        assertThat(stats.moodFrequencies).containsEntry(SleepLog.WakeUpMood.GOOD, 1)
        assertThat(stats.moodFrequencies).containsEntry(SleepLog.WakeUpMood.OK, 1)
    }
}
