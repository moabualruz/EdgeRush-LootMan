package com.edgerush.lootman.api.guild

import com.edgerush.lootman.application.guild.GuildRosterSyncResult
import com.edgerush.lootman.application.guild.GuildRosterSyncService
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

/**
 * REST controller for guild sync operations.
 *
 * Provides endpoints to configure and trigger Battle.net and WoWAudit sync per guild.
 * Access should be restricted to users with SETTINGS_ACCESS permission on the guild.
 */
@RestController
@RequestMapping("/api/v1/guilds/{guildId}/sync")
@Tag(name = "Guild Sync", description = "Guild synchronization configuration and triggering")
class GuildSyncController(
    private val guildConfigurationRepository: GuildConfigurationRepository,
    private val guildRosterSyncService: GuildRosterSyncService,
) {
    private val logger = LoggerFactory.getLogger(GuildSyncController::class.java)

    @GetMapping("/config")
    @Operation(summary = "Get sync configuration for a guild")
    fun getSyncConfig(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): GuildSyncConfigResponse {
        val config = guildConfigurationRepository.findByGuildId(guildId)
            ?: throw GuildNotFoundException(guildId)

        return GuildSyncConfigResponse(
            guildId = config.guildId,
            guildName = config.guildName,
            // WoWAudit config
            wowauditGuildUri = config.wowauditGuildUri,
            wowauditBaseUrl = config.wowauditBaseUrl,
            wowauditApiKeyConfigured = !config.wowauditApiKeyEncrypted.isNullOrBlank(),
            syncEnabled = config.syncEnabled,
            lastSyncAt = config.lastSyncAt,
            lastSyncStatus = config.lastSyncStatus,
            lastSyncError = config.lastSyncError,
            // Battle.net config
            bnetRealmSlug = config.bnetRealmSlug,
            bnetGuildNameSlug = config.bnetGuildNameSlug,
            bnetRegion = config.bnetRegion,
            bnetSyncEnabled = config.bnetSyncEnabled,
            bnetLastSyncAt = config.bnetLastSyncAt,
            bnetLastSyncStatus = config.bnetLastSyncStatus,
            bnetLastSyncError = config.bnetLastSyncError,
        )
    }

    @PutMapping("/config")
    @Operation(summary = "Update sync configuration for a guild")
    fun updateSyncConfig(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Valid @RequestBody request: UpdateGuildSyncConfigRequest,
    ): GuildSyncConfigResponse {
        val existing = guildConfigurationRepository.findByGuildId(guildId)
            ?: throw GuildNotFoundException(guildId)

        val updated = existing.copy(
            wowauditGuildUri = request.wowauditGuildUri ?: existing.wowauditGuildUri,
            wowauditApiKeyEncrypted = request.wowauditApiKey ?: existing.wowauditApiKeyEncrypted,
            syncEnabled = request.syncEnabled ?: existing.syncEnabled,
            bnetRealmSlug = request.bnetRealmSlug ?: existing.bnetRealmSlug,
            bnetGuildNameSlug = request.bnetGuildNameSlug ?: existing.bnetGuildNameSlug,
            bnetRegion = request.bnetRegion ?: existing.bnetRegion,
            bnetSyncEnabled = request.bnetSyncEnabled ?: existing.bnetSyncEnabled,
            updatedAt = OffsetDateTime.now(),
        )

        val saved = guildConfigurationRepository.save(updated)
        logger.info("Updated sync config for guild $guildId")

        return GuildSyncConfigResponse(
            guildId = saved.guildId,
            guildName = saved.guildName,
            wowauditGuildUri = saved.wowauditGuildUri,
            wowauditBaseUrl = saved.wowauditBaseUrl,
            wowauditApiKeyConfigured = !saved.wowauditApiKeyEncrypted.isNullOrBlank(),
            syncEnabled = saved.syncEnabled,
            lastSyncAt = saved.lastSyncAt,
            lastSyncStatus = saved.lastSyncStatus,
            lastSyncError = saved.lastSyncError,
            bnetRealmSlug = saved.bnetRealmSlug,
            bnetGuildNameSlug = saved.bnetGuildNameSlug,
            bnetRegion = saved.bnetRegion,
            bnetSyncEnabled = saved.bnetSyncEnabled,
            bnetLastSyncAt = saved.bnetLastSyncAt,
            bnetLastSyncStatus = saved.bnetLastSyncStatus,
            bnetLastSyncError = saved.bnetLastSyncError,
        )
    }

    @PostMapping("/bnet/trigger")
    @Operation(summary = "Trigger Battle.net guild roster sync")
    fun triggerBnetSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): ResponseEntity<GuildSyncTriggerResponse> {
        val config = guildConfigurationRepository.findByGuildId(guildId)
            ?: throw GuildNotFoundException(guildId)

        if (!config.bnetSyncEnabled) {
            return ResponseEntity.badRequest().body(
                GuildSyncTriggerResponse(
                    success = false,
                    message = "Battle.net sync is disabled for this guild",
                )
            )
        }

        val realmSlug = config.bnetRealmSlug
        val guildNameSlug = config.bnetGuildNameSlug
        val region = config.bnetRegion

        if (realmSlug.isNullOrBlank() || guildNameSlug.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(
                GuildSyncTriggerResponse(
                    success = false,
                    message = "Battle.net realm and guild name must be configured before syncing",
                )
            )
        }

        // Update status to IN_PROGRESS
        guildConfigurationRepository.save(
            config.copy(
                bnetLastSyncStatus = "IN_PROGRESS",
                bnetLastSyncError = null,
                updatedAt = OffsetDateTime.now(),
            )
        )

        return try {
            val result = guildRosterSyncService.syncGuildRoster(
                realmSlug = realmSlug,
                guildNameSlug = guildNameSlug,
                region = region,
                guildId = guildId,
            )

            // Update status to SUCCESS
            guildConfigurationRepository.save(
                config.copy(
                    bnetLastSyncAt = OffsetDateTime.now(),
                    bnetLastSyncStatus = "SUCCESS",
                    bnetLastSyncError = null,
                    updatedAt = OffsetDateTime.now(),
                )
            )

            logger.info("Battle.net sync completed for guild $guildId: $result")

            ResponseEntity.ok(
                GuildSyncTriggerResponse(
                    success = true,
                    message = "Synced ${result.total} members (created: ${result.created}, updated: ${result.updated}, skipped: ${result.skipped})",
                    result = result,
                )
            )
        } catch (e: Exception) {
            logger.error("Battle.net sync failed for guild $guildId", e)

            // Update status to FAILED
            guildConfigurationRepository.save(
                config.copy(
                    bnetLastSyncStatus = "FAILED",
                    bnetLastSyncError = e.message ?: "Unknown error",
                    updatedAt = OffsetDateTime.now(),
                )
            )

            ResponseEntity.internalServerError().body(
                GuildSyncTriggerResponse(
                    success = false,
                    message = "Sync failed: ${e.message}",
                )
            )
        }
    }
}

