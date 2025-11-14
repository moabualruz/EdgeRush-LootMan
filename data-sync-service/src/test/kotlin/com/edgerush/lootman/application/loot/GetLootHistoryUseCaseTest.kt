package com.edgerush.lootman.application.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class GetLootHistoryUseCaseTest : UnitTest() {
    private val lootAwardRepository = mockk<LootAwardRepository>()
    private val useCase = GetLootHistoryUseCase(lootAwardRepository)

    @Test
    fun `should get loot history for guild`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val awards =
            listOf(
                LootAward.create(
                    itemId = ItemId(1),
                    raiderId = RaiderId("raider-1"),
                    guildId = guildId,
                    flpsScore = FlpsScore.of(0.85),
                    tier = LootTier.MYTHIC,
                ),
                LootAward.create(
                    itemId = ItemId(2),
                    raiderId = RaiderId("raider-2"),
                    guildId = guildId,
                    flpsScore = FlpsScore.of(0.75),
                    tier = LootTier.HEROIC,
                ),
            )

        every { lootAwardRepository.findByGuildId(guildId) } returns awards

        // Act
        val result = useCase.getGuildHistory(guildId, limit = 100)

        // Assert
        result.isSuccess shouldBe true
        result.getOrThrow().size shouldBe 2
    }

    @Test
    fun `should get loot history for raider`() {
        // Arrange
        val raiderId = RaiderId("test-raider")
        val awards =
            listOf(
                LootAward.create(
                    itemId = ItemId(1),
                    raiderId = raiderId,
                    guildId = GuildId("test-guild"),
                    flpsScore = FlpsScore.of(0.85),
                    tier = LootTier.MYTHIC,
                ),
            )

        every { lootAwardRepository.findByRaiderId(raiderId) } returns awards

        // Act
        val result = useCase.getRaiderHistory(raiderId)

        // Assert
        result.isSuccess shouldBe true
        result.getOrThrow().size shouldBe 1
    }
}

