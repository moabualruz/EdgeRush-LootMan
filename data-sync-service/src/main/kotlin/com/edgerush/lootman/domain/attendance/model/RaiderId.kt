package com.edgerush.lootman.domain.attendance.model

/**
 * Value object representing a unique raider identifier.
 */
data class RaiderId(val value: Long) {
    init {
        require(value > 0) { "Raider ID must be positive, got $value" }
    }
}
