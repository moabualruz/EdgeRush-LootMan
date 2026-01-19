package com.edgerush.lootman.api.common

import com.edgerush.lootman.api.auth.GuildAccessDeniedException
import com.edgerush.lootman.api.auth.NoLinkedRaiderException
import com.edgerush.lootman.domain.shared.GuildNotFoundException
import com.edgerush.lootman.domain.shared.ItemNotFoundException
import com.edgerush.lootman.domain.shared.LootBanActiveException
import com.edgerush.lootman.domain.shared.RaiderNotFoundException
import com.edgerush.lootman.domain.shared.InvalidCredentialsException
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
     * Handle RaiderNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(RaiderNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleRaiderNotFoundException(ex: RaiderNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Not Found",
                    message = ex.message ?: "Raider not found",
                ),
            )
    }

    /**
     * Handle GuildNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(GuildNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleGuildNotFoundException(ex: GuildNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Not Found",
                    message = ex.message ?: "Guild not found",
                ),
            )
    }

    /**
     * Handle ItemNotFoundException as 404 Not Found.
     */
    @ExceptionHandler(ItemNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleItemNotFoundException(ex: ItemNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = "Not Found",
                    message = ex.message ?: "Item not found",
                ),
            )
    }

    /**
     * Handle LootBanActiveException as 409 Conflict.
     */
    @ExceptionHandler(LootBanActiveException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleLootBanActiveException(ex: LootBanActiveException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    status = HttpStatus.CONFLICT.value(),
                    error = "Conflict",
                    message = ex.message ?: "Raider has active loot bans",
                ),
            )
    }

    /**
     * Handle InvalidCredentialsException as 401 Unauthorized.
     */
    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    error = "Unauthorized",
                    message = ex.message ?: "Invalid credentials",
                ),
            )
    }

    /**
     * Handle NoLinkedRaiderException as 400 Bad Request.
     * This occurs when trying to access user-specific data but no raider is linked.
     */
    @ExceptionHandler(NoLinkedRaiderException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNoLinkedRaiderException(ex: NoLinkedRaiderException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = "Bad Request",
                    message = ex.message ?: "No character linked. Please link a character first.",
                ),
            )
    }

    /**
     * Handle GuildAccessDeniedException as 403 Forbidden.
     */
    @ExceptionHandler(GuildAccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleGuildAccessDeniedException(ex: GuildAccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    status = HttpStatus.FORBIDDEN.value(),
                    error = "Forbidden",
                    message = ex.message ?: "You do not have access to this guild",
                ),
            )
    }

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
    private val logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * Handle generic exceptions.
     * This catches Spring's parameter binding exceptions and other errors.
     */
    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        return if (isParameterBindingException(ex)) {
            logger.warn("Parameter binding exception: ${ex.message}", ex)
            createBadRequestResponse(ex.message)
        } else {
            logger.error("Unhandled exception occurred", ex)
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
            exceptionName.contains("BindException") ||
            ex is org.springframework.web.server.ServerWebInputException
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
