package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing External Preparation Score (EPS).
 *
 * EPS measures a raider's preparation through vault slots, crest usage, and heroic clears.
 * Normalized value between 0.0 and 1.0.
 */
data class ExternalPreparationScore private constructor(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "External Preparation Score must be between 0.0 and 1.0, got $value"
        }
    }

    companion object {
        fun of(value: Double): ExternalPreparationScore = ExternalPreparationScore(value)

        fun zero(): ExternalPreparationScore = ExternalPreparationScore(0.0)

        fun max(): ExternalPreparationScore = ExternalPreparationScore(1.0)
    }
}
