package com.edgerush.datasync.config.raidbots

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

@ConfigurationProperties(prefix = "raidbots")
@Validated
data class RaidbotsProperties(
    val enabled: Boolean = false,
    val apiKey: String = "", // API key if available (currently not publicly available)
    
    // Raidbots doesn't have a public API
    // Raiders submit Droptimizer report URLs manually
    val baseUrl: String = "https://www.raidbots.com",
    
    @field:Min(1, message = "Max retries must be at least 1")
    val maxRetries: Int = 3,
    
    @field:Min(100, message = "Retry delay must be at least 100ms")
    val retryDelayMs: Long = 2000
)
