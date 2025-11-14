package com.edgerush.datasync.domain.flps.model

/**
 * Value object representing a Final Loot Priority Score (FLPS).
 * FLPS scores are normalized to the range [0.0, 1.0] where:
 * - 0.0 represents the lowest priority
 * - 1.0 represents the highest priority
 *
 * This is an immutable value object that ensures score validity through construction.
 */
data class FlpsScore private constructor(val value: Double) : Comparable<FlpsScore> {

    init {
        require(value in 0.0..1.0) { "FLPS score must be between 0.0 and 1.0, got: $value" }
    }

    companion object {
        /**
         * Creates a FlpsScore from a double value.
         * @throws IllegalArgumentException if value is not in [0.0, 1.0]
         */
        fun of(value: Double): FlpsScore = FlpsScore(value)

        /**
         * Returns a FlpsScore with value 0.0 (lowest priority)
         */
        fun zero(): FlpsScore = FlpsScore(0.0)

        /**
         * Returns a FlpsScore with value 1.0 (highest priority)
         */
        fun max(): FlpsScore = FlpsScore(1.0)
    }

    /**
     * Adds two FLPS scores together, capping the result at 1.0
     */
    operator fun plus(other: FlpsScore): FlpsScore =
        FlpsScore((value + other.value).coerceIn(0.0, 1.0))

    /**
     * Multiplies the FLPS score by a factor, capping the result at [0.0, 1.0]
     */
    operator fun times(multiplier: Double): FlpsScore =
        FlpsScore((value * multiplier).coerceIn(0.0, 1.0))

    override fun compareTo(other: FlpsScore): Int = value.compareTo(other.value)

    override fun toString(): String = "FlpsScore($value)"
}
