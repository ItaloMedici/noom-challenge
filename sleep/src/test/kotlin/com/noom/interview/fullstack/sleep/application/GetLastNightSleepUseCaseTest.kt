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
    fun `should return the latest sleep log for the user`() {
        val user = User.create("italo")
        val latestSleepLog =
            SleepLog.create(
                userId = user.id,
                sleepDate = LocalDate.now().minusDays(1),
                bedTime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 0),
                mood = SleepLog.WakeUpMood.GOOD,
            )

        every { sleepLogRepository.findLastByUserId(user.id) } returns latestSleepLog

        val result = useCase.execute(user.id)

        assertThat(result).isSameAs(latestSleepLog)
        verify(exactly = 1) { sleepLogRepository.findLastByUserId(user.id) }
    }

    @Test
    fun `should return null when the user has no sleep logs`() {
        val unknownUserId = UUID.randomUUID()

        every { sleepLogRepository.findLastByUserId(unknownUserId) } returns null

        val result = useCase.execute(unknownUserId)

        assertThat(result).isNull()
        verify(exactly = 1) { sleepLogRepository.findLastByUserId(unknownUserId) }
    }
}