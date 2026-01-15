package com.edgerush.lootman.api.raider

import com.edgerush.datasync.test.base.IntegrationTest
import io.kotest.matchers.collections.shouldBeEmpty
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
import java.time.LocalDateTime

/**
 * Integration tests for Raider REST API endpoints.
 *
 * Tests verify:
 * - Full request/response cycle through HTTP
 * - Database persistence and retrieval
 * - API contract compliance
 * - Error handling for invalid inputs
 * - Pagination functionality
 */
class RaiderControllerIntegrationTest : IntegrationTest() {
    private fun createRequest(
        id: Long = 1L,
        guildId: String = "test-guild",
        characterName: String = "Testchar",
        realm: String = "TestRealm",
        characterClass: String = "WARRIOR",
        role: String = "DPS",
        rank: String? = "Raider",
        status: String = "ACTIVE",
    ): HttpEntity<CreateRaiderRequest> {
        val request =
            CreateRaiderRequest(
                id = id,
                guildId = guildId,
                characterName = characterName,
                realm = realm,
                characterClass = characterClass,
                role = role,
                rank = rank,
                status = status,
                joinDate = LocalDateTime.now(),
            )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(request, headers)
    }

    private fun createUpdateRequest(
        characterName: String? = null,
        realm: String? = null,
        status: String? = null,
        rank: String? = null,
    ): HttpEntity<UpdateRaiderRequest> {
        val request =
            UpdateRaiderRequest(
                characterName = characterName,
                realm = realm,
                status = status,
                rank = rank,
            )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(request, headers)
    }

    @Nested
    inner class CreateRaider {
        @Test
        fun `should create raider and return 201 Created`() {
            // Given
            val entity = createRequest(id = 100L, characterName = "NewRaider")

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/raiders",
                    entity,
                    TestRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body shouldNotBe null
            response.body?.characterName shouldBe "NewRaider"
            response.body?.guildId shouldBe "test-guild"
            response.body?.characterClass shouldBe "WARRIOR"
            response.body?.role shouldBe "DPS"
            response.body?.status shouldBe "ACTIVE"
        }

        @Test
        fun `should persist raider to database after creation`() {
            // Given
            val entity = createRequest(id = 101L, guildId = "persist-guild", characterName = "PersistChar")

            // When
            restTemplate.postForEntity(
                "/api/v1/raiders",
                entity,
                TestRaiderResponse::class.java,
            )

            // Then - verify in database
            val raiderCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM raiders WHERE guild_id = ? AND character_name = ?",
                    Long::class.java,
                    "persist-guild",
                    "PersistChar",
                )
            raiderCount shouldBe 1L
        }

        @Test
        fun `should return raider with computed fields`() {
            // Given
            val entity = createRequest(id = 102L, characterName = "ComputedChar", realm = "ComputedRealm")

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/raiders",
                    entity,
                    TestRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.fullName shouldBe "ComputedChar-ComputedRealm"
            response.body?.isEligibleForLoot shouldBe true
        }

