package com.edgerush.lootman.api.wishlist

import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

/**
 * Integration tests for Wishlist REST API endpoints.
 *
 * Tests verify:
 * - Full request/response cycle through HTTP
 * - Database persistence and retrieval
 * - API contract compliance
 * - Error handling for invalid inputs
 * - Wishlist item priority handling
 */
class WishlistControllerIntegrationTest : IntegrationTest() {
    private fun createRaider(
        raiderId: Long,
        guildId: String = "test-guild",
    ): Long {
        // Insert a raider directly into the database for wishlist tests
        jdbcTemplate.update(
            """INSERT INTO raiders (id, guild_id, character_name, realm, character_class, role, status)
               VALUES (?, ?, ?, ?, ?, ?, ?)""",
            raiderId,
            guildId,
            "TestChar$raiderId",
            "TestRealm",
            "WARRIOR",
            "DPS",
            "ACTIVE",
        )
        return raiderId
    }

    private fun createWishlistRequest(
        raiderId: Long,
        items: List<WishlistItemRequest> =
            listOf(
                WishlistItemRequest(
                    itemId = 12345L,
                    itemName = "Awesome Sword",
                    priority = 1,
                    upgradePercentage = 15.5,
                    specName = "Fury",
                ),
                WishlistItemRequest(
                    itemId = 12346L,
                    itemName = "Great Shield",
                    priority = 2,
                    upgradePercentage = 10.0,
                    specName = "Protection",
                ),
            ),
    ): HttpEntity<SaveWishlistRequest> {
        val request =
            SaveWishlistRequest(
                raiderId = raiderId,
                items = items,
            )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(request, headers)
    }

    @Nested
    inner class CreateWishlist {
        @Test
        fun `should create wishlist and return 201 Created`() {
            // Given
            val raiderId = createRaider(100L)
            val entity = createWishlistRequest(raiderId)

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/wishlists",
                    entity,
                    TestWishlistResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body shouldNotBe null
            response.body?.raiderId shouldBe raiderId
            response.body?.items?.shouldHaveSize(2)
            response.body?.itemCount shouldBe 2
        }

        @Test
        fun `should persist wishlist to database after creation`() {
            // Given
            val raiderId = createRaider(101L)
            val entity = createWishlistRequest(raiderId)

            // When
            restTemplate.postForEntity(
                "/api/v1/wishlists",
                entity,
                TestWishlistResponse::class.java,
            )

            // Then - verify in database
            val wishlistCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM wishlist_items WHERE raider_id = ?",
                    Long::class.java,
                    raiderId,
                )
            wishlistCount shouldBe 2L
        }

        @Test
        fun `should return top priority item`() {
            // Given
            val raiderId = createRaider(102L)
            val items =
                listOf(
                    WishlistItemRequest(1L, "Second Priority", 2, 10.0, null),
                    WishlistItemRequest(2L, "First Priority", 1, 20.0, null),
                    WishlistItemRequest(3L, "Third Priority", 3, 5.0, null),
                )
            val entity = createWishlistRequest(raiderId, items)

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/wishlists",
                    entity,
                    TestWishlistResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.topPriorityItem shouldNotBe null
            response.body?.topPriorityItem?.itemName shouldBe "First Priority"
            response.body?.topPriorityItem?.priority shouldBe 1
        }

