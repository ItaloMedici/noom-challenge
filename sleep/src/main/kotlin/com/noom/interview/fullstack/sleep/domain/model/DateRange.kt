package com.noom.interview.fullstack.sleep.domain.model

import java.time.Clock
import java.time.LocalDate

data class DateRange(
    override val start: LocalDate,
    val end: LocalDate,
) : ClosedRange<LocalDate> {

    init {
        require(!start.isAfter(end)) { "Date range start ($start) cannot be after end ($end)" }
    }

    override val endInclusive: LocalDate
        get() = end

    companion object {
        fun lastNDays(days: Long, clock: Clock): DateRange {
            require(days > 0) { "Number of days must be positive, but was $days" }
            val today = LocalDate.now(clock)
            return DateRange(start = today.minusDays(days - 1), end = today)
        }
    }
}
