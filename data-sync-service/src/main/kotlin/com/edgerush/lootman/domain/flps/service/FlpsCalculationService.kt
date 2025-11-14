package com.edgerush.lootman.domain.flps.service

import com.edgerush.lootman.domain.flps.model.*

/**
 * Domain service for calculating Final Loot Priority Score (FLPS).
 *
 * This service implements the core FLPS algorithm:
 * FLPS = (RMS × IPI) × RDF
 *
 * Where:
 * - RMS = Raider Merit Score (attendance, mechanical skill, preparation)
 * - IPI = Item Priority Index (upgrade value, tier bonus, role multiplier)
 * - RDF = Recency Decay Factor (reduces score for recent loot recipients)
 *
 * This is a stateless domain service that contains pure business logic.
 */
class FlpsCalculationService {
    /**
     * Calculates FLPS from the three main component scores.
     *
     * @param rms Raider Merit Score
     * @param ipi Item Priority Index
     * @param rdf Recency Decay Factor
     * @return Calculated FlpsScore
     */
    fun calculateFlps(
        rms: RaiderMeritScore,
        ipi: ItemPriorityIndex,
        rdf: RecencyDecayFactor,
    ): FlpsScore {
        val flpsValue = (rms.value * ipi.value) * rdf.value
        return FlpsScore.of(flpsValue.coerceIn(0.0, 1.0))
    }

    /**
     * Calculates FLPS from all individual component scores.
     *
     * This method first calculates RMS and IPI from their components,
     * then calculates the final FLPS.
     *
     * @param acs Attendance Commitment Score
     * @param mas Mechanical Adherence Score
     * @param eps External Preparation Score
     * @param uv Upgrade Value
     * @param tb Tier Bonus
     * @param rm Role Multiplier
     * @param rdf Recency Decay Factor
     * @param attendanceWeight Weight for attendance in RMS (default 0.4)
     * @param mechanicalWeight Weight for mechanical in RMS (default 0.4)
     * @param preparationWeight Weight for preparation in RMS (default 0.2)
     * @param upgradeWeight Weight for upgrade in IPI (default 0.45)
     * @param tierWeight Weight for tier in IPI (default 0.35)
     * @param roleWeight Weight for role in IPI (default 0.20)
     * @return Calculated FlpsScore
     */
    fun calculateFlpsFromComponents(
        acs: AttendanceCommitmentScore,
        mas: MechanicalAdherenceScore,
        eps: ExternalPreparationScore,
        uv: UpgradeValue,
        tb: TierBonus,
        rm: RoleMultiplier,
        rdf: RecencyDecayFactor,
        attendanceWeight: Double = 0.4,
        mechanicalWeight: Double = 0.4,
        preparationWeight: Double = 0.2,
        upgradeWeight: Double = 0.45,
        tierWeight: Double = 0.35,
        roleWeight: Double = 0.20,
    ): FlpsScore {
        val rms =
            RaiderMeritScore.fromComponents(
                acs,
                mas,
                eps,
                attendanceWeight,
                mechanicalWeight,
                preparationWeight,
            )
        val ipi =
            ItemPriorityIndex.fromComponents(
                uv,
                tb,
                rm,
                upgradeWeight,
                tierWeight,
                roleWeight,
            )
        return calculateFlps(rms, ipi, rdf)
    }
}
