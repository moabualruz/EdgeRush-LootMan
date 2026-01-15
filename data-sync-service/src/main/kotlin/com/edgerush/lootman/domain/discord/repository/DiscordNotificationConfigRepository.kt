package com.edgerush.lootman.domain.discord.repository

import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfig
import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfigId
import com.edgerush.lootman.domain.discord.model.DiscordNotificationType
import com.edgerush.lootman.domain.shared.GuildId

/**
 * Repository interface for Discord notification configuration operations.
 */
interface DiscordNotificationConfigRepository {
    /**
     * Finds a configuration by its unique identifier.
     */
    fun findById(id: DiscordNotificationConfigId): DiscordNotificationConfig?

    /**
     * Finds all configurations for a guild.
     */
    fun findByGuildId(guildId: GuildId): List<DiscordNotificationConfig>

    /**
     * Finds a configuration by guild and notification type.
     */
    fun findByGuildIdAndType(
        guildId: GuildId,
        type: DiscordNotificationType,
    ): DiscordNotificationConfig?

    /**
     * Finds all enabled configurations for a guild.
     */
    fun findEnabledByGuildId(guildId: GuildId): List<DiscordNotificationConfig>

    /**
     * Finds a specific enabled configuration for a guild and type.
     */
    fun findEnabledByGuildIdAndType(
        guildId: GuildId,
        type: DiscordNotificationType,
    ): DiscordNotificationConfig?

    /**
     * Saves a configuration (creates or updates).
     */
    fun save(config: DiscordNotificationConfig): DiscordNotificationConfig

    /**
     * Deletes a configuration by its ID.
     */
    fun deleteById(id: DiscordNotificationConfigId)

    /**
     * Deletes all configurations for a guild.
     */
    fun deleteByGuildId(guildId: GuildId): Int

    /**
     * Checks if a configuration exists for guild and type.
     */
    fun existsByGuildIdAndType(
        guildId: GuildId,
        type: DiscordNotificationType,
    ): Boolean
}
