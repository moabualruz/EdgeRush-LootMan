package com.edgerush.lootman.api.guild

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.guild.model.BenchmarkMode
import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.guild.model.GuildSettings
import com.edgerush.lootman.domain.guild.model.Region
import com.edgerush.lootman.domain.guild.model.SyncStatus
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for Guild DTO mapping.
 *
 * Tests verify:
 * - CreateGuildRequest validation fields
 * - UpdateGuildRequest validation fields
 * - GuildResponse mapping from domain model
 * - GuildListResponse mapping
 * - Computed fields (canSync)
 */
class GuildDtoTest : UnitTest() {
    @Nested
    inner class CreateGuildRequestTests {
        @Test
        fun `should create valid request with required fields`() {
            // Given / When
            val request =
                CreateGuildRequest(
                    id = "test-guild",
                    name = "Test Guild",
                )

            // Then
            request.id shouldBe "test-guild"
            request.name shouldBe "Test Guild"
            request.region shouldBe "US" // default
            request.syncEnabled shouldBe true // default
            request.benchmarkMode shouldBe "THEORETICAL" // default
        }

        @Test
        fun `should create request with all fields`() {
            // Given / When
            val request =
                CreateGuildRequest(
                    id = "full-guild",
                    name = "Full Guild",
                    description = "A complete guild",
                    realm = "Area 52",
                    region = "EU",
                    syncEnabled = false,
                    syncCronExpression = "0 0 6 * * *",
                    timezone = "America/New_York",
                    benchmarkMode = "CUSTOM",
                )

            // Then
            request.id shouldBe "full-guild"
            request.name shouldBe "Full Guild"
            request.description shouldBe "A complete guild"
            request.realm shouldBe "Area 52"
            request.region shouldBe "EU"
            request.syncEnabled shouldBe false
            request.syncCronExpression shouldBe "0 0 6 * * *"
            request.timezone shouldBe "America/New_York"
            request.benchmarkMode shouldBe "CUSTOM"
        }
    }

    @Nested
    inner class UpdateGuildRequestTests {
        @Test
        fun `should create update request with partial fields`() {
            // Given / When
            val request =
                UpdateGuildRequest(
                    name = "Updated Name",
                )

            // Then
            request.name shouldBe "Updated Name"
            request.description shouldBe null
            request.syncEnabled shouldBe null
            request.benchmarkMode shouldBe null
            request.isActive shouldBe null
        }

        @Test
        fun `should create update request with all fields`() {
            // Given / When
            val request =
                UpdateGuildRequest(
                    name = "Updated Name",
                    description = "Updated description",
                    realm = "New Realm",
                    region = "EU",
                    syncEnabled = false,
                    syncCronExpression = "0 0 8 * * *",
                    timezone = "Europe/London",
                    benchmarkMode = "TOP_PERFORMER",
                    customBenchmarkRms = 0.95,
                    customBenchmarkIpi = 0.90,
                    isActive = false,
                )

            // Then
            request.name shouldBe "Updated Name"
            request.description shouldBe "Updated description"
            request.realm shouldBe "New Realm"
            request.region shouldBe "EU"
            request.syncEnabled shouldBe false
            request.syncCronExpression shouldBe "0 0 8 * * *"
            request.timezone shouldBe "Europe/London"
            request.benchmarkMode shouldBe "TOP_PERFORMER"
            request.customBenchmarkRms shouldBe 0.95
            request.customBenchmarkIpi shouldBe 0.90
            request.isActive shouldBe false
        }
    }

