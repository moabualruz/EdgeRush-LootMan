package com.edgerush.lootman.api.guild

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

/**
 * Integration tests for Guild REST API endpoints.
 *
 * Tests verify:
 * - Full request/response cycle through HTTP
 * - Database persistence and retrieval
 * - API contract compliance
 * - Error handling for invalid inputs
 * - Guild settings management
 */
class GuildControllerIntegrationTest : IntegrationTest() {
    private fun createRequest(
        id: String = "test-guild",
        name: String = "Test Guild",
        description: String? = "A test guild",
        realm: String? = "TestRealm",
        region: String = "US",
        syncEnabled: Boolean = true,
        benchmarkMode: String = "THEORETICAL",
    ): HttpEntity<CreateGuildRequest> {
        val request =
            CreateGuildRequest(
                id = id,
                name = name,
                description = description,
                realm = realm,
                region = region,
                syncEnabled = syncEnabled,
                benchmarkMode = benchmarkMode,
            )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(request, headers)
    }

    private fun createUpdateRequest(
        name: String? = null,
        description: String? = null,
        syncEnabled: Boolean? = null,
        benchmarkMode: String? = null,
        isActive: Boolean? = null,
    ): HttpEntity<UpdateGuildRequest> {
        val request =
            UpdateGuildRequest(
                name = name,
                description = description,
                syncEnabled = syncEnabled,
                benchmarkMode = benchmarkMode,
                isActive = isActive,
            )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(request, headers)
    }

    @Nested
    inner class CreateGuild {
        @Test
        fun `should create guild and return 201 Created`() {
            // Given
            val entity = createRequest(id = "new-guild", name = "New Guild")

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/guilds",
                    entity,
                    TestGuildResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body shouldNotBe null
            response.body?.id shouldBe "new-guild"
            response.body?.name shouldBe "New Guild"
            response.body?.region shouldBe "US"
            response.body?.syncEnabled shouldBe true
        }

        @Test
        fun `should persist guild to database after creation`() {
            // Given
            val entity = createRequest(id = "persist-guild", name = "Persist Guild")

            // When
            restTemplate.postForEntity(
                "/api/v1/guilds",
                entity,
                TestGuildResponse::class.java,
            )

            // Then - verify in database
            val guildCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM guilds WHERE id = ?",
                    Long::class.java,
                    "persist-guild",
                )
            guildCount shouldBe 1L
        }

        @Test
        fun `should return guild with computed canSync field`() {
            // Given
            val entity = createRequest(id = "sync-guild", name = "Sync Guild", syncEnabled = true)

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/guilds",
                    entity,
                    TestGuildResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.canSync shouldBe true
            response.body?.syncStatus shouldNotBe null
        }

