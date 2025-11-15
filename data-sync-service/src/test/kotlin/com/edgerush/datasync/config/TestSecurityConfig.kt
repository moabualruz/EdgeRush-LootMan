package com.edgerush.datasync.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Profile

/**
 * Test security configuration placeholder.
 *
 * Security is disabled in tests by excluding WebMvc security auto-configuration.
 * Admin mode is enabled in test profile, so no authentication is required.
 *
 * Only active in test profile.
 */
@TestConfiguration
@Profile("test")
class TestSecurityConfig {
    // No beans needed - security is disabled via exclusions
}
