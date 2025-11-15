package com.edgerush.lootman.api.loot

import com.edgerush.datasync.test.base.IntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.Instant

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
                LootHistoryResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertNotNull(response.body!!.awards)
    }

    @Test
    fun `should return 200 OK for raider loot history endpoint`() {
        // Given
        val raiderId = "test-raider-123"

        // When
        val response =
            restTemplate.getForEntity(
                "/api/v1/loot/raiders/$raiderId/history",
                LootHistoryResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertNotNull(response.body!!.awards)
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
                LootBansResponse::class.java,
            )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertNotNull(response.body!!.bans)
    }

    @Test
    fun `should award loot and return 201 Created`() {
        // Given
        val request =
            AwardLootRequest(
                itemId = 12345L,
                raiderId = "test-raider-456",
                guildId = "test-guild-789",
                flpsScore = 0.85,
                tier = "MYTHIC",
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/loot/awards",
                entity,
                LootAwardDto::class.java,
            )

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(request.itemId, response.body!!.itemId)
        assertEquals(request.raiderId, response.body!!.raiderId)
        assertEquals(request.guildId, response.body!!.guildId)
        assertEquals(request.flpsScore, response.body!!.flpsScore)
        assertTrue(response.body!!.isActive)
    }

    @Test
    fun `should create loot ban and return 201 Created`() {
        // Given
        val request =
            CreateLootBanRequest(
                raiderId = "test-raider-ban-123",
                guildId = "test-guild-ban-456",
                reason = "Repeated loot hoarding",
                expiresAt = Instant.now().plusSeconds(86400), // 1 day from now
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/loot/bans",
                entity,
                LootBanDto::class.java,
            )

        // Then
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(request.raiderId, response.body!!.raiderId)
        assertEquals(request.guildId, response.body!!.guildId)
        assertEquals(request.reason, response.body!!.reason)
        assertTrue(response.body!!.isActive)
    }

    @Test
    fun `should remove loot ban and return 204 No Content`() {
        // Given - First create a ban
        val createRequest =
            CreateLootBanRequest(
                raiderId = "test-raider-remove-123",
                guildId = "test-guild-remove-456",
                reason = "Test ban for removal",
                expiresAt = null, // permanent
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val createEntity = HttpEntity(createRequest, headers)

        val createResponse =
            restTemplate.postForEntity(
                "/api/v1/loot/bans",
                createEntity,
                LootBanDto::class.java,
            )

        val banId = createResponse.body!!.id

        // When - Remove the ban
        restTemplate.delete("/api/v1/loot/bans/$banId")

        // Then - Verify ban is removed by checking it's no longer active
        val checkResponse =
            restTemplate.getForEntity(
                "/api/v1/loot/raiders/${createRequest.raiderId}/bans?guildId=${createRequest.guildId}",
                LootBansResponse::class.java,
            )

        assertEquals(HttpStatus.OK, checkResponse.statusCode)
        assertTrue(checkResponse.body!!.bans.none { it.id == banId })
    }

    @Test
    fun `should return 400 Bad Request when awarding loot with invalid FLPS score`() {
        // Given
        val request =
            AwardLootRequest(
                itemId = 12345L,
                raiderId = "test-raider-456",
                guildId = "test-guild-789",
                flpsScore = 1.5, // Invalid - must be 0.0-1.0
                tier = "MYTHIC",
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/loot/awards",
                entity,
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun `should return 400 Bad Request when creating ban with blank reason`() {
        // Given
        val request =
            CreateLootBanRequest(
                raiderId = "test-raider-123",
                guildId = "test-guild-456",
                reason = "", // Invalid - blank reason
                expiresAt = null,
            )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(request, headers)

        // When
        val response =
            restTemplate.postForEntity(
                "/api/v1/loot/bans",
                entity,
                String::class.java,
            )

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}
