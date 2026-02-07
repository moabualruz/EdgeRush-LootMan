package com.edgerush.lootman.api.graphql.subscription

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for SyncSubscriptionResolver.
 *
 * Tests the GraphQL subscription resolver for sync events following TDD principles.
 */
class SyncSubscriptionResolverTest : UnitTest() {
    private val syncEventPublisher = SyncEventPublisher()
    private val resolver = SyncSubscriptionResolver(syncEventPublisher)

    @Nested
    inner class SyncCompletedSubscription {
        @Test
        fun `should receive sync completed events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val event =
                SyncCompletedEvent(
                    guildId = guildId,
                    syncType = SyncType.WOWAUDIT_FULL,
                    recordsProcessed = 150,
                    recordsCreated = 25,
                    recordsUpdated = 75,
                    durationMs = 5000L,
                    completedAt = Instant.now(),
                )

            // Act
            val subscription = resolver.syncCompleted(guildId)

            // Publish event after subscription is set up
            syncEventPublisher.publishSyncCompleted(event)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.syncType shouldBe SyncType.WOWAUDIT_FULL
                    received.recordsProcessed shouldBe 150
                    received.recordsCreated shouldBe 25
                    received.recordsUpdated shouldBe 75
                    received.durationMs shouldBe 5000L
                }
                .verifyComplete()
        }

        @Test
        fun `should filter events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.syncCompleted(targetGuildId)

            syncEventPublisher.publishSyncCompleted(
                guildId = otherGuildId,
                syncType = SyncType.CHARACTER,
                recordsProcessed = 10,
                recordsCreated = 5,
                recordsUpdated = 5,
                durationMs = 1000L,
            )
            syncEventPublisher.publishSyncCompleted(
                guildId = targetGuildId,
                syncType = SyncType.ATTENDANCE,
                recordsProcessed = 100,
                recordsCreated = 50,
                recordsUpdated = 50,
                durationMs = 2000L,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.syncType shouldBe SyncType.ATTENDANCE
                }
                .verifyComplete()
        }

