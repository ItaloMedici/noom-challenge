package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.exception.InvalidDateRangeException
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class GetSleepStatisticsUseCaseTest {

    private val sleepLogRepository = mockk<SleepLogRepository>()
    private val useCase = GetSleepStatisticsUseCase(sleepLogRepository)

    @Test
    fun `should compute stats for the last 30 days range`() {
        val user = User.create("italo")

        val expectedRangeStart = LocalDate.now().minusDays(29)
        val expectedRangeEnd = LocalDate.now()

        val sleepLog =
            SleepLog.create(
                userId = user.id,
                sleepDate = LocalDate.now(),
                bedTime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 0),
                mood = SleepLog.WakeUpMood.GOOD,
            )

        every {
            sleepLogRepository.findByUserIdAndSleepDateBetween(user.id, expectedRangeStart, expectedRangeEnd)
        } returns listOf(sleepLog)

        val command = GetStatsCommand(user.id, expectedRangeStart, expectedRangeEnd)
        val result = useCase.execute(command)

        assertThat(result).isNotNull
        val stats = result!!
        assertThat(stats.averageTimeInBed).isEqualTo(Duration.ofHours(8))
        assertThat(stats.averageBedTime).isEqualTo(LocalTime.of(22, 0))
        assertThat(stats.averageWakeTime).isEqualTo(LocalTime.of(6, 0))
        assertThat(stats.moodFrequencies).containsEntry(SleepLog.WakeUpMood.GOOD, 1)

        verify(exactly = 1) {
            sleepLogRepository.findByUserIdAndSleepDateBetween(user.id, expectedRangeStart, expectedRangeEnd)
        }
    }

    @Test
    fun `should delegate statistics calculation to SleepStatistics`() {
        val user = User.create("italo")
        val sleepLog =
            SleepLog.create(
                userId = user.id,
                sleepDate = LocalDate.now(),
                bedTime = LocalTime.of(23, 0),
                wakeTime = LocalTime.of(7, 0),
                mood = SleepLog.WakeUpMood.OK,
            )

        every {
            sleepLogRepository.findByUserIdAndSleepDateBetween(any(), any(), any())
        } returns listOf(sleepLog)

        val expectedRangeStart = LocalDate.now().minusDays(29)
        val expectedRangeEnd = LocalDate.now()

        val command = GetStatsCommand(user.id, expectedRangeStart, expectedRangeEnd)
        val result = useCase.execute(command)

        val expectedRange = expectedRangeStart..expectedRangeEnd
        val expectedStats = SleepStatistics.calculate(listOf(sleepLog), expectedRange)

        assertThat(result).isNotNull
        assertThat(result).isEqualTo(expectedStats)
    }

    @Test
    fun `should return null when the user has no sleep logs in range`() {
        val unknownUserId = UUID.randomUUID()
        val expectedRangeStart = LocalDate.now().minusDays(29)
        val expectedRangeEnd = LocalDate.now()

        every {
            sleepLogRepository.findByUserIdAndSleepDateBetween(unknownUserId, expectedRangeStart, expectedRangeEnd)
        } returns emptyList()

        val command = GetStatsCommand(unknownUserId, expectedRangeStart, expectedRangeEnd)
        val result = useCase.execute(command)

        assertThat(result).isNull()
        verify(exactly = 1) {
            sleepLogRepository.findByUserIdAndSleepDateBetween(unknownUserId, expectedRangeStart, expectedRangeEnd)
        }
    }

    @Test
    fun `should throw when start date is after end date`() {
        val userId = UUID.randomUUID()
        val start = LocalDate.of(2026, 1, 10)
        val end = LocalDate.of(2026, 1, 1)

        val command = GetStatsCommand(userId, start, end)

        assertThrows(InvalidDateRangeException::class.java) {
            useCase.execute(command)
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

        val stats = SleepStatistics.calculate(listOf(sleepLog), sleepDate..sleepDate)

        assertThat(stats.averageTimeInBed).isEqualTo(Duration.ofHours(6).plusMinutes(45))
        assertThat(stats.averageBedTime).isEqualTo(LocalTime.of(23, 30))
        assertThat(stats.averageWakeTime).isEqualTo(LocalTime.of(6, 15))
        assertThat(stats.moodFrequencies).containsEntry(SleepLog.WakeUpMood.GOOD, 1)
    }
}