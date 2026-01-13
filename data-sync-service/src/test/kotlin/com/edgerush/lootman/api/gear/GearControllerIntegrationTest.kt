package com.edgerush.lootman.api.gear

import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

/**
 * Integration tests for Gear REST API endpoints.
 *
 * Tests verify:
 * - Full request/response cycle through HTTP
 * - Database persistence and retrieval
 * - API contract compliance
 * - Error handling for invalid inputs
 * - Gear set calculations
 */
class GearControllerIntegrationTest : IntegrationTest() {

    private fun createRaider(raiderId: Long, guildId: String = "test-guild"): Long {
        // Insert a raider directly into the database for gear tests
        jdbcTemplate.update(
            """INSERT INTO raiders (id, guild_id, character_name, realm, character_class, role, status)
               VALUES (?, ?, ?, ?, ?, ?, ?)""",
            raiderId, guildId, "TestChar$raiderId", "TestRealm", "WARRIOR", "DPS", "ACTIVE"
        )
        return raiderId
    }

    private fun createGearRequest(
        gearSetType: String = "EQUIPPED",
        items: List<GearItemRequest> = listOf(
            GearItemRequest(
                itemId = 12345L,
                name = "Test Sword",
                itemLevel = 489,
                quality = "EPIC",
                slot = "MAIN_HAND",
                isTierPiece = false,
                enchant = "Burning Writ",
                sockets = 1
            ),
            GearItemRequest(
                itemId = 12346L,
                name = "Test Shield",
                itemLevel = 489,
                quality = "EPIC",
                slot = "OFF_HAND",
                isTierPiece = false,
                enchant = null,
                sockets = 0
            )
        )
    ): HttpEntity<SaveGearRequest> {
        val request = SaveGearRequest(
            gearSetType = gearSetType,
            items = items
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(request, headers)
    }

    @Nested
    inner class CreateGear {
        @Test
        fun `should create gear and return 201 Created`() {
            // Given
            val raiderId = createRaider(100L)
            val entity = createGearRequest()

            // When
            val response = restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                TestGearSetResponse::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body shouldNotBe null
            response.body?.gearSetType shouldBe "EQUIPPED"
            response.body?.items?.shouldHaveSize(2)
        }

        @Test
        fun `should persist gear to database after creation`() {
            // Given
            val raiderId = createRaider(101L)
            val entity = createGearRequest()

            // When
            restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                TestGearSetResponse::class.java
            )

            // Then - verify in database
            val gearCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM raider_gear_items WHERE raider_id = ?",
                Long::class.java,
                raiderId
            )
            gearCount shouldBe 2L
        }

        @Test
        fun `should calculate average item level correctly`() {
            // Given
            val raiderId = createRaider(102L)
            val items = listOf(
                GearItemRequest(1L, "Helm", 500, "EPIC", "HEAD", false, null, 0),
                GearItemRequest(2L, "Chest", 490, "EPIC", "CHEST", false, null, 0),
                GearItemRequest(3L, "Legs", 495, "EPIC", "LEGS", false, null, 0)
            )
            val entity = createGearRequest(items = items)

            // When
            val response = restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                TestGearSetResponse::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.averageItemLevel shouldBe 495.0  // (500 + 490 + 495) / 3
        }

