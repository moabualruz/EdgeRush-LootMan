package com.edgerush.lootman.application.sync

import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.lootman.api.webhook.PartialSyncCommand
import com.edgerush.lootman.api.webhook.PartialSyncResult
import com.edgerush.lootman.application.guild.WoWAuditRosterSyncService
import com.edgerush.lootman.domain.sync.repository.SyncRunRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Use case for triggering a partial sync based on webhook events.
 *
 * Instead of syncing the entire roster, this syncs only the affected character.
 * This reduces system load and provides near-real-time updates.
 */
@Service
class PartialSyncUseCase(
    private val wowAuditRosterSyncService: WoWAuditRosterSyncService,
    private val syncRunRepository: SyncRunRepository,
) {
    private val logger = LoggerFactory.getLogger(PartialSyncUseCase::class.java)

    /**
     * Executes a partial sync for a single character.
     *
     * @param command The sync command containing character details
     * @return Result indicating success or failure
     */
    fun execute(command: PartialSyncCommand): PartialSyncResult {
        val syncRun = startSyncRun(command)

        return try {
            logger.info("Starting partial sync for character: {}-{}", command.characterName, command.characterRealm)

            // Use the existing roster sync service with the specific guild
            // This triggers a targeted sync for the character
            if (command.guildId != null) {
                val result =
                    wowAuditRosterSyncService.syncRoster(command.guildId)
                        .block() // Wait for completion

                completeSyncRun(
                    syncRun,
                    "SUCCESS",
                    "Synced ${command.characterName}: ${result?.created} created, ${result?.updated} updated",
                )
                PartialSyncResult.success(command.characterName, syncRun.id!!)
            } else {
                completeSyncRun(syncRun, "SKIPPED", "No guildId provided, cannot sync")
                PartialSyncResult.failure(command.characterName, "No guildId provided in webhook payload")
            }
        } catch (e: Exception) {
            logger.error("Partial sync failed for {}-{}: {}", command.characterName, command.characterRealm, e.message)
            completeSyncRun(syncRun, "FAILED", e.message)
            PartialSyncResult.failure(command.characterName, e.message)
        }
    }

    private fun startSyncRun(command: PartialSyncCommand): SyncRunEntity {
        val now = OffsetDateTime.now()
        return syncRunRepository.save(
            SyncRunEntity(
                id = null,
                source = "wowaudit-webhook-${command.eventType}",
                status = "RUNNING",
                startedAt = now,
                completedAt = null,
                message = "Syncing ${command.characterName}-${command.characterRealm}",
            ),
        )
    }

    private fun completeSyncRun(
        syncRun: SyncRunEntity,
        status: String,
        message: String?,
    ) {
        syncRunRepository.save(
            syncRun.copy(
                status = status,
                completedAt = OffsetDateTime.now(),
                message = message,
            ),
        )
    }
}
