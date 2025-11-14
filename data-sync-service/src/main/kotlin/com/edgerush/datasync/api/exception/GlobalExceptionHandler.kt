package com.edgerush.datasync.api.exception

import com.edgerush.datasync.api.dto.response.ErrorResponse
import com.edgerush.datasync.api.dto.response.FieldError
import com.edgerush.datasync.api.dto.response.ValidationErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(
        ex: ResourceNotFoundException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Resource not found",
                path = exchange.request.path.value(),
            )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidation(
        ex: WebExchangeBindException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ValidationErrorResponse> {
        val fieldErrors =
            ex.bindingResult.fieldErrors.map {
                FieldError(it.field, it.defaultMessage ?: "Invalid value")
            }

        val error =
            ValidationErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Failed",
                message = "Input validation failed",
                path = exchange.request.path.value(),
                fieldErrors = fieldErrors,
            )
        return ResponseEntity.badRequest().body(error)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(
        ex: AccessDeniedException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> {
        val error =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = ex.message ?: "Access denied",
                path = exchange.request.path.value(),
            )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(
        ex: DataIntegrityViolationException,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Database constraint violation", ex)
        val error =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = "Database constraint violation",
                path = exchange.request.path.value(),
            )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(
        ex: Exception,
        exchange: ServerWebExchange,
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error", ex)
        val error =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "An unexpected error occurred",
                path = exchange.request.path.value(),
            )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error)
    }
}
