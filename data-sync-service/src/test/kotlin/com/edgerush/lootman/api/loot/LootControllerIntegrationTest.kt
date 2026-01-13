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

    @Test
    fun `should get specific loot award by ID and return 200 OK`() {
        // Given - First create an award
        val createRequest = AwardLootRequest(
            itemId = 99001L,
            raiderId = "test-raider-get-award",
            guildId = "test-guild-get-award",
            flpsScore = 0.75,
            tier = "HEROIC"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.postForEntity(
            "/api/v1/loot/awards",
            createEntity,
            LootAwardDto::class.java
        )

        val awardId = createResponse.body!!.id

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/loot/awards/$awardId",
            LootAwardDto::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(awardId, response.body!!.id)
        assertEquals(createRequest.itemId, response.body!!.itemId)
        assertEquals(createRequest.raiderId, response.body!!.raiderId)
    }

    @Test
    fun `should return 404 Not Found when getting non-existent loot award`() {
        // Given
        val nonExistentId = "non-existent-award-id"

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/loot/awards/$nonExistentId",
            String::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should revoke loot award and return 204 No Content`() {
        // Given - First create an award
        val createRequest = AwardLootRequest(
            itemId = 99002L,
            raiderId = "test-raider-revoke-award",
            guildId = "test-guild-revoke-award",
            flpsScore = 0.80,
            tier = "MYTHIC"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.postForEntity(
            "/api/v1/loot/awards",
            createEntity,
            LootAwardDto::class.java
        )

        val awardId = createResponse.body!!.id

        // When
        val response = restTemplate.exchange(
            "/api/v1/loot/awards/$awardId",
            org.springframework.http.HttpMethod.DELETE,
            null,
            Void::class.java
        )

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        // Verify the award is actually revoked
        val getResponse = restTemplate.getForEntity(
            "/api/v1/loot/awards/$awardId",
            String::class.java
        )
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }

    @Test
    fun `should return 404 Not Found when revoking non-existent loot award`() {
        // Given
        val nonExistentId = "non-existent-revoke-id"

        // When
        val response = restTemplate.exchange(
            "/api/v1/loot/awards/$nonExistentId",
            org.springframework.http.HttpMethod.DELETE,
            null,
            String::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should list all loot awards for guild and return 200 OK`() {
        // Given - First create some awards
        val guildId = "test-guild-list-all"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val request1 = AwardLootRequest(
            itemId = 99003L,
            raiderId = "test-raider-list-1",
            guildId = guildId,
            flpsScore = 0.70,
            tier = "NORMAL"
        )
        restTemplate.postForEntity(
            "/api/v1/loot/awards",
            HttpEntity(request1, headers),
            LootAwardDto::class.java
        )

        val request2 = AwardLootRequest(
            itemId = 99004L,
            raiderId = "test-raider-list-2",
            guildId = guildId,
            flpsScore = 0.90,
            tier = "MYTHIC"
        )
        restTemplate.postForEntity(
            "/api/v1/loot/awards",
            HttpEntity(request2, headers),
            LootAwardDto::class.java
        )

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/loot/awards/all?guildId=$guildId",
            LootAwardsListResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body!!.totalCount >= 2)
        assertTrue(response.body!!.awards.isNotEmpty())
    }

    @Test
    fun `should list paginated loot awards for guild and return 200 OK`() {
        // Given - First create some awards
        val guildId = "test-guild-paginated"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        for (i in 1..5) {
            val request = AwardLootRequest(
                itemId = (99100 + i).toLong(),
                raiderId = "test-raider-page-$i",
                guildId = guildId,
                flpsScore = 0.50 + (i * 0.05),
                tier = "HEROIC"
            )
            restTemplate.postForEntity(
                "/api/v1/loot/awards",
                HttpEntity(request, headers),
                LootAwardDto::class.java
            )
        }

        // When - Get first page
        val response = restTemplate.getForEntity(
            "/api/v1/loot/awards?guildId=$guildId&page=0&size=3",
            String::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        // Verify it's a paginated response (contains page info)
        assertTrue(response.body!!.contains("content"))
        assertTrue(response.body!!.contains("totalElements"))
    }

    @Test
    fun `should get specific loot ban by ID and return 200 OK`() {
        // Given - First create a ban
        val createRequest = CreateLootBanRequest(
            raiderId = "test-raider-get-ban",
            guildId = "test-guild-get-ban",
            reason = "Test ban for retrieval",
            expiresAt = Instant.now().plusSeconds(86400 * 7)
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.postForEntity(
            "/api/v1/loot/bans",
            createEntity,
            LootBanDto::class.java
        )

        val banId = createResponse.body!!.id

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/loot/bans/$banId",
            LootBanDto::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(banId, response.body!!.id)
        assertEquals(createRequest.raiderId, response.body!!.raiderId)
        assertEquals(createRequest.reason, response.body!!.reason)
    }

    @Test
    fun `should return 404 Not Found when getting non-existent loot ban`() {
        // Given
        val nonExistentId = "non-existent-ban-id"

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/loot/bans/$nonExistentId",
            String::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should update loot ban and return 200 OK`() {
        // Given - First create a ban
        val createRequest = CreateLootBanRequest(
            raiderId = "test-raider-update-ban",
            guildId = "test-guild-update-ban",
            reason = "Original reason",
            expiresAt = Instant.now().plusSeconds(86400)
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.postForEntity(
            "/api/v1/loot/bans",
            createEntity,
            LootBanDto::class.java
        )

        val banId = createResponse.body!!.id

        // When - Update the ban
        val newExpiry = Instant.now().plusSeconds(86400 * 14)
        val updateRequest = UpdateLootBanRequest(
            reason = "Updated reason",
            expiresAt = newExpiry
        )

        val updateEntity = HttpEntity(updateRequest, headers)
        val response = restTemplate.exchange(
            "/api/v1/loot/bans/$banId",
            org.springframework.http.HttpMethod.PUT,
            updateEntity,
            LootBanDto::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(banId, response.body!!.id)
        assertEquals("Updated reason", response.body!!.reason)
    }

    @Test
    fun `should return 404 Not Found when updating non-existent loot ban`() {
        // Given
        val nonExistentId = "non-existent-update-ban-id"
        val updateRequest = UpdateLootBanRequest(
            reason = "New reason"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val updateEntity = HttpEntity(updateRequest, headers)

        // When
        val response = restTemplate.exchange(
            "/api/v1/loot/bans/$nonExistentId",
            org.springframework.http.HttpMethod.PUT,
            updateEntity,
            String::class.java
        )

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `should return empty list for guild with no loot awards`() {
        // Given
        val guildId = "empty-guild-no-awards"

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/loot/awards/all?guildId=$guildId",
            LootAwardsListResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(0, response.body!!.totalCount)
        assertTrue(response.body!!.awards.isEmpty())
    }

    @Test
    fun `should return empty history for raider with no loot`() {
        // Given
        val raiderId = "empty-raider-no-loot"

        // When
        val response = restTemplate.getForEntity(
            "/api/v1/loot/raiders/$raiderId/history",
            LootHistoryResponse::class.java
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertTrue(response.body!!.awards.isEmpty())
    }
}
