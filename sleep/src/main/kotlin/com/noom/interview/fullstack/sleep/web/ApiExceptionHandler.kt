package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.domain.exception.DuplicateUsernameException
import com.noom.interview.fullstack.sleep.domain.exception.InvalidDateRangeException
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.web.dto.ErrorResponseDto
import com.noom.interview.fullstack.sleep.web.dto.FieldErrorDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
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

    @ExceptionHandler(DuplicateUsernameException::class)
    fun handleDuplicateUsername(ex: DuplicateUsernameException): ResponseEntity<ErrorResponseDto> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponseDto(HttpStatus.CONFLICT.value(), ex.message ?: "Username already taken"))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponseDto> =
        badRequest(ex.message ?: "Invalid request")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponseDto> =
        badRequest(
            message = "Validation failed",
            errors = ex.bindingResult.fieldErrors.map { FieldErrorDto(it.field, it.defaultMessage ?: "Invalid value") }
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(@Suppress("UNUSED_PARAMETER") ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponseDto> =
        badRequest("Malformed request body")

    private fun badRequest(
        message: String,
        errors: List<FieldErrorDto> = emptyList(),
    ): ResponseEntity<ErrorResponseDto> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), message, errors))
}
