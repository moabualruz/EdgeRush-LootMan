package com.edgerush.lootman.application.flps

import com.edgerush.lootman.application.simulation.UpgradeValueCalculator
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
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Service to calculate individual FLPS components from raider data.
 *
 * Converts raw data (attendance records, loot history, wishlist, gear)
 * into FLPS component scores (ACS, MAS, EPS, UV, TB, RM, RDF).
 */
@Service
class FlpsComponentCalculator(
    private val upgradeValueCalculator: UpgradeValueCalculator? = null
) {

    /**
     * Calculate Attendance Commitment Score (ACS) from attendance records.
     */
    fun calculateACS(attendance: List<AttendanceRecord>): AttendanceCommitmentScore {
        if (attendance.isEmpty()) {
            return AttendanceCommitmentScore.of(0.0)
        }

        val totalAttended = attendance.sumOf { it.attendedRaids }
        val totalPossible = attendance.sumOf { it.totalRaids }

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
     * This is the legacy method - prefer calculateUVWithSimulation when character context is available.
     */
    fun calculateUV(wishlist: Wishlist?, itemId: ItemId): UpgradeValue {
        val upgradePercentage = wishlist?.getUpgradePercentage(itemId) ?: 0.0

        // Convert percentage to 0-1 range
        val normalizedValue = (upgradePercentage / 100.0).coerceIn(0.0, 1.0)

        return UpgradeValue.of(normalizedValue)
    }

    /**
     * Calculate Upgrade Value (UV) from simulation data with wishlist fallback.
     *
     * Priority:
     * 1. Use SimulationCraft simulation results if available
     * 2. Fall back to wishlist percentage if no simulation data
     * 3. Return zero if no data available
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @param characterRealm The realm name
     * @param itemId The WoW item ID
     * @param wishlist Optional wishlist for fallback
     * @return The calculated UpgradeValue (0.0-1.0)
     */
    fun calculateUVWithSimulation(
        guildId: String,
        characterName: String,
        characterRealm: String,
        itemId: ItemId,
        wishlist: Wishlist?
    ): UpgradeValue {
        // Use simulation-based calculator if available
        if (upgradeValueCalculator != null) {
            return upgradeValueCalculator.calculateUpgradeValue(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
                itemId = itemId,
                wishlistFallback = wishlist
            )
        }

        // Fall back to wishlist-only calculation
        return calculateUV(wishlist, itemId)
    }

    /**
     * Checks if simulation data is available for a character.
     */
    fun hasSimulationData(
        guildId: String,
        characterName: String,
        characterRealm: String
    ): Boolean {
        return upgradeValueCalculator?.hasSimulationData(
            guildId = guildId,
            characterName = characterName,
            characterRealm = characterRealm
        ) ?: false
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

        val now = Instant.now()
        val twoWeeksAgo = now.minus(14, ChronoUnit.DAYS)
        val oneWeekAgo = now.minus(7, ChronoUnit.DAYS)

        // Count recent Mythic and Heroic tier loot
        val recentMythic = lootHistory.count {
            it.tier == LootTier.MYTHIC && it.awardedAt.isAfter(twoWeeksAgo)
        }

        val recentHeroic = lootHistory.count {
            it.tier == LootTier.HEROIC && it.awardedAt.isAfter(oneWeekAgo)
        }

        // Apply decay based on recent loot
        val decayValue = when {
            recentMythic > 0 -> 0.8  // 20% penalty for Mythic loot
            recentHeroic > 0 -> 0.9  // 10% penalty for Heroic loot
            else -> 1.0              // No penalty
        }

        return RecencyDecayFactor.of(decayValue)
    }
}
