package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing a Final Loot Priority Score (FLPS).
 *
 * FLPS scores are normalized values between 0.0 and 1.0 that represent
 * a raider's priority for receiving loot based on multiple factors.
 *
 * This is an immutable value object that validates its range on construction.
 */
data class FlpsScore private constructor(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "FLPS score must be between 0.0 and 1.0, got $value"
        }
    }

    companion object {
        /**
         * Creates a FlpsScore from a double value.
         *
         * @param value The score value, must be between 0.0 and 1.0
         * @return A new FlpsScore instance
         * @throws IllegalArgumentException if value is outside valid range
         */
        fun of(value: Double): FlpsScore = FlpsScore(value)

        /**
         * Creates a FlpsScore with zero value.
         *
         * @return A FlpsScore with value 0.0
         */
        fun zero(): FlpsScore = FlpsScore(0.0)

        /**
         * Creates a FlpsScore with maximum value.
         *
         * @return A FlpsScore with value 1.0
         */
        fun max(): FlpsScore = FlpsScore(1.0)
    }

    /**
     * Adds two FlpsScore values together, coercing the result to valid range [0.0, 1.0].
     *
     * @param other The FlpsScore to add
     * @return A new FlpsScore with the sum, coerced to valid range
     */
    operator fun plus(other: FlpsScore): FlpsScore = FlpsScore((value + other.value).coerceIn(0.0, 1.0))

    /**
     * Multiplies this FlpsScore by a scalar value, coercing the result to valid range [0.0, 1.0].
     *
     * @param multiplier The scalar to multiply by
     * @return A new FlpsScore with the product, coerced to valid range
     */
    operator fun times(multiplier: Double): FlpsScore = FlpsScore((value * multiplier).coerceIn(0.0, 1.0))
}
