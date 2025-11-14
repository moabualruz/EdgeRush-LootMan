package com.edgerush.lootman.domain.attendance.model

/**
 * Value object representing a unique guild identifier.
 */
data class GuildId(val value: String) {
    init {
        require(value.isNotBlank()) { "Guild ID cannot be blank" }
    }
}
