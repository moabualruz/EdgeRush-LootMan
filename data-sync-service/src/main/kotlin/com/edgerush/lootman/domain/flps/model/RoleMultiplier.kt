package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing Role Multiplier (RM).
 *
 * RM adjusts priority based on role (DPS, Tank, Healer).
 * Values typically range from 0.7 (Healer) to 1.0 (DPS).
 * Stored as value between 0.0 and 2.0 to allow for multipliers.
 */
data class RoleMultiplier private constructor(val value: Double) {
    init {
        require(value in 0.0..2.0) {
            "Role Multiplier must be between 0.0 and 2.0, got $value"
        }
    }

    companion object {
        private const val TANK_MULTIPLIER = 0.8
        private const val HEALER_MULTIPLIER = 0.7

        fun of(value: Double): RoleMultiplier = RoleMultiplier(value)

        fun dps(): RoleMultiplier = RoleMultiplier(1.0)

        fun tank(): RoleMultiplier = RoleMultiplier(TANK_MULTIPLIER)

        fun healer(): RoleMultiplier = RoleMultiplier(HEALER_MULTIPLIER)
    }
}
