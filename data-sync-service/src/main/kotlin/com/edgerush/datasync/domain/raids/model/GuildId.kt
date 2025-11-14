package com.edgerush.datasync.domain.raids.model

/**
 * Value object representing a unique guild identifier.
 */
@JvmInline
value class GuildId(val value: String) {
    init {
        require(value.isNotBlank()) { "Guild ID cannot be blank" }
    }
}
