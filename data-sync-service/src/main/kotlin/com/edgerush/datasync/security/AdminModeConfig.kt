package com.edgerush.datasync.security

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "api.admin-mode")
data class AdminModeConfig(
    var enabled: Boolean = false,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun logWarning() {
        if (enabled) {
            logger.warn("⚠️  ═══════════════════════════════════════════════════════════")
            logger.warn("⚠️  ADMIN MODE ENABLED - Authentication bypassed for all requests!")
            logger.warn("⚠️  This should ONLY be used in development/testing environments")
            logger.warn("⚠️  ═══════════════════════════════════════════════════════════")
        }
    }

    fun isEnabled(): Boolean = enabled
}
