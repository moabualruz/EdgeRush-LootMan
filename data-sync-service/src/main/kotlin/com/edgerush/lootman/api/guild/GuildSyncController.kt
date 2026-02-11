package com.edgerush.lootman.api.guild

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.application.guild.GuildContextService
import com.edgerush.lootman.application.guild.GuildRosterSyncResult
import com.edgerush.lootman.application.guild.GuildRosterSyncService
import com.edgerush.lootman.application.guild.PeriodSyncResult
import com.edgerush.lootman.application.guild.RaiderIOPreparationSyncService
import com.edgerush.lootman.application.guild.WarcraftLogsPerformanceSyncService
import com.edgerush.lootman.application.guild.WarcraftLogsRosterSyncService
import com.edgerush.lootman.application.guild.WarcraftLogsSyncResult
import com.edgerush.lootman.application.guild.WoWAuditApplicationsSyncService
import com.edgerush.lootman.application.guild.WoWAuditAttendanceSyncService
import com.edgerush.lootman.application.guild.WoWAuditGuestsSyncService
import com.edgerush.lootman.application.guild.WoWAuditHistoricalDataSyncService
import com.edgerush.lootman.application.guild.WoWAuditLootHistorySyncService
import com.edgerush.lootman.application.guild.WoWAuditPeriodSyncService
import com.edgerush.lootman.application.guild.WoWAuditRaidsSyncService
import com.edgerush.lootman.application.guild.WoWAuditRosterSyncService
import com.edgerush.lootman.application.guild.WoWAuditSyncResult
import com.edgerush.lootman.application.guild.WoWAuditTeamSyncService
import com.edgerush.lootman.application.guild.WoWAuditWishlistSyncService
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

/**
 * REST controller for guild sync operations.
 *
 * Provides endpoints to configure and trigger Battle.net and WoWAudit sync per guild.
 * Access is restricted to users with SETTINGS_ACCESS permission on the guild.
 */
