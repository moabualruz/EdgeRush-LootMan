package com.edgerush.lootman.domain.guild.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for Guild domain model.
 *
 * Tests verify:
 * - Guild creation and validation
 * - Business logic methods
 * - Value objects (Region, BenchmarkMode, SyncStatus)
 * - GuildSettings configuration
 */
class GuildTest : UnitTest() {

    @Nested
    inner class GuildCreation {
        @Test
        fun `should create guild with valid name`() {
            // Given / When
            val guild = createGuild(name = "Test Guild")

            // Then
            guild.name shouldBe "Test Guild"
            guild.id.value shouldBe "test-guild"
        }

        @Test
        fun `should throw exception for blank guild name`() {
            // When / Then
            shouldThrow<IllegalArgumentException> {
                createGuild(name = "")
            }.message shouldBe "Guild name cannot be blank"
        }

        @Test
        fun `should throw exception for whitespace-only guild name`() {
            // When / Then
            shouldThrow<IllegalArgumentException> {
                createGuild(name = "   ")
            }.message shouldBe "Guild name cannot be blank"
        }
    }

    @Nested
    inner class IsSyncingTests {
        @Test
        fun `should return true when status is IN_PROGRESS`() {
            // Given
            val guild = createGuild(syncStatus = SyncStatus.IN_PROGRESS)

            // When / Then
            guild.isSyncing() shouldBe true
        }

        @Test
        fun `should return false when status is SUCCESS`() {
            // Given
            val guild = createGuild(syncStatus = SyncStatus.SUCCESS)

            // When / Then
            guild.isSyncing() shouldBe false
        }

        @Test
        fun `should return false when status is FAILED`() {
            // Given
            val guild = createGuild(syncStatus = SyncStatus.FAILED)

            // When / Then
            guild.isSyncing() shouldBe false
        }

        @Test
        fun `should return false when status is NEVER_RUN`() {
            // Given
            val guild = createGuild(syncStatus = SyncStatus.NEVER_RUN)

            // When / Then
            guild.isSyncing() shouldBe false
        }
    }

    @Nested
    inner class CanSyncTests {
        @Test
        fun `should return true when active and sync enabled`() {
            // Given
            val settings = GuildSettings(syncEnabled = true)
            val guild = createGuild(isActive = true, settings = settings)

            // When / Then
            guild.canSync() shouldBe true
        }

        @Test
        fun `should return false when inactive`() {
            // Given
            val settings = GuildSettings(syncEnabled = true)
            val guild = createGuild(isActive = false, settings = settings)

            // When / Then
            guild.canSync() shouldBe false
        }

        @Test
        fun `should return false when sync disabled`() {
            // Given
            val settings = GuildSettings(syncEnabled = false)
            val guild = createGuild(isActive = true, settings = settings)

            // When / Then
            guild.canSync() shouldBe false
        }

        @Test
        fun `should return false when inactive and sync disabled`() {
            // Given
            val settings = GuildSettings(syncEnabled = false)
            val guild = createGuild(isActive = false, settings = settings)

            // When / Then
            guild.canSync() shouldBe false
        }
    }

    @Nested
    inner class GuildSettingsTests {
        @Test
        fun `should create default settings`() {
            // Given / When
            val settings = GuildSettings.default()

            // Then
            settings.syncEnabled shouldBe true
            settings.syncCronExpression shouldBe "0 0 4 * * *"
            settings.syncRunOnStartup shouldBe false
            settings.timezone shouldBe "UTC"
            settings.benchmarkMode shouldBe BenchmarkMode.THEORETICAL
            settings.customBenchmarkRms shouldBe null
            settings.customBenchmarkIpi shouldBe null
        }

        @Test
        fun `should create custom settings`() {
            // Given / When
            val settings = GuildSettings(
                syncEnabled = false,
                syncCronExpression = "0 0 6 * * *",
                syncRunOnStartup = true,
                timezone = "America/New_York",
                benchmarkMode = BenchmarkMode.CUSTOM,
                customBenchmarkRms = 0.95,
                customBenchmarkIpi = 0.90
            )

            // Then
            settings.syncEnabled shouldBe false
            settings.syncCronExpression shouldBe "0 0 6 * * *"
            settings.syncRunOnStartup shouldBe true
            settings.timezone shouldBe "America/New_York"
            settings.benchmarkMode shouldBe BenchmarkMode.CUSTOM
            settings.customBenchmarkRms shouldBe 0.95
            settings.customBenchmarkIpi shouldBe 0.90
        }
    }

