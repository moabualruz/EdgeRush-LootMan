package com.edgerush.lootman.api.graphql.mutation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.loot.AwardLootCommand
import com.edgerush.lootman.application.loot.AwardLootUseCase
import com.edgerush.lootman.application.loot.RevokeLootAwardCommand
import com.edgerush.lootman.application.loot.RevokeLootAwardUseCase
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.LootBanActiveException
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for LootMutationResolver.
 *
 * Tests the GraphQL mutation resolver for loot operations following TDD principles.
 */
class LootMutationResolverTest : UnitTest() {
    @MockK
    private lateinit var awardLootUseCase: AwardLootUseCase

    @MockK
    private lateinit var revokeLootAwardUseCase: RevokeLootAwardUseCase

    @InjectMockKs
    private lateinit var resolver: LootMutationResolver

    @Nested
    inner class AwardLootMutation {
        @Test
        fun `should award loot successfully`() {
            // Arrange
            val input =
                AwardLootInput(
                    itemId = "12345",
                    raiderId = "42",
                    guildId = "guild-123",
                    flpsScore = 0.85,
                    tier = "MYTHIC",
                )
            val award =
                createTestLootAward(
                    id = "award-1",
                    itemId = 12345L,
                    raiderId = 42L,
                    guildId = "guild-123",
                    tier = LootTier.MYTHIC,
                    flpsScore = 0.85,
                )
            val commandSlot = slot<AwardLootCommand>()
            every { awardLootUseCase.execute(capture(commandSlot)) } returns Result.success(award)

            // Act
            val result = resolver.awardLoot(input)

            // Assert
            result.itemId shouldBe "12345"
            result.raiderId shouldBe "42"
            result.guildId shouldBe "guild-123"
            result.tier shouldBe LootTier.MYTHIC
            result.flpsScore shouldBe 0.85
            commandSlot.captured.itemId.value shouldBe 12345L
            commandSlot.captured.raiderId.value shouldBe 42L
        }

        @Test
        fun `should propagate exception when raider has active ban`() {
            // Arrange
            val input =
                AwardLootInput(
                    itemId = "12345",
                    raiderId = "42",
                    guildId = "guild-123",
                    flpsScore = 0.85,
                    tier = "MYTHIC",
                )
            every { awardLootUseCase.execute(any()) } returns
                Result.failure(LootBanActiveException(RaiderId(42L), emptyList()))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<LootBanActiveException> {
                    resolver.awardLoot(input)
                }
            exception.raiderId.value shouldBe 42L
        }

        @Test
        fun `should handle all tier types`() {
            // Arrange
            val tiers = listOf("MYTHIC", "HEROIC", "NORMAL", "LFR")
            val expectedTiers = listOf(LootTier.MYTHIC, LootTier.HEROIC, LootTier.NORMAL, LootTier.LFR)

            tiers.forEachIndexed { index, tierString ->
                val input =
                    AwardLootInput(
                        itemId = "12345",
                        raiderId = "42",
                        guildId = "guild-123",
                        flpsScore = 0.85,
                        tier = tierString,
                    )
                val award = createTestLootAward(tier = expectedTiers[index])
                every { awardLootUseCase.execute(any()) } returns Result.success(award)

                // Act
                val result = resolver.awardLoot(input)

                // Assert
                result.tier shouldBe expectedTiers[index]
            }
        }

        @Test
        fun `should include awarded timestamp in result`() {
            // Arrange
            val awardedAt = Instant.parse("2026-01-14T10:30:00Z")
            val input =
                AwardLootInput(
                    itemId = "12345",
                    raiderId = "42",
                    guildId = "guild-123",
                    flpsScore = 0.85,
                    tier = "MYTHIC",
                )
            val award = createTestLootAward(awardedAt = awardedAt)
            every { awardLootUseCase.execute(any()) } returns Result.success(award)

            // Act
            val result = resolver.awardLoot(input)

            // Assert
            result.awardedAt shouldBe awardedAt
        }
    }

    @Nested
    inner class RevokeLootMutation {
        @Test
        fun `should revoke loot award successfully`() {
            // Arrange
            val commandSlot = slot<RevokeLootAwardCommand>()
            every { revokeLootAwardUseCase.execute(capture(commandSlot)) } returns Result.success(Unit)

            // Act
            val result = resolver.revokeLootAward(awardId = "award-123")

            // Assert
            result shouldBe true
            commandSlot.captured.awardId shouldBe "award-123"
        }

        @Test
        fun `should return error when award not found`() {
            // Arrange
            every { revokeLootAwardUseCase.execute(any()) } returns
                Result.failure(NoSuchElementException("Loot award not found: award-999"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<NoSuchElementException> {
                    resolver.revokeLootAward(awardId = "award-999")
                }
            exception.message shouldBe "Loot award not found: award-999"
        }

        @Test
        fun `should propagate exception on revoke failure`() {
            // Arrange
            every { revokeLootAwardUseCase.execute(any()) } returns
                Result.failure(RuntimeException("Database error"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.revokeLootAward(awardId = "award-123")
                }
            exception.message shouldBe "Database error"
        }
    }

    // Helper function
    private fun createTestLootAward(
        id: String = "test-award",
        itemId: Long = 12345L,
        raiderId: Long = 42L,
        guildId: String = "guild-123",
        tier: LootTier = LootTier.MYTHIC,
        flpsScore: Double = 0.85,
        awardedAt: Instant = Instant.now(),
    ): LootAward =
        LootAward(
            id = LootAwardId(id),
            itemId = ItemId(itemId),
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            tier = tier,
            flpsScore = FlpsScore.of(flpsScore),
            awardedAt = awardedAt,
        )
}
