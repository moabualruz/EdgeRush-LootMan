package com.edgerush.lootman.domain.flps.model

/**
 * Value object representing Upgrade Value (UV).
 *
 * UV measures the relative power gain from an item upgrade.
 * Normalized value between 0.0 and 1.0.
 */
data class UpgradeValue private constructor(val value: Double) {
    init {
        require(value in 0.0..1.0) {
            "Upgrade Value must be between 0.0 and 1.0, got $value"
        }
    }

    companion object {
        fun of(value: Double): UpgradeValue = UpgradeValue(value)

        fun zero(): UpgradeValue = UpgradeValue(0.0)

        fun max(): UpgradeValue = UpgradeValue(1.0)
    }
}