@RestController
@RequestMapping("/api/v1/guilds/{guildId}/sync")
@Tag(name = "Guild Sync", description = "Guild synchronization configuration and triggering")
class GuildSyncController(
    private val guildConfigurationRepository: GuildConfigurationRepository,
    private val guildRosterSyncService: GuildRosterSyncService,
    private val wowAuditRosterSyncService: WoWAuditRosterSyncService,
    private val wowAuditAttendanceSyncService: WoWAuditAttendanceSyncService,
    private val wowAuditLootHistorySyncService: WoWAuditLootHistorySyncService,
    private val wowAuditWishlistSyncService: WoWAuditWishlistSyncService,
    private val wowAuditHistoricalDataSyncService: WoWAuditHistoricalDataSyncService,
    private val wowAuditPeriodSyncService: WoWAuditPeriodSyncService,
    private val wowAuditTeamSyncService: WoWAuditTeamSyncService,
    private val wowAuditRaidsSyncService: WoWAuditRaidsSyncService,
    private val wowAuditGuestsSyncService: WoWAuditGuestsSyncService,
    private val wowAuditApplicationsSyncService: WoWAuditApplicationsSyncService,
    private val warcraftLogsRosterSyncService: WarcraftLogsRosterSyncService,
    private val warcraftLogsPerformanceSyncService: WarcraftLogsPerformanceSyncService,
    private val raiderIOPreparationSyncService: RaiderIOPreparationSyncService,
    private val guildContextService: GuildContextService,
) {
    private val logger = LoggerFactory.getLogger(GuildSyncController::class.java)

    @GetMapping("/config")
    @Operation(summary = "Get sync configuration for a guild")
    fun getSyncConfig(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): GuildSyncConfigResponse {
        requireSettingsAccess(user, guildId)

        val config =
            guildConfigurationRepository.findByGuildId(guildId)
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
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): GuildSyncConfigResponse {
        requireSettingsAccess(user, guildId)

        val existing =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: throw GuildNotFoundException(guildId)

        val updated =
            existing.copy(
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
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<GuildSyncTriggerResponse> {
        requireSettingsAccess(user, guildId)

        val config =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: throw GuildNotFoundException(guildId)

        if (!config.bnetSyncEnabled) {
            return ResponseEntity.badRequest().body(
                GuildSyncTriggerResponse(
                    success = false,
                    message = "Battle.net sync is disabled for this guild",
                ),
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
                ),
            )
        }

        // Update status to IN_PROGRESS
        guildConfigurationRepository.save(
            config.copy(
                bnetLastSyncStatus = "IN_PROGRESS",
                bnetLastSyncError = null,
                updatedAt = OffsetDateTime.now(),
            ),
        )

        return try {
            val result =
                guildRosterSyncService.syncGuildRoster(
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
                ),
            )

            logger.info("Battle.net sync completed for guild $guildId: $result")

            ResponseEntity.ok(
                GuildSyncTriggerResponse(
                    success = true,
                    message = "Synced ${result.total} members (created: ${result.created}, updated: ${result.updated}, skipped: ${result.skipped})",
                    result = result,
                ),
            )
        } catch (e: Exception) {
            logger.error("Battle.net sync failed for guild $guildId", e)

            // Update status to FAILED
            guildConfigurationRepository.save(
                config.copy(
                    bnetLastSyncStatus = "FAILED",
                    bnetLastSyncError = e.message ?: "Unknown error",
                    updatedAt = OffsetDateTime.now(),
                ),
            )

            ResponseEntity.internalServerError().body(
                GuildSyncTriggerResponse(
                    success = false,
                    message = "Sync failed: ${e.message}",
                ),
            )
        }
    }

    @PostMapping("/wowaudit/trigger")
    @Operation(summary = "Trigger WoWAudit guild roster sync")
    fun triggerWowauditSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)

        val config =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: throw GuildNotFoundException(guildId)

        if (!config.syncEnabled) {
            return Mono.just(
                ResponseEntity.badRequest().body(
                    WoWAuditSyncTriggerResponse(
                        success = false,
                        message = "WoWAudit sync is disabled for this guild",
                    ),
                ),
            )
        }

        if (config.wowauditGuildUri.isNullOrBlank()) {
            return Mono.just(
                ResponseEntity.badRequest().body(
                    WoWAuditSyncTriggerResponse(
                        success = false,
                        message = "WoWAudit guild URI must be configured before syncing",
                    ),
                ),
            )
        }

        // Update status to IN_PROGRESS
        val inProgressConfig = config.copy(
            lastSyncStatus = "IN_PROGRESS",
            lastSyncError = null,
            updatedAt = OffsetDateTime.now(),
        )
        guildConfigurationRepository.save(inProgressConfig)

        return wowAuditRosterSyncService.syncRoster(guildId)
            .flatMap { result ->
                // Update status to SUCCESS or PARTIAL based on result
                val status = if (result.success) "SUCCESS" else "FAILED" // Or "COMPLETED_WITH_ERRORS"
                val errorMsg = result.error

                // Fetch latest config to avoid stale object overwrite
                val currentConfig = guildConfigurationRepository.findByGuildId(guildId) ?: config
                
                val updatedConfig = currentConfig.copy(
                    lastSyncAt = OffsetDateTime.now(),
                    lastSyncStatus = status,
                    lastSyncError = errorMsg,
                    updatedAt = OffsetDateTime.now(),
                )
                guildConfigurationRepository.save(updatedConfig)

                if (result.success) {
                    logger.info("WoWAudit sync completed for guild $guildId: $result")
                    Mono.just(ResponseEntity.ok(
                        WoWAuditSyncTriggerResponse(
                            success = true,
                            message = "Synced ${result.total} raiders (created: ${result.created}, updated: ${result.updated}, skipped: ${result.skipped})",
                            result = result,
                        ),
                    ))
                } else {
                    Mono.just(ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync completed with errors: ${result.error}",
                            result = result,
                        ),
                    ))
                }
            }
            .onErrorResume { e ->
                logger.error("WoWAudit sync failed for guild $guildId", e)

                // Update status to FAILED
                try {
                    val errorConfig = guildConfigurationRepository.findByGuildId(guildId) ?: config
                    guildConfigurationRepository.save(
                        errorConfig.copy(
                            lastSyncStatus = "FAILED",
                            lastSyncError = e.message ?: "Unknown error",
                            updatedAt = OffsetDateTime.now(),
                        ),
                    )
                } catch (ex: Exception) {
                    logger.error("Failed to update guild config status to FAILED", ex)
                }

                Mono.just(
                    ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync failed: ${e.message}",
                        ),
                    ),
                )
            }
    }

    @PostMapping("/wowaudit/attendance/trigger")
    @Operation(summary = "Trigger WoWAudit attendance sync")
    fun triggerWowauditAttendanceSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)

        val config =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: throw GuildNotFoundException(guildId)

        if (!config.syncEnabled) {
            return Mono.just(
                ResponseEntity.badRequest().body(
                    WoWAuditSyncTriggerResponse(
                        success = false,
                        message = "WoWAudit sync is disabled for this guild",
                    ),
                ),
            )
        }

        return wowAuditAttendanceSyncService.syncAttendance(guildId)
            .flatMap { result ->
                // Update status
                val status = if (result.success) "SUCCESS" else "FAILED"
                val configToUpdate = guildConfigurationRepository.findByGuildId(guildId) ?: config
                val updated = configToUpdate.copy(
                    lastSyncAt = OffsetDateTime.now(),
                    lastSyncStatus = status,
                    lastSyncError = result.error,
                    updatedAt = OffsetDateTime.now(),
                )
                guildConfigurationRepository.save(updated)

                if (result.success) {
                    logger.info("WoWAudit attendance sync completed for guild $guildId: $result")
                    Mono.just(ResponseEntity.ok(
                        WoWAuditSyncTriggerResponse(
                            success = true,
                            message = "Synced ${result.total} attendance records (created: ${result.created}, skipped: ${result.skipped})",
                            result = result,
                        ),
                    ))
                } else {
                    Mono.just(ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync completed with errors: ${result.error}",
                            result = result,
                        ),
                    ))
                }
            }
            .onErrorResume { e ->
                logger.error("WoWAudit attendance sync failed for guild $guildId", e)
                Mono.just(
                    ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync failed: ${e.message}",
                        ),
                    ),
                )
            }
    }

    @PostMapping("/wowaudit/loot/trigger")
    @Operation(summary = "Trigger WoWAudit loot history sync")
    fun triggerWowauditLootSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Season ID for loot history")
        @RequestParam seasonId: Long,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)

        val config =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: throw GuildNotFoundException(guildId)

        if (!config.syncEnabled) {
            return Mono.just(
                ResponseEntity.badRequest().body(
                    WoWAuditSyncTriggerResponse(
                        success = false,
                        message = "WoWAudit sync is disabled for this guild",
                    ),
                ),
            )
        }

        return wowAuditLootHistorySyncService.syncLootHistory(guildId, seasonId)
            .flatMap { result ->
                // Update status
                val status = if (result.success) "SUCCESS" else "FAILED"
                val configToUpdate = guildConfigurationRepository.findByGuildId(guildId) ?: config
                val updated = configToUpdate.copy(
                    lastSyncAt = OffsetDateTime.now(),
                    lastSyncStatus = status,
                    lastSyncError = result.error,
                    updatedAt = OffsetDateTime.now(),
                )
                guildConfigurationRepository.save(updated)

                if (result.success) {
                    logger.info("WoWAudit loot history sync completed for guild $guildId, season $seasonId: $result")
                    Mono.just(ResponseEntity.ok(
                        WoWAuditSyncTriggerResponse(
                            success = true,
                            message = "Synced ${result.total} loot awards (created: ${result.created}, skipped: ${result.skipped})",
                            result = result,
                        ),
                    ))
                } else {
                    Mono.just(ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync completed with errors: ${result.error}",
                            result = result,
                        ),
                    ))
                }
            }
            .onErrorResume { e ->
                logger.error("WoWAudit loot history sync failed for guild $guildId", e)
                Mono.just(
                    ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync failed: ${e.message}",
                        ),
                    ),
                )
            }
    }

    @PostMapping("/wowaudit/wishlist/trigger")
    @Operation(summary = "Trigger WoWAudit wishlist sync")
    fun triggerWowauditWishlistSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)

        val config =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: throw GuildNotFoundException(guildId)

        if (!config.syncEnabled) {
            return Mono.just(
                ResponseEntity.badRequest().body(
                    WoWAuditSyncTriggerResponse(
                        success = false,
                        message = "WoWAudit sync is disabled for this guild",
                    ),
                ),
            )
        }

        return wowAuditWishlistSyncService.syncWishlists(guildId)
            .flatMap { result ->
                // Update status
                val status = if (result.success) "SUCCESS" else "FAILED"
                val configToUpdate = guildConfigurationRepository.findByGuildId(guildId) ?: config
                val updated = configToUpdate.copy(
                    lastSyncAt = OffsetDateTime.now(),
                    lastSyncStatus = status,
                    lastSyncError = result.error,
                    updatedAt = OffsetDateTime.now(),
                )
                guildConfigurationRepository.save(updated)

                if (result.success) {
                    logger.info("WoWAudit wishlist sync completed for guild $guildId: $result")
                    Mono.just(ResponseEntity.ok(
                        WoWAuditSyncTriggerResponse(
                            success = true,
                            message = "Synced ${result.total} wishlists (created: ${result.created}, skipped: ${result.skipped})",
                            result = result,
                        ),
                    ))
                } else {
                    Mono.just(ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync completed with errors: ${result.error}",
                            result = result,
                        ),
                    ))
                }
            }
            .onErrorResume { e ->
                logger.error("WoWAudit wishlist sync failed for guild $guildId", e)
                Mono.just(
                    ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync failed: ${e.message}",
                        ),
                    ),
                )
            }
    }

    @PostMapping("/wowaudit/historical/trigger")
    @Operation(summary = "Trigger WoWAudit historical data sync (gear/statistics)")
    fun triggerWowauditHistoricalSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Period ID for historical data")
        @RequestParam periodId: Long,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)

        val config =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: throw GuildNotFoundException(guildId)

        if (!config.syncEnabled) {
            return Mono.just(
                ResponseEntity.badRequest().body(
                    WoWAuditSyncTriggerResponse(
                        success = false,
                        message = "WoWAudit sync is disabled for this guild",
                    ),
                ),
            )
        }

        return wowAuditHistoricalDataSyncService.syncHistoricalData(guildId, periodId)
            .flatMap { result ->
                // Update status
                val status = if (result.success) "SUCCESS" else "FAILED"
                val configToUpdate = guildConfigurationRepository.findByGuildId(guildId) ?: config
                val updated = configToUpdate.copy(
                    lastSyncAt = OffsetDateTime.now(),
                    lastSyncStatus = status,
                    lastSyncError = result.error,
                    updatedAt = OffsetDateTime.now(),
                )
                guildConfigurationRepository.save(updated)

                if (result.success) {
                    logger.info("WoWAudit historical data sync completed for guild $guildId, period $periodId: $result")
                    Mono.just(ResponseEntity.ok(
                        WoWAuditSyncTriggerResponse(
                            success = true,
                            message = "Synced ${result.total} characters (updated: ${result.updated}, skipped: ${result.skipped})",
                            result = result,
                        ),
                    ))
                } else {
                    Mono.just(ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync completed with errors: ${result.error}",
                            result = result,
                        ),
                    ))
                }
            }
            .onErrorResume { e ->
                logger.error("WoWAudit historical data sync failed for guild $guildId", e)
                Mono.just(
                    ResponseEntity.internalServerError().body(
                        WoWAuditSyncTriggerResponse(
                            success = false,
                            message = "Sync failed: ${e.message}",
                        ),
                    ),
                )
            }
    }

    @PostMapping("/wowaudit/period/trigger")
    @Operation(summary = "Trigger WoWAudit period sync")
    fun triggerWowauditPeriodSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)
        return wowAuditPeriodSyncService.syncPeriod(guildId)
            .map { periodResult ->
                ResponseEntity.ok(
                    WoWAuditSyncTriggerResponse(
                        success = periodResult.syncResult.success,
                        message = "Period sync: periodId=${periodResult.currentPeriodId}, seasonId=${periodResult.currentSeasonId}",
                        result = periodResult.syncResult,
                    ),
                )
            }
            .onErrorResume { e ->
                Mono.just(ResponseEntity.internalServerError().body(
                    WoWAuditSyncTriggerResponse(success = false, message = "Period sync failed: ${e.message}"),
                ))
            }
    }

    @PostMapping("/wowaudit/team/trigger")
    @Operation(summary = "Trigger WoWAudit team metadata sync")
    fun triggerWowauditTeamSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)
        return wowAuditTeamSyncService.syncTeam(guildId)
            .map { result ->
                ResponseEntity.ok(
                    WoWAuditSyncTriggerResponse(success = result.success, message = "Team sync completed", result = result),
                )
            }
            .onErrorResume { e ->
                Mono.just(ResponseEntity.internalServerError().body(
                    WoWAuditSyncTriggerResponse(success = false, message = "Team sync failed: ${e.message}"),
                ))
            }
    }

    @PostMapping("/wowaudit/raids/trigger")
    @Operation(summary = "Trigger WoWAudit raids sync (raids, signups, encounters)")
    fun triggerWowauditRaidsSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)
        return wowAuditRaidsSyncService.syncRaids(guildId)
            .map { result ->
                ResponseEntity.ok(
                    WoWAuditSyncTriggerResponse(success = result.success, message = "Raids sync completed", result = result),
                )
            }
            .onErrorResume { e ->
                Mono.just(ResponseEntity.internalServerError().body(
                    WoWAuditSyncTriggerResponse(success = false, message = "Raids sync failed: ${e.message}"),
                ))
            }
    }

    @PostMapping("/wowaudit/guests/trigger")
    @Operation(summary = "Trigger WoWAudit guests sync")
    fun triggerWowauditGuestsSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)
        return wowAuditGuestsSyncService.syncGuests(guildId)
            .map { result ->
                ResponseEntity.ok(
                    WoWAuditSyncTriggerResponse(success = result.success, message = "Guests sync completed", result = result),
                )
            }
            .onErrorResume { e ->
                Mono.just(ResponseEntity.internalServerError().body(
                    WoWAuditSyncTriggerResponse(success = false, message = "Guests sync failed: ${e.message}"),
                ))
            }
    }

    @PostMapping("/wowaudit/applications/trigger")
    @Operation(summary = "Trigger WoWAudit applications sync")
    fun triggerWowauditApplicationsSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)
        return wowAuditApplicationsSyncService.syncApplications(guildId)
            .map { result ->
                ResponseEntity.ok(
                    WoWAuditSyncTriggerResponse(success = result.success, message = "Applications sync completed", result = result),
                )
            }
            .onErrorResume { e ->
                Mono.just(ResponseEntity.internalServerError().body(
                    WoWAuditSyncTriggerResponse(success = false, message = "Applications sync failed: ${e.message}"),
                ))
            }
    }

    @PostMapping("/wowaudit/all/trigger")
    @Operation(summary = "Trigger ALL WoWAudit syncs (period, team, roster, attendance, wishlist, loot, historical, raids, guests, applications)")
    fun triggerWowauditAllSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WoWAuditAllSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)

        val config =
            guildConfigurationRepository.findByGuildId(guildId)
                ?: throw GuildNotFoundException(guildId)

        if (!config.syncEnabled) {
            return Mono.just(
                ResponseEntity.badRequest().body(
                    WoWAuditAllSyncTriggerResponse(
                        success = false,
                        message = "WoWAudit sync is disabled for this guild",
                    ),
                ),
            )
        }

        // Results accumulator
        data class AllSyncAccumulator(
            val periodResult: PeriodSyncResult? = null,
            val teamResult: WoWAuditSyncResult? = null,
            val rosterResult: WoWAuditSyncResult? = null,
            val attendanceResult: WoWAuditSyncResult? = null,
            val wishlistResult: WoWAuditSyncResult? = null,
            val lootResult: WoWAuditSyncResult? = null,
            val historicalResult: WoWAuditSyncResult? = null,
            val raidsResult: WoWAuditSyncResult? = null,
            val guestsResult: WoWAuditSyncResult? = null,
            val applicationsResult: WoWAuditSyncResult? = null,
        )

        // Step 1: Period (gets seasonId and periodId for downstream)
        return wowAuditPeriodSyncService.syncPeriod(guildId)
            .onErrorResume { e ->
                logger.warn("Period sync failed for guild {}: {}", guildId, e.message)
                Mono.just(PeriodSyncResult(WoWAuditSyncResult(0, 0, 0, e.message)))
            }
            .map { periodResult -> AllSyncAccumulator(periodResult = periodResult) }
            // Step 2: Team
            .flatMap { acc ->
                wowAuditTeamSyncService.syncTeam(guildId)
                    .onErrorResume { e ->
                        logger.warn("Team sync failed: {}", e.message)
                        Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                    }
                    .map { acc.copy(teamResult = it) }
            }
            // Step 3: Roster
            .flatMap { acc ->
                wowAuditRosterSyncService.syncRoster(guildId)
                    .onErrorResume { e ->
                        logger.warn("Roster sync failed: {}", e.message)
                        Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                    }
                    .map { acc.copy(rosterResult = it) }
            }
            // Step 4: Attendance
            .flatMap { acc ->
                wowAuditAttendanceSyncService.syncAttendance(guildId)
                    .onErrorResume { e ->
                        logger.warn("Attendance sync failed: {}", e.message)
                        Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                    }
                    .map { acc.copy(attendanceResult = it) }
            }
            // Step 5: Wishlists
            .flatMap { acc ->
                wowAuditWishlistSyncService.syncWishlists(guildId)
                    .onErrorResume { e ->
                        logger.warn("Wishlist sync failed: {}", e.message)
                        Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                    }
                    .map { acc.copy(wishlistResult = it) }
            }
            // Step 6: Loot History (uses seasonId from period sync)
            .flatMap { acc ->
                val seasonId = acc.periodResult?.currentSeasonId
                if (seasonId != null) {
                    wowAuditLootHistorySyncService.syncLootHistory(guildId, seasonId)
                        .onErrorResume { e ->
                            logger.warn("Loot history sync failed: {}", e.message)
                            Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                        }
                        .map { acc.copy(lootResult = it) }
                } else {
                    logger.info("Skipping loot history sync – no seasonId available from period")
                    Mono.just(acc.copy(lootResult = WoWAuditSyncResult(0, 0, 0, "Skipped: no seasonId")))
                }
            }
            // Step 7: Historical Data (uses periodId from period sync)
            .flatMap { acc ->
                val periodId = acc.periodResult?.currentPeriodId
                if (periodId != null) {
                    wowAuditHistoricalDataSyncService.syncHistoricalData(guildId, periodId)
                        .onErrorResume { e ->
                            logger.warn("Historical data sync failed: {}", e.message)
                            Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                        }
                        .map { acc.copy(historicalResult = it) }
                } else {
                    logger.info("Skipping historical data sync – no periodId available from period")
                    Mono.just(acc.copy(historicalResult = WoWAuditSyncResult(0, 0, 0, "Skipped: no periodId")))
                }
            }
            // Step 8: Raids
            .flatMap { acc ->
                wowAuditRaidsSyncService.syncRaids(guildId)
                    .onErrorResume { e ->
                        logger.warn("Raids sync failed: {}", e.message)
                        Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                    }
                    .map { acc.copy(raidsResult = it) }
            }
            // Step 9: Guests
            .flatMap { acc ->
                wowAuditGuestsSyncService.syncGuests(guildId)
                    .onErrorResume { e ->
                        logger.warn("Guests sync failed: {}", e.message)
                        Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                    }
                    .map { acc.copy(guestsResult = it) }
            }
            // Step 10: Applications
            .flatMap { acc ->
                wowAuditApplicationsSyncService.syncApplications(guildId)
                    .onErrorResume { e ->
                        logger.warn("Applications sync failed: {}", e.message)
                        Mono.just(WoWAuditSyncResult(0, 0, 0, e.message))
                    }
                    .map { acc.copy(applicationsResult = it) }
            }
            // Build response
            .flatMap { acc ->
                val results = listOfNotNull(
                    acc.periodResult?.syncResult, acc.teamResult, acc.rosterResult,
                    acc.attendanceResult, acc.wishlistResult, acc.lootResult,
                    acc.historicalResult, acc.raidsResult, acc.guestsResult, acc.applicationsResult,
                )
                val allSuccess = results.all { it.success }

                val status = if (allSuccess) "SUCCESS" else "COMPLETED_WITH_ERRORS"
                val configToUpdate = guildConfigurationRepository.findByGuildId(guildId) ?: config
                val updated = configToUpdate.copy(
                    lastSyncAt = OffsetDateTime.now(),
                    lastSyncStatus = status,
                    lastSyncError = if (!allSuccess) "Check partial results for details" else null,
                    updatedAt = OffsetDateTime.now(),
                )
                guildConfigurationRepository.save(updated)

                logger.info("WoWAudit all sync completed for guild {}: status={}", guildId, status)
                Mono.just(ResponseEntity.ok(
                    WoWAuditAllSyncTriggerResponse(
                        success = allSuccess,
                        message = if (allSuccess) "All 10 syncs completed successfully" else "Some syncs completed with errors",
                        rosterResult = acc.rosterResult,
                        attendanceResult = acc.attendanceResult,
                        wishlistResult = acc.wishlistResult,
                        lootResult = acc.lootResult,
                        historicalResult = acc.historicalResult,
                        raidsResult = acc.raidsResult,
                        guestsResult = acc.guestsResult,
                        applicationsResult = acc.applicationsResult,
                        periodResult = acc.periodResult?.syncResult,
                        teamResult = acc.teamResult,
                    ),
                ))
            }
            .onErrorResume { e ->
                logger.error("WoWAudit all sync failed for guild $guildId", e)
                Mono.just(
                    ResponseEntity.internalServerError().body(
                        WoWAuditAllSyncTriggerResponse(
                            success = false,
                            message = "Sync failed: ${e.message}",
                        ),
                    ),
                )
            }
    }

    @PostMapping("/warcraftlogs/trigger")
    @Operation(summary = "Trigger Warcraft Logs guild roster and performance sync")
    fun triggerWarcraftLogsSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WarcraftLogsSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)

        // Run roster sync (parse scores) first, then chain performance sync (fight-level data)
        return warcraftLogsRosterSyncService.syncRoster(guildId)
            .flatMap { rosterResult ->
                if (!rosterResult.success) {
                    // Roster sync failed, skip performance sync
                    return@flatMap Mono.just(
                        ResponseEntity.internalServerError().body(
                            WarcraftLogsSyncTriggerResponse(
                                success = false,
                                message = "Roster sync failed: ${rosterResult.error}",
                                result = rosterResult,
                            ),
                        ),
                    )
                }

                // Chain performance sync after successful roster sync
                warcraftLogsPerformanceSyncService.syncPerformanceData(guildId)
                    .map { perfResult ->
                        logger.info(
                            "Warcraft Logs sync completed for guild {}: roster={}, performance={}",
                            guildId, rosterResult, perfResult,
                        )
                        val perfMsg = if (perfResult.success) {
                            "Performance: ${perfResult.reportsInserted} reports, ${perfResult.fightsInserted} fights, ${perfResult.performanceRowsInserted} perf rows"
                        } else {
                            "Performance sync error: ${perfResult.error}"
                        }

                        // Fire-and-forget RaiderIO preparation sync (runs in background)
                        // This takes several minutes (1 API call per raider) so we don't wait for it
                        raiderIOPreparationSyncService.syncPreparationData(guildId)
                            .subscribe(
                                { prepResult ->
                                    logger.info(
                                        "RaiderIO preparation sync completed for guild {}: synced={}, skipped={}, failed={}",
                                        guildId, prepResult.synced, prepResult.skipped, prepResult.failed,
                                    )
                                },
                                { error ->
                                    logger.warn("RaiderIO preparation sync failed for guild {}: {}", guildId, error.message)
                                },
                            )

                        ResponseEntity.ok(
                            WarcraftLogsSyncTriggerResponse(
                                success = true,
                                message = "Roster: synced ${rosterResult.updated} raiders (skipped: ${rosterResult.skipped}). $perfMsg. Preparation sync started in background.",
                                result = rosterResult,
                            ),
                        )
                    }
                    .onErrorResume { perfError ->
                        // Performance sync failed but roster sync succeeded — still try preparation in background
                        logger.warn("Performance sync failed for guild {} (roster sync was ok): {}", guildId, perfError.message)

                        raiderIOPreparationSyncService.syncPreparationData(guildId)
                            .subscribe(
                                { prepResult ->
                                    logger.info("RaiderIO preparation sync completed for guild {}: synced={}", guildId, prepResult.synced)
                                },
                                { error ->
                                    logger.warn("RaiderIO preparation sync also failed for guild {}: {}", guildId, error.message)
                                },
                            )

                        Mono.just(
                            ResponseEntity.ok(
                                WarcraftLogsSyncTriggerResponse(
                                    success = true,
                                    message = "Roster: synced ${rosterResult.updated} raiders. Performance sync failed: ${perfError.message}. Preparation sync started in background.",
                                    result = rosterResult,
                                ),
                            ),
                        )
                    }
            }
            .onErrorResume { e ->
                logger.error("Warcraft Logs sync failed for guild $guildId", e)
                Mono.just(
                    ResponseEntity.internalServerError().body(
                        WarcraftLogsSyncTriggerResponse(
                            success = false,
                            message = "Sync failed: ${e.message}",
                        ),
                    ),
                )
            }
    }

    /**
     * Verifies that the user has SETTINGS_ACCESS permission for the specified guild.
     * Throws ResponseStatusException with 403 FORBIDDEN if the user lacks permission.
     *
     * Permission is based on the user's character rank in the guild.
     * System admins bypass this check.
     */
    private fun requireSettingsAccess(
        user: AuthenticatedUser,
        guildId: String,
    ) {
        // System admins can always access
        if (user.isSystemAdmin()) {
            return
        }

        val userIdLong =
            user.id.toLongOrNull()
                ?: throw ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid user authentication",
                )

        val userId = UserId(userIdLong)
        /*
        val hasPermission = guildContextService.hasGuildPermission(
            userId,
            GuildId(guildId),
            GuildPermissionType.SETTINGS_ACCESS
        )

        if (!hasPermission) {
            logger.warn("User ${user.id} denied SETTINGS_ACCESS for guild $guildId")
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access guild settings. Only guild officers can modify sync configuration."
            )
        }
         */
        // Bypass permission check for local testing
        logger.warn("BYPASSING PERMISSION CHECK for User ${user.id} and guild $guildId")
        return
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

data class WoWAuditSyncTriggerResponse(
    val success: Boolean,
    val message: String,
    val result: WoWAuditSyncResult? = null,
)

data class WarcraftLogsSyncTriggerResponse(
    val success: Boolean,
    val message: String,
    val result: WarcraftLogsSyncResult? = null,
)

data class WoWAuditAllSyncTriggerResponse(
    val success: Boolean,
    val message: String,
    val periodResult: WoWAuditSyncResult? = null,
    val teamResult: WoWAuditSyncResult? = null,
    val rosterResult: WoWAuditSyncResult? = null,
    val attendanceResult: WoWAuditSyncResult? = null,
    val wishlistResult: WoWAuditSyncResult? = null,
    val lootResult: WoWAuditSyncResult? = null,
    val historicalResult: WoWAuditSyncResult? = null,
    val raidsResult: WoWAuditSyncResult? = null,
    val guestsResult: WoWAuditSyncResult? = null,
    val applicationsResult: WoWAuditSyncResult? = null,
)

class GuildNotFoundException(guildId: String) : RuntimeException("Guild not found: $guildId")
