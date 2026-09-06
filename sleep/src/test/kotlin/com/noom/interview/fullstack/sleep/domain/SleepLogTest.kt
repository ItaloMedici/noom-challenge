package com.noom.interview.fullstack.sleep.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class SleepLogTest {
    @Test
    fun `should not allow future sleep dates`() {
        val tomorrowDate = LocalDate.now().plusDays(1)
        val bedTime = java.time.LocalTime.of(22, 0)
        val wakeTime = java.time.LocalTime.of(6, 0)

        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                SleepLog(
                    id = UUID.randomUUID(),
                    userId = UUID.randomUUID(),
                    sleepDate = tomorrowDate,
                    bedTime = bedTime,
                    wakeTime = wakeTime,
                    mood = SleepLog.WakeUpMood.GOOD,
                )
            }

        assertEquals("Sleep date cannot be in the future", execution.message)
    }

    @Test
    fun `should allow past sleep dates`() {
        val yesterdayDate = LocalDate.now().minusDays(1)
        val bedTime = LocalTime.of(22, 0)
        val wakeTime = LocalTime.of(6, 0)

        val sleepLog =
            SleepLog(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                sleepDate = yesterdayDate,
                bedTime = bedTime,
                wakeTime = wakeTime,
                mood = SleepLog.WakeUpMood.GOOD,
            )

        assertEquals(yesterdayDate, sleepLog.sleepDate)
    }

    @Test
    fun `should the sleep date be today as default`() {
        val bedTime = LocalTime.of(22, 0)
        val wakeTime = LocalTime.of(6, 0)

        val sleepLog =
            SleepLog(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                bedTime = bedTime,
                wakeTime = wakeTime,
                mood = SleepLog.WakeUpMood.GOOD,
            )

        assertEquals(LocalDate.now(), sleepLog.sleepDate)
    }

    @Test
    fun `create should set defaults and generate unique ids`() {
        val s1 = SleepLog.create(userId = UUID.randomUUID(), bedTime = LocalTime.of(22, 0), wakeTime = LocalTime.of(6, 0), mood = SleepLog.WakeUpMood.GOOD)
        val s2 = SleepLog.create(userId = UUID.randomUUID(), bedTime = LocalTime.of(22, 0), wakeTime = LocalTime.of(6, 0), mood = SleepLog.WakeUpMood.GOOD)

        assertEquals(LocalDate.now(), s1.sleepDate)
        assertEquals(LocalTime.of(22, 0), s1.bedTime)
        assertEquals(LocalTime.of(6, 0), s1.wakeTime)
        assertEquals(SleepLog.WakeUpMood.GOOD, s1.mood)
        assertTrue(s1.id != s2.id)
    }

    @Test
    fun `create should allow past sleep dates`() {
        val yesterday = LocalDate.now().minusDays(1)

        val sleepLog =
            SleepLog.create(
                userId = UUID.randomUUID(),
                sleepDate = yesterday,
                bedTime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 0),
                mood = SleepLog.WakeUpMood.GOOD,
            )

        assertEquals(yesterday, sleepLog.sleepDate)
    }

    @Test
    fun `create should calculate time in bed for overnight sleeps`() {
        val sleepLog = SleepLog.create(userId = UUID.randomUUID(), bedTime = LocalTime.of(23, 0), wakeTime = LocalTime.of(7, 0), mood = SleepLog.WakeUpMood.OK)

        assertEquals(Duration.ofHours(8), sleepLog.timeInBed)
    }

    @Test
    fun `should not allow identical bed and wake times`() {
        val bedTime = LocalTime.of(6, 0)
        val wakeTime = LocalTime.of(6, 0)

        val execution =
            assertThrows(IllegalArgumentException::class.java) {
                SleepLog(
                    id = UUID.randomUUID(),
                    userId = UUID.randomUUID(),
                    bedTime = bedTime,
                    wakeTime = wakeTime,
                    mood = SleepLog.WakeUpMood.GOOD,
                )
            }

        assertEquals("Bed time and wake time cannot be identical", execution.message)
    }

    @Test
    fun `should calculate time in bed correctly with over midnight time`() {
        val bedTime = LocalTime.of(22, 0) // 10:00 PM
        val wakeTime = LocalTime.of(6, 0) // 6:00 AM

        val sleepLog =
            SleepLog(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                bedTime = bedTime,
                wakeTime = wakeTime,
                mood = SleepLog.WakeUpMood.GOOD,
            )

        assertEquals(Duration.ofHours(8), sleepLog.timeInBed)
    }

    @Test
    fun `should calculate time in bed correctly with same day times`() {
        val bedTime = LocalTime.of(6, 0) // 6:00 AM
        val wakeTime = LocalTime.of(14, 0) // 2:00 PM

        val sleepLog =
            SleepLog(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                bedTime = bedTime,
                wakeTime = wakeTime,
                mood = SleepLog.WakeUpMood.GOOD,
            )

        assertEquals(Duration.ofHours(8), sleepLog.timeInBed)
    }

    @Test
    fun `should accept minutes on time in bed calculation`() {
        val bedTime = LocalTime.of(22, 30) // 10:30 PM
        val wakeTime = LocalTime.of(6, 15) // 6:15 AM

        val sleepLog =
            SleepLog(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                bedTime = bedTime,
                wakeTime = wakeTime,
                mood = SleepLog.WakeUpMood.GOOD,
            )

        assertEquals(
            Duration
                .ofHours(7)
                .plusMinutes(45),
            sleepLog.timeInBed,
        )
    }

    @Test
    fun `should allow explicit today sleep date`() {
        val today = LocalDate.now()
        val bedTime = LocalTime.of(22, 0)
        val wakeTime = LocalTime.of(6, 0)

        val sleepLog =
            SleepLog(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                sleepDate = today,
                bedTime = bedTime,
                wakeTime = wakeTime,
                mood = SleepLog.WakeUpMood.OK,
            )

        assertEquals(today, sleepLog.sleepDate)
    }

    @Test
    fun `should calculate 1 minute across midnight correctly`() {
        val bedTime = LocalTime.of(23, 59)
        val wakeTime = LocalTime.of(0, 0)

        val sleepLog =
            SleepLog(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                bedTime = bedTime,
                wakeTime = wakeTime,
                mood = SleepLog.WakeUpMood.GOOD,
            )

        assertEquals(Duration.ofMinutes(1), sleepLog.timeInBed)
    }

    @Test
    fun `should calculate seconds precision across midnight correctly`() {
        val bedTime = LocalTime.of(23, 59, 59)
        val wakeTime = LocalTime.of(0, 0, 0)

        val sleepLog =
            SleepLog(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                bedTime = bedTime,
                wakeTime = wakeTime,
                mood = SleepLog.WakeUpMood.GOOD,
            )

        assertEquals(Duration.ofSeconds(1), sleepLog.timeInBed)
    }

    @Test
    fun `time in bed should always be positive and less than 24 hours`() {
        val cases =
            listOf(
                Pair(LocalTime.of(22, 0), LocalTime.of(6, 0)), // overnight
                Pair(LocalTime.of(0, 0), LocalTime.of(23, 59)), // same day long sleep
            )

        for ((bed, wake) in cases) {
            val sleepLog =
                SleepLog(
                    id = UUID.randomUUID(),
                    userId = UUID.randomUUID(),
                    bedTime = bed,
                    wakeTime = wake,
                    mood = SleepLog.WakeUpMood.OK,
                )

            assertTrue(!sleepLog.timeInBed.isNegative)
            assertTrue(sleepLog.timeInBed < Duration.ofHours(24))
        }
    }
}
