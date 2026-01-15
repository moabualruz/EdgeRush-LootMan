package com.edgerush.lootman.domain.simulation.model

import java.time.Instant

/**
 * Value object representing a simulation result for a specific item.
 *
 * Contains the DPS/HPS gain calculated by SimulationCraft for an item upgrade.
 */
@ConsistentCopyVisibility
data class SimulationResult private constructor(
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val dpsGain: Double,
    val percentGain: Double,
    val simulatedAt: Instant,
) {
    /**
     * Returns true if this item represents an upgrade (positive DPS gain).
     */
    val isUpgrade: Boolean
        get() = dpsGain > 0

    /**
     * Returns the normalized upgrade value in 0.0-1.0 range.
     *
     * @param maxPercentGain The maximum percent gain to use for normalization (default 10%)
     * @return Normalized value between 0.0 and 1.0
     */
    fun normalizedUpgradeValue(maxPercentGain: Double = DEFAULT_MAX_PERCENT_GAIN): Double {
        if (percentGain <= 0) return 0.0
        return (percentGain / maxPercentGain).coerceIn(0.0, 1.0)
    }

    companion object {
        private const val DEFAULT_MAX_PERCENT_GAIN = 10.0

        /**
         * Creates a new SimulationResult with validation.
         *
         * @param itemId The WoW item ID
         * @param itemName The item name
         * @param slot The equipment slot (e.g., "head", "trinket1")
         * @param dpsGain The absolute DPS gain from equipping this item
         * @param percentGain The percentage DPS gain
         * @param simulatedAt When the simulation was run
         * @return A validated SimulationResult
         * @throws IllegalArgumentException if validation fails
         */
        fun create(
            itemId: Long,
            itemName: String,
            slot: String,
            dpsGain: Double,
            percentGain: Double,
            simulatedAt: Instant,
        ): SimulationResult {
            require(itemId >= 0) { "itemId must not be negative" }
            require(itemName.isNotBlank()) { "itemName must not be blank" }
            require(slot.isNotBlank()) { "slot must not be blank" }

            return SimulationResult(
                itemId = itemId,
                itemName = itemName.trim(),
                slot = slot.trim().lowercase(),
                dpsGain = dpsGain,
                percentGain = percentGain,
                simulatedAt = simulatedAt,
            )
        }
    }
}
