package com.noom.interview.fullstack.sleep.web

import com.noom.interview.fullstack.sleep.domain.exception.DuplicateSleepLogException
import com.fasterxml.jackson.databind.JsonMappingException
import com.noom.interview.fullstack.sleep.domain.exception.DuplicateUsernameException
import com.noom.interview.fullstack.sleep.domain.exception.InvalidDateRangeException
import com.noom.interview.fullstack.sleep.domain.exception.StatisticsRangeExceededException
import com.noom.interview.fullstack.sleep.domain.exception.UserNotFoundException
import com.noom.interview.fullstack.sleep.web.dto.ErrorResponseDto
import com.noom.interview.fullstack.sleep.web.dto.FieldErrorDto
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(InvalidDateRangeException::class)
    fun handleInvalidDateRange(ex: InvalidDateRangeException): ResponseEntity<ErrorResponseDto> =
        badRequest(ex.message ?: "Invalid date range")

    @ExceptionHandler(StatisticsRangeExceededException::class)
    fun handleStatisticsRangeExceeded(ex: StatisticsRangeExceededException): ResponseEntity<ErrorResponseDto> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponseDto(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = ex.message ?: "Statistics range cannot exceed ${ex.maxDays} days",
                )
            )

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

    @ExceptionHandler(DuplicateSleepLogException::class)
    fun handleDuplicateSleepLog(ex: DuplicateSleepLogException): ResponseEntity<ErrorResponseDto> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponseDto(
                    status = HttpStatus.CONFLICT.value(),
                    message = ex.message ?: "Sleep log already exists",
                    errors = listOf(FieldErrorDto("sleepDate", "Sleep log already exists on this date")),
                )
            )

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(ex: DataIntegrityViolationException): ResponseEntity<ErrorResponseDto> =
        ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponseDto(HttpStatus.CONFLICT.value(), "Resource already exists"))

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponseDto> {
        val field = ex.name
        val requiredType = ex.requiredType
        val fieldMessage =
            if (requiredType != null && Number::class.java.isAssignableFrom(requiredType)) {
                "$field must be a positive integer"
            } else {
                "$field has an invalid value"
            }
        return badRequest("Invalid query parameter", listOf(FieldErrorDto(field, fieldMessage)))
    }

    @ExceptionHandler(NumberFormatException::class)
    fun handleNumberFormat(ex: NumberFormatException): ResponseEntity<ErrorResponseDto> =
        badRequest("Invalid query parameter")

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
    fun handleUnreadableBody(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponseDto> {
        val fieldErrors = ex.malformedFieldNames().map { FieldErrorDto(it, "Malformed value") }
        return badRequest("Malformed request body", fieldErrors)
    }

    private fun HttpMessageNotReadableException.malformedFieldNames(): List<String> {
        var cause: Throwable? = this.cause
        while (cause != null) {
            if (cause is JsonMappingException) {
                return listOfNotNull(cause.path.lastOrNull { it.fieldName != null }?.fieldName)
            }
            cause = cause.cause
        }
        return emptyList()
    }

    private fun badRequest(
        message: String,
        errors: List<FieldErrorDto> = emptyList(),
    ): ResponseEntity<ErrorResponseDto> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponseDto(HttpStatus.BAD_REQUEST.value(), message, errors))
}
