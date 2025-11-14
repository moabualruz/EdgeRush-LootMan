package com.edgerush.datasync.application.integrations

import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import com.edgerush.datasync.domain.integrations.model.SyncResult
import com.edgerush.datasync.domain.integrations.service.SyncOrchestrationService
import com.edgerush.datasync.service.WoWAuditSyncService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Use case for synchronizing WoWAudit data
 */
@Service
class SyncWoWAuditDataUseCase(
    private val syncOrchestrationService: SyncOrchestrationService,
    private val wowAuditSyncService: WoWAuditSyncService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Execute full WoWAudit synchronization
     */
    fun execute(): Result<SyncResult> = runCatching {
        val startTime = Instant.now()
        val operation = syncOrchestrationService.startSync(
            ExternalDataSource.WOWAUDIT,
            "full-sync"
        )

        try {
            // Execute all sync operations
            wowAuditSyncService.syncRoster()
                .then(wowAuditSyncService.syncLootHistory())
                .then(wowAuditSyncService.syncWishlists())
                .then(wowAuditSyncService.syncSupplementalData())
                .block()

            val result = syncOrchestrationService.createSuccessResult(
                recordsProcessed = 0, // Will be updated by individual sync operations
                startedAt = startTime,
                message = "WoWAudit full sync completed successfully"
            )

            syncOrchestrationService.completeSync(operation, result)
            result
        } catch (ex: Exception) {
            logger.error("WoWAudit sync failed", ex)
            val result = syncOrchestrationService.createFailureResult(
                startedAt = startTime,
                message = ex.message ?: "Unknown error during WoWAudit sync",
                errors = listOf(ex.toString())
            )
            syncOrchestrationService.failSync(operation, result.message!!, result.errors)
            throw ex
        }
    }

    /**
     * Sync only roster data
     */
    fun syncRoster(): Result<SyncResult> = runCatching {
        val startTime = Instant.now()
        val operation = syncOrchestrationService.startSync(
            ExternalDataSource.WOWAUDIT,
            "roster-sync"
        )

        try {
            wowAuditSyncService.syncRoster().block()

            val result = syncOrchestrationService.createSuccessResult(
                recordsProcessed = 0,
                startedAt = startTime,
                message = "WoWAudit roster sync completed"
            )

            syncOrchestrationService.completeSync(operation, result)
            result
        } catch (ex: Exception) {
            logger.error("WoWAudit roster sync failed", ex)
            val result = syncOrchestrationService.createFailureResult(
                startedAt = startTime,
                message = ex.message ?: "Unknown error during roster sync",
                errors = listOf(ex.toString())
            )
            syncOrchestrationService.failSync(operation, result.message!!, result.errors)
            throw ex
        }
    }

    /**
     * Sync only loot history
     */
    fun syncLootHistory(): Result<SyncResult> = runCatching {
        val startTime = Instant.now()
        val operation = syncOrchestrationService.startSync(
            ExternalDataSource.WOWAUDIT,
            "loot-sync"
        )

        try {
            wowAuditSyncService.syncLootHistory().block()

            val result = syncOrchestrationService.createSuccessResult(
                recordsProcessed = 0,
                startedAt = startTime,
                message = "WoWAudit loot history sync completed"
            )

            syncOrchestrationService.completeSync(operation, result)
            result
        } catch (ex: Exception) {
            logger.error("WoWAudit loot sync failed", ex)
            val result = syncOrchestrationService.createFailureResult(
                startedAt = startTime,
                message = ex.message ?: "Unknown error during loot sync",
                errors = listOf(ex.toString())
            )
            syncOrchestrationService.failSync(operation, result.message!!, result.errors)
            throw ex
        }
    }
}
