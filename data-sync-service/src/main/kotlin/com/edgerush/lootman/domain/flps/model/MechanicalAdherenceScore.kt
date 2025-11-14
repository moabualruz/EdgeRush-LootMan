package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing Mechanical Adherence Score (MAS).
 *
 * MAS measures a raider's mechanical skill through death rates and avoidable damage.
 * Normalized value between 0.0 and 1.0.
 */
data class MechanicalAdherenceScore private constructor(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "Mechanical Adherence Score must be between 0.0 and 1.0, got $value"
        }
    }

    companion object {
        fun of(value: Double): MechanicalAdherenceScore = MechanicalAdherenceScore(value)

        fun zero(): MechanicalAdherenceScore = MechanicalAdherenceScore(0.0)

        fun max(): MechanicalAdherenceScore = MechanicalAdherenceScore(1.0)
    }
}
