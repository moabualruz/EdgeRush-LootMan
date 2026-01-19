package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("discord_notification_configs")
data class DiscordNotificationConfigEntity(
    @Id
    val id: Long? = null,
    @Column("guild_id")
    val guildId: String,
    @Column("discord_server_id")
    val discordServerId: String,
    @Column("notification_type")
    val notificationType: String,
    @Column("channel_id")
    val channelId: String,
    @Column("enabled")
    val enabled: Boolean = true,
    @Column("mention_role_id")
    val mentionRoleId: String?,
    @Column("created_at")
    val createdAt: Instant,
    @Column("updated_at")
    val updatedAt: Instant?,
)
