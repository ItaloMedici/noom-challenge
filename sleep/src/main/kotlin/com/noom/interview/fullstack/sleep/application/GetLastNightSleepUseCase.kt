package com.noom.interview.fullstack.sleep.application

import com.noom.interview.fullstack.sleep.common.annotation.UseCase
import com.noom.interview.fullstack.sleep.domain.SleepLog
import com.noom.interview.fullstack.sleep.domain.repository.SleepLogRepository
import java.util.*

@UseCase
class GetLastNightSleepUseCase(
    private val sleepLogRepository: SleepLogRepository,
) {
    fun execute(userId: UUID): SleepLog? {
        return sleepLogRepository.findLatestByUserId(userId)
    }
}