package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.application.GetSleepStatisticsUseCase
import com.noom.interview.fullstack.sleep.application.GetStatsCommand
import com.noom.interview.fullstack.sleep.web.dto.SleepStatsResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/users/{userId}/sleep-logs")
class SleepLogController(
    private val getSleepStatisticsUseCase: GetSleepStatisticsUseCase,
) {
    @GetMapping("/stats")
    fun getStatistics(
        @PathVariable userId: UUID,
        @RequestParam(required = false) days: Long? = null,
    ): ResponseEntity<SleepStatsResponseDto> {
        val calculation = getSleepStatisticsUseCase.execute(
            GetStatsCommand(userId = userId, days = days)
        )
        return ResponseEntity.ok(SleepStatsResponseDto.from(calculation))
    }
}
