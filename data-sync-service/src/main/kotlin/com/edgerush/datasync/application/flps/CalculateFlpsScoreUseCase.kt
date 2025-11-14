package com.edgerush.datasync.application.flps

import com.edgerush.datasync.domain.flps.model.*
import com.edgerush.datasync.domain.flps.repository.FlpsModifierRepository
import com.edgerush.datasync.domain.flps.service.FlpsCalculationService
import com.edgerush.datasync.domain.flps.service.RaiderRole
import org.springframework.stereotype.Service
import kotlin.math.max

/**
 * Use case for calculating FLPS score for a raider and item combination.
 * This orchestrates the domain services to compute all FLPS components.
 */
@Service
class CalculateFlpsScoreUseCase(
    private val flpsCalculationService: FlpsCalculationService,
    private val flpsModifierRepository: FlpsModifierRepository
) {

    /**
     * Executes the FLPS calculation for the given command.
     *
     * @param command The calculation parameters
     * @return Result containing FlpsCalculationResult or error
     */
    fun execute(command: CalculateFlpsScoreCommand): Result<FlpsCalculationResult> = runCatching {
        // Load guild configuration
        val config = flpsModifierRepository.findByGuildId(command.guildId)

        // Calculate RMS components
        val attendanceScore = flpsCalculationService.calculateAttendanceCommitmentScore(
            max(0, command.attendancePercent)
        )
        
        val mechanicalScore = flpsCalculationService.calculateMechanicalAdherenceScore(
            deathsPerAttempt = max(0.0, command.deathsPerAttempt),
            specAvgDpa = command.specAvgDpa,
            avoidableDamagePct = max(0.0, command.avoidableDamagePct),
            specAvgAdt = command.specAvgAdt
        )
        
        val preparationScore = flpsCalculationService.calculateExternalPreparationScore(
            vaultSlots = max(0, command.vaultSlots),
            crestUsageRatio = command.crestUsageRatio.coerceIn(0.0, 1.0),
            heroicKills = max(0, command.heroicKills)
        )

        // Calculate weighted RMS
        val rms = (attendanceScore * config.rmsWeights.attendance +
                   mechanicalScore * config.rmsWeights.mechanical +
                   preparationScore * config.rmsWeights.preparation) /
                  (config.rmsWeights.attendance + config.rmsWeights.mechanical + config.rmsWeights.preparation)
        
        val raiderMerit = RaiderMeritScore.of(rms)

        // Calculate IPI components
        val upgradeValue = flpsCalculationService.calculateUpgradeValue(
            simulatedGain = max(0.0, command.simulatedGain),
            specBaseline = command.specBaseline
        )
        
        val tierBonus = flpsCalculationService.calculateTierBonus(
            tierPiecesOwned = max(0, command.tierPiecesOwned)
        )
        
        val roleMultiplier = flpsCalculationService.calculateRoleMultiplier(command.role)

        // Calculate weighted IPI
        val ipi = (upgradeValue * config.ipiWeights.upgradeValue +
                   tierBonus * config.ipiWeights.tierBonus +
                   roleMultiplier * config.ipiWeights.roleMultiplier)
        
        val itemPriority = ItemPriorityIndex.of(ipi.coerceIn(0.0, 1.0))

        // Calculate RDF based on recent loot count
        val rdfValue = calculateRecencyDecay(max(0, command.recentLootCount), config)
        val recencyDecay = RecencyDecayFactor.of(rdfValue)

        // Calculate final FLPS
        val flpsScore = flpsCalculationService.calculateFlpsScore(
            raiderMerit = raiderMerit,
            itemPriority = itemPriority,
            recencyDecay = recencyDecay
        )

        FlpsCalculationResult(
            flpsScore = flpsScore,
            raiderMerit = raiderMerit,
            itemPriority = itemPriority,
            recencyDecay = recencyDecay,
            attendanceScore = attendanceScore,
            mechanicalScore = mechanicalScore,
            preparationScore = preparationScore,
            upgradeValue = upgradeValue,
            tierBonus = tierBonus,
            roleMultiplier = roleMultiplier
        )
    }

    /**
     * Calculates recency decay factor based on recent loot count.
     * More recent loot = lower RDF = lower priority
     */
    private fun calculateRecencyDecay(recentLootCount: Int, config: com.edgerush.datasync.domain.flps.repository.FlpsConfiguration): Double {
        return when (recentLootCount) {
            0 -> 1.0
            1 -> config.recencyPenalties.tierC
            2 -> config.recencyPenalties.tierB
            else -> config.recencyPenalties.tierA
        }
    }
}

/**
 * Command containing all parameters needed for FLPS calculation.
 */
data class CalculateFlpsScoreCommand(
    val guildId: String,
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
 * Result containing calculated FLPS score and all component scores.
 */
data class FlpsCalculationResult(
    val flpsScore: FlpsScore,
    val raiderMerit: RaiderMeritScore,
    val itemPriority: ItemPriorityIndex,
    val recencyDecay: RecencyDecayFactor,
    // Component scores for transparency
    val attendanceScore: Double,
    val mechanicalScore: Double,
    val preparationScore: Double,
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double
)