        @Test
        fun `should receive events for different sync types`() {
            // Arrange
            val guildId = "guild-123"
            val syncTypes =
                listOf(
                    SyncType.WOWAUDIT_FULL,
                    SyncType.WARCRAFT_LOGS,
                    SyncType.SIMULATION,
                )

            // Act
            val subscription = resolver.syncCompleted(guildId)

            syncTypes.forEachIndexed { i, syncType ->
                syncEventPublisher.publishSyncCompleted(
                    guildId = guildId,
                    syncType = syncType,
                    recordsProcessed = (i + 1) * 10,
                    recordsCreated = (i + 1) * 5,
                    recordsUpdated = (i + 1) * 5,
                    durationMs = (i + 1) * 1000L,
                )
            }

            // Assert
            StepVerifier.create(subscription.take(3))
                .assertNext { it.syncType shouldBe SyncType.WOWAUDIT_FULL }
                .assertNext { it.syncType shouldBe SyncType.WARCRAFT_LOGS }
                .assertNext { it.syncType shouldBe SyncType.SIMULATION }
                .verifyComplete()
        }
    }

    @Nested
    inner class SyncStartedSubscription {
        @Test
        fun `should receive sync started events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val event =
                SyncStartedEvent(
                    guildId = guildId,
                    syncType = SyncType.WOWAUDIT_INCREMENTAL,
                    startedAt = Instant.now(),
                )

            // Act
            val subscription = resolver.syncStarted(guildId)
            syncEventPublisher.publishSyncStarted(event)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.syncType shouldBe SyncType.WOWAUDIT_INCREMENTAL
                    received.startedAt shouldNotBe null
                }
                .verifyComplete()
        }

        @Test
        fun `should filter started events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.syncStarted(targetGuildId)

            syncEventPublisher.publishSyncStarted(otherGuildId, SyncType.CHARACTER)
            syncEventPublisher.publishSyncStarted(targetGuildId, SyncType.LOOT)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.syncType shouldBe SyncType.LOOT
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class SyncFailedSubscription {
        @Test
        fun `should receive sync failed events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val event =
                SyncFailedEvent(
                    guildId = guildId,
                    syncType = SyncType.WARCRAFT_LOGS,
                    errorMessage = "API rate limit exceeded",
                    errorCode = "RATE_LIMIT_EXCEEDED",
                    failedAt = Instant.now(),
                )

            // Act
            val subscription = resolver.syncFailed(guildId)
            syncEventPublisher.publishSyncFailed(event)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.syncType shouldBe SyncType.WARCRAFT_LOGS
                    received.errorMessage shouldBe "API rate limit exceeded"
                    received.errorCode shouldBe "RATE_LIMIT_EXCEEDED"
                }
                .verifyComplete()
        }

        @Test
        fun `should handle null error code`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.syncFailed(guildId)

            syncEventPublisher.publishSyncFailed(
                guildId = guildId,
                syncType = SyncType.SIMULATION,
                errorMessage = "Unknown error occurred",
                errorCode = null,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.errorCode shouldBe null
                    received.errorMessage shouldBe "Unknown error occurred"
                }
                .verifyComplete()
        }

        @Test
        fun `should filter failed events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.syncFailed(targetGuildId)

            syncEventPublisher.publishSyncFailed(
                guildId = otherGuildId,
                syncType = SyncType.CHARACTER,
                errorMessage = "Other error",
                errorCode = null,
            )
            syncEventPublisher.publishSyncFailed(
                guildId = targetGuildId,
                syncType = SyncType.ATTENDANCE,
                errorMessage = "Target error",
                errorCode = "TARGET_ERROR",
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.errorMessage shouldBe "Target error"
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class SyncProgressSubscription {
        @Test
        fun `should receive sync progress events for guild`() {
            // Arrange
            val guildId = "guild-123"
            val event =
                SyncProgressEvent(
                    guildId = guildId,
                    syncType = SyncType.WOWAUDIT_FULL,
                    currentStep = "Processing characters",
                    processedCount = 50,
                    totalCount = 100,
                    percentComplete = 50,
                    timestamp = Instant.now(),
                )

            // Act
            val subscription = resolver.syncProgress(guildId)
            syncEventPublisher.publishSyncProgress(event)

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe guildId
                    received.currentStep shouldBe "Processing characters"
                    received.processedCount shouldBe 50
                    received.totalCount shouldBe 100
                    received.percentComplete shouldBe 50
                }
                .verifyComplete()
        }

        @Test
        fun `should handle unknown total count`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.syncProgress(guildId)

            syncEventPublisher.publishSyncProgress(
                guildId = guildId,
                syncType = SyncType.WARCRAFT_LOGS,
                currentStep = "Fetching logs",
                processedCount = 25,
                totalCount = null,
                percentComplete = null,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.totalCount shouldBe null
                    received.percentComplete shouldBe null
                }
                .verifyComplete()
        }

        @Test
        fun `should filter progress events by guild id`() {
            // Arrange
            val targetGuildId = "guild-123"
            val otherGuildId = "guild-456"

            // Act
            val subscription = resolver.syncProgress(targetGuildId)

            syncEventPublisher.publishSyncProgress(
                guildId = otherGuildId,
                syncType = SyncType.CHARACTER,
                currentStep = "Other step",
                processedCount = 10,
                totalCount = 50,
                percentComplete = 20,
            )
            syncEventPublisher.publishSyncProgress(
                guildId = targetGuildId,
                syncType = SyncType.LOOT,
                currentStep = "Target step",
                processedCount = 75,
                totalCount = 100,
                percentComplete = 75,
            )

            // Assert
            StepVerifier.create(subscription.take(1))
                .assertNext { received ->
                    received.guildId shouldBe targetGuildId
                    received.currentStep shouldBe "Target step"
                }
                .verifyComplete()
        }

        @Test
        fun `should receive multiple progress updates in order`() {
            // Arrange
            val guildId = "guild-123"

            // Act
            val subscription = resolver.syncProgress(guildId)

            listOf(25, 50, 75, 100).forEachIndexed { i, percent ->
                syncEventPublisher.publishSyncProgress(
                    guildId = guildId,
                    syncType = SyncType.WOWAUDIT_FULL,
                    currentStep = "Step ${i + 1}",
                    processedCount = percent,
                    totalCount = 100,
                    percentComplete = percent,
                )
            }

            // Assert
            StepVerifier.create(subscription.take(4))
                .assertNext { it.percentComplete shouldBe 25 }
                .assertNext { it.percentComplete shouldBe 50 }
                .assertNext { it.percentComplete shouldBe 75 }
                .assertNext { it.percentComplete shouldBe 100 }
                .verifyComplete()
        }
    }

    @Nested
    inner class SyncTypeEnum {
        @Test
        fun `should have all expected sync types`() {
            val expectedTypes =
                setOf(
                    SyncType.WOWAUDIT_FULL,
                    SyncType.WOWAUDIT_INCREMENTAL,
                    SyncType.WARCRAFT_LOGS,
                    SyncType.SIMULATION,
                    SyncType.CHARACTER,
                    SyncType.ATTENDANCE,
                    SyncType.LOOT,
                )

            SyncType.entries.toSet() shouldBe expectedTypes
        }
    }
}
