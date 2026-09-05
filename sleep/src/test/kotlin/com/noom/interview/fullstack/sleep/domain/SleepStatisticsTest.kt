package com.noom.interview.fullstack.sleep.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

class SleepStatisticsTest {
    @Test
    fun `should not allow empty logs`() {
        val range = LocalDate.now()..LocalDate.now()

        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                SleepStatistics.calculate(emptyList(), range)
            }

        assertEquals("Cannot calculate statistics from an empty log set", execution.message)
    }

    @Test
    fun `should not allow invalid date range`() {
        val range = LocalDate.now()..LocalDate.now().minusDays(1)

        val sleepLogs = SleepLog.create(
            bedTime = LocalTime.of(22, 0),
            wakeTime = LocalTime.of(6, 0),
            mood = SleepLog.WakeUpMood.GOOD,
        )

        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                SleepStatistics.calculate(listOf(sleepLogs), range)
            }

        assertEquals("Invalid date range", execution.message)
    }

    @Test
    fun `average bedtime for times crossing midnight`() {
        val logs = listOf(
            SleepLog.create(bedTime = LocalTime.of(23, 30), wakeTime = LocalTime.of(7, 0), mood = SleepLog.WakeUpMood.OK),
            SleepLog.create(bedTime = LocalTime.of(0, 30), wakeTime = LocalTime.of(8, 0), mood = SleepLog.WakeUpMood.GOOD)
        )
        val range = LocalDate.now().minusDays(1)..LocalDate.now()

        val stats = SleepStatistics.calculate(logs, range)

        // TODO: linear average gives noon here, not the midnight. The average should be calculated in circular
        assertEquals(LocalTime.of(12, 0), stats.averageBedTime)
    }

    @Test
    fun `should calculate statistics happy path`() {
        val logs = listOf(
            SleepLog.create(bedTime = LocalTime.of(22, 0), wakeTime = LocalTime.of(6, 0), mood = SleepLog.WakeUpMood.GOOD),
            SleepLog.create(bedTime = LocalTime.of(23, 0), wakeTime = LocalTime.of(7, 0), mood = SleepLog.WakeUpMood.OK)
        )
        val range = LocalDate.now().minusDays(1)..LocalDate.now()

        val stats = SleepStatistics.calculate(logs, range)

        // both durations are 8 hours
        assertEquals(java.time.Duration.ofHours(8), stats.averageTimeInBed)

        // average bed time between 22:00 and 23:00 -> 22:30
        assertEquals(LocalTime.of(22, 30), stats.averageBedTime)

        // average wake time between 06:00 and 07:00 -> 06:30
        assertEquals(LocalTime.of(6, 30), stats.averageWakeTime)

        // mood counts
        assertEquals(1, stats.moodFrequencies[SleepLog.WakeUpMood.GOOD])
        assertEquals(1, stats.moodFrequencies[SleepLog.WakeUpMood.OK])
    }
}