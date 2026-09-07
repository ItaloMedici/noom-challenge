package com.noom.interview.fullstack.sleep.web.dto

data class ErrorResponseDto(
    val status: Int,
    val message: String,
    val errors: List<String> = emptyList(),
)
