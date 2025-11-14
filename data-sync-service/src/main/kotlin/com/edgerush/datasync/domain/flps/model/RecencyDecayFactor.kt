package com.edgerush.datasync.domain.flps.model

import kotlin.math.min

/**
 * Value object representing a Recency Decay Factor (RDF).
 * RDF is a component of FLPS that penalizes raiders who recently received loot,
 * ensuring fair distribution over time.
 *
 * RDF scores are in the range [0.0, 1.0] where:
 * - 1.0 means no penalty (no recent loot or fully recovered)
 * - Lower values indicate recent loot awards with active penalty
 *
 * The penalty decays over time, recovering at a configurable rate per week.
 */
data class RecencyDecayFactor private constructor(val value: Double) : Comparable<RecencyDecayFactor> {

    init {
        require(value in 0.0..1.0) { "Recency Decay Factor must be between 0.0 and 1.0, got: $value" }
    }

    companion object {
        /**
         * Creates a RecencyDecayFactor from a double value.
         * @throws IllegalArgumentException if value is not in [0.0, 1.0]
         */
        fun of(value: Double): RecencyDecayFactor = RecencyDecayFactor(value)

        /**
         * Returns a RecencyDecayFactor with value 1.0 (no penalty)
         */
        fun noPenalty(): RecencyDecayFactor = RecencyDecayFactor(1.0)

        /**
         * Returns a RecencyDecayFactor with value 0.0 (maximum penalty)
         */
        fun maxPenalty(): RecencyDecayFactor = RecencyDecayFactor(0.0)

        /**
         * Calculates RDF based on weeks since last loot award.
         * RDF = min(1.0, basePenalty + (recoveryRate × weeksSince))
         *
         * @param weeksSinceLastAward Number of weeks since last loot award (null if no history)
         * @param basePenalty Initial penalty applied immediately after receiving loot [0.0, 1.0]
         * @param recoveryRate Rate of recovery per week (default 0.1 = 10% per week)
         * @return RecencyDecayFactor with calculated penalty
         */
        fun fromWeeksSince(
            weeksSinceLastAward: Int?,
            basePenalty: Double,
            recoveryRate: Double = 0.1
        ): RecencyDecayFactor {
            require(basePenalty in 0.0..1.0) { "Base penalty must be in [0.0, 1.0]" }
            require(recoveryRate >= 0.0) { "Recovery rate must be non-negative" }

            // No loot history = no penalty
            if (weeksSinceLastAward == null) {
                return noPenalty()
            }

            // Calculate recovery: base penalty + (recovery rate × weeks)
            val rdf = min(1.0, basePenalty + (recoveryRate * weeksSinceLastAward))
            return RecencyDecayFactor(rdf)
        }
    }

    override fun compareTo(other: RecencyDecayFactor): Int = value.compareTo(other.value)

    override fun toString(): String = "RecencyDecayFactor($value)"
}
