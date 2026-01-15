package com.edgerush.lootman.domain.discord.model

/**
 * Types of Discord notifications that can be configured.
 */
enum class DiscordNotificationType {
    /**
     * Notification when loot is awarded to a raider.
     */
    LOOT_AWARD,

    /**
     * Notification when a raider's RDF (Recency Decay Factor) expires.
     */
    RDF_EXPIRY,

    /**
     * Notification when a penalty is applied to a raider.
     */
    PENALTY,

    /**
     * Notification when a loot ban is applied to a raider.
     */
    LOOT_BAN,

    /**
     * Notification when a data sync completes.
     */
    SYNC_COMPLETE,

    ;

    companion object {
        fun fromString(value: String): DiscordNotificationType? {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
