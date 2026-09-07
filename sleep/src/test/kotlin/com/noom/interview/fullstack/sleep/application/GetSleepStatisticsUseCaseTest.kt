package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.model.DateRange
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class GetSleepStatisticsUseCaseTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-09-06T12:00:00Z"), ZoneId.of("UTC"))
    private val sleepLogRepository = mockk<SleepLogRepository>()
    private val useCase = GetSleepStatisticsUseCase(sleepLogRepository, fixedClock)

    @Test
    fun `should resolve the last 30 days relative to the fixed clock when querying with days = 30`() {
        val user = User.create("italo")
        val expectedRange = DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))

        val sleepLog = SleepLog.create(
            userId = user.id,
            sleepDate = LocalDate.of(2026, 9, 6),
            bedTime = LocalTime.of(22, 0),
            wakeTime = LocalTime.of(6, 0),
            mood = SleepLog.WakeUpMood.GOOD,
        )

        every {
            sleepLogRepository.findByUserIdAndSleepDateBetween(user.id, expectedRange.start, expectedRange.end)
        } returns listOf(sleepLog)

        val result = useCase.execute(GetStatsCommand(userId = user.id, days = 30))

        assertThat(result.averageTimeInBed).isEqualTo(Duration.ofHours(8))
        assertThat(result.averageBedTime).isEqualTo(LocalTime.of(22, 0))
        assertThat(result.averageWakeTime).isEqualTo(LocalTime.of(6, 0))
        assertThat(result.moodFrequencies).containsEntry(SleepLog.WakeUpMood.GOOD, 1)

        verify(exactly = 1) {
            sleepLogRepository.findByUserIdAndSleepDateBetween(user.id, expectedRange.start, expectedRange.end)
        }
    }

    @Test
    fun `should fall back to the standard 30-day window when days is not provided`() {
        val user = User.create("italo")
        val expectedRange = DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))

        every {
            sleepLogRepository.findByUserIdAndSleepDateBetween(user.id, expectedRange.start, expectedRange.end)
        } returns emptyList()

        useCase.execute(GetStatsCommand(userId = user.id))

        verify(exactly = 1) {
            sleepLogRepository.findByUserIdAndSleepDateBetween(user.id, expectedRange.start, expectedRange.end)
        }
    }

    @Test
    fun `should return a non-null empty calculation when the user has no sleep logs in range`() {
        val unknownUserId = UUID.randomUUID()
        val expectedRange = DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))

        every {
            sleepLogRepository.findByUserIdAndSleepDateBetween(unknownUserId, expectedRange.start, expectedRange.end)
        } returns emptyList()

        val result = useCase.execute(GetStatsCommand(userId = unknownUserId, days = 30))

        assertThat(result).isNotNull
        assertThat(result).isEqualTo(SleepStatistics.empty(expectedRange))
        assertThat(result.averageTimeInBed).isNull()
        assertThat(result.averageBedTime).isNull()
        assertThat(result.averageWakeTime).isNull()
        assertThat(result.moodFrequencies).isEmpty()

        verify(exactly = 1) {
            sleepLogRepository.findByUserIdAndSleepDateBetween(unknownUserId, expectedRange.start, expectedRange.end)
        }
    }

    @Test
    fun `should delegate statistics calculation to SleepStatistics`() {
        val user = User.create("italo")
        val expectedRange = DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))

        val sleepLog = SleepLog.create(
            userId = user.id,
            sleepDate = LocalDate.of(2026, 9, 6),
            bedTime = LocalTime.of(23, 0),
            wakeTime = LocalTime.of(7, 0),
            mood = SleepLog.WakeUpMood.OK,
        )

        every {
            sleepLogRepository.findByUserIdAndSleepDateBetween(user.id, expectedRange.start, expectedRange.end)
        } returns listOf(sleepLog)

        val result = useCase.execute(GetStatsCommand(userId = user.id, days = 30))
        val expectedStats = SleepStatistics.calculate(listOf(sleepLog), expectedRange)

        assertThat(result).isEqualTo(expectedStats)
    }

    @Test
    fun `should throw when days is not positive`() {
        val userId = UUID.randomUUID()

        assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(GetStatsCommand(userId = userId, days = 0))
        }

        verify(exactly = 0) {
            sleepLogRepository.findByUserIdAndSleepDateBetween(any(), any(), any())
        }
    }

    @Test
    fun `should compute overnight sleep duration correctly`() {
        val user = User.create("italo")
        val sleepDate = LocalDate.of(2026, 1, 1)

        val sleepLog = SleepLog.create(
            userId = user.id,
            sleepDate = sleepDate,
            bedTime = LocalTime.of(23, 30),
            wakeTime = LocalTime.of(6, 15),
            mood = SleepLog.WakeUpMood.GOOD,
        )

        val stats = SleepStatistics.calculate(listOf(sleepLog), DateRange(sleepDate, sleepDate))

        assertThat(stats.averageTimeInBed).isEqualTo(Duration.ofHours(6).plusMinutes(45))
        assertThat(stats.averageBedTime).isEqualTo(LocalTime.of(23, 30))
        assertThat(stats.averageWakeTime).isEqualTo(LocalTime.of(6, 15))
        assertThat(stats.moodFrequencies).containsEntry(SleepLog.WakeUpMood.GOOD, 1)
    }
}
