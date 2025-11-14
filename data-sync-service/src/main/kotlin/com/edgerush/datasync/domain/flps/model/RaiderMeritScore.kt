package com.edgerush.datasync.domain.flps.model

/**
 * Value object representing a Raider Merit Score (RMS).
 * RMS is a component of FLPS that measures raider performance across:
 * - Attendance Commitment Score (ACS)
 * - Mechanical Adherence Score (MAS)
 * - External Preparation Score (EPS)
 *
 * RMS scores are normalized to the range [0.0, 1.0].
 */
data class RaiderMeritScore private constructor(val value: Double) : Comparable<RaiderMeritScore> {

    init {
        require(value in 0.0..1.0) { "Raider Merit Score must be between 0.0 and 1.0, got: $value" }
    }

    companion object {
        /**
         * Creates a RaiderMeritScore from a double value.
         * @throws IllegalArgumentException if value is not in [0.0, 1.0]
         */
        fun of(value: Double): RaiderMeritScore = RaiderMeritScore(value)

        /**
         * Returns a RaiderMeritScore with value 0.0
         */
        fun zero(): RaiderMeritScore = RaiderMeritScore(0.0)

        /**
         * Returns a RaiderMeritScore with value 1.0 (perfect score)
         */
        fun perfect(): RaiderMeritScore = RaiderMeritScore(1.0)

        /**
         * Calculates RMS from component scores using weighted average.
         * RMS = (ACS × attendance_weight) + (MAS × mechanical_weight) + (EPS × preparation_weight)
         *
         * @param attendance Attendance Commitment Score [0.0, 1.0]
         * @param mechanical Mechanical Adherence Score [0.0, 1.0]
         * @param preparation External Preparation Score [0.0, 1.0]
         * @param weights Weights for each component (must sum to 1.0)
         */
        fun fromComponents(
            attendance: Double,
            mechanical: Double,
            preparation: Double,
            weights: RmsWeights
        ): RaiderMeritScore {
            require(attendance in 0.0..1.0) { "Attendance score must be in [0.0, 1.0]" }
            require(mechanical in 0.0..1.0) { "Mechanical score must be in [0.0, 1.0]" }
            require(preparation in 0.0..1.0) { "Preparation score must be in [0.0, 1.0]" }

            val rms = (attendance * weights.attendance) +
                    (mechanical * weights.mechanical) +
                    (preparation * weights.preparation)

            return RaiderMeritScore(rms.coerceIn(0.0, 1.0))
        }
    }

    override fun compareTo(other: RaiderMeritScore): Int = value.compareTo(other.value)

    override fun toString(): String = "RaiderMeritScore($value)"
}

/**
 * Weights for RMS component scores.
 * Weights should sum to 1.0 for proper normalization.
 */
data class RmsWeights(
    val attendance: Double,
    val mechanical: Double,
    val preparation: Double
) {
    init {
        require(attendance >= 0.0 && mechanical >= 0.0 && preparation >= 0.0) {
            "All weights must be non-negative"
        }
        val sum = attendance + mechanical + preparation
        require(sum > 0.0) { "Sum of weights must be greater than 0" }
    }

    companion object {
        /**
         * Default RMS weights: 40% attendance, 40% mechanical, 20% preparation
         */
        fun default(): RmsWeights = RmsWeights(
            attendance = 0.4,
            mechanical = 0.4,
            preparation = 0.2
        )
    }
}
