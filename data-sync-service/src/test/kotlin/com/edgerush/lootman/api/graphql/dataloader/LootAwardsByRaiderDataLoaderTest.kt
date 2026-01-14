package com.edgerush.lootman.api.graphql.dataloader

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for LootAwardsByRaiderDataLoader.
 *
 * Tests batch loading of loot awards grouped by raider to prevent N+1 query issues.
 */
class LootAwardsByRaiderDataLoaderTest : UnitTest() {

    @MockK
    private lateinit var lootAwardRepository: LootAwardRepository

    private lateinit var dataLoader: LootAwardsByRaiderBatchLoader

    @BeforeEach
    fun setup() {
        dataLoader = LootAwardsByRaiderBatchLoader(lootAwardRepository)
    }

    @Nested
    inner class BatchLoad {

        @Test
        fun `should batch load loot awards for multiple raiders in single query`() = runBlocking {
            // Arrange
            val raiderIds = listOf(RaiderId(1L), RaiderId(2L), RaiderId(3L))
            val awards = listOf(
                createTestLootAward(id = "award-1", raiderId = 1L),
                createTestLootAward(id = "award-2", raiderId = 1L),
                createTestLootAward(id = "award-3", raiderId = 2L),
            )
            every { lootAwardRepository.findByRaiderIds(raiderIds) } returns awards

            // Act
            val result = dataLoader.load(raiderIds)

            // Assert
            result shouldHaveSize 3
            result[0] shouldHaveSize 2  // Raider 1 has 2 awards
            result[1] shouldHaveSize 1  // Raider 2 has 1 award
            result[2] shouldHaveSize 0  // Raider 3 has no awards
            verify(exactly = 1) { lootAwardRepository.findByRaiderIds(raiderIds) }
        }

        @Test
        fun `should return empty lists for raiders with no awards`() = runBlocking {
            // Arrange
            val raiderIds = listOf(RaiderId(1L), RaiderId(2L))
            every { lootAwardRepository.findByRaiderIds(raiderIds) } returns emptyList()

            // Act
            val result = dataLoader.load(raiderIds)

            // Assert
            result shouldHaveSize 2
            result[0] shouldHaveSize 0
            result[1] shouldHaveSize 0
        }

        @Test
        fun `should handle empty raider id list`() = runBlocking {
            // Arrange
            val raiderIds = emptyList<RaiderId>()
            every { lootAwardRepository.findByRaiderIds(raiderIds) } returns emptyList()

            // Act
            val result = dataLoader.load(raiderIds)

            // Assert
            result shouldHaveSize 0
        }

        @Test
        fun `should preserve order of requested raider ids`() = runBlocking {
            // Arrange
            val raiderIds = listOf(RaiderId(3L), RaiderId(1L), RaiderId(2L))
            val awards = listOf(
                createTestLootAward(id = "award-1", raiderId = 1L),
                createTestLootAward(id = "award-2", raiderId = 3L),
            )
            every { lootAwardRepository.findByRaiderIds(raiderIds) } returns awards

            // Act
            val result = dataLoader.load(raiderIds)

            // Assert
            result shouldHaveSize 3
            result[0] shouldHaveSize 1  // Raider 3 (first requested)
            result[0][0].id.value shouldBe "award-2"
            result[1] shouldHaveSize 1  // Raider 1 (second requested)
            result[1][0].id.value shouldBe "award-1"
            result[2] shouldHaveSize 0  // Raider 2 (third requested, no awards)
        }
    }

    // Helper function
    private fun createTestLootAward(
        id: String = "test-award",
        itemId: Long = 12345L,
        raiderId: Long = 1L,
        guildId: String = "guild-123",
        tier: LootTier = LootTier.MYTHIC,
        flpsScore: Double = 0.85,
        awardedAt: Instant = Instant.now(),
    ): LootAward = LootAward(
        id = LootAwardId(id),
        itemId = ItemId(itemId),
        raiderId = RaiderId(raiderId),
        guildId = GuildId(guildId),
        tier = tier,
        flpsScore = FlpsScore.of(flpsScore),
        awardedAt = awardedAt,
    )
}
