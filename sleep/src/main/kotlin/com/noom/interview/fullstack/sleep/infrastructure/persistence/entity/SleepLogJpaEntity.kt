package com.noom.interview.fullstack.sleep.infrastructure.persistence.entity

import com.noom.interview.fullstack.sleep.domain.SleepLog
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "sleep_logs")
data class SleepLogJpaEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "sleep_date", nullable = false)
    val sleepDate: LocalDate,

    @Column(name = "bed_time", nullable = false)
    val bedTime: LocalTime,

    @Column(name = "wake_time", nullable = false)
    val wakeTime: LocalTime,

    @Enumerated(EnumType.STRING)
    @Column(name = "wake_mood", nullable = false, length = 10)
    val wakeMood: SleepLog.WakeUpMood,
) {
    companion object {
        fun fromDomain(sleepLog: SleepLog): SleepLogJpaEntity =
            SleepLogJpaEntity(
                id = sleepLog.id,
                userId = sleepLog.userId,
                sleepDate = sleepLog.sleepDate,
                bedTime = sleepLog.bedTime,
                wakeTime = sleepLog.wakeTime,
                wakeMood = sleepLog.mood,
            )

        fun toDomain(entity: SleepLogJpaEntity): SleepLog =
            SleepLog(
                id = entity.id,
                userId = entity.userId,
                sleepDate = entity.sleepDate,
                bedTime = entity.bedTime,
                wakeTime = entity.wakeTime,
                mood = entity.wakeMood,
            )
    }
}