package com.noom.interview.fullstack.sleep.domain.exception

class StatisticsRangeExceededException(
    val requestedDays: Long,
    val maxDays: Long,
) : RuntimeException("Statistics range cannot exceed $maxDays days, but $requestedDays days was requested")