    @Nested
    inner class RegionTests {
        @Test
        fun `should parse US region from string`() {
            Region.fromString("US") shouldBe Region.US
        }

        @Test
        fun `should parse EU region from string`() {
            Region.fromString("EU") shouldBe Region.EU
        }

        @Test
        fun `should parse KR region from string`() {
            Region.fromString("KR") shouldBe Region.KR
        }

        @Test
        fun `should parse TW region from string`() {
            Region.fromString("TW") shouldBe Region.TW
        }

        @Test
        fun `should parse CN region from string`() {
            Region.fromString("CN") shouldBe Region.CN
        }

        @Test
        fun `should parse region case-insensitively`() {
            Region.fromString("us") shouldBe Region.US
            Region.fromString("Eu") shouldBe Region.EU
        }

        @Test
        fun `should return null for invalid region`() {
            Region.fromString("INVALID") shouldBe null
            Region.fromString("") shouldBe null
        }
    }

    @Nested
    inner class BenchmarkModeTests {
        @Test
        fun `should parse THEORETICAL from string`() {
            BenchmarkMode.fromString("THEORETICAL") shouldBe BenchmarkMode.THEORETICAL
        }

        @Test
        fun `should parse TOP_PERFORMER from string`() {
            BenchmarkMode.fromString("TOP_PERFORMER") shouldBe BenchmarkMode.TOP_PERFORMER
        }

        @Test
        fun `should parse CUSTOM from string`() {
            BenchmarkMode.fromString("CUSTOM") shouldBe BenchmarkMode.CUSTOM
        }

        @Test
        fun `should parse benchmark mode case-insensitively`() {
            BenchmarkMode.fromString("theoretical") shouldBe BenchmarkMode.THEORETICAL
            BenchmarkMode.fromString("Top_Performer") shouldBe BenchmarkMode.TOP_PERFORMER
        }

        @Test
        fun `should return null for invalid benchmark mode`() {
            BenchmarkMode.fromString("INVALID") shouldBe null
            BenchmarkMode.fromString("") shouldBe null
        }
    }

    @Nested
    inner class SyncStatusTests {
        @Test
        fun `should parse NEVER_RUN from string`() {
            SyncStatus.fromString("NEVER_RUN") shouldBe SyncStatus.NEVER_RUN
        }

        @Test
        fun `should parse SUCCESS from string`() {
            SyncStatus.fromString("SUCCESS") shouldBe SyncStatus.SUCCESS
        }

        @Test
        fun `should parse FAILED from string`() {
            SyncStatus.fromString("FAILED") shouldBe SyncStatus.FAILED
        }

        @Test
        fun `should parse IN_PROGRESS from string`() {
            SyncStatus.fromString("IN_PROGRESS") shouldBe SyncStatus.IN_PROGRESS
        }

        @Test
        fun `should parse sync status case-insensitively`() {
            SyncStatus.fromString("success") shouldBe SyncStatus.SUCCESS
            SyncStatus.fromString("In_Progress") shouldBe SyncStatus.IN_PROGRESS
        }

        @Test
        fun `should return null for invalid sync status`() {
            SyncStatus.fromString("INVALID") shouldBe null
            SyncStatus.fromString("") shouldBe null
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
        updatedAt: Instant = Instant.now()
    ): Guild = Guild(
        id = id,
        name = name,
        description = description,
        realm = realm,
        region = region,
        settings = settings,
        syncStatus = syncStatus,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
