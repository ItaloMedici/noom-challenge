package com.noom.interview.fullstack.sleep.web.dto

import com.noom.interview.fullstack.sleep.application.CreateSleepLogCommand
import com.noom.interview.fullstack.sleep.domain.SleepLog
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import javax.validation.constraints.NotNull


data class CreateSleepLogRequestDto(
    @field:NotNull(message = "Sleep date is required")
    val sleepDate: LocalDate?,

    @field:NotNull(message = "Bed time is required")
    val bedTime: LocalTime?,

    @field:NotNull(message = "Wake time is required")
    val wakeTime: LocalTime?,

    @field:NotNull(message = "Mood is required")
    val mood: SleepLog.WakeUpMood?
) {

    fun toCommand(userId: UUID): CreateSleepLogCommand = CreateSleepLogCommand(
        userId = userId,
        sleepDate = sleepDate!!,
        bedTime = bedTime!!,
        wakeTime = wakeTime!!,
        mood = mood!!
    )
}