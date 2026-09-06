package com.noom.interview.fullstack.sleep.infrastructure.persistence

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import com.noom.interview.fullstack.sleep.infrastructure.persistence.entity.SleepLogJpaEntity
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class JpaSleepLogRepository(
    private val springDataSleepLogRepository: SpringDataSleepLogRepository,
) : SleepLogRepository {
    override fun save(sleepLog: SleepLog): SleepLog {
        val entity = SleepLogJpaEntity.fromDomain(sleepLog)
        val saved = springDataSleepLogRepository.save(entity)
        return SleepLogJpaEntity.toDomain(saved)
    }

    override fun findByUserIdAndSleepDateBetween(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<SleepLog> {
        return springDataSleepLogRepository
            .findByUserIdAndSleepDateBetween(userId, startDate, endDate)
            .map(SleepLogJpaEntity::toDomain)
    }

    override fun findLastByUserId(userId: UUID): SleepLog? {
        return springDataSleepLogRepository
            .findTopByUserIdOrderBySleepDateDesc(userId)
            ?.let(SleepLogJpaEntity::toDomain)
    }
}