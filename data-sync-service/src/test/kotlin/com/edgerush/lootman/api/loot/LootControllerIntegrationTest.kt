package com.edgerush.lootman.api.loot

import com.edgerush.datasync.test.base.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

/**
 * Integration tests for Loot API endpoints.
 */
class LootControllerIntegrationTest : IntegrationTest() {
    @Test
    fun `should return 200 OK for loot history endpoint`() {
        // Given
        val guildId = "test-guild-123"

        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/loot/guilds/$guildId/history",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should return 200 OK for raider loot history endpoint`() {
        // Given
        val raiderId = "test-raider-123"

        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/loot/raiders/$raiderId/history",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `should return 200 OK for active bans endpoint`() {
        // Given
        val raiderId = "test-raider-123"
        val guildId = "test-guild-123"

        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/loot/raiders/$raiderId/bans?guildId=$guildId",
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
    }
}

