package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.SyncOperationDto
import com.edgerush.datasync.api.dto.SyncResultDto
import com.edgerush.datasync.application.integrations.GetSyncStatusUseCase
import com.edgerush.datasync.application.integrations.SyncWarcraftLogsDataUseCase
import com.edgerush.datasync.application.integrations.SyncWoWAuditDataUseCase
import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for managing external integrations
 */
@RestController
@RequestMapping("/api/v1/integrations")
@Tag(name = "Integrations", description = "External data source integration management")
class IntegrationController(
    private val syncWoWAuditDataUseCase: SyncWoWAuditDataUseCase,
    private val syncWarcraftLogsDataUseCase: SyncWarcraftLogsDataUseCase,
    private val getSyncStatusUseCase: GetSyncStatusUseCase
) {

    @PostMapping("/wowaudit/sync")
    @Operation(summary = "Trigger full WoWAudit synchronization")
    fun syncWoWAudit(): ResponseEntity<SyncResultDto> {
        return syncWoWAuditDataUseCase.execute()
            .map { result -> ResponseEntity.ok(SyncResultDto.from(result)) }
            .getOrElse { ex ->
                ResponseEntity.internalServerError()
                    .body(SyncResultDto.error(ex.message ?: "Sync failed"))
            }
    }

    @PostMapping("/wowaudit/sync/roster")
    @Operation(summary = "Sync only WoWAudit roster data")
    fun syncWoWAuditRoster(): ResponseEntity<SyncResultDto> {
        return syncWoWAuditDataUseCase.syncRoster()
            .map { result -> ResponseEntity.ok(SyncResultDto.from(result)) }
            .getOrElse { ex ->
                ResponseEntity.internalServerError()
                    .body(SyncResultDto.error(ex.message ?: "Roster sync failed"))
            }
    }

    @PostMapping("/wowaudit/sync/loot")
    @Operation(summary = "Sync only WoWAudit loot history")
    fun syncWoWAuditLoot(): ResponseEntity<SyncResultDto> {
        return syncWoWAuditDataUseCase.syncLootHistory()
            .map { result -> ResponseEntity.ok(SyncResultDto.from(result)) }
            .getOrElse { ex ->
                ResponseEntity.internalServerError()
                    .body(SyncResultDto.error(ex.message ?: "Loot sync failed"))
            }
    }

    @PostMapping("/warcraftlogs/sync/{guildId}")
    @Operation(summary = "Trigger Warcraft Logs synchronization for a guild")
    fun syncWarcraftLogs(@PathVariable guildId: String): ResponseEntity<SyncResultDto> {
        return syncWarcraftLogsDataUseCase.execute(guildId)
            .map { result -> ResponseEntity.ok(SyncResultDto.from(result)) }
            .getOrElse { ex ->
                ResponseEntity.internalServerError()
                    .body(SyncResultDto.error(ex.message ?: "Warcraft Logs sync failed"))
            }
    }

    @PostMapping("/warcraftlogs/sync/all")
    @Operation(summary = "Trigger Warcraft Logs synchronization for all guilds")
    fun syncAllWarcraftLogs(@RequestBody guildIds: List<String>): ResponseEntity<SyncResultDto> {
        return syncWarcraftLogsDataUseCase.syncAllGuilds(guildIds)
            .map { result -> ResponseEntity.ok(SyncResultDto.from(result)) }
            .getOrElse { ex ->
                ResponseEntity.internalServerError()
                    .body(SyncResultDto.error(ex.message ?: "Warcraft Logs sync failed"))
            }
    }

    @GetMapping("/status/{source}")
    @Operation(summary = "Get latest sync status for a data source")
    fun getSyncStatus(@PathVariable source: String): ResponseEntity<SyncOperationDto> {
        val dataSource = try {
            ExternalDataSource.valueOf(source.uppercase())
        } catch (ex: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        val operation = getSyncStatusUseCase.getLatestForSource(dataSource)
        return if (operation != null) {
            ResponseEntity.ok(SyncOperationDto.from(operation))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/status/{source}/recent")
    @Operation(summary = "Get recent sync operations for a data source")
    fun getRecentSyncOperations(
        @PathVariable source: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<SyncOperationDto>> {
        val dataSource = try {
            ExternalDataSource.valueOf(source.uppercase())
        } catch (ex: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        val operations = getSyncStatusUseCase.getRecentForSource(dataSource, limit)
        return ResponseEntity.ok(operations.map { SyncOperationDto.from(it) })
    }

    @GetMapping("/status/all")
    @Operation(summary = "Get all recent sync operations")
    fun getAllRecentSyncOperations(
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<List<SyncOperationDto>> {
        val operations = getSyncStatusUseCase.getAllRecent(limit)
        return ResponseEntity.ok(operations.map { SyncOperationDto.from(it) })
    }

    @GetMapping("/status/{source}/in-progress")
    @Operation(summary = "Check if a sync is currently in progress")
    fun isSyncInProgress(@PathVariable source: String): ResponseEntity<Map<String, Boolean>> {
        val dataSource = try {
            ExternalDataSource.valueOf(source.uppercase())
        } catch (ex: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }

        val inProgress = getSyncStatusUseCase.isSyncInProgress(dataSource)
        return ResponseEntity.ok(mapOf("inProgress" to inProgress))
    }
}
