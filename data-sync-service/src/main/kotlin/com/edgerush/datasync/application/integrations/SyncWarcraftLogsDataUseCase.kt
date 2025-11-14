package com.edgerush.datasync.application.integrations

import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import com.edgerush.datasync.domain.integrations.model.SyncResult
import com.edgerush.datasync.domain.integrations.service.SyncOrchestrationService
import com.edgerush.datasync.service.warcraftlogs.WarcraftLogsSyncService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Use case for synchronizing Warcraft Logs data
 */
@Service
class SyncWarcraftLogsDataUseCase(
    private val syncOrchestrationService: SyncOrchestrationService,
    private val warcraftLogsSyncService: WarcraftLogsSyncService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Execute Warcraft Logs synchronization for a guild
     */
    fun execute(guildId: String): Result<SyncResult> = runCatching {
        val startTime = Instant.now()
        val operation = syncOrchestrationService.startSync(
            ExternalDataSource.WARCRAFT_LOGS,
            "guild-sync-$guildId"
        )

        try {
            runBlocking {
                warcraftLogsSyncService.syncReportsForGuild(guildId)
            }

            val result = syncOrchestrationService.createSuccessResult(
                recordsProcessed = 0,
                startedAt = startTime,
                message = "Warcraft Logs sync completed for guild $guildId"
            )

            syncOrchestrationService.completeSync(operation, result)
            result
        } catch (ex: Exception) {
            logger.error("Warcraft Logs sync failed for guild $guildId", ex)
            val result = syncOrchestrationService.createFailureResult(
                startedAt = startTime,
                message = ex.message ?: "Unknown error during Warcraft Logs sync",
                errors = listOf(ex.toString())
            )
            syncOrchestrationService.failSync(operation, result.message!!, result.errors)
            throw ex
        }
    }

    /**
     * Sync performance data for all configured guilds
     * Note: This would need to iterate through all guild configurations
     */
    fun syncAllGuilds(guildIds: List<String>): Result<SyncResult> = runCatching {
        val startTime = Instant.now()
        val operation = syncOrchestrationService.startSync(
            ExternalDataSource.WARCRAFT_LOGS,
            "all-guilds-sync"
        )

        try {
            var totalRecords = 0
            runBlocking {
                guildIds.forEach { guildId ->
                    warcraftLogsSyncService.syncReportsForGuild(guildId)
                    totalRecords++
                }
            }

            val result = syncOrchestrationService.createSuccessResult(
                recordsProcessed = totalRecords,
                startedAt = startTime,
                message = "Warcraft Logs sync completed for ${guildIds.size} guilds"
            )

            syncOrchestrationService.completeSync(operation, result)
            result
        } catch (ex: Exception) {
            logger.error("Warcraft Logs sync failed for all guilds", ex)
            val result = syncOrchestrationService.createFailureResult(
                startedAt = startTime,
                message = ex.message ?: "Unknown error during Warcraft Logs sync",
                errors = listOf(ex.toString())
            )
            syncOrchestrationService.failSync(operation, result.message!!, result.errors)
            throw ex
        }
    }
}
