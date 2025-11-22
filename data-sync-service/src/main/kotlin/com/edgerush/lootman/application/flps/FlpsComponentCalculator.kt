package com.edgerush.lootman.application.flps

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.flps.model.*
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.shared.model.Role
import com.edgerush.lootman.domain.shared.model.Wishlist
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Service to calculate individual FLPS components from raider data.
 *
 * Converts raw data (attendance records, loot history, wishlist, gear)
 * into FLPS component scores (ACS, MAS, EPS, UV, TB, RM, RDF).
 */
@Service
class FlpsComponentCalculator {

    /**
     * Calculate Attendance Commitment Score (ACS) from attendance records.
     */
    fun calculateACS(attendance: List<AttendanceRecord>): AttendanceCommitmentScore {
        if (attendance.isEmpty()) {
            return AttendanceCommitmentScore.of(0.0)
        }

        val totalAttended = attendance.sumOf { it.attendedAmount }
        val totalPossible = attendance.sumOf { it.totalAmount }

        val percentage = if (totalPossible > 0) {
            totalAttended.toDouble() / totalPossible
        } else {
            0.0
        }

        return AttendanceCommitmentScore.of(percentage.coerceIn(0.0, 1.0))
    }

    /**
     * Calculate Mechanical Adherence Score (MAS).
     * Currently returns 0.0 - requires Warcraft Logs integration.
     */
    fun calculateMAS(): MechanicalAdherenceScore {
        // TODO: Implement with Warcraft Logs data (deaths, avoidable damage)
        return MechanicalAdherenceScore.of(0.0)
    }

    /**
     * Calculate External Preparation Score (EPS) from gear and activity.
     * Currently simplified - needs vault/crest/M+ data.
     */
    fun calculateEPS(gear: GearSet?): ExternalPreparationScore {
        // Simplified: Base score on having gear
        val baseScore = if (gear != null) 0.7 else 0.0

        // TODO: Add vault unlocks, crest usage, M+ score
        return ExternalPreparationScore.of(baseScore.coerceIn(0.0, 1.0))
    }

    /**
     * Calculate Upgrade Value (UV) from wishlist data.
     */
    fun calculateUV(wishlist: Wishlist?, itemId: ItemId): UpgradeValue {
        val upgradePercentage = wishlist?.getUpgradePercentage(itemId) ?: 0.0

        // Convert percentage to 0-1 range
        val normalizedValue = (upgradePercentage / 100.0).coerceIn(0.0, 1.0)

        return UpgradeValue.of(normalizedValue)
    }

    /**
     * Calculate Tier Bonus value based on current gear.
     */
    fun calculateTierBonus(gear: GearSet?): TierBonus {
        if (gear == null) {
            return TierBonus.of(0.0)
        }

        val tierCount = gear.getTierPieceCount()

        // Value increases significantly when approaching tier bonuses
        val value = when {
            tierCount >= 4 -> 1.0      // Has 4-piece
            tierCount == 3 -> 0.8      // Close to 4-piece
            tierCount >= 2 -> 0.6      // Has 2-piece
            tierCount == 1 -> 0.3      // Close to 2-piece
            else -> 0.0                 // No tier
        }

        return TierBonus.of(value)
    }

    /**
     * Calculate Role Multiplier based on raider's role.
     */
    fun calculateRoleMultiplier(role: Role): RoleMultiplier {
        // Default multipliers (can be made guild-configurable)
        val value = when (role) {
            Role.TANK -> 1.0
            Role.HEALER -> 1.0
            Role.DPS -> 1.0
        }

        return RoleMultiplier.of(value)
    }

    /**
     * Calculate Recency Decay Factor (RDF) from recent loot awards and active bans.
     */
    fun calculateRDF(
        lootHistory: List<LootAward>,
        activeBans: List<LootBan>
    ): RecencyDecayFactor {
        // If banned, RDF is 0
        if (activeBans.isNotEmpty()) {
            return RecencyDecayFactor.of(0.0)
        }

        val now = LocalDateTime.now()
        val twoWeeksAgo = now.minusWeeks(2)
        val oneWeekAgo = now.minusWeeks(1)

        // Count recent A-tier and B-tier loot
        val recentATier = lootHistory.count {
            it.tier == LootTier.A && it.awardedAt.isAfter(twoWeeksAgo)
        }

        val recentBTier = lootHistory.count {
            it.tier == LootTier.B && it.awardedAt.isAfter(oneWeekAgo)
        }

        // Apply decay based on recent loot
        val decayValue = when {
            recentATier > 0 -> 0.8  // 20% penalty for A-tier loot
            recentBTier > 0 -> 0.9  // 10% penalty for B-tier loot
            else -> 1.0             // No penalty
        }

        return RecencyDecayFactor.of(decayValue)
    }
}
