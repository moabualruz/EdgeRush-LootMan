package com.edgerush.lootman.api.graphql.query

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.loot.GetLootHistoryByGuildQuery
import com.edgerush.lootman.application.loot.GetLootHistoryByRaiderQuery
import com.edgerush.lootman.application.loot.GetLootHistoryUseCase
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for LootQueryResolver.
 *
 * Tests the GraphQL query resolver for loot operations following TDD principles.
 */
class LootQueryResolverTest : UnitTest() {

    @MockK
    private lateinit var getLootHistoryUseCase: GetLootHistoryUseCase

    @InjectMockKs
    private lateinit var resolver: LootQueryResolver

    @Nested
    inner class LootAwardsByGuildQuery {

        @Test
        fun `should return loot awards for guild`() {
            // Arrange
            val awards = listOf(
                createTestLootAward(id = "award-1", raiderId = 1L, itemId = 100L),
                createTestLootAward(id = "award-2", raiderId = 2L, itemId = 200L),
                createTestLootAward(id = "award-3", raiderId = 3L, itemId = 300L),
            )
            val querySlot = slot<GetLootHistoryByGuildQuery>()
            every { getLootHistoryUseCase.getByGuild(capture(querySlot)) } returns Result.success(awards)

            // Act
            val result = resolver.lootAwards(guildId = "guild-123")

            // Assert
            result shouldHaveSize 3
            result[0].id shouldBe "award-1"
            result[1].id shouldBe "award-2"
            result[2].id shouldBe "award-3"
            querySlot.captured.guildId.value shouldBe "guild-123"
        }

        @Test
        fun `should return empty list when no loot awards exist`() {
            // Arrange
            every { getLootHistoryUseCase.getByGuild(any()) } returns Result.success(emptyList())

            // Act
            val result = resolver.lootAwards(guildId = "empty-guild")

            // Assert
            result shouldHaveSize 0
        }

        @Test
        fun `should filter active awards only when requested`() {
            // Arrange
            val awards = listOf(
                createTestLootAward(id = "award-1"),
            )
            val querySlot = slot<GetLootHistoryByGuildQuery>()
            every { getLootHistoryUseCase.getByGuild(capture(querySlot)) } returns Result.success(awards)

            // Act
            resolver.lootAwards(guildId = "guild-123", activeOnly = true)

            // Assert
            querySlot.captured.activeOnly shouldBe true
        }

        @Test
        fun `should propagate exception on error`() {
            // Arrange
            every { getLootHistoryUseCase.getByGuild(any()) } returns
                Result.failure(RuntimeException("Database connection failed"))

            // Act & Assert
            val exception = org.junit.jupiter.api.assertThrows<RuntimeException> {
                resolver.lootAwards(guildId = "guild-123")
            }
            exception.message shouldBe "Database connection failed"
        }
    }

    @Nested
    inner class LootHistoryByRaiderQuery {

        @Test
        fun `should return loot history for raider`() {
            // Arrange
            val awards = listOf(
                createTestLootAward(id = "award-1", itemId = 100L, tier = LootTier.MYTHIC),
                createTestLootAward(id = "award-2", itemId = 200L, tier = LootTier.HEROIC),
            )
            val querySlot = slot<GetLootHistoryByRaiderQuery>()
            every { getLootHistoryUseCase.getByRaider(capture(querySlot)) } returns Result.success(awards)

            // Act
            val result = resolver.lootHistory(raiderId = "42")

            // Assert
            result shouldHaveSize 2
            result[0].tier shouldBe LootTier.MYTHIC
            result[1].tier shouldBe LootTier.HEROIC
            querySlot.captured.raiderId.value shouldBe 42L
        }

        @Test
        fun `should return empty list when raider has no loot history`() {
            // Arrange
            every { getLootHistoryUseCase.getByRaider(any()) } returns Result.success(emptyList())

            // Act
            val result = resolver.lootHistory(raiderId = "999")

            // Assert
            result shouldHaveSize 0
        }

        @Test
        fun `should filter active awards only when requested`() {
            // Arrange
            val querySlot = slot<GetLootHistoryByRaiderQuery>()
            every { getLootHistoryUseCase.getByRaider(capture(querySlot)) } returns Result.success(emptyList())

            // Act
            resolver.lootHistory(raiderId = "42", activeOnly = true)

            // Assert
            querySlot.captured.activeOnly shouldBe true
        }

        @Test
        fun `should propagate exception on error`() {
            // Arrange
            every { getLootHistoryUseCase.getByRaider(any()) } returns
                Result.failure(RuntimeException("Database error"))

            // Act & Assert
            val exception = org.junit.jupiter.api.assertThrows<RuntimeException> {
                resolver.lootHistory(raiderId = "42")
            }
            exception.message shouldBe "Database error"
        }
    }

    @Nested
    inner class LootAwardTypeConversion {

        @Test
        fun `should correctly convert all loot award fields`() {
            // Arrange
            val awardedAt = Instant.parse("2026-01-14T10:30:00Z")
            val award = createTestLootAward(
                id = "award-42",
                raiderId = 123L,
                itemId = 456L,
                guildId = "guild-789",
                tier = LootTier.MYTHIC,
                flpsScore = 0.85,
                awardedAt = awardedAt,
            )
            every { getLootHistoryUseCase.getByGuild(any()) } returns Result.success(listOf(award))

            // Act
            val result = resolver.lootAwards(guildId = "guild-789")

            // Assert
            result shouldHaveSize 1
            result[0].id shouldBe "award-42"
            result[0].raiderId shouldBe "123"
            result[0].itemId shouldBe "456"
            result[0].guildId shouldBe "guild-789"
            result[0].tier shouldBe LootTier.MYTHIC
            result[0].flpsScore shouldBe 0.85
            result[0].awardedAt shouldBe awardedAt
            result[0].isActive shouldBe true
        }

        @Test
        fun `should include all tier types`() {
            // Arrange
            val awards = listOf(
                createTestLootAward(id = "1", tier = LootTier.MYTHIC),
                createTestLootAward(id = "2", tier = LootTier.HEROIC),
                createTestLootAward(id = "3", tier = LootTier.NORMAL),
                createTestLootAward(id = "4", tier = LootTier.LFR),
            )
            every { getLootHistoryUseCase.getByGuild(any()) } returns Result.success(awards)

            // Act
            val result = resolver.lootAwards(guildId = "guild-123")

            // Assert
            result shouldHaveSize 4
            result.map { it.tier } shouldBe listOf(
                LootTier.MYTHIC,
                LootTier.HEROIC,
                LootTier.NORMAL,
                LootTier.LFR,
            )
        }
    }

    // Helper function to create test loot awards
    private fun createTestLootAward(
        id: String = "test-award",
        raiderId: Long = 1L,
        itemId: Long = 100L,
        guildId: String = "test-guild",
        tier: LootTier = LootTier.MYTHIC,
        flpsScore: Double = 0.75,
        awardedAt: Instant = Instant.now(),
    ): LootAward = LootAward(
        id = LootAwardId(id),
        raiderId = RaiderId(raiderId),
        itemId = ItemId(itemId),
        guildId = GuildId(guildId),
        tier = tier,
        flpsScore = FlpsScore.of(flpsScore),
        awardedAt = awardedAt,
    )
}
