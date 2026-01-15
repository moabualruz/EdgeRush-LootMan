package com.edgerush.lootman.infrastructure.discord

import com.edgerush.lootman.domain.discord.model.DiscordUserId
import com.edgerush.lootman.domain.discord.model.DiscordUserLink
import com.edgerush.lootman.domain.discord.model.DiscordUserLinkId
import com.edgerush.lootman.domain.discord.repository.DiscordUserLinkRepository
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp

/**
 * JDBC implementation of DiscordUserLinkRepository.
 *
 * Persists Discord user links to the discord_user_links table.
 */
@Repository
class JdbcDiscordUserLinkRepository(
    private val jdbcTemplate: JdbcTemplate,
) : DiscordUserLinkRepository {
    override fun findById(id: DiscordUserLinkId): DiscordUserLink? {
        val sql =
            """
            SELECT id, discord_user_id, raider_id, is_primary, linked_at, linked_by
            FROM discord_user_links
            WHERE id = ?
            """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, id.value).firstOrNull()
    }

    override fun findByDiscordUserId(discordUserId: DiscordUserId): List<DiscordUserLink> {
        val sql =
            """
            SELECT id, discord_user_id, raider_id, is_primary, linked_at, linked_by
            FROM discord_user_links
            WHERE discord_user_id = ?
            ORDER BY is_primary DESC, linked_at ASC
            """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, discordUserId.value)
    }

    override fun findPrimaryByDiscordUserId(discordUserId: DiscordUserId): DiscordUserLink? {
        val sql =
            """
            SELECT id, discord_user_id, raider_id, is_primary, linked_at, linked_by
            FROM discord_user_links
            WHERE discord_user_id = ? AND is_primary = true
            """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, discordUserId.value).firstOrNull()
    }

    override fun findByRaiderId(raiderId: RaiderId): List<DiscordUserLink> {
        val sql =
            """
            SELECT id, discord_user_id, raider_id, is_primary, linked_at, linked_by
            FROM discord_user_links
            WHERE raider_id = ?
            ORDER BY linked_at ASC
            """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, raiderId.value)
    }

    override fun existsByDiscordUserIdAndRaiderId(
        discordUserId: DiscordUserId,
        raiderId: RaiderId,
    ): Boolean {
        val sql =
            """
            SELECT COUNT(*) FROM discord_user_links
            WHERE discord_user_id = ? AND raider_id = ?
            """.trimIndent()

        val count = jdbcTemplate.queryForObject(sql, Long::class.java, discordUserId.value, raiderId.value)
        return (count ?: 0) > 0
    }

    override fun save(link: DiscordUserLink): DiscordUserLink {
        return if (link.id == null) {
            insert(link)
        } else {
            update(link)
        }
    }

    private fun insert(link: DiscordUserLink): DiscordUserLink {
        val sql =
            """
            INSERT INTO discord_user_links (discord_user_id, raider_id, is_primary, linked_at, linked_by)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, link.discordUserId.value)
            ps.setLong(2, link.raiderId.value)
            ps.setBoolean(3, link.isPrimary)
            ps.setTimestamp(4, Timestamp.from(link.linkedAt))
            ps.setString(5, link.linkedBy)
            ps
        }, keyHolder)

        val generatedId =
            keyHolder.keys?.get("id") as? Long
                ?: throw IllegalStateException("Failed to retrieve generated ID for discord_user_link")

        return link.withId(DiscordUserLinkId(generatedId))
    }

    private fun update(link: DiscordUserLink): DiscordUserLink {
        val sql =
            """
            UPDATE discord_user_links
            SET discord_user_id = ?, raider_id = ?, is_primary = ?, linked_at = ?, linked_by = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            link.discordUserId.value,
            link.raiderId.value,
            link.isPrimary,
            Timestamp.from(link.linkedAt),
            link.linkedBy,
            link.id!!.value,
        )

        return link
    }

    override fun deleteById(id: DiscordUserLinkId) {
        val sql = "DELETE FROM discord_user_links WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    override fun deleteByDiscordUserId(discordUserId: DiscordUserId): Int {
        val sql = "DELETE FROM discord_user_links WHERE discord_user_id = ?"
        return jdbcTemplate.update(sql, discordUserId.value)
    }

    override fun clearPrimaryForDiscordUser(discordUserId: DiscordUserId) {
        val sql =
            """
            UPDATE discord_user_links
            SET is_primary = false
            WHERE discord_user_id = ? AND is_primary = true
            """.trimIndent()

        jdbcTemplate.update(sql, discordUserId.value)
    }

    override fun countByDiscordUserId(discordUserId: DiscordUserId): Long {
        val sql = "SELECT COUNT(*) FROM discord_user_links WHERE discord_user_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, discordUserId.value) ?: 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<DiscordUserLink> {
        val sql =
            """
            SELECT id, discord_user_id, raider_id, is_primary, linked_at, linked_by
            FROM discord_user_links
            ORDER BY id ASC
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, rowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM discord_user_links"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0
    }

    private val rowMapper =
        RowMapper { rs, _ ->
            DiscordUserLink(
                id = DiscordUserLinkId(rs.getLong("id")),
                discordUserId = DiscordUserId(rs.getString("discord_user_id")),
                raiderId = RaiderId(rs.getLong("raider_id")),
                isPrimary = rs.getBoolean("is_primary"),
                linkedAt = rs.getTimestamp("linked_at").toInstant(),
                linkedBy = rs.getString("linked_by"),
            )
        }
}