        @Test
        fun `should handle gear with tier pieces`() {
            // Given
            val raiderId = createRaider(103L)
            val items = listOf(
                GearItemRequest(1L, "Tier Helm", 500, "EPIC", "HEAD", true, null, 0),
                GearItemRequest(2L, "Tier Chest", 500, "EPIC", "CHEST", true, null, 0),
                GearItemRequest(3L, "Tier Shoulders", 500, "EPIC", "SHOULDERS", true, null, 0),
                GearItemRequest(4L, "Tier Gloves", 500, "EPIC", "HANDS", true, null, 0)
            )
            val entity = createGearRequest(items = items)

            // When
            val response = restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                TestGearSetResponse::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.tierPieceCount shouldBe 4
            response.body?.has2PieceBonus shouldBe true
            response.body?.has4PieceBonus shouldBe true
        }
    }

    @Nested
    inner class GetCurrentGear {
        @Test
        fun `should get current gear for raider`() {
            // Given - create raider and gear
            val raiderId = createRaider(200L)
            val entity = createGearRequest()
            restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                TestGearSetResponse::class.java
            )

            // When
            val response = restTemplate.getForEntity(
                "/api/v1/gear/raider/$raiderId",
                TestGearSetResponse::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            response.body?.gearSetType shouldBe "EQUIPPED"
            response.body?.items?.shouldHaveSize(2)
        }

        @Test
        fun `should return 404 when raider has no gear`() {
            // Given
            val raiderId = createRaider(201L)

            // When
            val response = restTemplate.getForEntity(
                "/api/v1/gear/raider/$raiderId",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class GetGearByType {
        @Test
        fun `should get gear by type EQUIPPED`() {
            // Given - create raider and gear
            val raiderId = createRaider(300L)
            val entity = createGearRequest(gearSetType = "EQUIPPED")
            restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                TestGearSetResponse::class.java
            )

            // When
            val response = restTemplate.getForEntity(
                "/api/v1/gear/raider/$raiderId/type/EQUIPPED",
                TestGearSetResponse::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.gearSetType shouldBe "EQUIPPED"
        }

        @Test
        fun `should get gear by type BEST`() {
            // Given - create raider and BEST gear
            val raiderId = createRaider(301L)
            val entity = createGearRequest(gearSetType = "BEST")
            restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                TestGearSetResponse::class.java
            )

            // When
            val response = restTemplate.getForEntity(
                "/api/v1/gear/raider/$raiderId/type/BEST",
                TestGearSetResponse::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.gearSetType shouldBe "BEST"
        }

        @Test
        fun `should return 404 for non-existent gear type`() {
            // Given
            val raiderId = createRaider(302L)
            val entity = createGearRequest(gearSetType = "EQUIPPED")
            restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                TestGearSetResponse::class.java
            )

            // When
            val response = restTemplate.getForEntity(
                "/api/v1/gear/raider/$raiderId/type/BEST",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class UpdateGear {
        @Test
        fun `should update gear and return 200 OK`() {
            // Given - create raider and initial gear
            val raiderId = createRaider(400L)
            val createEntity = createGearRequest()
            restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                createEntity,
                TestGearSetResponse::class.java
            )

            // Update with different items
            val updateItems = listOf(
                GearItemRequest(99999L, "New Sword", 500, "LEGENDARY", "MAIN_HAND", false, "Best Enchant", 2)
            )
            val updateEntity = createGearRequest(items = updateItems)

            // When
            val response = restTemplate.exchange(
                "/api/v1/gear/raider/$raiderId",
                HttpMethod.PUT,
                updateEntity,
                TestGearSetResponse::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.items?.shouldHaveSize(1)
            response.body?.items?.first()?.itemId shouldBe 99999L
            response.body?.items?.first()?.itemLevel shouldBe 500
        }

        @Test
        fun `should persist updated gear to database`() {
            // Given - create raider and initial gear
            val raiderId = createRaider(401L)
            val createEntity = createGearRequest()
            restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                createEntity,
                TestGearSetResponse::class.java
            )

            // Update
            val updateItems = listOf(
                GearItemRequest(88888L, "Updated Weapon", 510, "LEGENDARY", "MAIN_HAND", true, null, 0)
            )
            val updateEntity = createGearRequest(items = updateItems)

            // When
            restTemplate.exchange(
                "/api/v1/gear/raider/$raiderId",
                HttpMethod.PUT,
                updateEntity,
                TestGearSetResponse::class.java
            )

            // Then - verify in database
            val itemId = jdbcTemplate.queryForObject(
                "SELECT item_id FROM raider_gear_items WHERE raider_id = ? LIMIT 1",
                Long::class.java,
                raiderId
            )
            itemId shouldBe 88888L
        }
    }

    @Nested
    inner class ApiContractVerification {
        @Test
        fun `should return correct JSON structure for gear response`() {
            // Given
            val raiderId = createRaider(500L)
            val entity = createGearRequest()

            // When
            val response = restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            val body = response.body!!
            body.contains("\"gearSetType\"") shouldBe true
            body.contains("\"items\"") shouldBe true
            body.contains("\"averageItemLevel\"") shouldBe true
            body.contains("\"tierPieceCount\"") shouldBe true
            body.contains("\"has2PieceBonus\"") shouldBe true
            body.contains("\"has4PieceBonus\"") shouldBe true
            body.contains("\"totalSlots\"") shouldBe true
        }

        @Test
        fun `should return correct JSON structure for gear items`() {
            // Given
            val raiderId = createRaider(501L)
            val entity = createGearRequest()

            // When
            val response = restTemplate.postForEntity(
                "/api/v1/gear/raider/$raiderId",
                entity,
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            val body = response.body!!
            body.contains("\"itemId\"") shouldBe true
            body.contains("\"name\"") shouldBe true
            body.contains("\"itemLevel\"") shouldBe true
            body.contains("\"quality\"") shouldBe true
            body.contains("\"slot\"") shouldBe true
            body.contains("\"isTierPiece\"") shouldBe true
            body.contains("\"enchant\"") shouldBe true
            body.contains("\"sockets\"") shouldBe true
        }
    }
}

// Response DTOs for test deserialization
data class TestGearSetResponse(
    val gearSetType: String,
    val items: List<TestGearItemResponse>,
    val averageItemLevel: Double,
    val tierPieceCount: Int,
    val has2PieceBonus: Boolean,
    val has4PieceBonus: Boolean,
    val totalSlots: Int
)

data class TestGearItemResponse(
    val itemId: Long,
    val name: String,
    val itemLevel: Int,
    val quality: String,
    val slot: String,
    val isTierPiece: Boolean,
    val enchant: String?,
    val sockets: Int
)
