package com.noom.interview.fullstack.sleep.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DateRangeTest {

    private val fixedClock = Clock.fixed(Instant.parse("2026-09-06T12:00:00Z"), ZoneId.of("UTC"))

    @Test
    fun `should throw when start date is after end date`() {
        val execution = assertThrows(IllegalArgumentException::class.java) {
            DateRange(start = LocalDate.of(2026, 9, 6), end = LocalDate.of(2026, 9, 1))
        }

        assertThat(execution.message).contains("start")
    }

    @Test
    fun `should allow start date equal to end date`() {
        val today = LocalDate.of(2026, 9, 6)

        val range = DateRange(start = today, end = today)

        assertThat(range.start).isEqualTo(today)
        assertThat(range.end).isEqualTo(today)
    }

    @Test
    fun `lastNDays should resolve range relative to the given clock`() {
        val range = DateRange.lastNDays(days = 30, clock = fixedClock)

        assertThat(range.start).isEqualTo(LocalDate.of(2026, 8, 8))
        assertThat(range.end).isEqualTo(LocalDate.of(2026, 9, 6))
    }

    @Test
    fun `lastNDays with a single day covers only today`() {
        val range = DateRange.lastNDays(days = 1, clock = fixedClock)

        assertThat(range.start).isEqualTo(LocalDate.of(2026, 9, 6))
        assertThat(range.end).isEqualTo(LocalDate.of(2026, 9, 6))
    }

    @Test
    fun `lastNDays should throw when days is zero`() {
        assertThrows(IllegalArgumentException::class.java) {
            DateRange.lastNDays(days = 0, clock = fixedClock)
        }
    }

    @Test
    fun `lastNDays should throw when days is negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            DateRange.lastNDays(days = -5, clock = fixedClock)
        }
    }

    @Test
    fun `should behave as a ClosedRange of LocalDate`() {
        val range = DateRange(start = LocalDate.of(2026, 9, 1), end = LocalDate.of(2026, 9, 6))

        assertThat(range.endInclusive).isEqualTo(LocalDate.of(2026, 9, 6))
        assertThat(range.contains(LocalDate.of(2026, 9, 3))).isTrue
        assertThat(range.contains(LocalDate.of(2026, 8, 31))).isFalse
    }
}
