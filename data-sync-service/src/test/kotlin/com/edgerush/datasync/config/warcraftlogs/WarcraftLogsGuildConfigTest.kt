package com.edgerush.datasync.config.warcraftlogs

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for WarcraftLogsGuildConfig.
 *
 * Tests default values and configuration structure.
 */
class WarcraftLogsGuildConfigTest : UnitTest() {

    @Test
    fun `should have correct default values`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Assert
        config.enabled shouldBe true
        config.clientId shouldBe null
        config.clientSecret shouldBe null
        config.syncIntervalHours shouldBe 6
        config.syncTimeWindowDays shouldBe 30
    }

    @Test
    fun `should have correct default included difficulties`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Assert
        config.includedDifficulties shouldHaveSize 2
        config.includedDifficulties shouldContain "Mythic"
        config.includedDifficulties shouldContain "Heroic"
    }

    @Test
    fun `should have correct default MAS calculation weights`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Assert
        config.dpaWeight shouldBe 0.25
        config.adtWeight shouldBe 0.25
        config.criticalThreshold shouldBe 1.5
    }

    @Test
    fun `should have correct default fallback values`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Assert
        config.fallbackMAS shouldBe 0.0
        config.fallbackDPA shouldBe 0.5
        config.fallbackADT shouldBe 10.0
    }

    @Test
    fun `should have correct default time weighting configuration`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Assert
        config.recentPerformanceWeightMultiplier shouldBe 2.0
        config.recentPerformanceDays shouldBe 14
    }

    @Test
    fun `should have correct default spec average configuration`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Assert
        config.specAveragePercentile shouldBe 50
        config.minimumSampleSize shouldBe 5
    }

    @Test
    fun `should have correct default cache configuration`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Assert
        config.masCacheTTLMinutes shouldBe 60
    }

    @Test
    fun `should have empty character name mappings by default`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Assert
        config.characterNameMappings.shouldBeEmpty()
    }

    @Test
    fun `should allow custom guild credentials`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
            clientId = "guild-specific-id",
            clientSecret = "guild-specific-secret",
        )

        // Assert
        config.clientId shouldBe "guild-specific-id"
        config.clientSecret shouldBe "guild-specific-secret"
    }

    @Test
    fun `should allow custom sync configuration`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
            syncIntervalHours = 12,
            syncTimeWindowDays = 60,
            includedDifficulties = listOf("Mythic"),
        )

        // Assert
        config.syncIntervalHours shouldBe 12
        config.syncTimeWindowDays shouldBe 60
        config.includedDifficulties shouldHaveSize 1
        config.includedDifficulties shouldContain "Mythic"
    }

    @Test
    fun `should allow custom MAS weights`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
            dpaWeight = 0.30,
            adtWeight = 0.30,
            criticalThreshold = 2.0,
        )

        // Assert
        config.dpaWeight shouldBe 0.30
        config.adtWeight shouldBe 0.30
        config.criticalThreshold shouldBe 2.0
    }

    @Test
    fun `should allow custom fallback values`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
            fallbackMAS = 0.5,
            fallbackDPA = 0.3,
            fallbackADT = 15.0,
        )

        // Assert
        config.fallbackMAS shouldBe 0.5
        config.fallbackDPA shouldBe 0.3
        config.fallbackADT shouldBe 15.0
    }

    @Test
    fun `should allow character name mappings`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
            characterNameMappings = mapOf(
                "WoWAuditName" to "WCLName",
                "AnotherName" to "DifferentName",
            ),
        )

        // Assert
        config.characterNameMappings["WoWAuditName"] shouldBe "WCLName"
        config.characterNameMappings["AnotherName"] shouldBe "DifferentName"
    }

    @Test
    fun `should allow disabling guild config`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
            enabled = false,
        )

        // Assert
        config.enabled shouldBe false
    }

    @Test
    fun `should support copy with modifications`() {
        // Arrange
        val original = WarcraftLogsGuildConfig(
            guildId = "test-guild",
            guildName = "Test Guild",
            realm = "Illidan",
            region = "US",
        )

        // Act
        val copied = original.copy(
            syncIntervalHours = 3,
            enabled = false,
        )

        // Assert
        copied.guildId shouldBe "test-guild"
        copied.guildName shouldBe "Test Guild"
        copied.syncIntervalHours shouldBe 3
        copied.enabled shouldBe false
    }

    @Test
    fun `should store correct guild identifiers`() {
        // Arrange & Act
        val config = WarcraftLogsGuildConfig(
            guildId = "my-guild-123",
            guildName = "Awesome Raiders",
            realm = "Stormrage",
            region = "EU",
        )

        // Assert
        config.guildId shouldBe "my-guild-123"
        config.guildName shouldBe "Awesome Raiders"
        config.realm shouldBe "Stormrage"
        config.region shouldBe "EU"
    }
}
