package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing Tier Bonus (TB).
 *
 * TB is a multiplier based on tier set completion status.
 * Values typically range from 1.0 (has tier) to 1.2 (needs tier).
 * Stored as normalized value between 0.0 and 2.0 to allow for multipliers.
 */
data class TierBonus private constructor(val value: Double) {
    init {
        require(value in 0.0..2.0) {
            "Tier Bonus must be between 0.0 and 2.0, got $value"
        }
    }

    companion object {
        private const val MAX_TIER_BONUS = 1.2

        fun of(value: Double): TierBonus = TierBonus(value)

        fun none(): TierBonus = TierBonus(1.0)

        fun max(): TierBonus = TierBonus(MAX_TIER_BONUS)
    }
}
