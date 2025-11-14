package com.edgerush.lootman.domain.shared

/**
 * Value object representing a Guild identifier.
 */
data class GuildId(val value: String) {
    init {
        require(value.isNotBlank()) { "Guild ID cannot be blank" }
    }
}
