package com.edgerush.lootman.infrastructure.discord

import com.edgerush.datasync.entity.DiscordNotificationConfigEntity
import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfig
import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfigId
import com.edgerush.lootman.domain.discord.model.DiscordNotificationType
import com.edgerush.lootman.domain.discord.repository.DiscordNotificationConfigRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.infrastructure.springdata.DiscordNotificationConfigEntitySpringRepository
import org.springframework.stereotype.Repository

/**
 * JDBC implementation of DiscordNotificationConfigRepository.
 *
 * Persists notification configs using Spring Data JDBC.
 */
@Repository
class JdbcDiscordNotificationConfigRepository(
    private val springRepository: DiscordNotificationConfigEntitySpringRepository,
) : DiscordNotificationConfigRepository {

    override fun findById(id: DiscordNotificationConfigId): DiscordNotificationConfig? =
        springRepository.findById(id.value).orElse(null)?.toDomain()

    override fun findByGuildId(guildId: GuildId): List<DiscordNotificationConfig> =
        springRepository.findByGuildIdOrderByNotificationTypeAsc(guildId.value).map { it.toDomain() }

    override fun findByGuildIdAndType(
        guildId: GuildId,
        type: DiscordNotificationType,
    ): DiscordNotificationConfig? =
        springRepository.findByGuildIdAndNotificationType(guildId.value, type.name)?.toDomain()

    override fun findEnabledByGuildId(guildId: GuildId): List<DiscordNotificationConfig> =
        springRepository.findByGuildIdAndEnabledTrueOrderByNotificationTypeAsc(guildId.value)
            .map { it.toDomain() }

    override fun findEnabledByGuildIdAndType(
        guildId: GuildId,
        type: DiscordNotificationType,
    ): DiscordNotificationConfig? =
        springRepository.findByGuildIdAndNotificationTypeAndEnabledTrue(guildId.value, type.name)
            ?.toDomain()

    override fun save(config: DiscordNotificationConfig): DiscordNotificationConfig {
        val entity = config.toEntity()
        val savedEntity = springRepository.save(entity)
        return savedEntity.toDomain()
    }

    override fun deleteById(id: DiscordNotificationConfigId) {
        springRepository.deleteById(id.value)
    }

    override fun deleteByGuildId(guildId: GuildId): Int =
        springRepository.deleteByGuildId(guildId.value)

    override fun existsByGuildIdAndType(
        guildId: GuildId,
        type: DiscordNotificationType,
    ): Boolean =
        springRepository.existsByGuildIdAndNotificationType(guildId.value, type.name)

    private fun DiscordNotificationConfigEntity.toDomain(): DiscordNotificationConfig =
        DiscordNotificationConfig(
            id = id?.let { DiscordNotificationConfigId(it) },
            guildId = GuildId(guildId),
            discordServerId = discordServerId,
            notificationType = DiscordNotificationType.valueOf(notificationType),
            channelId = channelId,
            enabled = enabled,
            mentionRoleId = mentionRoleId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun DiscordNotificationConfig.toEntity(): DiscordNotificationConfigEntity =
        DiscordNotificationConfigEntity(
            id = id?.value,
            guildId = guildId.value,
            discordServerId = discordServerId,
            notificationType = notificationType.name,
            channelId = channelId,
            enabled = enabled,
            mentionRoleId = mentionRoleId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
