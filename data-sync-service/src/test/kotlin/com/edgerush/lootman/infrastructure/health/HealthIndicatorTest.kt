package com.edgerush.lootman.infrastructure.health

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Unit tests for custom health indicators.
 */
class HealthIndicatorTest : UnitTest() {

    @Nested
    inner class DatabaseHealthIndicatorTests {

        @Test
        fun `should return UP when database is accessible`() {
            // Given
            val jdbcTemplate = mockk<JdbcTemplate>()
            every { jdbcTemplate.queryForObject("SELECT 1", Int::class.java) } returns 1
            val indicator = DatabaseHealthIndicator(jdbcTemplate)

            // When
            val health = indicator.health()

            // Then
            health.status shouldBe Status.UP
            health.details["database"] shouldBe "PostgreSQL"
        }

        @Test
        fun `should return DOWN when database is not accessible`() {
            // Given
            val jdbcTemplate = mockk<JdbcTemplate>()
            every { jdbcTemplate.queryForObject("SELECT 1", Int::class.java) } throws RuntimeException("Connection failed")
            val indicator = DatabaseHealthIndicator(jdbcTemplate)

            // When
            val health = indicator.health()

            // Then
            health.status shouldBe Status.DOWN
            health.details["error"] shouldBe "Connection failed"
        }
    }

    @Nested
    inner class WarcraftLogsHealthIndicatorTests {

        @Test
        fun `should return UP when WarcraftLogs API is available`() {
            // Given
            val indicator = WarcraftLogsHealthIndicator(isApiAvailable = true)

            // When
            val health = indicator.health()

            // Then
            health.status shouldBe Status.UP
            health.details["service"] shouldBe "WarcraftLogs API"
        }

        @Test
        fun `should return DOWN when WarcraftLogs API is not available`() {
            // Given
            val indicator = WarcraftLogsHealthIndicator(isApiAvailable = false)

            // When
            val health = indicator.health()

            // Then
            health.status shouldBe Status.DOWN
            health.details["service"] shouldBe "WarcraftLogs API"
            health.details["error"] shouldBe "API not reachable"
        }

        @Test
        fun `should use default constructor with API available`() {
            // Given - using default constructor
            val indicator = WarcraftLogsHealthIndicator()

            // When
            val health = indicator.health()

            // Then - default is API available = true
            health.status shouldBe Status.UP
        }
    }

    @Nested
    inner class WoWAuditHealthIndicatorTests {

        @Test
        fun `should return UP when WoWAudit API is available`() {
            // Given
            val indicator = WoWAuditHealthIndicator(isApiAvailable = true)

            // When
            val health = indicator.health()

            // Then
            health.status shouldBe Status.UP
            health.details["service"] shouldBe "WoWAudit API"
        }

        @Test
        fun `should return DOWN when WoWAudit API is not available`() {
            // Given
            val indicator = WoWAuditHealthIndicator(isApiAvailable = false)

            // When
            val health = indicator.health()

            // Then
            health.status shouldBe Status.DOWN
            health.details["service"] shouldBe "WoWAudit API"
            health.details["error"] shouldBe "API not reachable"
        }

        @Test
        fun `should use default constructor with API available`() {
            // Given - using default constructor
            val indicator = WoWAuditHealthIndicator()

            // When
            val health = indicator.health()

            // Then - default is API available = true
            health.status shouldBe Status.UP
        }
    }

    @Nested
    inner class DatabaseHealthIndicatorErrorHandlingTests {

        @Test
        fun `should handle exception with null message`() {
            // Given
            val jdbcTemplate = mockk<JdbcTemplate>()
            every { jdbcTemplate.queryForObject("SELECT 1", Int::class.java) } throws RuntimeException(null as String?)
            val indicator = DatabaseHealthIndicator(jdbcTemplate)

            // When
            val health = indicator.health()

            // Then
            health.status shouldBe Status.DOWN
            health.details["error"] shouldBe "Unknown error"
        }
    }
}