// DTOs

data class GuildSyncConfigResponse(
    val guildId: String,
    val guildName: String,
    // WoWAudit
    val wowauditGuildUri: String?,
    val wowauditBaseUrl: String,
    val wowauditApiKeyConfigured: Boolean,
    val syncEnabled: Boolean,
    val lastSyncAt: OffsetDateTime?,
    val lastSyncStatus: String?,
    val lastSyncError: String?,
    // Battle.net
    val bnetRealmSlug: String?,
    val bnetGuildNameSlug: String?,
    val bnetRegion: String,
    val bnetSyncEnabled: Boolean,
    val bnetLastSyncAt: OffsetDateTime?,
    val bnetLastSyncStatus: String?,
    val bnetLastSyncError: String?,
)

data class UpdateGuildSyncConfigRequest(
    val wowauditGuildUri: String? = null,
    val wowauditApiKey: String? = null,
    val syncEnabled: Boolean? = null,
    val bnetRealmSlug: String? = null,
    val bnetGuildNameSlug: String? = null,
    val bnetRegion: String? = null,
    val bnetSyncEnabled: Boolean? = null,
)

data class GuildSyncTriggerResponse(
    val success: Boolean,
    val message: String,
    val result: GuildRosterSyncResult? = null,
)

class GuildNotFoundException(guildId: String) : RuntimeException("Guild not found: $guildId")
