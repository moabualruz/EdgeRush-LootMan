package com.edgerush.lootman.api.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Global exception handler for lootman API endpoints.
 *
 * Handles common exceptions and converts them to appropriate HTTP responses.
 */
@ControllerAdvice(basePackages = ["com.edgerush.lootman.api"])
class GlobalExceptionHandler {
    /**
     * Handle IllegalArgumentException as 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Bad Request",
                    message = ex.message ?: "Invalid request parameters",
                ),
            )
    }

    /**
     * Handle IllegalStateException as 409 Conflict.
     */
    @ExceptionHandler(IllegalStateException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    status = HttpStatus.CONFLICT.value(),
                    error = "Conflict",
                    message = ex.message ?: "Operation cannot be completed due to current state",
                ),
            )
    }

    /**
     * Handle NoSuchElementException as 404 Not Found.
     */
    @ExceptionHandler(NoSuchElementException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Not Found",
                    message = ex.message ?: "Resource not found",
                ),
            )
    }

    /**
     * Handle generic exceptions.
     * This catches Spring's parameter binding exceptions and other errors.
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        return if (isParameterBindingException(ex)) {
            createBadRequestResponse(ex.message)
        } else {
            createInternalServerErrorResponse()
        }
    }

    /**
     * Checks if the exception is related to parameter binding.
     */
    private fun isParameterBindingException(ex: Exception): Boolean {
        val exceptionName = ex::class.simpleName ?: ""
        return exceptionName.contains("MissingServletRequestParameter") ||
            exceptionName.contains("MethodArgumentTypeMismatch") ||
            exceptionName.contains("BindException")
    }

    /**
     * Creates a Bad Request response.
     */
    private fun createBadRequestResponse(message: String?): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Bad Request",
                    message = message ?: "Invalid request parameters",
                ),
            )
    }

    /**
     * Creates an Internal Server Error response.
     */
    private fun createInternalServerErrorResponse(): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = "Internal Server Error",
                    message = "An unexpected error occurred",
                ),
            )
    }
}

/**
 * Standard error response format.
 */
data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
)
