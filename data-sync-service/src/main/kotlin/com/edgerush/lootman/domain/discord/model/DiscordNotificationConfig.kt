package com.edgerush.lootman.domain.discord.model

import com.edgerush.lootman.domain.shared.GuildId
import java.time.Instant

/**
 * Entity representing Discord notification configuration for a guild.
 *
 * Each guild can configure where different types of notifications are sent.
 */
data class DiscordNotificationConfig(
    val id: DiscordNotificationConfigId? = null,
    val guildId: GuildId,
    val discordServerId: String,
    val notificationType: DiscordNotificationType,
    val channelId: String,
    val enabled: Boolean = true,
    val mentionRoleId: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant? = null,
) {
    init {
        require(discordServerId.isNotBlank()) { "Discord server ID must not be blank" }
        require(channelId.isNotBlank()) { "Channel ID must not be blank" }
    }

    /**
     * Creates a copy with the given ID.
     */
    fun withId(id: DiscordNotificationConfigId): DiscordNotificationConfig = copy(id = id)

    /**
     * Enables this notification configuration.
     */
    fun enable(): DiscordNotificationConfig = copy(enabled = true, updatedAt = Instant.now())

    /**
     * Disables this notification configuration.
     */
    fun disable(): DiscordNotificationConfig = copy(enabled = false, updatedAt = Instant.now())

    /**
     * Updates the channel for this notification.
     */
    fun updateChannel(channelId: String): DiscordNotificationConfig {
        require(channelId.isNotBlank()) { "Channel ID must not be blank" }
        return copy(channelId = channelId, updatedAt = Instant.now())
    }

    /**
     * Updates the mention role for this notification.
     */
    fun updateMentionRole(roleId: String?): DiscordNotificationConfig = copy(mentionRoleId = roleId, updatedAt = Instant.now())

    companion object {
        /**
         * Creates a new notification configuration.
         */
        fun create(
            guildId: GuildId,
            discordServerId: String,
            notificationType: DiscordNotificationType,
            channelId: String,
            mentionRoleId: String? = null,
        ): DiscordNotificationConfig =
            DiscordNotificationConfig(
                guildId = guildId,
                discordServerId = discordServerId,
                notificationType = notificationType,
                channelId = channelId,
                mentionRoleId = mentionRoleId,
            )
    }
}
