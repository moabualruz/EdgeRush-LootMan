package com.edgerush.lootman.api.flps

import com.edgerush.datasync.test.base.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Integration tests for FLPS API endpoints.
 *
 * Tests verify:
 * - Endpoint availability and response structure
 * - Backward compatibility with existing API
 * - Error handling and validation
 * - Integration with new use cases
 */
class FlpsControllerIntegrationTest : IntegrationTest() {
    @Test
    fun `should return comprehensive FLPS report for guild`() {
        // Given
        val guildId = "test-guild-123"

        // When
        val response =
            restTemplate.getForEntity(
                "/api/flps/$guildId",
                Array<ComprehensiveFlpsReportDto>::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should return perfect score benchmarks for guild`() {
        // Given
        val guildId = "test-guild-123"

        // When
        val response =
            restTemplate.getForEntity(
                "/api/flps/$guildId/benchmarks",
                PerfectScoreBenchmarkDto::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should return system status`() {
        // When
        val response =
            restTemplate.getForEntity(
                "/api/flps/status",
                FlpsDataStatusDto::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertNotNull(response.body!!.message)
        assertNotNull(response.body!!.features)
        assertNotNull(response.body!!.endpoints)
    }

    @Test
    fun `should maintain backward compatibility with existing response format`() {
        // Given
        val guildId = "test-guild-123"

        // When
        val response =
            restTemplate.getForEntity(
                "/api/flps/$guildId",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        // Verify response contains expected fields for backward compatibility
        val body = response.body!!
        assert(body.contains("\"raiderId\"") || body.contains("[]"))
        assert(body.contains("\"flpsScore\"") || body.contains("[]"))
    }
}

// DTOs for testing
data class ComprehensiveFlpsReportDto(
    val raiderId: String,
    val raiderName: String,
    val flpsScore: Double,
    val eligible: Boolean,
)

data class PerfectScoreBenchmarkDto(
    val theoretical: Double,
    val topPerformer: Double,
)

data class FlpsDataStatusDto(
    val message: String,
    val features: List<String>,
    val endpoints: Map<String, String>,
)
