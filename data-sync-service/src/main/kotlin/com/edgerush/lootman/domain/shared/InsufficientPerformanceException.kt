package com.edgerush.lootman.domain.shared

import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore

/**
 * Thrown when a raider's performance score is below the required threshold for loot eligibility.
 */
class InsufficientPerformanceException(
    val raiderId: RaiderId,
    val actualScore: MechanicalAdherenceScore,
    val requiredThreshold: Double,
) : RuntimeException(
    "Raider ${raiderId.value} has MAS ${actualScore.value} which is below required threshold $requiredThreshold"
)
