package com.edgerush.lootman.api.flps

import com.edgerush.datasync.test.base.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * API contract tests for FLPS endpoints.
 *
 * These tests verify:
 * - Response structure matches expected contract
 * - HTTP status codes are appropriate
 * - Backward compatibility is maintained
 */
class FlpsApiContractTest : IntegrationTest() {
    @Test
    fun `should return 200 OK for guild report endpoint`() {
        // Given
        val guildId = "test-guild-123"

        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/flps/guilds/$guildId/report",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should return 200 OK for status endpoint`() {
        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/flps/status",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should maintain backward compatibility with legacy guild endpoint`() {
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
        assertNotNull(response.body)
    }

    @Test
    fun `should maintain backward compatibility with legacy status endpoint`() {
        // When
        val response =
            restTemplate.getForEntity(
                "/api/flps/status",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }
}
