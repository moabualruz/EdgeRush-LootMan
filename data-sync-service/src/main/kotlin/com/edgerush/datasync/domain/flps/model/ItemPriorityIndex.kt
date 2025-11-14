package com.edgerush.datasync.domain.flps.model

/**
 * Value object representing an Item Priority Index (IPI).
 * IPI is a component of FLPS that measures item value across:
 * - Upgrade Value (UV) - how much the item improves character performance
 * - Tier Bonus - bonus for completing tier sets
 * - Role Multiplier - adjustment based on role scarcity/importance
 *
 * IPI scores are normalized to the range [0.0, 1.0].
 */
data class ItemPriorityIndex private constructor(val value: Double) : Comparable<ItemPriorityIndex> {

    init {
        require(value in 0.0..1.0) { "Item Priority Index must be between 0.0 and 1.0, got: $value" }
    }

    companion object {
        /**
         * Creates an ItemPriorityIndex from a double value.
         * @throws IllegalArgumentException if value is not in [0.0, 1.0]
         */
        fun of(value: Double): ItemPriorityIndex = ItemPriorityIndex(value)

        /**
         * Returns an ItemPriorityIndex with value 0.0 (lowest priority)
         */
        fun zero(): ItemPriorityIndex = ItemPriorityIndex(0.0)

        /**
         * Returns an ItemPriorityIndex with value 1.0 (highest priority)
         */
        fun max(): ItemPriorityIndex = ItemPriorityIndex(1.0)

        /**
         * Calculates IPI from component scores using weighted average.
         * IPI = (UV × uv_weight) + (TB × tier_weight) + (RM × role_weight)
         *
         * @param upgradeValue Upgrade Value [0.0, 1.0+]
         * @param tierBonus Tier Bonus multiplier [1.0, 1.2]
         * @param roleMultiplier Role Multiplier [0.7, 1.0]
         * @param weights Weights for each component (must sum to 1.0)
         */
        fun fromComponents(
            upgradeValue: Double,
            tierBonus: Double,
            roleMultiplier: Double,
            weights: IpiWeights
        ): ItemPriorityIndex {
            require(upgradeValue >= 0.0) { "Upgrade value must be non-negative" }
            require(tierBonus >= 0.0) { "Tier bonus must be non-negative" }
            require(roleMultiplier >= 0.0) { "Role multiplier must be non-negative" }

            val ipi = (upgradeValue * weights.upgradeValue) +
                    (tierBonus * weights.tierBonus) +
                    (roleMultiplier * weights.roleMultiplier)

            return ItemPriorityIndex(ipi.coerceIn(0.0, 1.0))
        }
    }

    override fun compareTo(other: ItemPriorityIndex): Int = value.compareTo(other.value)

    override fun toString(): String = "ItemPriorityIndex($value)"
}

/**
 * Weights for IPI component scores.
 * Weights should sum to 1.0 for proper normalization.
 */
data class IpiWeights(
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double
) {
    init {
        require(upgradeValue >= 0.0 && tierBonus >= 0.0 && roleMultiplier >= 0.0) {
            "All weights must be non-negative"
        }
        val sum = upgradeValue + tierBonus + roleMultiplier
        require(sum > 0.0) { "Sum of weights must be greater than 0" }
    }

    companion object {
        /**
         * Default IPI weights: 45% upgrade value, 35% tier bonus, 20% role multiplier
         */
        fun default(): IpiWeights = IpiWeights(
            upgradeValue = 0.45,
            tierBonus = 0.35,
            roleMultiplier = 0.20
        )
    }
}
