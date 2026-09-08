package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class GetLastNightSleepUseCaseTest {

    private val sleepLogRepository = mockk<SleepLogRepository>()
    private val useCase = GetLastNightSleepUseCase(sleepLogRepository)

    @Test
    fun `should return the log with the latest sleepDate when multiple logs exist`() {
        val user = User.create("italo")
        val todayLog =
            SleepLog.create(
                userId = user.id,
                sleepDate = LocalDate.now(),
                bedTime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 0),
                mood = SleepLog.WakeUpMood.GOOD,
            )
        SleepLog.create(
            userId = user.id,
            sleepDate = LocalDate.now().minusDays(5),
            bedTime = LocalTime.of(21, 0),
            wakeTime = LocalTime.of(5, 0),
            mood = SleepLog.WakeUpMood.OK,
        )

        every { sleepLogRepository.findLatestByUserId(user.id) } returns todayLog

        val result = useCase.execute(user.id)

        assertThat(result).isSameAs(todayLog)
        verify(exactly = 1) { sleepLogRepository.findLatestByUserId(user.id) }
        verify(exactly = 0) { sleepLogRepository.findByUserIdAndSleepDate(any(), any()) }
    }

    @Test
    fun `should return null when the user has no sleep logs at all`() {
        val unknownUserId = UUID.randomUUID()

        every { sleepLogRepository.findLatestByUserId(unknownUserId) } returns null

        val result = useCase.execute(unknownUserId)

        assertThat(result).isNull()
        verify(exactly = 1) { sleepLogRepository.findLatestByUserId(unknownUserId) }
        verify(exactly = 0) { sleepLogRepository.findByUserIdAndSleepDate(any(), any()) }
    }
}
