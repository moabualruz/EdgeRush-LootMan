package com.edgerush.datasync.domain.flps.service

import com.edgerush.datasync.domain.flps.model.*
import org.springframework.stereotype.Service
import kotlin.math.max
import kotlin.math.min

/**
 * Domain service for calculating FLPS (Final Loot Priority Score) and its components.
 *
 * FLPS Formula: FLPS = (RMS × IPI) × RDF
 * Where:
 * - RMS = Raider Merit Score (attendance + mechanical + preparation)
 * - IPI = Item Priority Index (upgrade value + tier bonus + role multiplier)
 * - RDF = Recency Decay Factor (penalty for recent loot)
 *
 * This service contains the core business logic for FLPS calculations,
 * independent of infrastructure concerns.
 */
@Service
class FlpsCalculationService {

    /**
     * Calculates the final FLPS score from its three main components.
     *
     * @param raiderMerit Raider Merit Score [0.0, 1.0]
     * @param itemPriority Item Priority Index [0.0, 1.0]
     * @param recencyDecay Recency Decay Factor [0.0, 1.0]
     * @return Final FLPS score [0.0, 1.0]
     */
    fun calculateFlpsScore(
        raiderMerit: RaiderMeritScore,
        itemPriority: ItemPriorityIndex,
        recencyDecay: RecencyDecayFactor
    ): FlpsScore {
        val flps = (raiderMerit.value * itemPriority.value) * recencyDecay.value
        return FlpsScore.of(flps.coerceIn(0.0, 1.0))
    }

    /**
     * Calculates Attendance Commitment Score (ACS) from attendance percentage.
     *
     * Scoring:
     * - 100%+ attendance = 1.0
     * - 80-99% attendance = 0.9
     * - <80% attendance = 0.0 (ineligible)
     *
     * @param attendancePercent Attendance percentage [0, 100+]
     * @return ACS score [0.0, 1.0]
     */
    fun calculateAttendanceCommitmentScore(attendancePercent: Int): Double {
        return when {
            attendancePercent >= 100 -> 1.0
            attendancePercent >= 80 -> 0.9
            else -> 0.0
        }
    }

    /**
     * Calculates Mechanical Adherence Score (MAS) from performance metrics.
     *
     * MAS measures how well a raider executes mechanics compared to their spec average:
     * - Deaths per attempt (DPA) ratio
     * - Avoidable damage taken (ADT) ratio
     *
     * Penalties apply when ratios exceed 1.5x spec average.
     *
     * @param deathsPerAttempt Raider's deaths per attempt
     * @param specAvgDpa Spec average deaths per attempt
     * @param avoidableDamagePct Raider's avoidable damage percentage
     * @param specAvgAdt Spec average avoidable damage percentage
     * @return MAS score [0.0, 1.0]
     */
    fun calculateMechanicalAdherenceScore(
        deathsPerAttempt: Double,
        specAvgDpa: Double,
        avoidableDamagePct: Double,
        specAvgAdt: Double
    ): Double {
        // Handle division by zero
        val dpaRatio = if (specAvgDpa == 0.0) 0.0 else deathsPerAttempt / specAvgDpa
        val adtRatio = if (specAvgAdt == 0.0) 0.0 else avoidableDamagePct / specAvgAdt

        // Automatic failure if performance is 1.5x worse than spec average
        if (dpaRatio > 1.5 || adtRatio > 1.5) {
            return 0.0
        }

        // Calculate penalty: 25% penalty per component for each 1.0 ratio above baseline
        val penalty = ((dpaRatio - 1.0) * 0.25) + ((adtRatio - 1.0) * 0.25)
        return max(0.0, min(1.0, 1.0 - penalty))
    }

    /**
     * Calculates External Preparation Score (EPS) from out-of-raid activities.
     *
     * EPS measures raider preparation through:
     * - Vault slots filled (50% weight)
     * - Crest usage ratio (30% weight)
     * - Heroic boss kills (20% weight)
     *
     * @param vaultSlots Number of vault slots filled [0, 9]
     * @param crestUsageRatio Ratio of crests used [0.0, 1.0]
     * @param heroicKills Number of heroic bosses killed [0, 8+]
     * @return EPS score [0.0, 1.0]
     */
    fun calculateExternalPreparationScore(
        vaultSlots: Int,
        crestUsageRatio: Double,
        heroicKills: Int
    ): Double {
        val vaultScore = min(1.0, vaultSlots / 3.0)
        val crestScore = crestUsageRatio.coerceIn(0.0, 1.0)
        val heroicScore = min(1.0, heroicKills / 6.0)

        val eps = (vaultScore * 0.5) + (crestScore * 0.3) + (heroicScore * 0.2)
        return min(1.0, eps)
    }

    /**
     * Calculates Upgrade Value (UV) from simulation data.
     *
     * UV = simulatedGain / specBaseline
     *
     * @param simulatedGain DPS/HPS gain from item
     * @param specBaseline Baseline output for spec
     * @return UV score [0.0, 1.0+]
     */
    fun calculateUpgradeValue(simulatedGain: Double, specBaseline: Double): Double {
        if (specBaseline <= 0.0) return 0.0
        return simulatedGain / specBaseline
    }

    /**
     * Calculates Tier Bonus multiplier based on current tier pieces owned.
     *
     * Scoring:
     * - 0-1 pieces = 1.2x (high priority to complete set)
     * - 2-3 pieces = 1.1x (medium priority)
     * - 4+ pieces = 1.0x (set complete, normal priority)
     *
     * @param tierPiecesOwned Number of tier pieces currently owned [0, 5]
     * @return Tier bonus multiplier [1.0, 1.2]
     */
    fun calculateTierBonus(tierPiecesOwned: Int): Double {
        return when {
            tierPiecesOwned <= 1 -> 1.2
            tierPiecesOwned <= 3 -> 1.1
            else -> 1.0
        }
    }

    /**
     * Calculates Role Multiplier based on role scarcity/importance.
     *
     * Scoring:
     * - DPS = 1.0 (baseline)
     * - Tank = 0.8 (lower priority due to limited slots)
     * - Healer = 0.7 (lowest priority due to limited slots)
     *
     * @param role Raider's role
     * @return Role multiplier [0.7, 1.0]
     */
    fun calculateRoleMultiplier(role: RaiderRole): Double {
        return when (role) {
            RaiderRole.DPS -> 1.0
            RaiderRole.TANK -> 0.8
            RaiderRole.HEALER -> 0.7
        }
    }
}

/**
 * Enum representing raider roles in the game.
 */
enum class RaiderRole {
    TANK,
    HEALER,
    DPS
}
