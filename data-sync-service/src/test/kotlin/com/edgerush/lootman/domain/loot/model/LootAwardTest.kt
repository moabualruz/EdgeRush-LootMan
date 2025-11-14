package com.edgerush.lootman.domain.loot.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LootAwardTest : UnitTest() {
    @Test
    fun `should create loot award with valid data`() {
        // Arrange & Act
        val lootAward =
            LootAward.create(
                itemId = ItemId(12345),
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

        // Assert
        lootAward.isActive() shouldBe true
        lootAward.flpsScore.value shouldBe 0.85
        lootAward.tier shouldBe LootTier.MYTHIC
    }

    @Test
    fun `should revoke active loot award`() {
        // Arrange
        val lootAward =
            LootAward.create(
                itemId = ItemId(12345),
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

        // Act
        val revoked = lootAward.revoke("Mistake")

        // Assert
        revoked.isActive() shouldBe false
    }

    @Test
    fun `should throw exception when revoking non-active award`() {
        // Arrange
        val lootAward =
            LootAward.create(
                itemId = ItemId(12345),
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            ).revoke("First revoke")

        // Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                lootAward.revoke("Second revoke")
            }
        exception.message shouldBe "Can only revoke active loot awards"
    }
}
