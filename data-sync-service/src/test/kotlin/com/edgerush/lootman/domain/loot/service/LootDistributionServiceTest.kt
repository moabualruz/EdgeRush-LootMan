package com.edgerush.lootman.domain.loot.service

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for LootDistributionService domain service.
 */
class LootDistributionServiceTest : UnitTest() {
    private val service = LootDistributionService()

    @Test
    fun `should return true when raider has no active bans`() {
        // Arrange
        val raiderId = RaiderId("raider-1")
        val activeBans = emptyList<LootBan>()

        // Act
        val result = service.isEligibleForLoot(raiderId, activeBans)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return false when raider has active ban`() {
        // Arrange
        val raiderId = RaiderId("raider-1")
        val activeBan =
            LootBan(
                id = LootBanId.generate(),
                raiderId = raiderId,
                guildId = GuildId("guild-1"),
                reason = "Test ban",
                bannedAt = Instant.now().minusSeconds(3600),
                expiresAt = Instant.now().plusSeconds(3600),
            )
        val activeBans = listOf(activeBan)

        // Act
        val result = service.isEligibleForLoot(raiderId, activeBans)

        // Assert
        result shouldBe false
    }

    @Test
    fun `should return true when ban has expired`() {
        // Arrange
        val raiderId = RaiderId("raider-1")
        val expiredBan =
            LootBan(
                id = LootBanId.generate(),
                raiderId = raiderId,
                guildId = GuildId("guild-1"),
                reason = "Test ban",
                bannedAt = Instant.now().minusSeconds(7200),
                expiresAt = Instant.now().minusSeconds(3600),
            )
        val activeBans = listOf(expiredBan)
        val now = Instant.now()

        // Act
        val result = service.isEligibleForLoot(raiderId, activeBans, now)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return false when no recent awards`() {
        // Arrange
        val recentAwards = emptyList<LootAward>()

        // Act
        val result = service.shouldApplyRecencyDecay(recentAwards)

        // Assert
        result shouldBe false
    }

    @Test
    fun `should return true when recent award within threshold`() {
        // Arrange
        val recentAward =
            LootAward(
                id = LootAwardId.generate(),
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                awardedAt = Instant.now().minusSeconds(86400), // 1 day ago
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )
        val recentAwards = listOf(recentAward)

        // Act
        val result = service.shouldApplyRecencyDecay(recentAwards, decayThresholdDays = 14)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should return false when award outside threshold`() {
        // Arrange
        val oldAward =
            LootAward(
                id = LootAwardId.generate(),
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                awardedAt = Instant.now().minusSeconds(86400 * 20), // 20 days ago
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )
        val recentAwards = listOf(oldAward)

        // Act
        val result = service.shouldApplyRecencyDecay(recentAwards, decayThresholdDays = 14)

        // Assert
        result shouldBe false
    }

    @Test
    fun `should calculate effective score with no recent awards`() {
        // Arrange
        val baseScore = FlpsScore.of(0.85)
        val recentAwards = emptyList<LootAward>()

        // Act
        val result = service.calculateEffectiveScore(baseScore, recentAwards)

        // Assert
        result.value shouldBe 0.85
    }

    @Test
    fun `should calculate effective score with one recent award`() {
        // Arrange
        val baseScore = FlpsScore.of(0.85)
        val recentAward =
            LootAward(
                id = LootAwardId.generate(),
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                awardedAt = Instant.now().minusSeconds(86400),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )
        val recentAwards = listOf(recentAward)

        // Act
        val result = service.calculateEffectiveScore(baseScore, recentAwards, decayFactor = 0.9)

        // Assert
        // 0.85 * 0.9 = 0.765
        result.value shouldBe 0.765
    }

    @Test
    fun `should calculate effective score with multiple recent awards`() {
        // Arrange
        val baseScore = FlpsScore.of(0.85)
        val award1 =
            LootAward(
                id = LootAwardId.generate(),
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                awardedAt = Instant.now().minusSeconds(86400),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )
        val award2 =
            LootAward(
                id = LootAwardId.generate(),
                itemId = ItemId(12346),
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                awardedAt = Instant.now().minusSeconds(172800),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )
        val recentAwards = listOf(award1, award2)

        // Act
        val result = service.calculateEffectiveScore(baseScore, recentAwards, decayFactor = 0.9)

        // Assert
        // 0.85 * (0.9^2) = 0.85 * 0.81 = 0.6885
        result.value shouldBe 0.6885
    }

    @Test
    fun `should allow revocation of active award within time limit`() {
        // Arrange
        val lootAward =
            LootAward(
                id = LootAwardId.generate(),
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                awardedAt = Instant.now().minusSeconds(3600), // 1 hour ago
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

        // Act
        val result = service.canRevokeLootAward(lootAward, maxRevocationDays = 7)

        // Assert
        result shouldBe true
    }

    @Test
    fun `should not allow revocation of award outside time limit`() {
        // Arrange
        val lootAward =
            LootAward(
                id = LootAwardId.generate(),
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                awardedAt = Instant.now().minusSeconds(86400 * 10), // 10 days ago
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

        // Act
        val result = service.canRevokeLootAward(lootAward, maxRevocationDays = 7)

        // Assert
        result shouldBe false
    }

    @Test
    fun `should not allow revocation of already revoked award`() {
        // Arrange
        val lootAward =
            LootAward(
                id = LootAwardId.generate(),
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-1"),
                guildId = GuildId("guild-1"),
                awardedAt = Instant.now().minusSeconds(3600),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            ).revoke("Already revoked")

        // Act
        val result = service.canRevokeLootAward(lootAward, maxRevocationDays = 7)

        // Assert
        result shouldBe false
    }
}
