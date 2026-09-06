package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import java.util.*

class GetLastNightSleepUseCase(
    private val sleepLogRepository: SleepLogRepository,
) {
    fun execute(userId: UUID): SleepLog? =
        sleepLogRepository.findLastByUserId(userId)
}