        @Test
        fun `should set isEligibleForLoot false for non-active raiders`() {
            // Given
            val entity = createRequest(id = 103L, characterName = "BenchedChar", status = "BENCHED")

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/raiders",
                    entity,
                    TestRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.isEligibleForLoot shouldBe false
        }
    }

    @Nested
    inner class GetRaider {
        @Test
        fun `should get raider by id and return 200 OK`() {
            // Given - create a raider first
            val createEntity = createRequest(id = 200L, characterName = "GetTestChar")
            restTemplate.postForEntity(
                "/api/v1/raiders",
                createEntity,
                TestRaiderResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/200",
                    TestRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.id shouldBe 200L
            response.body?.characterName shouldBe "GetTestChar"
        }

        @Test
        fun `should return 404 when raider not found`() {
            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/999999",
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class UpdateRaider {
        @Test
        fun `should update raider and return 200 OK`() {
            // Given - create a raider first
            val createEntity = createRequest(id = 300L, characterName = "UpdateChar", rank = "Raider")
            restTemplate.postForEntity(
                "/api/v1/raiders",
                createEntity,
                TestRaiderResponse::class.java,
            )

            val updateEntity = createUpdateRequest(rank = "Officer", status = "BENCHED")

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/raiders/300",
                    HttpMethod.PUT,
                    updateEntity,
                    TestRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.rank shouldBe "Officer"
            response.body?.status shouldBe "BENCHED"
        }

        @Test
        fun `should persist updated fields to database`() {
            // Given - create a raider first
            val createEntity = createRequest(id = 301L, guildId = "update-guild", characterName = "PersistUpdate")
            restTemplate.postForEntity(
                "/api/v1/raiders",
                createEntity,
                TestRaiderResponse::class.java,
            )

            val updateEntity = createUpdateRequest(rank = "Guild Master")

            // When
            restTemplate.exchange(
                "/api/v1/raiders/301",
                HttpMethod.PUT,
                updateEntity,
                TestRaiderResponse::class.java,
            )

            // Then - verify in database
            val rank =
                jdbcTemplate.queryForObject(
                    "SELECT rank FROM raiders WHERE id = ?",
                    String::class.java,
                    301L,
                )
            rank shouldBe "Guild Master"
        }

        @Test
        fun `should return 404 when updating non-existent raider`() {
            // Given
            val updateEntity = createUpdateRequest(status = "BENCHED")

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/raiders/999999",
                    HttpMethod.PUT,
                    updateEntity,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class DeleteRaider {
        @Test
        fun `should delete raider and return 204 No Content`() {
            // Given - create a raider first
            val createEntity = createRequest(id = 400L, characterName = "DeleteChar")
            restTemplate.postForEntity(
                "/api/v1/raiders",
                createEntity,
                TestRaiderResponse::class.java,
            )

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/raiders/400",
                    HttpMethod.DELETE,
                    null,
                    Void::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NO_CONTENT
        }

        @Test
        fun `should remove raider from database after deletion`() {
            // Given - create a raider first
            val createEntity = createRequest(id = 401L, guildId = "delete-guild", characterName = "RemoveChar")
            restTemplate.postForEntity(
                "/api/v1/raiders",
                createEntity,
                TestRaiderResponse::class.java,
            )

            // When
            restTemplate.exchange(
                "/api/v1/raiders/401",
                HttpMethod.DELETE,
                null,
                Void::class.java,
            )

            // Then - verify removed from database
            val raiderCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM raiders WHERE id = ?",
                    Long::class.java,
                    401L,
                )
            raiderCount shouldBe 0L
        }

        @Test
        fun `should return 404 when deleting non-existent raider`() {
            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/raiders/999999",
                    HttpMethod.DELETE,
                    null,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class GetRaidersByGuild {
        @Test
        fun `should return paginated list of raiders for guild`() {
            // Given - create multiple raiders for a guild
            val guildId = "paginated-guild"
            (1..5).forEach { i ->
                val entity = createRequest(id = 500L + i, guildId = guildId, characterName = "Raider$i")
                restTemplate.postForEntity(
                    "/api/v1/raiders",
                    entity,
                    TestRaiderResponse::class.java,
                )
            }

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/$guildId?page=0&size=3",
                    TestPagedRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            response.body?.content?.shouldHaveSize(3)
            response.body?.totalElements shouldBe 5
            response.body?.totalPages shouldBe 2
            response.body?.page shouldBe 0
            response.body?.size shouldBe 3
        }

        @Test
        fun `should return second page of raiders`() {
            // Given - create multiple raiders for a guild
            val guildId = "second-page-guild"
            (1..5).forEach { i ->
                val entity = createRequest(id = 600L + i, guildId = guildId, characterName = "PageRaider$i")
                restTemplate.postForEntity(
                    "/api/v1/raiders",
                    entity,
                    TestRaiderResponse::class.java,
                )
            }

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/$guildId?page=1&size=3",
                    TestPagedRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.content?.shouldHaveSize(2) // Remaining 2 on page 1
            response.body?.page shouldBe 1
        }

        @Test
        fun `should return empty page when guild has no raiders`() {
            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/empty-guild?page=0&size=20",
                    TestPagedRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.content?.shouldBeEmpty()
            response.body?.totalElements shouldBe 0
            response.body?.totalPages shouldBe 0
        }

        @Test
        fun `should use default page size when size not provided`() {
            // Given - create some raiders
            val guildId = "default-size-guild"
            (1..3).forEach { i ->
                val entity = createRequest(id = 700L + i, guildId = guildId, characterName = "DefaultRaider$i")
                restTemplate.postForEntity(
                    "/api/v1/raiders",
                    entity,
                    TestRaiderResponse::class.java,
                )
            }

            // When - no size parameter
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/$guildId?page=0",
                    TestPagedRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.size shouldBe 20 // default page size
        }

        @Test
        fun `should cap page size at maximum`() {
            // Given
            val guildId = "max-size-guild"
            val entity = createRequest(id = 800L, guildId = guildId, characterName = "MaxRaider")
            restTemplate.postForEntity(
                "/api/v1/raiders",
                entity,
                TestRaiderResponse::class.java,
            )

            // When - request size > max (100)
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/$guildId?page=0&size=500",
                    TestPagedRaiderResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.size shouldBe 100 // max page size
        }
    }

    @Nested
    inner class GetAllRaidersByGuild {
        @Test
        fun `should return all raiders for guild without pagination`() {
            // Given - create multiple raiders for a guild
            val guildId = "all-raiders-guild"
            (1..5).forEach { i ->
                val entity = createRequest(id = 900L + i, guildId = guildId, characterName = "AllRaider$i")
                restTemplate.postForEntity(
                    "/api/v1/raiders",
                    entity,
                    TestRaiderResponse::class.java,
                )
            }

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/$guildId/all",
                    TestRaiderListResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            response.body?.raiders?.shouldHaveSize(5)
            response.body?.count shouldBe 5
        }

        @Test
        fun `should return empty list when guild has no raiders`() {
            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/no-raiders-guild/all",
                    TestRaiderListResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.raiders?.shouldBeEmpty()
            response.body?.count shouldBe 0
        }

        @Test
        fun `should filter raiders by guild correctly`() {
            // Given - create raiders for different guilds
            val entity1 = createRequest(id = 1001L, guildId = "guild-A", characterName = "GuildARaider")
            val entity2 = createRequest(id = 1002L, guildId = "guild-B", characterName = "GuildBRaider")
            restTemplate.postForEntity("/api/v1/raiders", entity1, TestRaiderResponse::class.java)
            restTemplate.postForEntity("/api/v1/raiders", entity2, TestRaiderResponse::class.java)

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/guild-A/all",
                    TestRaiderListResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.raiders?.shouldHaveSize(1)
            response.body?.raiders?.first()?.characterName shouldBe "GuildARaider"
        }
    }

    @Nested
    inner class ApiContractVerification {
        @Test
        fun `should return correct JSON structure for raider response`() {
            // Given
            val entity = createRequest(id = 1100L, characterName = "JsonChar", realm = "JsonRealm")

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/raiders",
                    entity,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            val body = response.body!!
            body.contains("\"id\"") shouldBe true
            body.contains("\"guildId\"") shouldBe true
            body.contains("\"characterName\"") shouldBe true
            body.contains("\"realm\"") shouldBe true
            body.contains("\"characterClass\"") shouldBe true
            body.contains("\"role\"") shouldBe true
            body.contains("\"status\"") shouldBe true
            body.contains("\"fullName\"") shouldBe true
            body.contains("\"isEligibleForLoot\"") shouldBe true
        }

        @Test
        fun `should return correct JSON structure for paginated response`() {
            // Given
            val guildId = "json-paginated-guild"
            val entity = createRequest(id = 1200L, guildId = guildId, characterName = "JsonPagedChar")
            restTemplate.postForEntity("/api/v1/raiders", entity, TestRaiderResponse::class.java)

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/raiders/guild/$guildId?page=0&size=10",
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            val body = response.body!!
            body.contains("\"content\"") shouldBe true
            body.contains("\"totalElements\"") shouldBe true
            body.contains("\"totalPages\"") shouldBe true
            body.contains("\"page\"") shouldBe true
            body.contains("\"size\"") shouldBe true
        }
    }
}

// Response DTOs for test deserialization
data class TestRaiderResponse(
    val id: Long,
    val guildId: String,
    val characterName: String,
    val realm: String,
    val characterClass: String,
    val role: String,
    val rank: String?,
    val status: String,
    val joinDate: String?,
    val wowauditId: Long?,
    val fullName: String,
    val isEligibleForLoot: Boolean,
)

data class TestRaiderListResponse(
    val raiders: List<TestRaiderResponse>,
    val count: Int,
)

data class TestPagedRaiderResponse(
    val content: List<TestRaiderResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)
