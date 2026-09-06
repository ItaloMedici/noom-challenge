package com.noom.interview.fullstack.sleep.domain.repository

import com.noom.interview.fullstack.sleep.domain.SleepLog
import java.time.LocalDate
import java.util.UUID

interface SleepLogRepository {
    fun save(sleepLog: SleepLog): SleepLog

    fun findByUserIdAndSleepDateBetween(
        userId: UUID,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<SleepLog>

    fun findLastByUserId(userId: UUID): SleepLog?
}