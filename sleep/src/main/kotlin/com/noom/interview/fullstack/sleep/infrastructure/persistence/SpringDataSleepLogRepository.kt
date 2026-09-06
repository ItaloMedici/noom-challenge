package com.noom.interview.fullstack.sleep.infrastructure.persistence

import com.noom.interview.fullstack.sleep.infrastructure.persistence.entity.SleepLogJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface SpringDataSleepLogRepository : JpaRepository<SleepLogJpaEntity, UUID> {
    fun findByUserIdAndSleepDateBetween(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<SleepLogJpaEntity>

    fun findTopByUserIdOrderBySleepDateDesc(userId: UUID): SleepLogJpaEntity?
}