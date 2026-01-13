package com.edgerush.lootman.infrastructure.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * Health indicator that checks WarcraftLogs API availability.
 *
 * In production, this would make an actual API call to verify connectivity.
 * For testing purposes, the availability can be injected.
 */
@Component
class WarcraftLogsHealthIndicator(
    private val isApiAvailable: Boolean = true
) : HealthIndicator {

    override fun health(): Health {
        return if (isApiAvailable) {
            Health.up()
                .withDetail("service", "WarcraftLogs API")
                .build()
        } else {
            Health.down()
                .withDetail("service", "WarcraftLogs API")
                .withDetail("error", "API not reachable")
                .build()
        }
    }
}
