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
    fun `should return the sleep log for yesterday`() {
        val user = User.create("italo")
        val lastNightSleepLog =
            SleepLog.create(
                userId = user.id,
                sleepDate = LocalDate.now().minusDays(1),
                bedTime = LocalTime.of(22, 0),
                wakeTime = LocalTime.of(6, 0),
                mood = SleepLog.WakeUpMood.GOOD,
            )

        every {
            sleepLogRepository.findByUserIdAndSleepDate(user.id, LocalDate.now().minusDays(1))
        } returns lastNightSleepLog

        val result = useCase.execute(user.id)

        assertThat(result).isSameAs(lastNightSleepLog)
        verify(exactly = 1) {
            sleepLogRepository.findByUserIdAndSleepDate(user.id, LocalDate.now().minusDays(1))
        }
    }

    @Test
    fun `should return null when the user has no sleep log for last night`() {
        val unknownUserId = UUID.randomUUID()
        val lastNightDate = LocalDate.now().minusDays(1)

        every { sleepLogRepository.findByUserIdAndSleepDate(unknownUserId, lastNightDate) } returns null

        val result = useCase.execute(unknownUserId)

        assertThat(result).isNull()
        verify(exactly = 1) { sleepLogRepository.findByUserIdAndSleepDate(unknownUserId, lastNightDate) }
    }
}