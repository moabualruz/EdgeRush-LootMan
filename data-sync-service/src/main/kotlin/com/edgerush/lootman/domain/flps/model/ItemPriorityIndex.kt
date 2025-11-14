package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing Item Priority Index (IPI).
 *
 * IPI is calculated from three component values:
 * - Upgrade Value (UV)
 * - Tier Bonus (TB)
 * - Role Multiplier (RM)
 *
 * Formula: IPI = (UV × upgrade_weight) + (TB × tier_weight) + (RM × role_weight)
 *
 * Normalized value between 0.0 and 1.0.
 */
data class ItemPriorityIndex private constructor(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "Item Priority Index must be between 0.0 and 1.0, got $value"
        }
    }

    companion object {
        // Default weights for IPI components
        private const val DEFAULT_UPGRADE_WEIGHT = 0.45
        private const val DEFAULT_TIER_WEIGHT = 0.35
        private const val DEFAULT_ROLE_WEIGHT = 0.20

        fun of(value: Double): ItemPriorityIndex = ItemPriorityIndex(value)

        fun zero(): ItemPriorityIndex = ItemPriorityIndex(0.0)

        fun max(): ItemPriorityIndex = ItemPriorityIndex(1.0)

        /**
         * Creates an ItemPriorityIndex from component values.
         *
         * @param uv Upgrade Value
         * @param tb Tier Bonus
         * @param rm Role Multiplier
         * @param upgradeWeight Weight for upgrade component (default 0.45)
         * @param tierWeight Weight for tier component (default 0.35)
         * @param roleWeight Weight for role component (default 0.20)
         * @return Calculated ItemPriorityIndex
         */
        fun fromComponents(
            uv: UpgradeValue,
            tb: TierBonus,
            rm: RoleMultiplier,
            upgradeWeight: Double = DEFAULT_UPGRADE_WEIGHT,
            tierWeight: Double = DEFAULT_TIER_WEIGHT,
            roleWeight: Double = DEFAULT_ROLE_WEIGHT,
        ): ItemPriorityIndex {
            val ipi =
                (uv.value * upgradeWeight) +
                    (tb.value * tierWeight) +
                    (rm.value * roleWeight)
            return ItemPriorityIndex(ipi.coerceIn(0.0, 1.0))
        }
    }
}
