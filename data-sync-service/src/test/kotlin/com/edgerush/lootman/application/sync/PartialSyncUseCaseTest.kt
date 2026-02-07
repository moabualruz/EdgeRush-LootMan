package com.edgerush.lootman.application.sync

import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.api.webhook.PartialSyncCommand
import com.edgerush.lootman.application.guild.WoWAuditRosterSyncService
import com.edgerush.lootman.application.guild.WoWAuditSyncResult
import com.edgerush.lootman.domain.sync.repository.SyncRunRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * Unit tests for PartialSyncUseCase.
 *
 * Tests the partial sync logic triggered by webhook events.
 */
class PartialSyncUseCaseTest : UnitTest() {
    private lateinit var wowAuditRosterSyncService: WoWAuditRosterSyncService
    private lateinit var syncRunRepository: SyncRunRepository
    private lateinit var useCase: PartialSyncUseCase

    @BeforeEach
    fun setup() {
        wowAuditRosterSyncService = mockk()
        syncRunRepository = mockk()
        useCase = PartialSyncUseCase(wowAuditRosterSyncService, syncRunRepository)
    }

    @Nested
    inner class ExecuteTests {
        @Test
        fun `should execute partial sync successfully when guildId is provided`() {
            // Given
            val command =
                PartialSyncCommand(
                    characterName = "TestChar",
                    characterRealm = "TestRealm",
                    guildId = "test-guild",
                    eventType = "character.updated",
                )

            val savedSyncRun =
                SyncRunEntity(
                    id = 123L,
                    source = "wowaudit-webhook-character.updated",
                    status = "RUNNING",
                    startedAt = OffsetDateTime.now(),
                    completedAt = null,
                    message = "Syncing TestChar-TestRealm",
                )

            val syncResult = WoWAuditSyncResult(created = 0, updated = 1, skipped = 0, error = null)

            val entitySlot = slot<SyncRunEntity>()
            every { syncRunRepository.save(capture(entitySlot)) } answers {
                if (entitySlot.captured.id == null) savedSyncRun else entitySlot.captured
            }
            every { wowAuditRosterSyncService.syncRoster("test-guild") } returns Mono.just(syncResult)

            // When
            val result = useCase.execute(command)

            // Then
            result.success shouldBe true
            result.characterName shouldBe "TestChar"
            result.syncRunId shouldBe 123L

            verify(exactly = 1) { wowAuditRosterSyncService.syncRoster("test-guild") }
            verify(exactly = 2) { syncRunRepository.save(any()) }
        }

        @Test
        fun `should return failure when guildId is not provided`() {
            // Given
            val command =
                PartialSyncCommand(
                    characterName = "TestChar",
                    characterRealm = "TestRealm",
                    guildId = null,
                    eventType = "character.updated",
                )

            val savedSyncRun =
                SyncRunEntity(
                    id = 456L,
                    source = "wowaudit-webhook-character.updated",
                    status = "RUNNING",
                    startedAt = OffsetDateTime.now(),
                    completedAt = null,
                    message = "Syncing TestChar-TestRealm",
                )

            val entitySlot = slot<SyncRunEntity>()
            every { syncRunRepository.save(capture(entitySlot)) } answers {
                if (entitySlot.captured.id == null) savedSyncRun else entitySlot.captured
            }

            // When
            val result = useCase.execute(command)

            // Then
            result.success shouldBe false
            result.characterName shouldBe "TestChar"
            result.message shouldBe "No guildId provided in webhook payload"

            verify(exactly = 0) { wowAuditRosterSyncService.syncRoster(any()) }
        }

        @Test
        fun `should handle sync service exception gracefully`() {
            // Given
            val command =
                PartialSyncCommand(
                    characterName = "TestChar",
                    characterRealm = "TestRealm",
                    guildId = "test-guild",
                    eventType = "character.updated",
                )

            val savedSyncRun =
                SyncRunEntity(
                    id = 789L,
                    source = "wowaudit-webhook-character.updated",
                    status = "RUNNING",
                    startedAt = OffsetDateTime.now(),
                    completedAt = null,
                    message = "Syncing TestChar-TestRealm",
                )

            val entitySlot = slot<SyncRunEntity>()
            every { syncRunRepository.save(capture(entitySlot)) } answers {
                if (entitySlot.captured.id == null) savedSyncRun else entitySlot.captured
            }
            every { wowAuditRosterSyncService.syncRoster("test-guild") } returns Mono.error(RuntimeException("API error"))

            // When
            val result = useCase.execute(command)

            // Then
            result.success shouldBe false
            result.characterName shouldBe "TestChar"
            result.message shouldBe "API error"
        }
    }
}
