package com.edgerush.lootman.domain.shared

/**
 * Value object representing a Raider identifier.
 */
data class RaiderId(val value: String) {
    init {
        require(value.isNotBlank()) { "Raider ID cannot be blank" }
    }
}
