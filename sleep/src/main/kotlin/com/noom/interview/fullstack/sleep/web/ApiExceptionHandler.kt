package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.domain.exception.InvalidDateRangeException
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.web.dto.ErrorResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(InvalidDateRangeException::class)
    fun handleInvalidDateRange(ex: InvalidDateRangeException): ResponseEntity<ErrorResponseDto> =
        badRequest(ex.message ?: "Invalid date range")

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(ex: UserNotFoundException): ResponseEntity<ErrorResponseDto> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponseDto(HttpStatus.NOT_FOUND.value(), ex.message ?: "User not found"))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponseDto> =
        badRequest(ex.message ?: "Invalid request")

    private fun badRequest(message: String): ResponseEntity<ErrorResponseDto> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), message))
}
