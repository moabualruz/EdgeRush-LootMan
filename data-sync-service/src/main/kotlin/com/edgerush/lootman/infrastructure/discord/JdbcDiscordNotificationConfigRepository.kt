package com.edgerush.lootman.infrastructure.discord

import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfig
import com.edgerush.lootman.domain.discord.model.DiscordNotificationConfigId
import com.edgerush.lootman.domain.discord.model.DiscordNotificationType
import com.edgerush.lootman.domain.discord.repository.DiscordNotificationConfigRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp

/**
 * JDBC implementation of DiscordNotificationConfigRepository.
 */
@Repository
class JdbcDiscordNotificationConfigRepository(
    private val jdbcTemplate: JdbcTemplate
) : DiscordNotificationConfigRepository {

    override fun findById(id: DiscordNotificationConfigId): DiscordNotificationConfig? {
        val sql = """
            SELECT id, guild_id, discord_server_id, notification_type, channel_id, enabled, mention_role_id, created_at, updated_at
            FROM discord_notification_configs
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, id.value).firstOrNull()
    }

    override fun findByGuildId(guildId: GuildId): List<DiscordNotificationConfig> {
        val sql = """
            SELECT id, guild_id, discord_server_id, notification_type, channel_id, enabled, mention_role_id, created_at, updated_at
            FROM discord_notification_configs
            WHERE guild_id = ?
            ORDER BY notification_type ASC
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, guildId.value)
    }

    override fun findByGuildIdAndType(guildId: GuildId, type: DiscordNotificationType): DiscordNotificationConfig? {
        val sql = """
            SELECT id, guild_id, discord_server_id, notification_type, channel_id, enabled, mention_role_id, created_at, updated_at
            FROM discord_notification_configs
            WHERE guild_id = ? AND notification_type = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, guildId.value, type.name).firstOrNull()
    }

    override fun findEnabledByGuildId(guildId: GuildId): List<DiscordNotificationConfig> {
        val sql = """
            SELECT id, guild_id, discord_server_id, notification_type, channel_id, enabled, mention_role_id, created_at, updated_at
            FROM discord_notification_configs
            WHERE guild_id = ? AND enabled = true
            ORDER BY notification_type ASC
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, guildId.value)
    }

    override fun findEnabledByGuildIdAndType(guildId: GuildId, type: DiscordNotificationType): DiscordNotificationConfig? {
        val sql = """
            SELECT id, guild_id, discord_server_id, notification_type, channel_id, enabled, mention_role_id, created_at, updated_at
            FROM discord_notification_configs
            WHERE guild_id = ? AND notification_type = ? AND enabled = true
        """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, guildId.value, type.name).firstOrNull()
    }

    override fun save(config: DiscordNotificationConfig): DiscordNotificationConfig {
        return if (config.id == null) {
            insert(config)
        } else {
            update(config)
        }
    }

    private fun insert(config: DiscordNotificationConfig): DiscordNotificationConfig {
        val sql = """
            INSERT INTO discord_notification_configs (guild_id, discord_server_id, notification_type, channel_id, enabled, mention_role_id, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, config.guildId.value)
            ps.setString(2, config.discordServerId)
            ps.setString(3, config.notificationType.name)
            ps.setString(4, config.channelId)
            ps.setBoolean(5, config.enabled)
            ps.setString(6, config.mentionRoleId)
            ps.setTimestamp(7, Timestamp.from(config.createdAt))
            ps.setTimestamp(8, config.updatedAt?.let { Timestamp.from(it) })
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Long
            ?: throw IllegalStateException("Failed to retrieve generated ID for discord_notification_config")

        return config.withId(DiscordNotificationConfigId(generatedId))
    }

    private fun update(config: DiscordNotificationConfig): DiscordNotificationConfig {
        val sql = """
            UPDATE discord_notification_configs
            SET guild_id = ?, discord_server_id = ?, notification_type = ?, channel_id = ?, enabled = ?, mention_role_id = ?, updated_at = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            config.guildId.value,
            config.discordServerId,
            config.notificationType.name,
            config.channelId,
            config.enabled,
            config.mentionRoleId,
            config.updatedAt?.let { Timestamp.from(it) },
            config.id!!.value
        )

        return config
    }

    override fun deleteById(id: DiscordNotificationConfigId) {
        val sql = "DELETE FROM discord_notification_configs WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    override fun deleteByGuildId(guildId: GuildId): Int {
        val sql = "DELETE FROM discord_notification_configs WHERE guild_id = ?"
        return jdbcTemplate.update(sql, guildId.value)
    }

    override fun existsByGuildIdAndType(guildId: GuildId, type: DiscordNotificationType): Boolean {
        val sql = """
            SELECT COUNT(*) FROM discord_notification_configs
            WHERE guild_id = ? AND notification_type = ?
        """.trimIndent()

        val count = jdbcTemplate.queryForObject(sql, Long::class.java, guildId.value, type.name)
        return (count ?: 0) > 0
    }

    private val rowMapper = RowMapper { rs, _ ->
        DiscordNotificationConfig(
            id = DiscordNotificationConfigId(rs.getLong("id")),
            guildId = GuildId(rs.getString("guild_id")),
            discordServerId = rs.getString("discord_server_id"),
            notificationType = DiscordNotificationType.valueOf(rs.getString("notification_type")),
            channelId = rs.getString("channel_id"),
            enabled = rs.getBoolean("enabled"),
            mentionRoleId = rs.getString("mention_role_id"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            updatedAt = rs.getTimestamp("updated_at")?.toInstant()
        )
    }
}
