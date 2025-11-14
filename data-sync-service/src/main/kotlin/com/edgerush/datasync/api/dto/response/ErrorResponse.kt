package com.edgerush.datasync.api.dto.response

import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
)

data class ValidationErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: List<FieldError>,
)

data class FieldError(
    val field: String,
    val message: String,
)
