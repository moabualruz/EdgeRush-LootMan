package com.edgerush.lootman.api.graphql.subscription

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for FlpsSubscriptionResolver.
 *
 * Tests the GraphQL subscription resolver for FLPS events following TDD principles.
 */
class FlpsSubscriptionResolverTest : UnitTest() {
    private val flpsEventPublisher = FlpsEventPublisher()
    private val resolver = FlpsSubscriptionResolver(flpsEventPublisher)

    @Nested
    inner class FlpsScoreUpdatedSubscription {
        @Test
        fun `should receive flps score updated events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val event =
                FlpsScoreUpdatedEvent(
                    guildId = guildId,
                    raiderId = "raider-42",
                    itemId = 12345L,
                    oldScore = 0.75,
                    newScore = 0.85,
                    updatedAt = Instant.now(),
                )

            // Act
            val subscription = resolver.flpsScoreUpdated(guildId)
            flpsEventPublisher.publishScoreUpdated(event)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.raiderId shouldBe "raider-42"
                    received.oldScore shouldBe 0.75
                    received.newScore shouldBe 0.85
                }
                .verifyComplete()
        }

        @Test
        fun `should filter events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.flpsScoreUpdated(targetGuildId)

            flpsEventPublisher.publishScoreUpdated(
                guildId = otherGuildId,
                raiderId = "raider-1",
                itemId = 123L,
                oldScore = null,
                newScore = 0.5,
            )
            flpsEventPublisher.publishScoreUpdated(
                guildId = targetGuildId,
                raiderId = "raider-2",
                itemId = 456L,
                oldScore = 0.6,
                newScore = 0.8,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.raiderId shouldBe "raider-2"
                }
                .verifyComplete()
        }

        @Test
        fun `should receive multiple events`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.flpsScoreUpdated(guildId)

            repeat(3) { i ->
                flpsEventPublisher.publishScoreUpdated(
                    guildId = guildId,
                    raiderId = "raider-$i",
                    itemId = i.toLong(),
                    oldScore = null,
                    newScore = 0.5 + (i * 0.1),
                )
            }

            // Assert
            StepVerifier.create(subscription.take(3))
                .assertNext { it.raiderId shouldBe "raider-0" }
                .assertNext { it.raiderId shouldBe "raider-1" }
                .assertNext { it.raiderId shouldBe "raider-2" }
                .verifyComplete()
        }

        @Test
        fun `should handle null old score for first calculation`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.flpsScoreUpdated(guildId)

            flpsEventPublisher.publishScoreUpdated(
                guildId = guildId,
                raiderId = "new-raider",
                itemId = 999L,
                oldScore = null,
                newScore = 0.65,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.oldScore shouldBe null
                    received.newScore shouldBe 0.65
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class RdfExpiredSubscription {
        @Test
        fun `should receive rdf expired events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val event =
                RdfExpiredEvent(
                    guildId = guildId,
                    raiderId = "raider-42",
                    itemId = 12345L,
                    expiredRdf = 0.15,
                    expiredAt = Instant.now(),
                )

            // Act
            val subscription = resolver.rdfExpired(guildId)
            flpsEventPublisher.publishRdfExpired(event)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.raiderId shouldBe "raider-42"
                    received.expiredRdf shouldBe 0.15
                }
                .verifyComplete()
        }

        @Test
        fun `should filter rdf events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.rdfExpired(targetGuildId)

            flpsEventPublisher.publishRdfExpired(
                guildId = otherGuildId,
                raiderId = "raider-1",
                itemId = 123L,
                expiredRdf = 0.1,
            )
            flpsEventPublisher.publishRdfExpired(
                guildId = targetGuildId,
                raiderId = "raider-2",
                itemId = 456L,
                expiredRdf = 0.2,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.raiderId shouldBe "raider-2"
                }
                .verifyComplete()
        }

        @Test
        fun `should include timestamp for rdf expiry`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.rdfExpired(guildId)

            flpsEventPublisher.publishRdfExpired(
                guildId = guildId,
                raiderId = "raider-1",
                itemId = 123L,
                expiredRdf = 0.25,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.expiredAt shouldNotBe null
                }
                .verifyComplete()
        }
    }
}
