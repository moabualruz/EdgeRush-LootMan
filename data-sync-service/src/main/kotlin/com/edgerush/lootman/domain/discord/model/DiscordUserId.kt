package com.edgerush.lootman.domain.discord.model

/**
 * Value object representing a Discord user identifier (snowflake).
 *
 * Discord IDs are 64-bit integers represented as strings to preserve precision.
 */
data class DiscordUserId(val value: String) {
    init {
        require(value.isNotBlank()) { "Discord user ID cannot be blank" }
        require(value.all { it.isDigit() }) { "Discord user ID must contain only digits, got: $value" }
        require(value.length in 17..20) { "Discord user ID must be 17-20 digits, got ${value.length} digits" }
    }
}
