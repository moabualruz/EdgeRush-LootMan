package com.edgerush.lootman.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for Discord bot.
 */
@ConfigurationProperties(prefix = "discord")
data class DiscordProperties(
    val bot: BotProperties = BotProperties(),
    val backend: BackendProperties = BackendProperties(),
    val commands: CommandsProperties = CommandsProperties(),
    val guilds: List<GuildConfig> = emptyList(),
    val notifications: NotificationsConfig = NotificationsConfig(),
)

/**
 * Bot-specific properties.
 */
data class BotProperties(
    val token: String = "",
    val applicationId: String = "",
)

/**
 * Backend API properties.
 */
data class BackendProperties(
    val url: String = "http://localhost:8080",
    val timeout: Long = 30,
)

/**
 * Command-related properties.
 */
data class CommandsProperties(
    val enabled: Boolean = true,
    val adminRoleIds: List<String> = emptyList(),
    val officerRoleIds: List<String> = emptyList(),
)

/**
 * Guild-specific configuration.
 */
data class GuildConfig(
    val guildId: String = "",
    val discordServerId: String = "",
    val notificationChannels: NotificationChannels = NotificationChannels(),
)

/**
 * Notification channel IDs for a guild.
 */
data class NotificationChannels(
    val lootAwards: String? = null,
    val rdfExpiry: String? = null,
    val penalties: String? = null,
)

/**
 * Global notification settings.
 */
data class NotificationsConfig(
    val lootAwards: LootAwardNotificationConfig = LootAwardNotificationConfig(),
    val rdfExpiry: RdfExpiryNotificationConfig = RdfExpiryNotificationConfig(),
    val penalties: PenaltyNotificationConfig = PenaltyNotificationConfig(),
)

/**
 * Loot award notification settings.
 */
data class LootAwardNotificationConfig(
    val enabled: Boolean = true,
    val includeRunnerUps: Int = 3,
)

/**
 * RDF expiry notification settings.
 */
data class RdfExpiryNotificationConfig(
    val enabled: Boolean = true,
    val dmUsers: Boolean = true,
)

/**
 * Penalty notification settings.
 */
data class PenaltyNotificationConfig(
    val enabled: Boolean = true,
    val dmUsers: Boolean = true,
)
