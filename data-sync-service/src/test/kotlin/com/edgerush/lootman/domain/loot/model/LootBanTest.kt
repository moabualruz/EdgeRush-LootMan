package com.edgerush.lootman.domain.loot.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

/**
 * Unit tests for LootBan entity.
 *
 * Tests validation, expiration logic, and lifecycle methods.
 */
class LootBanTest : UnitTest() {
    @Test
    fun `should create loot ban with valid data`() {
        // Arrange & Act
        val lootBan =
            LootBan.create(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Behavioral issue",
                expiresAt = Instant.now().plusSeconds(86400 * 7), // 7 days
            )

        // Assert
        lootBan.raiderId.value shouldBe "test-raider"
        lootBan.guildId.value shouldBe "test-guild"
        lootBan.reason shouldBe "Behavioral issue"
        lootBan.isActive() shouldBe true
    }

    @Test
    fun `should create permanent ban with null expiration`() {
        // Arrange & Act
        val lootBan =
            LootBan.create(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Permanent ban",
                expiresAt = null,
            )

        // Assert
        lootBan.expiresAt shouldBe null
        lootBan.isActive() shouldBe true
    }

    @Test
    fun `should return true for active ban with future expiration`() {
        // Arrange
        val futureExpiration = Instant.now().plusSeconds(3600) // 1 hour from now
        val lootBan =
            LootBan.create(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Test ban",
                expiresAt = futureExpiration,
            )

        // Act
        val result = lootBan.isActive()

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return false for expired ban`() {
        // Arrange
        val pastExpiration = Instant.now().minusSeconds(3600) // 1 hour ago
        val lootBan =
            LootBan.create(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Test ban",
                expiresAt = pastExpiration,
            )

        // Act
        val result = lootBan.isActive()

        // Assert
        result shouldBe false
    }

    @Test
    fun `should return true for permanent ban at any time`() {
        // Arrange
        val lootBan =
            LootBan.create(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Permanent ban",
                expiresAt = null,
            )

        // Act
        val resultNow = lootBan.isActive(Instant.now())
        val resultFuture = lootBan.isActive(Instant.now().plusSeconds(86400 * 365)) // 1 year from now

        // Assert
        resultNow shouldBe true
        resultFuture shouldBe true
    }

    @Test
    fun `should check active status at specific time`() {
        // Arrange
        val checkTime = Instant.parse("2024-01-15T12:00:00Z")
        val expiresAt = Instant.parse("2024-01-20T12:00:00Z")
        val lootBan =
            LootBan(
                id = LootBanId.generate(),
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Test ban",
                bannedAt = Instant.parse("2024-01-10T12:00:00Z"),
                expiresAt = expiresAt,
            )

        // Act
        val resultBeforeExpiry = lootBan.isActive(checkTime)
        val resultAfterExpiry = lootBan.isActive(Instant.parse("2024-01-25T12:00:00Z"))

        // Assert
        resultBeforeExpiry shouldBe true
        resultAfterExpiry shouldBe false
    }

    @Test
    fun `should generate unique ban IDs`() {
        // Arrange & Act
        val ban1 =
            LootBan.create(
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                reason = "Ban 1",
                expiresAt = null,
            )
        val ban2 =
            LootBan.create(
                raiderId = RaiderId("raider-2"),
                guildId = GuildId("guild-1"),
                reason = "Ban 2",
                expiresAt = null,
            )

        // Assert
        ban1.id.value shouldBe ban1.id.value // Same ID for same object
        (ban1.id.value != ban2.id.value) shouldBe true // Different IDs for different objects
    }

    @Test
    fun `should throw exception for blank ban ID`() {
        // Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                LootBanId("")
            }
        exception.message shouldBe "LootBan ID cannot be blank"
    }

    @Test
    fun `should create ban with bannedAt timestamp`() {
        // Arrange
        val beforeCreation = Instant.now()

        // Act
        val lootBan =
            LootBan.create(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Test ban",
                expiresAt = Instant.now().plusSeconds(86400),
            )

        val afterCreation = Instant.now()

        // Assert
        lootBan.bannedAt.isAfter(beforeCreation.minusSeconds(1)) shouldBe true
        lootBan.bannedAt.isBefore(afterCreation.plusSeconds(1)) shouldBe true
    }

    @Test
    fun `should handle ban expiring exactly at check time`() {
        // Arrange
        val expirationTime = Instant.parse("2024-01-20T12:00:00Z")
        val lootBan =
            LootBan(
                id = LootBanId.generate(),
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Test ban",
                bannedAt = Instant.parse("2024-01-10T12:00:00Z"),
                expiresAt = expirationTime,
            )

        // Act
        val resultAtExpiration = lootBan.isActive(expirationTime)

        // Assert
        // At the exact expiration time, the ban should be expired (not active)
        resultAtExpiration shouldBe false
    }

    @Test
    fun `should support different ban reasons`() {
        // Arrange & Act
        val behavioralBan =
            LootBan.create(
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                reason = "Toxic behavior in raid",
                expiresAt = Instant.now().plusSeconds(86400 * 7),
            )

        val attendanceBan =
            LootBan.create(
                raiderId = RaiderId("raider-2"),
                guildId = GuildId("guild-1"),
                reason = "Poor attendance record",
                expiresAt = Instant.now().plusSeconds(86400 * 14),
            )

        // Assert
        behavioralBan.reason shouldBe "Toxic behavior in raid"
        attendanceBan.reason shouldBe "Poor attendance record"
    }
}
