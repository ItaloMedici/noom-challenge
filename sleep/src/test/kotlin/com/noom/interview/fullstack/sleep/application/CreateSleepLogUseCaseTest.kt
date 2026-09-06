package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.domain.repository.UserNotFoundException
import com.noom.interview.fullstack.sleep.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class CreateSleepLogUseCaseTest {

    private val sleepLogRepository = mockk<SleepLogRepository>()
    private val userRepository = mockk<UserRepository>()
    private val useCase = CreateSleepLogUseCase(sleepLogRepository, userRepository)

    @Test
    fun `should save a new sleep log for an existing user`() {
        val user = User.create("italo")
        val sleepDate = LocalDate.now().minusDays(1)
        val command = CreateSleepLogCommand(
            userId = user.id,
            sleepDate = sleepDate,
            bedTime = LocalTime.of(22, 0),
            wakeTime = LocalTime.of(6, 0),
            mood = SleepLog.WakeUpMood.GOOD,
        )

        every { userRepository.findById(user.id) } returns user

        val savedSleepLogSlot = slot<SleepLog>()
        every { sleepLogRepository.save(capture(savedSleepLogSlot)) } answers {
            savedSleepLogSlot.captured
        }

        val result = useCase.execute(command)

        assertThat(savedSleepLogSlot.isCaptured).isTrue()
        val savedSleepLog = savedSleepLogSlot.captured
        assertThat(savedSleepLog).isSameAs(result)
        assertThat(savedSleepLog.userId).isEqualTo(user.id)
        assertThat(savedSleepLog.sleepDate).isEqualTo(sleepDate)
        assertThat(savedSleepLog.bedTime).isEqualTo(LocalTime.of(22, 0))
        assertThat(savedSleepLog.wakeTime).isEqualTo(LocalTime.of(6, 0))
        assertThat(savedSleepLog.mood).isEqualTo(SleepLog.WakeUpMood.GOOD)
        assertThat(savedSleepLog.timeInBed).isEqualTo(Duration.ofHours(8))

        verify(exactly = 1) { userRepository.findById(user.id) }
        verify(exactly = 1) { sleepLogRepository.save(any()) }
    }

    @Test
    fun `should throw when user does not exist`() {
        val unknownUserId = UUID.randomUUID()

        every { userRepository.findById(unknownUserId) } returns null

        val execution = assertThrows(UserNotFoundException::class.java) {
            useCase.execute(
                CreateSleepLogCommand(
                    userId = unknownUserId,
                    sleepDate = LocalDate.now(),
                    bedTime = LocalTime.of(22, 0),
                    wakeTime = LocalTime.of(6, 0),
                    mood = SleepLog.WakeUpMood.OK,
                )
            )
        }

        assertThat(execution.userId).isEqualTo(unknownUserId)
        verify(exactly = 1) { userRepository.findById(unknownUserId) }
        verify(exactly = 0) { sleepLogRepository.save(any()) }
    }
}