package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.domain.SleepLog
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class SleepLogResponseDto(
    val id: UUID,
    val userId: UUID,
    val sleepDate: LocalDate,
    val bedTime: LocalTime,
    val wakeTime: LocalTime,
    val mood: SleepLog.WakeUpMood,
    val durationInSeconds: Long
) {
    companion object {
        fun from(domain: SleepLog): SleepLogResponseDto = SleepLogResponseDto(
            id = domain.id,
            userId = domain.userId,
            sleepDate = domain.sleepDate,
            bedTime = domain.bedTime,
            wakeTime = domain.wakeTime,
            mood = domain.mood,
            durationInSeconds = domain.timeInBed.seconds
        )
    }
}