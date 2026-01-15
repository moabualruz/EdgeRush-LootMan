package com.edgerush.lootman.api.graphql.subscription

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for PenaltySubscriptionResolver.
 *
 * Tests the GraphQL subscription resolver for penalty events following TDD principles.
 */
class PenaltySubscriptionResolverTest : UnitTest() {
    private val penaltyEventPublisher = PenaltyEventPublisher()
    private val resolver = PenaltySubscriptionResolver(penaltyEventPublisher)

    @Nested
    inner class PenaltyAppliedSubscription {
        @Test
        fun `should receive penalty applied events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val event =
                PenaltyAppliedEvent(
                    guildId = guildId,
                    raiderId = "raider-42",
                    penaltyId = "penalty-1",
                    penaltyType = "LATE_ARRIVAL",
                    reason = "Arrived 15 minutes late to raid",
                    points = 5,
                    appliedAt = Instant.now(),
                )

            // Act
            val subscription = resolver.penaltyApplied(guildId)
            penaltyEventPublisher.publishPenaltyApplied(event)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.raiderId shouldBe "raider-42"
                    received.penaltyType shouldBe "LATE_ARRIVAL"
                    received.points shouldBe 5
                }
                .verifyComplete()
        }

        @Test
        fun `should filter events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.penaltyApplied(targetGuildId)

            penaltyEventPublisher.publishPenaltyApplied(
                guildId = otherGuildId,
                raiderId = "raider-1",
                penaltyId = "penalty-other",
                penaltyType = "AFK",
                reason = "AFK during raid",
                points = 3,
            )
            penaltyEventPublisher.publishPenaltyApplied(
                guildId = targetGuildId,
                raiderId = "raider-2",
                penaltyId = "penalty-target",
                penaltyType = "DIED_TO_MECHANICS",
                reason = "Stood in fire",
                points = 2,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.penaltyId shouldBe "penalty-target"
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class PenaltyRemovedSubscription {
        @Test
        fun `should receive penalty removed events for guild`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.penaltyRemoved(guildId)

            penaltyEventPublisher.publishPenaltyRemoved(
                guildId = guildId,
                raiderId = "raider-42",
                penaltyId = "penalty-1",
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.penaltyId shouldBe "penalty-1"
                    received.removedAt shouldNotBe null
                }
                .verifyComplete()
        }

        @Test
        fun `should filter removed events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.penaltyRemoved(targetGuildId)

            penaltyEventPublisher.publishPenaltyRemoved(otherGuildId, "raider-1", "other-penalty")
            penaltyEventPublisher.publishPenaltyRemoved(targetGuildId, "raider-2", "target-penalty")

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.penaltyId shouldBe "target-penalty"
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class LootBanAppliedSubscription {
        @Test
        fun `should receive loot ban applied events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val expiresAt = Instant.now().plusSeconds(604800) // 1 week

            // Act
            val subscription = resolver.lootBanApplied(guildId)

            penaltyEventPublisher.publishLootBanApplied(
                guildId = guildId,
                raiderId = "raider-42",
                banId = "ban-1",
                reason = "Excessive penalty points accumulated",
                expiresAt = expiresAt,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.raiderId shouldBe "raider-42"
                    received.reason shouldBe "Excessive penalty points accumulated"
                    received.expiresAt shouldBe expiresAt
                }
                .verifyComplete()
        }

        @Test
        fun `should handle permanent ban with null expiry`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.lootBanApplied(guildId)

            penaltyEventPublisher.publishLootBanApplied(
                guildId = guildId,
                raiderId = "raider-42",
                banId = "ban-1",
                reason = "Repeated violations",
                expiresAt = null,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.expiresAt shouldBe null
                }
                .verifyComplete()
        }

        @Test
        fun `should filter loot ban events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.lootBanApplied(targetGuildId)

            penaltyEventPublisher.publishLootBanApplied(
                guildId = otherGuildId,
                raiderId = "raider-1",
                banId = "other-ban",
                reason = "Other ban",
                expiresAt = null,
            )
            penaltyEventPublisher.publishLootBanApplied(
                guildId = targetGuildId,
                raiderId = "raider-2",
                banId = "target-ban",
                reason = "Target ban",
                expiresAt = null,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.banId shouldBe "target-ban"
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class LootBanLiftedSubscription {
        @Test
        fun `should receive loot ban lifted events for guild`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.lootBanLifted(guildId)

            penaltyEventPublisher.publishLootBanLifted(
                guildId = guildId,
                raiderId = "raider-42",
                banId = "ban-1",
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.banId shouldBe "ban-1"
                    received.liftedAt shouldNotBe null
                }
                .verifyComplete()
        }

        @Test
        fun `should filter lifted events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.lootBanLifted(targetGuildId)

            penaltyEventPublisher.publishLootBanLifted(otherGuildId, "raider-1", "other-ban")
            penaltyEventPublisher.publishLootBanLifted(targetGuildId, "raider-2", "target-ban")

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.banId shouldBe "target-ban"
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class MultipleEventsFlow {
        @Test
        fun `should receive multiple penalty events in order`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.penaltyApplied(guildId)

            repeat(3) { i ->
                penaltyEventPublisher.publishPenaltyApplied(
                    guildId = guildId,
                    raiderId = "raider-$i",
                    penaltyId = "penalty-$i",
                    penaltyType = "TYPE_$i",
                    reason = "Reason $i",
                    points = i + 1,
                )
            }

            // Assert
            StepVerifier.create(subscription.take(3))
                .assertNext { it.penaltyId shouldBe "penalty-0" }
                .assertNext { it.penaltyId shouldBe "penalty-1" }
                .assertNext { it.penaltyId shouldBe "penalty-2" }
                .verifyComplete()
        }
    }
}
