package com.edgerush.datasync.application.flps

import com.edgerush.datasync.domain.flps.model.*
import com.edgerush.datasync.domain.flps.repository.FlpsConfiguration
import com.edgerush.datasync.domain.flps.repository.FlpsModifierRepository
import com.edgerush.datasync.domain.flps.service.FlpsCalculationService
import com.edgerush.datasync.domain.flps.service.RaiderRole
import org.springframework.stereotype.Service
import kotlin.math.max

/**
 * Use case for generating comprehensive FLPS reports for a guild.
 * This aggregates FLPS calculations for multiple raiders and provides
 * detailed breakdowns and percentages.
 */
@Service
class GetFlpsReportUseCase(
    private val flpsCalculationService: FlpsCalculationService,
    private val flpsModifierRepository: FlpsModifierRepository
) {

    /**
     * Executes the report generation for the given query.
     *
     * @param query The report parameters
     * @return Result containing FlpsReport or error
     */
    fun execute(query: GetFlpsReportQuery): Result<FlpsReport> = runCatching {
        // Load guild configuration
        val config = flpsModifierRepository.findByGuildId(query.guildId)

        // Calculate FLPS for each raider
        val raiderReports = query.raiders.map { raider ->
            calculateRaiderReport(raider, config)
        }.sortedByDescending { it.flpsScore }

        FlpsReport(
            guildId = query.guildId,
            raiderReports = raiderReports,
            configuration = config
        )
    }

    private fun calculateRaiderReport(
        raider: RaiderFlpsData,
        config: FlpsConfiguration
    ): RaiderFlpsReport {
        // Calculate RMS components
        val attendanceScore = flpsCalculationService.calculateAttendanceCommitmentScore(
            max(0, raider.attendancePercent)
        )
        
        val mechanicalScore = flpsCalculationService.calculateMechanicalAdherenceScore(
            deathsPerAttempt = max(0.0, raider.deathsPerAttempt),
            specAvgDpa = raider.specAvgDpa,
            avoidableDamagePct = max(0.0, raider.avoidableDamagePct),
            specAvgAdt = raider.specAvgAdt
        )
        
        val preparationScore = flpsCalculationService.calculateExternalPreparationScore(
            vaultSlots = max(0, raider.vaultSlots),
            crestUsageRatio = raider.crestUsageRatio.coerceIn(0.0, 1.0),
            heroicKills = max(0, raider.heroicKills)
        )

        // Calculate weighted RMS
        val rms = (attendanceScore * config.rmsWeights.attendance +
                   mechanicalScore * config.rmsWeights.mechanical +
                   preparationScore * config.rmsWeights.preparation) /
                  (config.rmsWeights.attendance + config.rmsWeights.mechanical + config.rmsWeights.preparation)
        
        val raiderMerit = RaiderMeritScore.of(rms)

        // Calculate IPI components
        val upgradeValue = flpsCalculationService.calculateUpgradeValue(
            simulatedGain = max(0.0, raider.simulatedGain),
            specBaseline = raider.specBaseline
        )
        
        val tierBonus = flpsCalculationService.calculateTierBonus(
            tierPiecesOwned = max(0, raider.tierPiecesOwned)
        )
        
        val roleMultiplier = flpsCalculationService.calculateRoleMultiplier(raider.role)

        // Calculate weighted IPI
        val ipi = (upgradeValue * config.ipiWeights.upgradeValue +
                   tierBonus * config.ipiWeights.tierBonus +
                   roleMultiplier * config.ipiWeights.roleMultiplier)
        
        val itemPriority = ItemPriorityIndex.of(ipi.coerceIn(0.0, 1.0))

        // Calculate RDF
        val rdfValue = calculateRecencyDecay(max(0, raider.recentLootCount), config)
        val recencyDecay = RecencyDecayFactor.of(rdfValue)

        // Calculate final FLPS
        val flpsScore = flpsCalculationService.calculateFlpsScore(
            raiderMerit = raiderMerit,
            itemPriority = itemPriority,
            recencyDecay = recencyDecay
        )

        // Calculate percentages (relative to perfect scores)
        val attendancePercentage = (attendanceScore / 1.0) * 100.0
        val mechanicalPercentage = (mechanicalScore / 1.0) * 100.0
        val preparationPercentage = (preparationScore / 1.0) * 100.0
        val rmsPercentage = (raiderMerit.value / 1.0) * 100.0
        
        val upgradeValuePercentage = (upgradeValue / 1.0) * 100.0
        val tierBonusPercentage = (tierBonus / 1.2) * 100.0 // Max tier bonus is 1.2
        val roleMultiplierPercentage = (roleMultiplier / 1.0) * 100.0 // Max role multiplier is 1.0
        val ipiPercentage = (itemPriority.value / 1.0) * 100.0
        
        val recencyDecayPercentage = (recencyDecay.value / 1.0) * 100.0
        val flpsPercentage = (flpsScore.value / 1.0) * 100.0

        return RaiderFlpsReport(
            raiderId = raider.raiderId,
            raiderName = raider.raiderName,
            flpsScore = flpsScore,
            raiderMerit = raiderMerit,
            itemPriority = itemPriority,
            recencyDecay = recencyDecay,
            attendanceScore = attendanceScore,
            mechanicalScore = mechanicalScore,
            preparationScore = preparationScore,
            upgradeValue = upgradeValue,
            tierBonus = tierBonus,
            roleMultiplier = roleMultiplier,
            attendancePercentage = attendancePercentage,
            mechanicalPercentage = mechanicalPercentage,
            preparationPercentage = preparationPercentage,
            rmsPercentage = rmsPercentage,
            upgradeValuePercentage = upgradeValuePercentage,
            tierBonusPercentage = tierBonusPercentage,
            roleMultiplierPercentage = roleMultiplierPercentage,
            ipiPercentage = ipiPercentage,
            recencyDecayPercentage = recencyDecayPercentage,
            flpsPercentage = flpsPercentage
        )
    }

    private fun calculateRecencyDecay(recentLootCount: Int, config: FlpsConfiguration): Double {
        return when (recentLootCount) {
            0 -> 1.0
            1 -> config.recencyPenalties.tierC
            2 -> config.recencyPenalties.tierB
            else -> config.recencyPenalties.tierA
        }
    }
}

