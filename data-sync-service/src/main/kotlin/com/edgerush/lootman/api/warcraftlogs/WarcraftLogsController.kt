package com.edgerush.lootman.api.warcraftlogs

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.api.raider.RaiderWarcraftLogCrudService
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.GuildId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for Warcraft Logs reports.
 *
 * Provides endpoints for retrieving Warcraft Logs performance data.
 */
@RestController
@RequestMapping("/api/v1/warcraftlogs")
@Tag(name = "WarcraftLogs", description = "Warcraft Logs performance endpoints")
class WarcraftLogsController(
    private val warcraftLogService: RaiderWarcraftLogCrudService,
    private val raiderRepository: RaiderEntityRepository,
    private val currentUserService: CurrentUserService,
) {
    /**
     * Get Warcraft Logs reports for a specific raider.
     *
     * @param guildId The guild's unique identifier
     * @param raiderId The raider's unique identifier
     * @param limit Maximum number of reports to return
     * @return Warcraft Logs report data for the raider
     */
    @GetMapping("/guilds/{guildId}/raiders/{raiderId}/reports")
    @Operation(summary = "Get Warcraft Logs reports for a raider")
    fun getReports(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Raider ID")
        @PathVariable raiderId: Long,
        @Parameter(description = "Maximum number of reports")
        @RequestParam(defaultValue = "20") limit: Int,
    ): WarcraftLogsReportResponse {
        val raider =
            raiderRepository.findById(raiderId)
                ?: throw NoSuchElementException("Raider not found: $raiderId")

        // Get the Warcraft Logs scores for the raider
        val logs = warcraftLogService.findByRaiderIdUnpaged(raiderId, limit)

        // Transform to the expected format
        val entries =
            logs.map { log ->
                WarcraftLogsEntryResponse(
                    reportId = "wcl_${log.id}",
                    encounterId = 0,
                    encounterName = log.difficulty,
                    difficulty = log.difficulty,
                    date = "",
                    dps = null,
                    hps = null,
                    ilvl = 0,
                    spec = "",
                    percentile = log.score?.toDouble() ?: 0.0,
                    deaths = 0,
                )
            }

        return WarcraftLogsReportResponse(
            raiderId = raiderId,
            characterName = raider.characterName,
            reports = entries,
        )
    }

    /**
     * Get current user's Warcraft Logs reports.
     *
     * @param guildId The guild's unique identifier
     * @param authenticatedUser The authenticated user from the JWT token
     * @param limit Maximum number of reports to return
     * @return Warcraft Logs report data for the current user
     */
    @GetMapping("/guilds/{guildId}/me/reports")
    @Operation(summary = "Get current user's Warcraft Logs reports")
    fun getMyReports(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @Parameter(description = "Maximum number of reports")
        @RequestParam(defaultValue = "20") limit: Int,
    ): WarcraftLogsReportResponse {
        currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId))
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser)

        return getReports(guildId, raiderId.value, limit)
    }
}

/**
 * Response DTO for Warcraft Logs reports.
 */
data class WarcraftLogsReportResponse(
    val raiderId: Long,
    val characterName: String,
    val reports: List<WarcraftLogsEntryResponse>,
)

/**
 * Response DTO for a single Warcraft Logs entry.
 */
data class WarcraftLogsEntryResponse(
    val reportId: String,
    val encounterId: Int,
    val encounterName: String,
    val difficulty: String,
    val date: String,
    val dps: Double?,
    val hps: Double?,
    val ilvl: Int,
    val spec: String,
    val percentile: Double,
    val deaths: Int,
)