        @Test
        fun `should handle wishlist with single item`() {
            // Given
            val raiderId = createRaider(103L)
            val items =
                listOf(
                    WishlistItemRequest(1L, "Only Item", 1, 25.0, "Arms"),
                )
            val entity = createWishlistRequest(raiderId, items)

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/wishlists",
                    entity,
                    TestWishlistResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.items?.shouldHaveSize(1)
            response.body?.topPriorityItem?.itemName shouldBe "Only Item"
        }
    }

    @Nested
    inner class GetWishlist {
        @Test
        fun `should get wishlist for raider`() {
            // Given - create raider and wishlist
            val raiderId = createRaider(200L)
            val entity = createWishlistRequest(raiderId)
            restTemplate.postForEntity(
                "/api/v1/wishlists",
                entity,
                TestWishlistResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/wishlists/raider/$raiderId",
                    TestWishlistResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            response.body?.raiderId shouldBe raiderId
            response.body?.items?.shouldHaveSize(2)
        }

        @Test
        fun `should return 404 when wishlist not found`() {
            // Given
            val raiderId = createRaider(201L)

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/wishlists/raider/$raiderId",
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        @Test
        fun `should preserve item priority order`() {
            // Given
            val raiderId = createRaider(202L)
            val items =
                listOf(
                    WishlistItemRequest(1L, "Priority 3", 3, 5.0, null),
                    WishlistItemRequest(2L, "Priority 1", 1, 25.0, null),
                    WishlistItemRequest(3L, "Priority 2", 2, 15.0, null),
                )
            val entity = createWishlistRequest(raiderId, items)
            restTemplate.postForEntity(
                "/api/v1/wishlists",
                entity,
                TestWishlistResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/wishlists/raider/$raiderId",
                    TestWishlistResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.topPriorityItem?.priority shouldBe 1
            response.body?.topPriorityItem?.itemName shouldBe "Priority 1"
        }
    }

    @Nested
    inner class UpdateWishlist {
        @Test
        fun `should update wishlist and return 200 OK`() {
            // Given - create raider and initial wishlist
            val raiderId = createRaider(300L)
            val createEntity = createWishlistRequest(raiderId)
            restTemplate.postForEntity(
                "/api/v1/wishlists",
                createEntity,
                TestWishlistResponse::class.java,
            )

            // Update with different items
            val updateItems =
                listOf(
                    WishlistItemRequest(99999L, "New Top Item", 1, 50.0, "Fury"),
                )
            val updateEntity = createWishlistRequest(raiderId, updateItems)

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/wishlists/raider/$raiderId",
                    HttpMethod.PUT,
                    updateEntity,
                    TestWishlistResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.items?.shouldHaveSize(1)
            response.body?.items?.first()?.itemId shouldBe 99999L
            response.body?.items?.first()?.upgradePercentage shouldBe 50.0
        }

        @Test
        fun `should replace existing wishlist items on update`() {
            // Given - create raider and initial wishlist with 2 items
            val raiderId = createRaider(301L)
            val createEntity = createWishlistRequest(raiderId)
            restTemplate.postForEntity(
                "/api/v1/wishlists",
                createEntity,
                TestWishlistResponse::class.java,
            )

            // Update with 3 items
            val updateItems =
                listOf(
                    WishlistItemRequest(1L, "Item 1", 1, 30.0, null),
                    WishlistItemRequest(2L, "Item 2", 2, 20.0, null),
                    WishlistItemRequest(3L, "Item 3", 3, 10.0, null),
                )
            val updateEntity = createWishlistRequest(raiderId, updateItems)

            // When
            restTemplate.exchange(
                "/api/v1/wishlists/raider/$raiderId",
                HttpMethod.PUT,
                updateEntity,
                TestWishlistResponse::class.java,
            )

            // Then - verify in database only 3 items exist
            val wishlistCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM wishlist_items WHERE raider_id = ?",
                    Long::class.java,
                    raiderId,
                )
            wishlistCount shouldBe 3L
        }
    }

    @Nested
    inner class DeleteWishlist {
        @Test
        fun `should delete wishlist and return 204 No Content`() {
            // Given - create raider and wishlist
            val raiderId = createRaider(400L)
            val entity = createWishlistRequest(raiderId)
            restTemplate.postForEntity(
                "/api/v1/wishlists",
                entity,
                TestWishlistResponse::class.java,
            )

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/wishlists/raider/$raiderId",
                    HttpMethod.DELETE,
                    null,
                    Void::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NO_CONTENT
        }

        @Test
        fun `should remove wishlist from database after deletion`() {
            // Given - create raider and wishlist
            val raiderId = createRaider(401L)
            val entity = createWishlistRequest(raiderId)
            restTemplate.postForEntity(
                "/api/v1/wishlists",
                entity,
                TestWishlistResponse::class.java,
            )

            // When
            restTemplate.exchange(
                "/api/v1/wishlists/raider/$raiderId",
                HttpMethod.DELETE,
                null,
                Void::class.java,
            )

            // Then - verify removed from database
            val wishlistCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM wishlist_items WHERE raider_id = ?",
                    Long::class.java,
                    raiderId,
                )
            wishlistCount shouldBe 0L
        }

        @Test
        fun `should return 404 when deleting non-existent wishlist`() {
            // Given
            val raiderId = createRaider(402L)

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/wishlists/raider/$raiderId",
                    HttpMethod.DELETE,
                    null,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class ApiContractVerification {
        @Test
        fun `should return correct JSON structure for wishlist response`() {
            // Given
            val raiderId = createRaider(500L)
            val entity = createWishlistRequest(raiderId)

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/wishlists",
                    entity,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            val body = response.body!!
            body.contains("\"raiderId\"") shouldBe true
            body.contains("\"items\"") shouldBe true
            body.contains("\"itemCount\"") shouldBe true
            body.contains("\"topPriorityItem\"") shouldBe true
        }

        @Test
        fun `should return correct JSON structure for wishlist items`() {
            // Given
            val raiderId = createRaider(501L)
            val entity = createWishlistRequest(raiderId)

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/wishlists",
                    entity,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            val body = response.body!!
            body.contains("\"itemId\"") shouldBe true
            body.contains("\"itemName\"") shouldBe true
            body.contains("\"priority\"") shouldBe true
            body.contains("\"upgradePercentage\"") shouldBe true
            body.contains("\"normalizedUpgradeValue\"") shouldBe true
        }
    }
}

// Response DTOs for test deserialization
data class TestWishlistResponse(
    val raiderId: Long,
    val items: List<TestWishlistItemResponse>,
    val itemCount: Int,
    val topPriorityItem: TestWishlistItemResponse?,
)

data class TestWishlistItemResponse(
    val itemId: Long,
    val itemName: String,
    val priority: Int,
    val upgradePercentage: Double,
    val normalizedUpgradeValue: Double,
    val specName: String?,
)
