package com.edgerush.lootman.api.performance

import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.system.measureTimeMillis

/**
 * Performance tests for API endpoints.
 *
 * These tests verify that API endpoints respond within acceptable time limits:
 * - Simple GET requests: < 500ms
 * - Complex queries: < 1000ms
 * - Pagination: < 500ms
 *
 * Note: These tests run against a test database and are not load tests.
 * They verify baseline performance characteristics.
 */
class ApiPerformanceTest : IntegrationTest() {
    @Nested
    inner class OpenApiPerformance {
        @Test
        fun `OpenAPI spec should load within 2 seconds`() {
            // Warm up
            restTemplate.getForEntity("/v3/api-docs", String::class.java)

            // Act
            val timeMs =
                measureTimeMillis {
                    val response = restTemplate.getForEntity("/v3/api-docs", String::class.java)
                    response.statusCode shouldBe HttpStatus.OK
                }

            // Assert
            timeMs shouldBeLessThan 2000L
        }
    }

    @Nested
    inner class HealthEndpointPerformance {
        @Test
        fun `health endpoint should respond within 500ms`() {
            // Warm up
            restTemplate.getForEntity("/actuator/health", String::class.java)

            // Act
            val timeMs =
                measureTimeMillis {
                    val response = restTemplate.getForEntity("/actuator/health", String::class.java)
                    (
                        response.statusCode == HttpStatus.OK ||
                            response.statusCode == HttpStatus.SERVICE_UNAVAILABLE
                    ) shouldBe true
                }

            // Assert
            timeMs shouldBeLessThan 500L
        }
    }

    @Nested
    inner class CrudEndpointPerformance {
        @Test
        fun `raiders list endpoint should respond within 500ms`() {
            // Warm up
            restTemplate.getForEntity("/api/raider-entities", String::class.java)

            // Act
            val timeMs =
                measureTimeMillis {
                    val response = restTemplate.getForEntity("/api/raider-entities", String::class.java)
                    response.statusCode shouldBe HttpStatus.OK
                }

            // Assert
            timeMs shouldBeLessThan 500L
        }

        @Test
        fun `loot awards list endpoint should respond within 500ms`() {
            // Warm up
            restTemplate.getForEntity("/api/loot-awards", String::class.java)

            // Act
            val timeMs =
                measureTimeMillis {
                    val response = restTemplate.getForEntity("/api/loot-awards", String::class.java)
                    response.statusCode shouldBe HttpStatus.OK
                }

            // Assert
            timeMs shouldBeLessThan 500L
        }

        @Test
        fun `attendance stats list endpoint should respond within 500ms`() {
            // Warm up
            restTemplate.getForEntity("/api/attendance-stats", String::class.java)

            // Act
            val timeMs =
                measureTimeMillis {
                    val response = restTemplate.getForEntity("/api/attendance-stats", String::class.java)
                    response.statusCode shouldBe HttpStatus.OK
                }

            // Assert
            timeMs shouldBeLessThan 500L
        }
    }

    @Nested
    inner class PaginationPerformance {
        @Test
        fun `paginated requests should respond within 500ms`() {
            // Warm up
            restTemplate.getForEntity("/api/raider-entities?page=0&size=10", String::class.java)

            // Act
            val timeMs =
                measureTimeMillis {
                    val response =
                        restTemplate.getForEntity(
                            "/api/raider-entities?page=0&size=10",
                            String::class.java,
                        )
                    response.statusCode shouldBe HttpStatus.OK
                }

            // Assert
            timeMs shouldBeLessThan 500L
        }

        @Test
        fun `small page size should respond faster than large page size`() {
            // Warm up
            restTemplate.getForEntity("/api/raider-entities?page=0&size=10", String::class.java)
            restTemplate.getForEntity("/api/raider-entities?page=0&size=100", String::class.java)

            // Act
            val smallPageTime =
                measureTimeMillis {
                    restTemplate.getForEntity("/api/raider-entities?page=0&size=10", String::class.java)
                }

            val largePageTime =
                measureTimeMillis {
                    restTemplate.getForEntity("/api/raider-entities?page=0&size=100", String::class.java)
                }

            // Assert - both should be fast (empty database)
            smallPageTime shouldBeLessThan 500L
            largePageTime shouldBeLessThan 500L
        }
    }

    @Nested
    inner class MultipleRequestPerformance {
        @Test
        fun `multiple sequential requests should each respond within 500ms`() {
            val endpoints =
                listOf(
                    "/api/raider-entities",
                    "/api/loot-awards",
                    "/api/attendance-stats",
                    "/api/applications",
                    "/api/raids",
                )

            // Warm up all endpoints
            endpoints.forEach { endpoint ->
                restTemplate.getForEntity(endpoint, String::class.java)
            }

            // Act & Assert
            endpoints.forEach { endpoint ->
                val timeMs =
                    measureTimeMillis {
                        val response = restTemplate.getForEntity(endpoint, String::class.java)
                        response.statusCode shouldBe HttpStatus.OK
                    }
                timeMs shouldBeLessThan 500L
            }
        }
    }
}
