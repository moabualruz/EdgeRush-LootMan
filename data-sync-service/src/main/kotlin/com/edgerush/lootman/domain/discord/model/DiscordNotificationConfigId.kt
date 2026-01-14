package com.edgerush.lootman.domain.discord.model

/**
 * Value object representing a Discord Notification Config identifier.
 */
data class DiscordNotificationConfigId(val value: Long) {
    init {
        require(value > 0) { "Discord Notification Config ID must be positive, got $value" }
    }
}
