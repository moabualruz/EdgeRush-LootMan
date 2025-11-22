package com.edgerush.lootman.domain.shared

/**
 * Value object representing a Raider identifier.
 */
data class RaiderId(val value: Long) {
    init {
        require(value > 0) { "Raider ID must be positive, got $value" }
    }
}
