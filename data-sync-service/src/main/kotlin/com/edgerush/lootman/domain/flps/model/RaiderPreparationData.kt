package com.edgerush.lootman.domain.flps.model

import com.edgerush.lootman.domain.shared.RaiderId

/**
 * Value object representing raider preparation data for EPS calculation.
 *
 * Contains vault unlock status, crest usage, M+ score, and other preparation metrics.
 */
data class RaiderPreparationData(
    val raiderId: RaiderId,
    /** Number of raid vault slots unlocked (0-3) */
    val raidVaultSlots: Int,
    /** Number of M+ vault slots unlocked (0-3) */
    val mythicPlusVaultSlots: Int,
    /** Number of PvP vault slots unlocked (0-3) */
    val pvpVaultSlots: Int,
    /** Current M+ rating score (0-3000+) */
    val mythicPlusRating: Int,
    /** Number of crests used this week/season */
    val crestsUsed: Int,
    /** Whether the raider has completed a heroic clear this tier */
    val hasHeroicClear: Boolean,
    /** Whether the raider has completed a full normal clear this tier */
    val hasNormalClear: Boolean,
) {
    init {
        require(raidVaultSlots in 0..3) { "Raid vault slots must be between 0 and 3" }
        require(mythicPlusVaultSlots in 0..3) { "M+ vault slots must be between 0 and 3" }
        require(pvpVaultSlots in 0..3) { "PvP vault slots must be between 0 and 3" }
        require(mythicPlusRating >= 0) { "M+ rating cannot be negative" }
        require(crestsUsed >= 0) { "Crests used cannot be negative" }
    }

    /**
     * Total vault slots unlocked across all types.
     */
    val totalVaultSlots: Int
        get() = raidVaultSlots + mythicPlusVaultSlots + pvpVaultSlots

    /**
     * Whether the raider has at least one vault slot unlocked.
     */
    val hasAnyVaultSlot: Boolean
        get() = totalVaultSlots > 0

    /**
     * Whether all 3 raid vault slots are unlocked.
     */
    val hasFullRaidVault: Boolean
        get() = raidVaultSlots == 3

    companion object {
        /**
         * Creates empty preparation data when no activity data is available.
         */
        fun empty(raiderId: RaiderId): RaiderPreparationData =
            RaiderPreparationData(
                raiderId = raiderId,
                raidVaultSlots = 0,
                mythicPlusVaultSlots = 0,
                pvpVaultSlots = 0,
                mythicPlusRating = 0,
                crestsUsed = 0,
                hasHeroicClear = false,
                hasNormalClear = false,
            )

        /**
         * Creates preparation data with common defaults for active raiders.
         */
        fun create(
            raiderId: RaiderId,
            raidVaultSlots: Int = 0,
            mythicPlusVaultSlots: Int = 0,
            pvpVaultSlots: Int = 0,
            mythicPlusRating: Int = 0,
            crestsUsed: Int = 0,
            hasHeroicClear: Boolean = false,
            hasNormalClear: Boolean = false,
        ): RaiderPreparationData =
            RaiderPreparationData(
                raiderId = raiderId,
                raidVaultSlots = raidVaultSlots,
                mythicPlusVaultSlots = mythicPlusVaultSlots,
                pvpVaultSlots = pvpVaultSlots,
                mythicPlusRating = mythicPlusRating,
                crestsUsed = crestsUsed,
                hasHeroicClear = hasHeroicClear,
                hasNormalClear = hasNormalClear,
            )
    }
}
