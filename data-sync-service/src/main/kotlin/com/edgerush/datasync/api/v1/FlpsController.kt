package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.request.*
import com.edgerush.datasync.api.dto.response.*
import com.edgerush.datasync.api.exception.ValidationException
import com.edgerush.datasync.application.flps.*
import com.edgerush.datasync.domain.flps.model.*
import com.edgerush.datasync.domain.flps.repository.*
import com.edgerush.datasync.domain.flps.service.RaiderRole
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for FLPS (Final Loot Priority Score) operations.
 *
 * This controller provides endpoints for:
 * - Calculating FLPS scores for individual raiders
 * - Generating comprehensive FLPS reports for guilds
 * - Managing FLPS modifiers and configuration
 *
 * All endpoints use the new domain-driven design architecture with use cases.
 */
@RestController
@RequestMapping("/api/v1/flps")
class FlpsController(
    private val calculateFlpsScoreUseCase: CalculateFlpsScoreUseCase,
    private val getFlpsReportUseCase: GetFlpsReportUseCase,
    private val updateModifiersUseCase: UpdateModifiersUseCase
) {

    /**
     * Calculate FLPS score for a single raider and item combination.
     *
     * @param request The calculation parameters
     * @return FlpsCalculationResponse with score and component breakdowns
     */
    @PostMapping("/calculate")
    fun calculateFlpsScore(
        @RequestBody request: FlpsCalculationRequest
    ): ResponseEntity<FlpsCalculationResponse> {
        val command = CalculateFlpsScoreCommand(
            guildId = request.guildId,
            attendancePercent = request.attendancePercent,
            deathsPerAttempt = request.deathsPerAttempt,
            specAvgDpa = request.specAvgDpa,
            avoidableDamagePct = request.avoidableDamagePct,
            specAvgAdt = request.specAvgAdt,
            vaultSlots = request.vaultSlots,
            crestUsageRatio = request.crestUsageRatio,
            heroicKills = request.heroicKills,
            simulatedGain = request.simulatedGain,
            specBaseline = request.specBaseline,
            tierPiecesOwned = request.tierPiecesOwned,
            role = parseRaiderRole(request.role),
            recentLootCount = request.recentLootCount
        )

        return calculateFlpsScoreUseCase.execute(command)
            .map { result -> toCalculationResponse(result) }
            .map { response -> ResponseEntity.ok(response) }
            .getOrElse { exception -> 
                throw ValidationException(exception.message ?: "FLPS calculation failed")
            }
    }

    /**
     * Generate comprehensive FLPS report for a guild.
     *
     * @param request The report parameters including all raiders
     * @return FlpsReportResponse with scores for all raiders and configuration
     */
    @PostMapping("/report")
    fun getFlpsReport(
        @RequestBody request: FlpsReportRequest
    ): ResponseEntity<FlpsReportResponse> {
        val query = GetFlpsReportQuery(
            guildId = request.guildId,
            raiders = request.raiders.map { raider ->
                RaiderFlpsData(
                    raiderId = raider.raiderId,
                    raiderName = raider.raiderName,
                    attendancePercent = raider.attendancePercent,
                    deathsPerAttempt = raider.deathsPerAttempt,
                    specAvgDpa = raider.specAvgDpa,
                    avoidableDamagePct = raider.avoidableDamagePct,
                    specAvgAdt = raider.specAvgAdt,
                    vaultSlots = raider.vaultSlots,
                    crestUsageRatio = raider.crestUsageRatio,
                    heroicKills = raider.heroicKills,
                    simulatedGain = raider.simulatedGain,
                    specBaseline = raider.specBaseline,
                    tierPiecesOwned = raider.tierPiecesOwned,
                    role = parseRaiderRole(raider.role),
                    recentLootCount = raider.recentLootCount
                )
            }
        )

        return getFlpsReportUseCase.execute(query)
            .map { report -> toReportResponse(report) }
            .map { response -> ResponseEntity.ok(response) }
            .getOrElse { exception -> 
                throw ValidationException(exception.message ?: "FLPS report generation failed")
            }
    }

    /**
     * Update FLPS modifiers and configuration for a guild.
     *
     * @param guildId The guild identifier
     * @param request The new configuration
     * @return 204 No Content on success
     */
    @PutMapping("/{guildId}/modifiers")
    fun updateModifiers(
        @PathVariable guildId: String,
        @RequestBody request: UpdateFlpsModifiersRequest
    ): ResponseEntity<Void> {
        val command = UpdateModifiersCommand(
            guildId = guildId,
            rmsWeights = RmsWeights(
                attendance = request.rmsWeights.attendance,
                mechanical = request.rmsWeights.mechanical,
                preparation = request.rmsWeights.preparation
            ),
            ipiWeights = IpiWeights(
                upgradeValue = request.ipiWeights.upgradeValue,
                tierBonus = request.ipiWeights.tierBonus,
                roleMultiplier = request.ipiWeights.roleMultiplier
            ),
            thresholds = FlpsThresholds(
                eligibilityAttendance = request.thresholds.eligibilityAttendance,
                eligibilityActivity = request.thresholds.eligibilityActivity
            ),
            roleMultipliers = RoleMultipliers(
                tank = request.roleMultipliers.tank,
                healer = request.roleMultipliers.healer,
                dps = request.roleMultipliers.dps
            ),
            recencyPenalties = RecencyPenalties(
                tierA = request.recencyPenalties.tierA,
                tierB = request.recencyPenalties.tierB,
                tierC = request.recencyPenalties.tierC,
                recoveryRate = request.recencyPenalties.recoveryRate
            )
        )

        return updateModifiersUseCase.execute(command)
            .map { ResponseEntity.noContent().build<Void>() }
            .getOrElse { exception -> 
                throw ValidationException(exception.message ?: "FLPS modifier update failed")
            }
    }

    /**
     * Get FLPS configuration for a guild.
     *
     * @param guildId The guild identifier
     * @return FlpsConfigurationResponse with current configuration
     */
    @GetMapping("/{guildId}/modifiers")
    fun getModifiers(
        @PathVariable guildId: String
    ): ResponseEntity<FlpsConfigurationResponse> {
        // This would typically use a GetModifiersUseCase, but for now we'll access the repository directly
        // In a future iteration, this should be refactored to use a proper use case
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    // Helper methods for mapping

    private fun parseRaiderRole(role: String): RaiderRole {
        return when (role.uppercase()) {
            "TANK" -> RaiderRole.TANK
            "HEALER" -> RaiderRole.HEALER
            "DPS" -> RaiderRole.DPS
            else -> throw IllegalArgumentException("Invalid role: $role. Must be TANK, HEALER, or DPS")
        }
    }

    private fun toCalculationResponse(result: FlpsCalculationResult): FlpsCalculationResponse {
        return FlpsCalculationResponse(
            flpsScore = result.flpsScore.value,
            raiderMerit = result.raiderMerit.value,
            itemPriority = result.itemPriority.value,
            recencyDecay = result.recencyDecay.value,
            attendanceScore = result.attendanceScore,
            mechanicalScore = result.mechanicalScore,
            preparationScore = result.preparationScore,
            upgradeValue = result.upgradeValue,
            tierBonus = result.tierBonus,
            roleMultiplier = result.roleMultiplier
        )
    }

    private fun toReportResponse(report: FlpsReport): FlpsReportResponse {
        return FlpsReportResponse(
            guildId = report.guildId,
            raiderReports = report.raiderReports.map { raider ->
                RaiderFlpsReportResponse(
                    raiderId = raider.raiderId,
                    raiderName = raider.raiderName,
                    flpsScore = raider.flpsScore.value,
                    raiderMerit = raider.raiderMerit.value,
                    itemPriority = raider.itemPriority.value,
                    recencyDecay = raider.recencyDecay.value,
                    attendanceScore = raider.attendanceScore,
                    mechanicalScore = raider.mechanicalScore,
                    preparationScore = raider.preparationScore,
                    upgradeValue = raider.upgradeValue,
                    tierBonus = raider.tierBonus,
                    roleMultiplier = raider.roleMultiplier,
                    attendancePercentage = raider.attendancePercentage,
                    mechanicalPercentage = raider.mechanicalPercentage,
                    preparationPercentage = raider.preparationPercentage,
                    rmsPercentage = raider.rmsPercentage,
                    upgradeValuePercentage = raider.upgradeValuePercentage,
                    tierBonusPercentage = raider.tierBonusPercentage,
                    roleMultiplierPercentage = raider.roleMultiplierPercentage,
                    ipiPercentage = raider.ipiPercentage,
                    recencyDecayPercentage = raider.recencyDecayPercentage,
                    flpsPercentage = raider.flpsPercentage
                )
            },
            configuration = toConfigurationResponse(report.configuration)
        )
    }

    private fun toConfigurationResponse(config: FlpsConfiguration): FlpsConfigurationResponse {
        return FlpsConfigurationResponse(
            rmsWeights = RmsWeightsResponse(
                attendance = config.rmsWeights.attendance,
                mechanical = config.rmsWeights.mechanical,
                preparation = config.rmsWeights.preparation
            ),
            ipiWeights = IpiWeightsResponse(
                upgradeValue = config.ipiWeights.upgradeValue,
                tierBonus = config.ipiWeights.tierBonus,
                roleMultiplier = config.ipiWeights.roleMultiplier
            ),
            thresholds = FlpsThresholdsResponse(
                eligibilityAttendance = config.thresholds.eligibilityAttendance,
                eligibilityActivity = config.thresholds.eligibilityActivity
            ),
            roleMultipliers = RoleMultipliersResponse(
                tank = config.roleMultipliers.tank,
                healer = config.roleMultipliers.healer,
                dps = config.roleMultipliers.dps
            ),
            recencyPenalties = RecencyPenaltiesResponse(
                tierA = config.recencyPenalties.tierA,
                tierB = config.recencyPenalties.tierB,
                tierC = config.recencyPenalties.tierC,
                recoveryRate = config.recencyPenalties.recoveryRate
            )
        )
    }
}
