package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing Recency Decay Factor (RDF).
 *
 * RDF reduces FLPS for raiders who recently received loot, ensuring fair distribution.
 * Value of 1.0 means no decay (no recent loot), lower values indicate recent loot receipt.
 *
 * Normalized value between 0.0 and 1.0.
 */
data class RecencyDecayFactor private constructor(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "Recency Decay Factor must be between 0.0 and 1.0, got $value"
        }
    }

    companion object {
        fun of(value: Double): RecencyDecayFactor = RecencyDecayFactor(value)

        fun noDecay(): RecencyDecayFactor = RecencyDecayFactor(1.0)

        fun zero(): RecencyDecayFactor = RecencyDecayFactor(0.0)
    }
}