/**
 * Query for generating FLPS report.
 */
data class GetFlpsReportQuery(
    val guildId: String,
    val raiders: List<RaiderFlpsData>
)

/**
 * Input data for a single raider's FLPS calculation.
 */
data class RaiderFlpsData(
    val raiderId: String,
    val raiderName: String,
    // RMS inputs
    val attendancePercent: Int,
    val deathsPerAttempt: Double,
    val specAvgDpa: Double,
    val avoidableDamagePct: Double,
    val specAvgAdt: Double,
    val vaultSlots: Int,
    val crestUsageRatio: Double,
    val heroicKills: Int,
    // IPI inputs
    val simulatedGain: Double,
    val specBaseline: Double,
    val tierPiecesOwned: Int,
    val role: RaiderRole,
    // RDF inputs
    val recentLootCount: Int
)

/**
 * Complete FLPS report for a guild.
 */
data class FlpsReport(
    val guildId: String,
    val raiderReports: List<RaiderFlpsReport>,
    val configuration: FlpsConfiguration
)

/**
 * FLPS report for a single raider with all component scores and percentages.
 */
data class RaiderFlpsReport(
    val raiderId: String,
    val raiderName: String,
    // Final scores
    val flpsScore: FlpsScore,
    val raiderMerit: RaiderMeritScore,
    val itemPriority: ItemPriorityIndex,
    val recencyDecay: RecencyDecayFactor,
    // Component scores (raw values)
    val attendanceScore: Double,
    val mechanicalScore: Double,
    val preparationScore: Double,
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double,
    // Percentages (relative to perfect scores)
    val attendancePercentage: Double,
    val mechanicalPercentage: Double,
    val preparationPercentage: Double,
    val rmsPercentage: Double,
    val upgradeValuePercentage: Double,
    val tierBonusPercentage: Double,
    val roleMultiplierPercentage: Double,
    val ipiPercentage: Double,
    val recencyDecayPercentage: Double,
    val flpsPercentage: Double
)
