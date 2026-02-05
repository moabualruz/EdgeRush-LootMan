package com.edgerush.datasync.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

/**
 * Configuration properties for HTTP clients (WebClient/RestTemplate).
 *
 * Provides configurable timeouts and retry behavior for external API calls.
 */
@ConfigurationProperties(prefix = "http-clients")
data class HttpClientsProperties(
    @DefaultValue("5000")
    val connectTimeoutMs: Int,
    @DefaultValue("10000")
    val readTimeoutMs: Int,
    @DefaultValue("10000")
    val writeTimeoutMs: Int,
    @DefaultValue("3")
    val maxRetries: Int,
    @DefaultValue("500")
    val retryBackoffMs: Long,
)
