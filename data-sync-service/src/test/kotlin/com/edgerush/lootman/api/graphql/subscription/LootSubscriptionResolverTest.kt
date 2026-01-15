package com.edgerush.lootman.api.graphql.subscription

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for LootSubscriptionResolver.
 *
 * Tests the GraphQL subscription resolver for loot events following TDD principles.
 */
class LootSubscriptionResolverTest : UnitTest() {
    private val lootEventPublisher = LootEventPublisher()
    private val resolver = LootSubscriptionResolver(lootEventPublisher)

    @Nested
    inner class LootAwardedSubscription {
        @Test
        fun `should receive loot awarded events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val award = createTestLootAward(guildId = guildId)

            // Act - Start subscription and publish event
            val subscription = resolver.lootAwarded(guildId)

            // Publish in separate context to simulate async event
            lootEventPublisher.publishLootAwarded(award)

            // Assert - Get first event
            StepVerifier.create(subscription.take(1))
                .assertNext { event ->
                    event.guildId shouldBe guildId
                    event.raiderId shouldBe "42"
                    event.itemId shouldBe "12345"
                    event.tier shouldBe LootTier.MYTHIC
                }
                .verifyComplete()
        }

        @Test
        fun `should filter events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            val targetAward = createTestLootAward(id = "award-1", guildId = targetGuildId)
            val otherAward = createTestLootAward(id = "award-2", guildId = otherGuildId)

            // Act
            val subscription = resolver.lootAwarded(targetGuildId)

            // Publish events for both guilds
            lootEventPublisher.publishLootAwarded(otherAward)
            lootEventPublisher.publishLootAwarded(targetAward)

            // Assert - Should only receive event for target guild
            StepVerifier.create(subscription.take(1))
                .assertNext { event ->
                    event.id shouldBe "award-1"
                    event.guildId shouldBe targetGuildId
                }
                .verifyComplete()
        }

        @Test
        fun `should receive multiple events`() {
            // Arrange
            val guildId = "guild-123"
            val awards =
                listOf(
                    createTestLootAward(id = "award-1", guildId = guildId, raiderId = 1L),
                    createTestLootAward(id = "award-2", guildId = guildId, raiderId = 2L),
                    createTestLootAward(id = "award-3", guildId = guildId, raiderId = 3L),
                )

            // Act
            val subscription = resolver.lootAwarded(guildId)

            awards.forEach { lootEventPublisher.publishLootAwarded(it) }

            // Assert
            StepVerifier.create(subscription.take(3))
                .assertNext { it.raiderId shouldBe "1" }
                .assertNext { it.raiderId shouldBe "2" }
                .assertNext { it.raiderId shouldBe "3" }
                .verifyComplete()
        }
    }

    @Nested
    inner class LootRevokedSubscription {
        @Test
        fun `should receive loot revoked events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val awardId = "award-456"

            // Act
            val subscription = resolver.lootRevoked(guildId)

            lootEventPublisher.publishLootRevoked(guildId, awardId)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { event ->
                    event.guildId shouldBe guildId
                    event.awardId shouldBe awardId
                }
                .verifyComplete()
        }

        @Test
        fun `should filter revoked events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.lootRevoked(targetGuildId)

            lootEventPublisher.publishLootRevoked(otherGuildId, "other-award")
            lootEventPublisher.publishLootRevoked(targetGuildId, "target-award")

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { event ->
                    event.guildId shouldBe targetGuildId
                    event.awardId shouldBe "target-award"
                }
                .verifyComplete()
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
