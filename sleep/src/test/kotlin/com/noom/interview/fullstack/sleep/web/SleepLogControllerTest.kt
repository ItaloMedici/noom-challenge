package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.application.GetSleepStatisticsUseCase
import com.noom.interview.fullstack.sleep.application.GetStatsCommand
import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.SleepStatistics
import com.noom.interview.fullstack.sleep.domain.model.DateRange
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class SleepLogControllerTest {

    private val getSleepStatisticsUseCase = mockk<GetSleepStatisticsUseCase>()
    private val controller = SleepLogController(getSleepStatisticsUseCase)

    @Test
    fun `should return 200 with mapped statistics from the use case`() {
        val userId = UUID.randomUUID()
        val calculation = SleepStatistics.SleepStatisticsCalculation(
            range = DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6)),
            averageTimeInBed = Duration.ofHours(8),
            averageBedTime = LocalTime.of(22, 0),
            averageWakeTime = LocalTime.of(6, 0),
            moodFrequencies = mapOf(SleepLog.WakeUpMood.GOOD to 1),
        )

        every { getSleepStatisticsUseCase.execute(any()) } returns calculation

        val response = controller.getStatistics(userId, days = 30)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body.rangeStart).isEqualTo(LocalDate.of(2026, 8, 8))
        assertThat(body.rangeEnd).isEqualTo(LocalDate.of(2026, 9, 6))
        assertThat(body.averageTimeInBed).isEqualTo(Duration.ofHours(8))
        assertThat(body.averageBedTime).isEqualTo(LocalTime.of(22, 0))
        assertThat(body.averageWakeTime).isEqualTo(LocalTime.of(6, 0))
        assertThat(body.moodFrequencies).containsEntry("GOOD", 1)

        verify(exactly = 1) {
            getSleepStatisticsUseCase.execute(GetStatsCommand(userId = userId, days = 30))
        }
    }

    @Test
    fun `should return 200 with empty stats when the use case reports no data in range`() {
        val userId = UUID.randomUUID()
        val emptyCalculation = SleepStatistics.empty(
            DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))
        )

        every { getSleepStatisticsUseCase.execute(any()) } returns emptyCalculation

        val response = controller.getStatistics(userId, days = 30)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val body = response.body!!
        assertThat(body.rangeStart).isEqualTo(LocalDate.of(2026, 8, 8))
        assertThat(body.rangeEnd).isEqualTo(LocalDate.of(2026, 9, 6))
        assertThat(body.averageTimeInBed).isNull()
        assertThat(body.averageBedTime).isNull()
        assertThat(body.averageWakeTime).isNull()
        assertThat(body.moodFrequencies).isEmpty()
    }

    @Test
    fun `should forward an absent days param to the use case`() {
        val userId = UUID.randomUUID()

        every { getSleepStatisticsUseCase.execute(any()) } returns SleepStatistics.empty(
            DateRange(LocalDate.of(2026, 8, 8), LocalDate.of(2026, 9, 6))
        )

        controller.getStatistics(userId)

        verify(exactly = 1) {
            getSleepStatisticsUseCase.execute(GetStatsCommand(userId = userId, days = null))
        }
    }

    @Test
    fun `should propagate exceptions from the use case`() {
        val userId = UUID.randomUUID()

        every {
            getSleepStatisticsUseCase.execute(any())
        } throws IllegalArgumentException("Number of days must be positive, but was 0")

        assertThrows(IllegalArgumentException::class.java) {
            controller.getStatistics(userId, days = 0)
        }
    }
}
