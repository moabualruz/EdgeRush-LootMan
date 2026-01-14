package com.edgerush.lootman.domain.discord.model

/**
 * Value object representing a Discord user link identifier.
 */
data class DiscordUserLinkId(val value: Long) {
    init {
        require(value > 0) { "Discord user link ID must be positive, got $value" }
    }
}
