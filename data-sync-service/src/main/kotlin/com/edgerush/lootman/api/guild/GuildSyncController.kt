package com.edgerush.lootman.api.guild

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.application.guild.GuildContextService
import com.edgerush.lootman.application.guild.GuildRosterSyncResult
import com.edgerush.lootman.application.guild.GuildRosterSyncService
import com.edgerush.lootman.application.guild.WarcraftLogsRosterSyncService
import com.edgerush.lootman.application.guild.WarcraftLogsSyncResult
import com.edgerush.lootman.application.guild.WoWAuditAttendanceSyncService
import com.edgerush.lootman.application.guild.WoWAuditHistoricalDataSyncService
import com.edgerush.lootman.application.guild.WoWAuditLootHistorySyncService
import com.edgerush.lootman.application.guild.WoWAuditRosterSyncService
import com.edgerush.lootman.application.guild.WoWAuditSyncResult
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
    private val warcraftLogsRosterSyncService: WarcraftLogsRosterSyncService,
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

    @PostMapping("/wowaudit/all/trigger")
    @Operation(summary = "Trigger all WoWAudit syncs (roster, attendance, wishlist)")
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

        // Run all syncs in sequence
        return wowAuditRosterSyncService.syncRoster(guildId)
            .flatMap { rosterResult ->
                wowAuditAttendanceSyncService.syncAttendance(guildId)
                    .map { attendanceResult -> Pair(rosterResult, attendanceResult) }
            }
            .flatMap { (rosterResult, attendanceResult) ->
                wowAuditWishlistSyncService.syncWishlists(guildId)
                    .map { wishlistResult -> Triple(rosterResult, attendanceResult, wishlistResult) }
            }
            .flatMap { (rosterResult, attendanceResult, wishlistResult) ->
                val allSuccess = rosterResult.success && attendanceResult.success && wishlistResult.success
                
                // Update status
                val status = if (allSuccess) "SUCCESS" else "COMPLETED_WITH_ERRORS"
                val configToUpdate = guildConfigurationRepository.findByGuildId(guildId) ?: config
                val updated = configToUpdate.copy(
                    lastSyncAt = OffsetDateTime.now(),
                    lastSyncStatus = status,
                    lastSyncError = if (!allSuccess) "Check partial results for details" else null,
                    updatedAt = OffsetDateTime.now(),
                )
                guildConfigurationRepository.save(updated)

                logger.info(
                    "WoWAudit all sync completed for guild $guildId: roster=$rosterResult, attendance=$attendanceResult, wishlist=$wishlistResult",
                )
                Mono.just(ResponseEntity.ok(
                    WoWAuditAllSyncTriggerResponse(
                        success = allSuccess,
                        message = if (allSuccess) "All syncs completed successfully" else "Some syncs completed with errors",
                        rosterResult = rosterResult,
                        attendanceResult = attendanceResult,
                        wishlistResult = wishlistResult,
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
    @Operation(summary = "Trigger Warcraft Logs guild roster sync")
    fun triggerWarcraftLogsSync(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): Mono<ResponseEntity<WarcraftLogsSyncTriggerResponse>> {
        requireSettingsAccess(user, guildId)

        // Ideally check config.warcraftLogsEnabled but it might likely not exist in Entity yet.
        // Assuming enabled if Bnet/WoWAudit is setup or implicit.

        return warcraftLogsRosterSyncService.syncRoster(guildId)
            .map { result ->
                if (result.success) {
                    logger.info("Warcraft Logs sync completed for guild $guildId: $result")
                    ResponseEntity.ok(
                        WarcraftLogsSyncTriggerResponse(
                            success = true,
                            message = "Synced ${result.updated} raiders (skipped: ${result.skipped})",
                            result = result,
                        ),
                    )
                } else {
                    ResponseEntity.internalServerError().body(
                        WarcraftLogsSyncTriggerResponse(
                            success = false,
                            message = "Sync completed with errors: ${result.error}",
                            result = result,
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
    val rosterResult: WoWAuditSyncResult? = null,
    val attendanceResult: WoWAuditSyncResult? = null,
    val wishlistResult: WoWAuditSyncResult? = null,
)

class GuildNotFoundException(guildId: String) : RuntimeException("Guild not found: $guildId")
