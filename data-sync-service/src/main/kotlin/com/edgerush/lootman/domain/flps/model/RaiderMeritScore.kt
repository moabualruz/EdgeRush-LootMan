package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing Raider Merit Score (RMS).
 *
 * RMS is calculated from three component scores:
 * - Attendance Commitment Score (ACS)
 * - Mechanical Adherence Score (MAS)
 * - External Preparation Score (EPS)
 *
 * Formula: RMS = (ACS × attendance_weight) + (MAS × mechanical_weight) + (EPS × preparation_weight)
 *
 * Normalized value between 0.0 and 1.0.
 */
data class RaiderMeritScore private constructor(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "Raider Merit Score must be between 0.0 and 1.0, got $value"
        }
    }

    companion object {
        // Default weights for RMS components
        private const val DEFAULT_ATTENDANCE_WEIGHT = 0.4
        private const val DEFAULT_MECHANICAL_WEIGHT = 0.4
        private const val DEFAULT_PREPARATION_WEIGHT = 0.2

        fun of(value: Double): RaiderMeritScore = RaiderMeritScore(value)

        fun zero(): RaiderMeritScore = RaiderMeritScore(0.0)

        fun max(): RaiderMeritScore = RaiderMeritScore(1.0)

        /**
         * Creates a RaiderMeritScore from component scores.
         *
         * @param acs Attendance Commitment Score
         * @param mas Mechanical Adherence Score
         * @param eps External Preparation Score
         * @param attendanceWeight Weight for attendance component (default 0.4)
         * @param mechanicalWeight Weight for mechanical component (default 0.4)
         * @param preparationWeight Weight for preparation component (default 0.2)
         * @return Calculated RaiderMeritScore
         */
        fun fromComponents(
            acs: AttendanceCommitmentScore,
            mas: MechanicalAdherenceScore,
            eps: ExternalPreparationScore,
            attendanceWeight: Double = DEFAULT_ATTENDANCE_WEIGHT,
            mechanicalWeight: Double = DEFAULT_MECHANICAL_WEIGHT,
            preparationWeight: Double = DEFAULT_PREPARATION_WEIGHT,
        ): RaiderMeritScore {
            val rms =
                (acs.value * attendanceWeight) +
                    (mas.value * mechanicalWeight) +
                    (eps.value * preparationWeight)
            return RaiderMeritScore(rms.coerceIn(0.0, 1.0))
        }
    }
}
