package com.edgerush.lootman.application.flps

import com.edgerush.lootman.application.simulation.UpgradeValueCalculator
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.flps.model.AttendanceCommitmentScore
import com.edgerush.lootman.domain.flps.model.ExternalPreparationScore
import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.model.RaiderPreparationData
import com.edgerush.lootman.domain.flps.model.RecencyDecayFactor
import com.edgerush.lootman.domain.flps.model.RoleMultiplier
import com.edgerush.lootman.domain.flps.model.TierBonus
import com.edgerush.lootman.domain.flps.model.UpgradeValue
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
     * Calculate Mechanical Adherence Score (MAS) from Warcraft Logs performance data.
     *
     * MAS is calculated based on:
     * - Deaths per attempt (DPA): Lower is better, weighted at 60%
     * - Avoidable damage percentage (ADT): Lower is better, weighted at 40%
     *
     * @param performanceData Aggregated performance data from Warcraft Logs, or null if unavailable
     * @return MAS score between 0.0 and 1.0
     */
    fun calculateMAS(performanceData: RaiderPerformanceData?): MechanicalAdherenceScore {
        if (performanceData == null || performanceData.totalFights == 0) {
            return MechanicalAdherenceScore.of(0.0)
        }

        // Weight factors (configurable via guild settings in future)
        val deathsWeight = 0.6
        val avoidableDamageWeight = 0.4

        // Calculate deaths per attempt score
        // 0 deaths = 1.0, 1 death/attempt = 0.5, 2+ deaths/attempt = approaching 0
        val dpa = performanceData.deathsPerAttempt
        val deathsScore = when {
            dpa <= 0.0 -> 1.0
            dpa <= 0.5 -> 1.0 - (dpa * 0.4)  // 0.5 dpa = 0.8 score
            dpa <= 1.0 -> 0.8 - ((dpa - 0.5) * 0.6) // 1.0 dpa = 0.5 score
            dpa <= 2.0 -> 0.5 - ((dpa - 1.0) * 0.3) // 2.0 dpa = 0.2 score
            else -> (0.2 - ((dpa - 2.0) * 0.1)).coerceAtLeast(0.0)
        }

        // Calculate avoidable damage score
        // 0% = 1.0, 50% = 0.5, 100%+ = approaching 0
        val adtPct = performanceData.avoidableDamagePercentage
        val avoidableDamageScore = when {
            adtPct <= 10.0 -> 1.0 - (adtPct * 0.01) // 10% = 0.9 score
            adtPct <= 30.0 -> 0.9 - ((adtPct - 10.0) * 0.015) // 30% = 0.6 score
            adtPct <= 60.0 -> 0.6 - ((adtPct - 30.0) * 0.01) // 60% = 0.3 score
            adtPct <= 100.0 -> 0.3 - ((adtPct - 60.0) * 0.005) // 100% = 0.1 score
            else -> (0.1 - ((adtPct - 100.0) * 0.001)).coerceAtLeast(0.0)
        }

        // Combine weighted scores
        val masValue = (deathsScore * deathsWeight) + (avoidableDamageScore * avoidableDamageWeight)

        return MechanicalAdherenceScore.of(masValue.coerceIn(0.0, 1.0))
    }

    /**
     * Calculate Mechanical Adherence Score (MAS) - legacy no-args version.
     * Returns 0.0 as fallback when no performance data is available.
     *
     * @deprecated Use calculateMAS(performanceData) instead
     */
    @Deprecated("Use calculateMAS(performanceData) instead", ReplaceWith("calculateMAS(null)"))
    fun calculateMAS(): MechanicalAdherenceScore {
        return MechanicalAdherenceScore.of(0.0)
    }

    /**
     * Calculate External Preparation Score (EPS) from gear and preparation data.
     *
     * EPS is calculated based on:
     * - Vault unlock status (raid, M+, PvP) - weighted by importance
     * - Mythic+ rating - normalized to 0-1 scale
     * - Heroic/Normal clear status - bonus points
     * - Base gear presence - fallback when no preparation data
     *
     * Weights (total 100%):
     * - Raid vault: 35% (most important for raiders)
     * - M+ vault: 20%
     * - PvP vault: 5%
     * - M+ rating: 25%
     * - Heroic clear: 10%
     * - Normal clear: 5%
     *
     * @param gear The raider's current gear set, or null if unavailable
     * @param preparation The raider's vault/activity data, or null if unavailable
     * @return EPS score between 0.0 and 1.0
     */
    fun calculateEPS(gear: GearSet?, preparation: RaiderPreparationData?): ExternalPreparationScore {
        // If both are null, return zero
        if (gear == null && preparation == null) {
            return ExternalPreparationScore.of(0.0)
        }

        // If no preparation data, fall back to legacy behavior
        if (preparation == null) {
            return ExternalPreparationScore.of(if (gear != null) 0.7 else 0.0)
        }

        // Raid vault contribution (35% max, ~11.67% per slot)
        val raidVaultScore = (preparation.raidVaultSlots / 3.0) * 0.35

        // M+ vault contribution (20% max, ~6.67% per slot)
        val mythicPlusVaultScore = (preparation.mythicPlusVaultSlots / 3.0) * 0.20

        // PvP vault contribution (5% max)
        val pvpVaultScore = (preparation.pvpVaultSlots / 3.0) * 0.05

        // M+ rating contribution (25% max)
        // Rating normalized: 0 = 0, 2500+ = 1.0
        val normalizedRating = (preparation.mythicPlusRating / 2500.0).coerceIn(0.0, 1.0)
        val ratingScore = normalizedRating * 0.25

        // Heroic clear bonus (10%)
        val heroicClearScore = if (preparation.hasHeroicClear) 0.10 else 0.0

        // Normal clear bonus (5%)
        val normalClearScore = if (preparation.hasNormalClear) 0.05 else 0.0

        // Total score
        val totalScore = raidVaultScore +
            mythicPlusVaultScore +
            pvpVaultScore +
            ratingScore +
            heroicClearScore +
            normalClearScore

        return ExternalPreparationScore.of(totalScore.coerceIn(0.0, 1.0))
    }

    /**
     * Calculate External Preparation Score (EPS) from gear only.
     * This is the legacy method - prefer calculateEPS(gear, preparation) when preparation data is available.
     *
     * @deprecated Use calculateEPS(gear, preparation) instead
     */
    @Deprecated("Use calculateEPS(gear, preparation) instead", ReplaceWith("calculateEPS(gear, null)"))
    fun calculateEPS(gear: GearSet?): ExternalPreparationScore {
        // Legacy behavior: Base score on having gear
        val baseScore = if (gear != null) 0.7 else 0.0
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
