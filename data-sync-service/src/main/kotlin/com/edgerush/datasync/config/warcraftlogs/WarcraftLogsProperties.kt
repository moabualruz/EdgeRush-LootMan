package com.edgerush.datasync.config.warcraftlogs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "warcraft-logs")
data class WarcraftLogsProperties(
    val enabled: Boolean = false,
    val clientId: String = "",
    val clientSecret: String = "",
    val baseUrl: String = "https://www.warcraftlogs.com/api/v2",
    val tokenUrl: String = "https://www.warcraftlogs.com/oauth/token",
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000,
    val maxConcurrentRequests: Int = 5,
    val requestTimeoutSeconds: Long = 30,
) {
    init {
        // Only validate credentials if Warcraft Logs integration is enabled
        if (enabled) {
            require(clientId.isNotBlank()) { "Warcraft Logs client ID is required when enabled=true" }
            require(clientSecret.isNotBlank()) { "Warcraft Logs client secret is required when enabled=true" }
            require(maxRetries >= 1) { "Max retries must be at least 1" }
            require(retryDelayMs >= MIN_RETRY_DELAY_MS) { "Retry delay must be at least ${MIN_RETRY_DELAY_MS}ms" }
            require(maxConcurrentRequests >= 1) { "Max concurrent requests must be at least 1" }
            require(requestTimeoutSeconds >= 1) { "Request timeout must be at least 1 second" }
        }
    }

    companion object {
        private const val MIN_RETRY_DELAY_MS = 100L
    }
}
