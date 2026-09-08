package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.application.CreateSleepLogUseCase
import com.noom.interview.fullstack.sleep.application.GetLastNightSleepUseCase
import com.noom.interview.fullstack.sleep.application.GetSleepStatisticsUseCase
import com.noom.interview.fullstack.sleep.application.GetStatsCommand
import com.noom.interview.fullstack.sleep.web.dto.CreateSleepLogRequestDto
import com.noom.interview.fullstack.sleep.web.dto.SleepLogResponseDto
import com.noom.interview.fullstack.sleep.web.dto.SleepStatsResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/v1/users/{userId}/sleep-logs")
class SleepLogController(
    private val getSleepStatisticsUseCase: GetSleepStatisticsUseCase,
    private val createSleepLogUseCase: CreateSleepLogUseCase,
    private val getLastNightSleepUseCase: GetLastNightSleepUseCase,
) {

    @PostMapping()
    fun createSleepLog(
        @PathVariable("userId") userId: UUID,
        @Valid @RequestBody request: CreateSleepLogRequestDto,
    ): ResponseEntity<SleepLogResponseDto> {
        val command = request.toCommand(userId = userId)

        val sleepLog = createSleepLogUseCase.execute(command)

        val response = SleepLogResponseDto.from(sleepLog)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

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

    @GetMapping("/last-night")
    fun getLastNightSleep(
        @PathVariable userId: UUID,
    ): ResponseEntity<SleepLogResponseDto> {
        val sleepLog = getLastNightSleepUseCase.execute(userId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(SleepLogResponseDto.from(sleepLog))
    }
}
