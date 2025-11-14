package com.edgerush.datasync.client.warcraftlogs

/**
 * Base exception for all Warcraft Logs client errors
 */
sealed class WarcraftLogsException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)

/**
 * Authentication failed with Warcraft Logs API
 */
class WarcraftLogsAuthenticationException(message: String, cause: Throwable? = null) : 
    WarcraftLogsException(message, cause)

/**
 * Rate limit exceeded
 */
class WarcraftLogsRateLimitException(
    message: String,
    val retryAfterSeconds: Long? = null
) : WarcraftLogsException(message)

/**
 * API returned an error response
 */
class WarcraftLogsApiException(
    message: String,
    val statusCode: Int,
    cause: Throwable? = null
) : WarcraftLogsException(message, cause)

/**
 * Data parsing or validation error
 */
class WarcraftLogsDataException(message: String, cause: Throwable? = null) : 
    WarcraftLogsException(message, cause)
