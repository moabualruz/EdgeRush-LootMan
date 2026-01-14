package com.edgerush.lootman.api.discord

import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfig
import com.edgerush.lootman.domain.discord.model.DiscordNotificationType
import java.time.Instant

/**
 * Request to create or update a notification configuration.
 */
data class UpsertNotificationConfigRequest(
    val discordServerId: String,
    val notificationType: String,
    val channelId: String,
    val enabled: Boolean = true,
    val mentionRoleId: String? = null
)

/**
 * Request to update notification settings.
 */
data class UpdateNotificationConfigRequest(
    val channelId: String? = null,
    val enabled: Boolean? = null,
    val mentionRoleId: String? = null
)

/**
 * Response for a notification configuration.
 */
data class DiscordNotificationConfigResponse(
    val id: Long,
    val guildId: String,
    val discordServerId: String,
    val notificationType: String,
    val channelId: String,
    val enabled: Boolean,
    val mentionRoleId: String?,
    val createdAt: Instant,
    val updatedAt: Instant?
) {
    companion object {
        fun from(config: DiscordNotificationConfig): DiscordNotificationConfigResponse =
            DiscordNotificationConfigResponse(
                id = config.id!!.value,
                guildId = config.guildId.value,
                discordServerId = config.discordServerId,
                notificationType = config.notificationType.name,
                channelId = config.channelId,
                enabled = config.enabled,
                mentionRoleId = config.mentionRoleId,
                createdAt = config.createdAt,
                updatedAt = config.updatedAt
            )
    }
}

/**
 * Response containing all notification configurations for a guild.
 */
data class GuildNotificationConfigsResponse(
    val guildId: String,
    val configs: List<DiscordNotificationConfigResponse>,
    val availableTypes: List<String> = DiscordNotificationType.entries.map { it.name }
)

/**
 * Response for a test notification.
 */
data class TestNotificationResponse(
    val success: Boolean,
    val message: String
)