        @Test
        fun `should create guild with all settings`() {
            // Given
            val entity =
                createRequest(
                    id = "full-guild",
                    name = "Full Guild",
                    description = "Full description",
                    realm = "FullRealm",
                    region = "EU",
                    syncEnabled = false,
                    benchmarkMode = "TOP_PERFORMER",
                )

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/guilds",
                    entity,
                    TestGuildResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            response.body?.description shouldBe "Full description"
            response.body?.realm shouldBe "FullRealm"
            response.body?.region shouldBe "EU"
            response.body?.syncEnabled shouldBe false
            response.body?.benchmarkMode shouldBe "TOP_PERFORMER"
        }
    }

    @Nested
    inner class GetGuild {
        @Test
        fun `should get guild by id and return 200 OK`() {
            // Given - create a guild first
            val createEntity = createRequest(id = "get-guild", name = "Get Guild")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                createEntity,
                TestGuildResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/guilds/get-guild",
                    TestGuildResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.id shouldBe "get-guild"
            response.body?.name shouldBe "Get Guild"
        }

        @Test
        fun `should return 404 when guild not found`() {
            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/guilds/non-existent-guild",
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class UpdateGuild {
        @Test
        fun `should update guild and return 200 OK`() {
            // Given - create a guild first
            val createEntity = createRequest(id = "update-guild", name = "Original Name")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                createEntity,
                TestGuildResponse::class.java,
            )

            val updateEntity = createUpdateRequest(name = "Updated Name", description = "Updated description")

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/guilds/update-guild",
                    HttpMethod.PUT,
                    updateEntity,
                    TestGuildResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.name shouldBe "Updated Name"
            response.body?.description shouldBe "Updated description"
        }

        @Test
        fun `should update guild settings correctly`() {
            // Given - create a guild first
            val createEntity = createRequest(id = "settings-guild", name = "Settings Guild", syncEnabled = true)
            restTemplate.postForEntity(
                "/api/v1/guilds",
                createEntity,
                TestGuildResponse::class.java,
            )

            val updateEntity = createUpdateRequest(syncEnabled = false, benchmarkMode = "TOP_PERFORMER")

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/guilds/settings-guild",
                    HttpMethod.PUT,
                    updateEntity,
                    TestGuildResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.syncEnabled shouldBe false
            response.body?.benchmarkMode shouldBe "TOP_PERFORMER"
        }

        @Test
        fun `should persist updated fields to database`() {
            // Given - create a guild first
            val createEntity = createRequest(id = "persist-update-guild", name = "Persist Update")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                createEntity,
                TestGuildResponse::class.java,
            )

            val updateEntity = createUpdateRequest(name = "Persisted Update")

            // When
            restTemplate.exchange(
                "/api/v1/guilds/persist-update-guild",
                HttpMethod.PUT,
                updateEntity,
                TestGuildResponse::class.java,
            )

            // Then - verify in database
            val name =
                jdbcTemplate.queryForObject(
                    "SELECT name FROM guilds WHERE id = ?",
                    String::class.java,
                    "persist-update-guild",
                )
            name shouldBe "Persisted Update"
        }

        @Test
        fun `should return 404 when updating non-existent guild`() {
            // Given
            val updateEntity = createUpdateRequest(name = "Updated")

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/guilds/non-existent-guild",
                    HttpMethod.PUT,
                    updateEntity,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }

        @Test
        fun `should deactivate guild`() {
            // Given - create a guild first
            val createEntity = createRequest(id = "deactivate-guild", name = "Deactivate Guild")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                createEntity,
                TestGuildResponse::class.java,
            )

            val updateEntity = createUpdateRequest(isActive = false)

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/guilds/deactivate-guild",
                    HttpMethod.PUT,
                    updateEntity,
                    TestGuildResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.isActive shouldBe false
            response.body?.canSync shouldBe false // canSync should be false when inactive
        }
    }

    @Nested
    inner class DeleteGuild {
        @Test
        fun `should delete guild and return 204 No Content`() {
            // Given - create a guild first
            val createEntity = createRequest(id = "delete-guild", name = "Delete Guild")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                createEntity,
                TestGuildResponse::class.java,
            )

            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/guilds/delete-guild",
                    HttpMethod.DELETE,
                    null,
                    Void::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NO_CONTENT
        }

        @Test
        fun `should remove guild from database after deletion`() {
            // Given - create a guild first
            val createEntity = createRequest(id = "remove-guild", name = "Remove Guild")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                createEntity,
                TestGuildResponse::class.java,
            )

            // When
            restTemplate.exchange(
                "/api/v1/guilds/remove-guild",
                HttpMethod.DELETE,
                null,
                Void::class.java,
            )

            // Then - verify removed from database
            val guildCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM guilds WHERE id = ?",
                    Long::class.java,
                    "remove-guild",
                )
            guildCount shouldBe 0L
        }

        @Test
        fun `should return 404 when deleting non-existent guild`() {
            // When
            val response =
                restTemplate.exchange(
                    "/api/v1/guilds/non-existent-guild",
                    HttpMethod.DELETE,
                    null,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class ListGuilds {
        @Test
        fun `should return all guilds`() {
            // Given - create multiple guilds
            (1..3).forEach { i ->
                val entity = createRequest(id = "list-guild-$i", name = "List Guild $i")
                restTemplate.postForEntity(
                    "/api/v1/guilds",
                    entity,
                    TestGuildResponse::class.java,
                )
            }

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/guilds",
                    TestGuildListResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            response.body?.guilds?.shouldHaveSize(3)
            response.body?.count shouldBe 3
        }

        @Test
        fun `should return empty list when no guilds exist`() {
            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/guilds",
                    TestGuildListResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.guilds?.shouldBeEmpty()
            response.body?.count shouldBe 0
        }
    }

    @Nested
    inner class ListActiveGuilds {
        @Test
        fun `should return only active guilds`() {
            // Given - create active and inactive guilds
            val activeEntity = createRequest(id = "active-guild", name = "Active Guild")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                activeEntity,
                TestGuildResponse::class.java,
            )

            val inactiveEntity = createRequest(id = "inactive-guild", name = "Inactive Guild")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                inactiveEntity,
                TestGuildResponse::class.java,
            )

            // Deactivate the second guild
            val updateEntity = createUpdateRequest(isActive = false)
            restTemplate.exchange(
                "/api/v1/guilds/inactive-guild",
                HttpMethod.PUT,
                updateEntity,
                TestGuildResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/guilds/active",
                    TestGuildListResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.guilds?.shouldHaveSize(1)
            response.body?.guilds?.first()?.id shouldBe "active-guild"
        }

        @Test
        fun `should return empty list when no active guilds exist`() {
            // Given - create and deactivate a guild
            val entity = createRequest(id = "all-inactive-guild", name = "All Inactive")
            restTemplate.postForEntity(
                "/api/v1/guilds",
                entity,
                TestGuildResponse::class.java,
            )

            val updateEntity = createUpdateRequest(isActive = false)
            restTemplate.exchange(
                "/api/v1/guilds/all-inactive-guild",
                HttpMethod.PUT,
                updateEntity,
                TestGuildResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/guilds/active",
                    TestGuildListResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.guilds?.shouldBeEmpty()
        }
    }

    @Nested
    inner class ApiContractVerification {
        @Test
        fun `should return correct JSON structure for guild response`() {
            // Given
            val entity = createRequest(id = "json-guild", name = "JSON Guild")

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/guilds",
                    entity,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.CREATED
            val body = response.body!!
            body.contains("\"id\"") shouldBe true
            body.contains("\"name\"") shouldBe true
            body.contains("\"region\"") shouldBe true
            body.contains("\"syncEnabled\"") shouldBe true
            body.contains("\"syncStatus\"") shouldBe true
            body.contains("\"benchmarkMode\"") shouldBe true
            body.contains("\"isActive\"") shouldBe true
            body.contains("\"canSync\"") shouldBe true
            body.contains("\"createdAt\"") shouldBe true
            body.contains("\"updatedAt\"") shouldBe true
        }

        @Test
        fun `should return correct JSON structure for list response`() {
            // Given
            val entity = createRequest(id = "list-json-guild", name = "List JSON Guild")
            restTemplate.postForEntity("/api/v1/guilds", entity, TestGuildResponse::class.java)

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/guilds",
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            val body = response.body!!
            body.contains("\"guilds\"") shouldBe true
            body.contains("\"count\"") shouldBe true
        }
    }
}

// Response DTOs for test deserialization
data class TestGuildResponse(
    val id: String,
    val name: String,
    val description: String?,
    val realm: String?,
    val region: String,
    val syncEnabled: Boolean,
    val syncCronExpression: String,
    val timezone: String,
    val benchmarkMode: String,
    val customBenchmarkRms: Double?,
    val customBenchmarkIpi: Double?,
    val syncStatus: String,
    val isActive: Boolean,
    val canSync: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

data class TestGuildListResponse(
    val guilds: List<TestGuildResponse>,
    val count: Int,
)