    @Nested
    inner class GuildResponseMappingTests {
        @Test
        fun `should map Guild to GuildResponse correctly`() {
            // Given
            val createdAt = Instant.parse("2024-01-01T00:00:00Z")
            val updatedAt = Instant.parse("2024-06-15T12:00:00Z")
            val guild =
                createGuild(
                    id = GuildId("test-guild"),
                    name = "Test Guild",
                    description = "Test description",
                    realm = "Area 52",
                    region = Region.EU,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            // When
            val response = GuildResponse.from(guild)

            // Then
            response.id shouldBe "test-guild"
            response.name shouldBe "Test Guild"
            response.description shouldBe "Test description"
            response.realm shouldBe "Area 52"
            response.region shouldBe "EU"
            response.createdAt shouldBe createdAt.toString()
            response.updatedAt shouldBe updatedAt.toString()
        }

        @Test
        fun `should map settings correctly`() {
            // Given
            val settings =
                GuildSettings(
                    syncEnabled = true,
                    syncCronExpression = "0 0 5 * * *",
                    timezone = "America/New_York",
                    benchmarkMode = BenchmarkMode.CUSTOM,
                    customBenchmarkRms = 0.95,
                    customBenchmarkIpi = 0.90,
                )
            val guild = createGuild(settings = settings)

            // When
            val response = GuildResponse.from(guild)

            // Then
            response.syncEnabled shouldBe true
            response.syncCronExpression shouldBe "0 0 5 * * *"
            response.timezone shouldBe "America/New_York"
            response.benchmarkMode shouldBe "CUSTOM"
            response.customBenchmarkRms shouldBe 0.95
            response.customBenchmarkIpi shouldBe 0.90
        }

        @Test
        fun `should calculate canSync true when active and sync enabled`() {
            // Given
            val settings = GuildSettings(syncEnabled = true)
            val guild = createGuild(isActive = true, settings = settings)

            // When
            val response = GuildResponse.from(guild)

            // Then
            response.canSync shouldBe true
        }

        @Test
        fun `should calculate canSync false when inactive`() {
            // Given
            val settings = GuildSettings(syncEnabled = true)
            val guild = createGuild(isActive = false, settings = settings)

            // When
            val response = GuildResponse.from(guild)

            // Then
            response.canSync shouldBe false
        }

        @Test
        fun `should calculate canSync false when sync disabled`() {
            // Given
            val settings = GuildSettings(syncEnabled = false)
            val guild = createGuild(isActive = true, settings = settings)

            // When
            val response = GuildResponse.from(guild)

            // Then
            response.canSync shouldBe false
        }

        @Test
        fun `should format syncStatus correctly`() {
            // Given
            val guild = createGuild(syncStatus = SyncStatus.SUCCESS)

            // When
            val response = GuildResponse.from(guild)

            // Then
            response.syncStatus shouldBe "SUCCESS"
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val guild =
                createGuild(
                    description = null,
                    realm = null,
                    settings =
                        GuildSettings(
                            customBenchmarkRms = null,
                            customBenchmarkIpi = null,
                        ),
                )

            // When
            val response = GuildResponse.from(guild)

            // Then
            response.description shouldBe null
            response.realm shouldBe null
            response.customBenchmarkRms shouldBe null
            response.customBenchmarkIpi shouldBe null
        }
    }

    @Nested
    inner class GuildListResponseMappingTests {
        @Test
        fun `should map list of guilds to response`() {
            // Given
            val guilds =
                listOf(
                    createGuild(id = GuildId("guild-1"), name = "Guild One"),
                    createGuild(id = GuildId("guild-2"), name = "Guild Two"),
                    createGuild(id = GuildId("guild-3"), name = "Guild Three"),
                )

            // When
            val response = GuildListResponse.from(guilds)

            // Then
            response.count shouldBe 3
            response.guilds.size shouldBe 3
            response.guilds[0].name shouldBe "Guild One"
            response.guilds[1].name shouldBe "Guild Two"
            response.guilds[2].name shouldBe "Guild Three"
        }

        @Test
        fun `should handle empty list`() {
            // Given
            val guilds = emptyList<Guild>()

            // When
            val response = GuildListResponse.from(guilds)

            // Then
            response.count shouldBe 0
            response.guilds shouldBe emptyList()
        }
    }

    // Helper method
    private fun createGuild(
        id: GuildId = GuildId("test-guild"),
        name: String = "Test Guild",
        description: String? = null,
        realm: String? = null,
        region: Region = Region.US,
        settings: GuildSettings = GuildSettings.default(),
        syncStatus: SyncStatus = SyncStatus.NEVER_RUN,
        isActive: Boolean = true,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Guild =
        Guild(
            id = id,
            name = name,
            description = description,
            realm = realm,
            region = region,
            settings = settings,
            syncStatus = syncStatus,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
