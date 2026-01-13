package com.edgerush.lootman.infrastructure.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

/**
 * Health indicator that checks database connectivity.
 */
@Component
class DatabaseHealthIndicator(
    private val jdbcTemplate: JdbcTemplate
) : HealthIndicator {

    override fun health(): Health {
        return try {
            jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
            Health.up()
                .withDetail("database", "PostgreSQL")
                .build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.message ?: "Unknown error")
                .build()
        }
    }
}
