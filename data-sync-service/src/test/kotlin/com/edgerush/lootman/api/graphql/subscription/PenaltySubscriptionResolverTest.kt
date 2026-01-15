package com.edgerush.lootman.api.graphql.subscription

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
        fun `should receive penalty applied events for guild`() =
            runBlocking {
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
                val received = subscription.first()
                received.guildId shouldBe guildId
                received.raiderId shouldBe "raider-42"
                received.penaltyType shouldBe "LATE_ARRIVAL"
                received.points shouldBe 5
            }

        @Test
        fun `should filter events by guild id`() =
            runBlocking {
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
                val received = subscription.first()
                received.guildId shouldBe targetGuildId
                received.penaltyId shouldBe "penalty-target"
            }
    }

    @Nested
    inner class PenaltyRemovedSubscription {
        @Test
        fun `should receive penalty removed events for guild`() =
            runBlocking {
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
                val received = subscription.first()
                received.guildId shouldBe guildId
                received.penaltyId shouldBe "penalty-1"
                received.removedAt shouldNotBe null
            }

        @Test
        fun `should filter removed events by guild id`() =
            runBlocking {
                // Arrange
                val targetGuildId = "guild-123"
                val otherGuildId = "guild-456"

                // Act
                val subscription = resolver.penaltyRemoved(targetGuildId)

                penaltyEventPublisher.publishPenaltyRemoved(otherGuildId, "raider-1", "other-penalty")
                penaltyEventPublisher.publishPenaltyRemoved(targetGuildId, "raider-2", "target-penalty")

                // Assert
                val received = subscription.first()
                received.guildId shouldBe targetGuildId
                received.penaltyId shouldBe "target-penalty"
            }
    }

    @Nested
    inner class LootBanAppliedSubscription {
        @Test
        fun `should receive loot ban applied events for guild`() =
            runBlocking {
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
                val received = subscription.first()
                received.guildId shouldBe guildId
                received.raiderId shouldBe "raider-42"
                received.reason shouldBe "Excessive penalty points accumulated"
                received.expiresAt shouldBe expiresAt
            }

        @Test
        fun `should handle permanent ban with null expiry`() =
            runBlocking {
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
                val received = subscription.first()
                received.expiresAt shouldBe null
            }

        @Test
        fun `should filter loot ban events by guild id`() =
            runBlocking {
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
                val received = subscription.first()
                received.banId shouldBe "target-ban"
            }
    }

    @Nested
    inner class LootBanLiftedSubscription {
        @Test
        fun `should receive loot ban lifted events for guild`() =
            runBlocking {
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
                val received = subscription.first()
                received.guildId shouldBe guildId
                received.banId shouldBe "ban-1"
                received.liftedAt shouldNotBe null
            }

        @Test
        fun `should filter lifted events by guild id`() =
            runBlocking {
                // Arrange
                val targetGuildId = "guild-123"
                val otherGuildId = "guild-456"

                // Act
                val subscription = resolver.lootBanLifted(targetGuildId)

                penaltyEventPublisher.publishLootBanLifted(otherGuildId, "raider-1", "other-ban")
                penaltyEventPublisher.publishLootBanLifted(targetGuildId, "raider-2", "target-ban")

                // Assert
                val received = subscription.first()
                received.guildId shouldBe targetGuildId
                received.banId shouldBe "target-ban"
            }
    }

    @Nested
    inner class MultipleEventsFlow {
        @Test
        fun `should receive multiple penalty events in order`() =
            runBlocking {
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
                val events = subscription.take(3).toList()
                events.size shouldBe 3
                events[0].penaltyId shouldBe "penalty-0"
                events[1].penaltyId shouldBe "penalty-1"
                events[2].penaltyId shouldBe "penalty-2"
            }
    }
}
