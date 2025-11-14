package com.edgerush.datasync.api.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

/**
 * Global exception handler for REST API endpoints.
 *
 * Converts domain exceptions and validation errors into appropriate HTTP responses.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Handle IllegalArgumentException as 400 Bad Request.
     *
     * These exceptions typically indicate validation failures in domain models or use cases.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    timestamp = Instant.now(),
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = ex.message ?: "Invalid request"
                )
            )
    }

    /**
     * Handle generic exceptions as 500 Internal Server Error.
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    timestamp = Instant.now(),
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                    message = "An unexpected error occurred"
                )
            )
    }
}

/**
 * Standard error response format.
 */
data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String
)
