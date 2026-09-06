package com.noom.interview.fullstack.sleep.domain

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class SleepLog(
    val id: UUID,
    val userId: UUID,
    val sleepDate: LocalDate = LocalDate.now(),
    val bedTime: LocalTime,
    val wakeTime: LocalTime,
    val mood: WakeUpMood,
) {
    val timeInBed: Duration
        get() = calculateTimeInBed(bedTime, wakeTime)

    init {
        require(!sleepDate.isAfter(LocalDate.now())) { "Sleep date cannot be in the future" }
        require(bedTime != wakeTime) { "Bed time and wake time cannot be identical" }
    }

    enum class WakeUpMood {
        BAD,
        OK,
        GOOD,
    }

    companion object {
        fun create(
            userId: UUID,
            sleepDate: LocalDate = LocalDate.now(),
            bedTime: LocalTime,
            wakeTime: LocalTime,
            mood: WakeUpMood,
        ): SleepLog = SleepLog(
            id = UUID.randomUUID(),
            userId = userId,
            sleepDate = sleepDate,
            bedTime = bedTime,
            wakeTime = wakeTime,
            mood = mood
        )
    }

    private fun calculateTimeInBed(
        bedTime: LocalTime,
        wakeTime: LocalTime,
    ): Duration {
        val duration = Duration.between(bedTime, wakeTime)
        // When overnight sleep, the duration will be negative, so adding 24h corrects the duration to be positive
        return if (duration.isNegative) duration.plusHours(24) else duration
    }
}
