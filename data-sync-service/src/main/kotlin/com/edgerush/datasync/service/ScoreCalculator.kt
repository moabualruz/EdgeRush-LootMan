package com.edgerush.datasync.service

import com.edgerush.datasync.model.FlpsBreakdown
import com.edgerush.datasync.model.LootAward
import com.edgerush.datasync.model.LootTier
import com.edgerush.datasync.model.RaiderInput
import com.edgerush.datasync.model.Role
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

@Service
class ScoreCalculator(
    private val dataTransformer: WoWAuditDataTransformerService,
    private val modifierService: FlpsModifierService,
    private val warcraftLogsPerformanceService: com.edgerush.datasync.service.warcraftlogs.WarcraftLogsPerformanceService? = null,
    private val raidbotsUpgradeService: com.edgerush.datasync.service.raidbots.RaidbotsUpgradeService? = null,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun calculate(inputs: List<RaiderInput>): List<FlpsBreakdown> {
        val referenceDate = LocalDate.now(clock)
        return inputs.map { calculateForRaider(it, referenceDate) }
            .sortedByDescending { it.flps }
    }

    /**
     * Calculate FLPS scores using real WoWAudit data with guild-specific modifiers
     */
    fun calculateWithRealData(guildId: String = "default"): List<FlpsBreakdown> {
        val attendanceData = dataTransformer.getAttendanceData()
        val activityData = dataTransformer.getActivityData()
        val wishlistData = dataTransformer.getWishlistData()
        val lootHistoryData = dataTransformer.getLootHistoryData()
        val gearData = dataTransformer.getCharacterGearData()
        val guildConfig = modifierService.getGuildModifiers(guildId)

        // Get all unique characters from attendance data
        val charactersToCalculate = attendanceData.characters

        return charactersToCalculate.map { character ->
            calculateForCharacterWithRealData(
                character,
                activityData,
                wishlistData,
                lootHistoryData,
                gearData,
                guildConfig,
                guildId,
            )
        }.sortedByDescending { it.flps }
    }

    private fun calculateForCharacterWithRealData(
        character: CharacterAttendanceInfo,
        activityData: ActivityData,
        wishlistData: WishlistData,
        lootHistoryData: LootHistoryData,
        gearData: CharacterGearData,
        guildConfig: FlpsGuildConfig,
        guildId: String = "default",
    ): FlpsBreakdown {
        val characterKey = "${character.characterName}-${character.characterRealm}"

        // Find character-specific data
        val activity =
            activityData.characters.find {
                "${it.characterName}-${it.characterRealm}" == characterKey
            }
        val wishlist =
            wishlistData.characters.find {
                "${it.characterName}-${it.characterRealm}" == characterKey
            }
        val lootHistory =
            lootHistoryData.characters.find {
                "${it.characterName}-${it.characterRealm}" == characterKey
            }
        val gear =
            gearData.characters.find {
                "${it.characterName}-${it.characterRealm}" == characterKey
            }

        // Calculate RMS using real data with guild-specific weights
        val acs = attendanceCommitmentScore(character.attendancePercentage.toInt())
        val mas =
            calculateMechanicalAdherenceWithWarcraftLogs(
                character.characterName,
                character.characterRealm,
                guildId,
                activity,
            )
        val eps = calculateExternalPreparationFromActivity(activity)
        val rms =
            (acs * guildConfig.rmsWeights.attendance) +
                (mas * guildConfig.rmsWeights.mechanical) +
                (eps * guildConfig.rmsWeights.preparation)

        // Calculate IPI using real data with guild-specific weights
        val uv = calculateUpgradeValueFromWishlist(wishlist, gear)
        val tierBonus = calculateTierBonusFromGear(gear)
        val characterRole = Role.fromWoWAuditRole(character.role)
        val roleMultiplier = calculateRoleMultiplier(characterRole, guildConfig.roleMultipliers)
        val ipi =
            (uv * guildConfig.ipiWeights.upgradeValue) +
                (tierBonus * guildConfig.ipiWeights.tierBonus) +
                (roleMultiplier * guildConfig.ipiWeights.roleMultiplier)

        // Calculate RDF using real loot history
        val rdf = calculateRecencyDecayFromHistory(lootHistory)

        val flps = (rms * ipi) * rdf
        val eligible =
            acs >= guildConfig.thresholds.eligibilityAttendance &&
                mas > guildConfig.thresholds.eligibilityActivity

        return FlpsBreakdown(
            name = character.characterName,
            role = characterRole,
            acs = round(acs),
            mas = round(mas),
            eps = round(eps),
            rms = round(rms),
            upgradeValue = round(uv),
            tierBonus = round(tierBonus),
            roleMultiplier = round(roleMultiplier),
            ipi = round(ipi),
            rdf = round(rdf),
            flps = round(flps),
            eligible = eligible,
        )
    }

    fun calculateForRaider(
        input: RaiderInput,
        referenceDate: LocalDate = LocalDate.now(clock),
    ): FlpsBreakdown {
        val acs = attendanceCommitmentScore(input.attendancePercent)
        val mas =
            mechanicalAdherenceScore(
                input.deathsPerAttempt,
                input.specAverageDpa,
                input.avoidableDamagePct,
                input.specAverageAdt,
            )
        val eps = externalPreparationScore(input.vaultSlots, input.crestUsageRatio, input.heroicBossesCleared)
        val rms = (acs * 0.40) + (mas * 0.40) + (eps * 0.20)

        val uv = upgradeValue(input.simulatedGain, input.specBaselineOutput)
        val tierBonus = tierBonus(input.tierPiecesOwned)
        val roleMultiplier = roleMultiplier(input.role)
        val ipi = (uv * 0.45) + (tierBonus * 0.35) + (roleMultiplier * 0.20)

        val rdf = recencyDecayFactor(input.lastAwards, referenceDate)
        val flps = (rms * ipi) * rdf
        val eligible = acs >= 0.8 && mas > 0.0

        return FlpsBreakdown(
            name = input.name,
            role = input.role,
            acs = round(acs),
            mas = round(mas),
            eps = round(eps),
            rms = round(rms),
            upgradeValue = round(uv),
            tierBonus = round(tierBonus),
            roleMultiplier = round(roleMultiplier),
            ipi = round(ipi),
            rdf = round(rdf),
            flps = round(flps),
            eligible = eligible,
        )
    }

    private fun attendanceCommitmentScore(attendancePercent: Int): Double =
        when {
            attendancePercent >= 100 -> 1.0
            attendancePercent >= 80 -> 0.9
            else -> 0.0
        }

    private fun mechanicalAdherenceScore(
        deathsPerAttempt: Double,
        specAvgDpa: Double,
        avoidableDamagePct: Double,
        specAvgAdt: Double,
    ): Double {
        val dpaRatio = if (specAvgDpa == 0.0) 0.0 else deathsPerAttempt / specAvgDpa
        val adtRatio = if (specAvgAdt == 0.0) 0.0 else avoidableDamagePct / specAvgAdt

        if (dpaRatio > 1.5 || adtRatio > 1.5) {
            return 0.0
        }

        val penalty = ((dpaRatio - 1.0) * 0.25) + ((adtRatio - 1.0) * 0.25)
        return round(max(0.0, min(1.0, 1 - penalty)))
    }

    private fun externalPreparationScore(
        vaultSlots: Int,
        crestUsageRatio: Double,
        heroicKills: Int,
    ): Double {
        val vault = min(1.0, vaultSlots / 3.0)
        val crest = crestUsageRatio
        val heroic = min(1.0, heroicKills / 6.0)
        val eps = (vault * 0.5) + (crest * 0.3) + (heroic * 0.2)
        return round(min(1.0, eps))
    }

    private fun upgradeValue(
        simulatedGain: Double,
        specBaseline: Double,
    ): Double {
        if (specBaseline <= 0.0) return 0.0
        return round(simulatedGain / specBaseline)
    }

    private fun tierBonus(tierPiecesOwned: Int): Double =
        when {
            tierPiecesOwned <= 1 -> 1.2
            tierPiecesOwned <= 3 -> 1.1
            else -> 1.0
        }

    private fun roleMultiplier(role: Role): Double =
        when (role) {
            Role.DPS -> 1.0
            Role.Tank -> 0.8
            Role.Healer -> 0.7
        }

    private fun recencyDecayFactor(
        lastAwards: List<LootAward>,
        referenceDate: LocalDate,
    ): Double {
        if (lastAwards.isEmpty()) return 1.0

        val mostRecent = lastAwards.maxByOrNull { it.awardedOn } ?: return 1.0
        val weeksSince = kotlin.math.max(0, java.time.temporal.ChronoUnit.WEEKS.between(mostRecent.awardedOn, referenceDate).toInt())

        val base =
            when (mostRecent.tier) {
                LootTier.A -> 0.8
                LootTier.B -> 0.9
                LootTier.C -> 1.0
            }
        return round(min(1.0, base + (0.1 * weeksSince)))
    }

    // Helper methods for calculating scores from real WoWAudit data
    private fun calculateMechanicalAdherenceFromActivity(activity: CharacterActivityInfo?): Double {
        // Focus on raid performance only - remove dungeon/keystone scoring
        if (activity == null) return 0.0 // No activity data = 0 score

        // This should ideally be based on actual raid logs/performance data
        // For now, return 0 since we don't have raid performance metrics from WoWAudit
        return 0.0 // Placeholder - should be replaced with actual raid performance data
    }

    /**
     * Calculate MAS using Warcraft Logs data if available, otherwise fall back to activity-based calculation
     */
    private fun calculateMechanicalAdherenceWithWarcraftLogs(
        characterName: String,
        characterRealm: String,
        guildId: String,
        activity: CharacterActivityInfo?,
    ): Double {
        // Try Warcraft Logs first if service is available
        if (warcraftLogsPerformanceService != null) {
            try {
                val mas = warcraftLogsPerformanceService.getMASForCharacter(characterName, characterRealm, guildId)
                if (mas > 0.0) {
                    return mas
                }
            } catch (ex: Exception) {
                // Log and fall back to activity-based calculation
                logger.warn("Failed to get Warcraft Logs MAS for $characterName-$characterRealm, falling back to activity-based", ex)
            }
        }

        // Fall back to activity-based calculation
        return calculateMechanicalAdherenceFromActivity(activity)
    }

    private fun calculateExternalPreparationFromActivity(activity: CharacterActivityInfo?): Double {
        if (activity == null) return 0.0 // No activity data = 0 score

        // Focus only on raid preparation activities - remove world quests and dungeons
        val vaultScore = calculateVaultPreparationScore(activity.vaultOptions)

        // Vault is the primary raid preparation indicator
        return round(vaultScore)
    }

    private fun calculateVaultPreparationScore(vault: VaultOptions): Double {
        val raidSlots =
            listOfNotNull(
                if (vault.raidsOption1 > 0) 1 else null,
                if (vault.raidsOption2 > 0) 1 else null,
                if (vault.raidsOption3 > 0) 1 else null,
            ).size

        val dungeonSlots =
            listOfNotNull(
                if (vault.dungeonsOption1 > 0) 1 else null,
                if (vault.dungeonsOption2 > 0) 1 else null,
                if (vault.dungeonsOption3 > 0) 1 else null,
            ).size

        val worldSlots =
            listOfNotNull(
                if (vault.worldOption1 > 0) 1 else null,
                if (vault.worldOption2 > 0) 1 else null,
                if (vault.worldOption3 > 0) 1 else null,
            ).size

        // 9 slots total possible, weight them
        val totalScore = (raidSlots * 0.5) + (dungeonSlots * 0.3) + (worldSlots * 0.2)
        return min(1.0, totalScore / 3.0) // Normalize to 0-1
    }

    private fun calculateUpgradeValueFromWishlist(
        wishlist: CharacterWishlistInfo?,
        gear: CharacterCurrentGear?,
    ): Double {
        if (wishlist == null || gear == null) return 0.5 // Default neutral score

        // Calculate average upgrade percentage from wishlist items
        val upgradePercentages =
            wishlist.items.mapNotNull { item ->
                val currentGearInSlot = gear.currentGear.find { it.slot == item.slot }
                if (currentGearInSlot != null && item.upgradePercentage > 0) {
                    item.upgradePercentage
                } else {
                    null
                }
            }

        return if (upgradePercentages.isNotEmpty()) {
            round(min(1.0, upgradePercentages.average() / 100.0)) // Normalize percentage to 0-1
        } else {
            0.5
        }
    }

    /**
     * Calculate UV using Raidbots data if available, otherwise fall back to wishlist percentages
     */
    private fun calculateUpgradeValueWithRaidbots(
        characterName: String,
        characterRealm: String,
        guildId: String,
        itemId: Long?,
        wishlist: CharacterWishlistInfo?,
        gear: CharacterCurrentGear?,
    ): Double {
        // Try Raidbots first if service is available and we have an item ID
        if (raidbotsUpgradeService != null && itemId != null) {
            try {
                val uv = raidbotsUpgradeService.getUVForItem(guildId, characterName, characterRealm, itemId)
                if (uv > 0.0) {
                    return uv
                }
            } catch (ex: Exception) {
                logger.warn("Failed to get Raidbots UV for $characterName-$characterRealm, falling back to wishlist", ex)
            }
        }

        // Fall back to wishlist-based calculation
        return calculateUpgradeValueFromWishlist(wishlist, gear)
    }

    private fun calculateTierBonusFromGear(gear: CharacterCurrentGear?): Double {
        if (gear == null) return 1.0 // Default no penalty

        // Count tier pieces (assuming tier items have specific naming patterns or item levels)
        val tierPieces =
            gear.currentGear.count { gearItem ->
                // This is a simplified check - in reality you'd need tier set item IDs
                gearItem.itemLevel >= 440 && gearItem.quality >= 4
            }

        return tierBonus(tierPieces)
    }

    private fun calculateRecencyDecayFromHistory(lootHistory: CharacterLootInfo?): Double {
        if (lootHistory == null || lootHistory.recentAwards.isEmpty()) return 1.0

        val mostRecentAward = lootHistory.recentAwards.maxByOrNull { it.awardedAt }
        return if (mostRecentAward != null) {
            // Use the stored RDF value if available, otherwise calculate
            if (mostRecentAward.rdf > 0) mostRecentAward.rdf else 1.0
        } else {
            1.0
        }
    }

    private fun calculateRoleMultiplier(
        role: Role,
        roleMultipliers: RoleMultipliers,
    ): Double {
        return when (role) {
            Role.Tank -> roleMultipliers.tank
            Role.Healer -> roleMultipliers.healer
            Role.DPS -> roleMultipliers.dps
        }
    }

    private fun round(
        value: Double,
        places: Int = 3,
    ): Double {
        val factor = Math.pow(10.0, places.toDouble())
        return kotlin.math.round(value * factor) / factor
    }
}
