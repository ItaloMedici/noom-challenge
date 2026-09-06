package com.noom.interview.fullstack.sleep.infrastructure.persistence

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.User
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.domain.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@SpringBootTest
@Transactional
class SleepLogRepositoryTest {

    @Autowired
    lateinit var sleepLogRepository: SleepLogRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var springDataUserRepository: SpringDataUserRepository

    private lateinit var userId: UUID

    @BeforeEach
    fun setUp() {
        val user = userRepository.save(User.create("italo"))
        userId = user.id
    }

    @Test
    fun `save should save sleep slog including mood successfully`() {
        val log =
            SleepLog.create(
                userId = userId,
                bedTime = LocalTime.of(22, 30),
                wakeTime = LocalTime.of(6, 15),
                mood = SleepLog.WakeUpMood.GOOD,
            )

        val saved = sleepLogRepository.save(log)

        val found = sleepLogRepository.findByUserIdAndSleepDateBetween(
            userId = userId,
            startDate = saved.sleepDate,
            endDate = saved.sleepDate,
        ).single()

        assertThat(found.id).isEqualTo(saved.id)
        assertThat(found.userId).isEqualTo(userId)
        assertThat(found.mood).isEqualTo(SleepLog.WakeUpMood.GOOD)
        assertThat(found.bedTime).isEqualTo(LocalTime.of(22, 30))
        assertThat(found.wakeTime).isEqualTo(LocalTime.of(6, 15))
    }

    @Test
    fun `findByUserIdAndSleepDateBetween should include boundaries`() {
        val otherUserId = userRepository.save(User.create("bob")).id

        val firstLog = SleepLog.create(
            userId = userId,
            sleepDate = LocalDate.now().minusDays(2),
            bedTime = LocalTime.of(22, 0),
            wakeTime = LocalTime.of(6, 0),
            mood = SleepLog.WakeUpMood.GOOD,
        )

        val secondLog = sleepLog(
            userId,
            LocalDate.now().minusDays(1),
            LocalTime.of(23, 0),
            LocalTime.of(7, 0)
        )

        val thirdLog = sleepLog(
            userId,
            LocalDate.now().minusDays(3),
            LocalTime.of(21, 0),
            LocalTime.of(5, 0)
        )

        val fourthLog = sleepLog(
            otherUserId,
            LocalDate.now().minusDays(1),
            LocalTime.of(23, 0),
            LocalTime.of(8, 0)
        )

        sleepLogRepository.save(firstLog)
        sleepLogRepository.save(secondLog)
        sleepLogRepository.save(thirdLog)
        sleepLogRepository.save(fourthLog)

        val result = sleepLogRepository.findByUserIdAndSleepDateBetween(
            userId = userId,
            startDate = LocalDate.now().minusDays(2),
            endDate = LocalDate.now().minusDays(1),
        )

        assertThat(result.map { it.id }).containsExactlyInAnyOrder(firstLog.id, secondLog.id)
    }

    @Test
    fun `findByUserIdAndSleepDateBetween should return empty when none`() {
        val result = sleepLogRepository.findByUserIdAndSleepDateBetween(
            userId = userId,
            startDate = LocalDate.now().minusDays(5),
            endDate = LocalDate.now().minusDays(4),
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `findLastByUserId should return most recent log`() {
        sleepLogRepository.save(sleepLog(userId, LocalDate.now().minusDays(3), LocalTime.of(22, 0), LocalTime.of(6, 0)))
        val newer =
            sleepLogRepository.save(
                sleepLog(
                    userId,
                    LocalDate.now().minusDays(1),
                    LocalTime.of(23, 0),
                    LocalTime.of(7, 0)
                )
            )

        val last = sleepLogRepository.findLastByUserId(userId)

        assertThat(last).isNotNull
        assertThat(last!!.id).isEqualTo(newer.id)
    }

    @Test
    fun `findLastByUserId should return null when user has no logs`() {
        val last = sleepLogRepository.findLastByUserId(userId)

        assertThat(last).isNull()
    }

    @Test
    fun `deleting a user should cascade delete the sleep logs`() {
        sleepLogRepository.save(sleepLog(userId, LocalDate.now().minusDays(1), LocalTime.of(22, 0), LocalTime.of(6, 0)))
        sleepLogRepository.save(sleepLog(userId, LocalDate.now(), LocalTime.of(23, 0), LocalTime.of(7, 0)))

        springDataUserRepository.deleteById(userId)

        val remaining = sleepLogRepository.findByUserIdAndSleepDateBetween(
            userId = userId,
            startDate = LocalDate.now().minusDays(30),
            endDate = LocalDate.now(),
        )

        assertThat(remaining).isEmpty()
    }

    private fun sleepLog(
        userId: UUID,
        sleepDate: LocalDate,
        bedTime: LocalTime,
        wakeTime: LocalTime,
    ): SleepLog =
        SleepLog.create(
            userId = userId,
            sleepDate = sleepDate,
            bedTime = bedTime,
            wakeTime = wakeTime,
            mood = SleepLog.WakeUpMood.GOOD,
        )
}