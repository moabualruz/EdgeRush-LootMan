package com.edgerush.lootman.api.discord

import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfig
import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfigId
import com.edgerush.lootman.domain.discord.model.DiscordNotificationType
import com.edgerush.lootman.domain.discord.repository.DiscordNotificationConfigRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Exception thrown when a notification configuration is not found.
 */
class NotificationConfigNotFoundException(val configId: Long) :
    RuntimeException("Notification config not found: $configId")

/**
 * Exception thrown when an invalid notification type is provided.
 */
class InvalidNotificationTypeException(val type: String) :
    RuntimeException("Invalid notification type: $type. Valid types: ${DiscordNotificationType.entries.map { it.name }}")

/**
 * Service for managing Discord notification configurations.
 */
@Service
@Transactional
class DiscordNotificationConfigService(
    private val repository: DiscordNotificationConfigRepository,
) {
    /**
     * Gets all notification configurations for a guild.
     */
    @Transactional(readOnly = true)
    fun getConfigsForGuild(guildId: String): GuildNotificationConfigsResponse {
        val configs = repository.findByGuildId(GuildId(guildId))
        return GuildNotificationConfigsResponse(
            guildId = guildId,
            configs = configs.map { DiscordNotificationConfigResponse.from(it) },
        )
    }

    /**
     * Gets a specific notification configuration by guild and type.
     */
    @Transactional(readOnly = true)
    fun getConfigByType(
        guildId: String,
        type: String,
    ): DiscordNotificationConfigResponse? {
        val notificationType = parseNotificationType(type)
        return repository.findByGuildIdAndType(GuildId(guildId), notificationType)
            ?.let { DiscordNotificationConfigResponse.from(it) }
    }

    /**
     * Creates or updates a notification configuration.
     */
    fun upsertConfig(
        guildId: String,
        request: UpsertNotificationConfigRequest,
    ): DiscordNotificationConfigResponse {
        val notificationType = parseNotificationType(request.notificationType)
        val guildIdObj = GuildId(guildId)

        val existingConfig = repository.findByGuildIdAndType(guildIdObj, notificationType)

        val config =
            if (existingConfig != null) {
                existingConfig
                    .updateChannel(request.channelId)
                    .updateMentionRole(request.mentionRoleId)
                    .let { if (request.enabled) it.enable() else it.disable() }
            } else {
                DiscordNotificationConfig.create(
                    guildId = guildIdObj,
                    discordServerId = request.discordServerId,
                    notificationType = notificationType,
                    channelId = request.channelId,
                    mentionRoleId = request.mentionRoleId,
                ).let { if (!request.enabled) it.disable() else it }
            }

        val savedConfig = repository.save(config)
        return DiscordNotificationConfigResponse.from(savedConfig)
    }

    /**
     * Updates an existing notification configuration.
     */
    fun updateConfig(
        guildId: String,
        configId: Long,
        request: UpdateNotificationConfigRequest,
    ): DiscordNotificationConfigResponse {
        val config =
            repository.findById(DiscordNotificationConfigId(configId))
                ?: throw NotificationConfigNotFoundException(configId)

        // Verify the config belongs to the guild
        if (config.guildId.value != guildId) {
            throw NotificationConfigNotFoundException(configId)
        }

        var updatedConfig = config

        request.channelId?.let { updatedConfig = updatedConfig.updateChannel(it) }
        request.mentionRoleId?.let { updatedConfig = updatedConfig.updateMentionRole(it) }
        request.enabled?.let { enabled ->
            updatedConfig = if (enabled) updatedConfig.enable() else updatedConfig.disable()
        }

        val savedConfig = repository.save(updatedConfig)
        return DiscordNotificationConfigResponse.from(savedConfig)
    }

    /**
     * Deletes a notification configuration.
     */
    fun deleteConfig(
        guildId: String,
        configId: Long,
    ) {
        val config =
            repository.findById(DiscordNotificationConfigId(configId))
                ?: throw NotificationConfigNotFoundException(configId)

        // Verify the config belongs to the guild
        if (config.guildId.value != guildId) {
            throw NotificationConfigNotFoundException(configId)
        }

        repository.deleteById(config.id!!)
    }

    /**
     * Tests a notification configuration by sending a test message.
     * Note: Actual Discord API call would be implemented here.
     */
    fun testNotification(
        guildId: String,
        type: String,
    ): TestNotificationResponse {
        val notificationType = parseNotificationType(type)
        val config =
            repository.findEnabledByGuildIdAndType(GuildId(guildId), notificationType)
                ?: return TestNotificationResponse(
                    success = false,
                    message = "No enabled configuration found for type: $type",
                )

        // TODO: Implement actual Discord API call to send test message
        // For now, just return success if config exists
        return TestNotificationResponse(
            success = true,
            message = "Test notification would be sent to channel ${config.channelId}",
        )
    }

    private fun parseNotificationType(type: String): DiscordNotificationType {
        return DiscordNotificationType.fromString(type)
            ?: throw InvalidNotificationTypeException(type)
    }
}
