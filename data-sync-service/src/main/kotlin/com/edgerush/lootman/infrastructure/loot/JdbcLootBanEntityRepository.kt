package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.lootman.domain.loot.repository.LootBanEntityRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * JDBC implementation of LootBanEntityRepository.
 *
 * Persists LootBanEntity to the loot_bans table.
 */
@Repository
class JdbcLootBanEntityRepository(
    private val jdbcTemplate: JdbcTemplate,
) : LootBanEntityRepository {

    override fun findById(id: Long): LootBanEntity? {
        val sql = """
            SELECT id, guild_id, character_name, reason, banned_by, banned_at, expires_at, is_active
            FROM loot_bans
            WHERE id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, lootBanRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM loot_bans WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(offset: Long, limit: Int): List<LootBanEntity> {
        val sql = """
            SELECT id, guild_id, character_name, reason, banned_by, banned_at, expires_at, is_active
            FROM loot_bans
            ORDER BY banned_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, lootBanRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM loot_bans"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByGuildId(guildId: String, offset: Long, limit: Int): List<LootBanEntity> {
        val sql = """
            SELECT id, guild_id, character_name, reason, banned_by, banned_at, expires_at, is_active
            FROM loot_bans
            WHERE guild_id = ?
            ORDER BY banned_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, lootBanRowMapper, guildId, limit, offset)
    }

    override fun countByGuildId(guildId: String): Long {
        val sql = "SELECT COUNT(*) FROM loot_bans WHERE guild_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId) ?: 0L
    }

    override fun findActiveByGuildId(guildId: String, offset: Long, limit: Int): List<LootBanEntity> {
        val sql = """
            SELECT id, guild_id, character_name, reason, banned_by, banned_at, expires_at, is_active
            FROM loot_bans
            WHERE guild_id = ? AND is_active = true
            AND (expires_at IS NULL OR expires_at > ?)
            ORDER BY banned_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, lootBanRowMapper, guildId, Timestamp.valueOf(LocalDateTime.now()), limit, offset)
    }

    override fun countActiveByGuildId(guildId: String): Long {
        val sql = """
            SELECT COUNT(*) FROM loot_bans
            WHERE guild_id = ? AND is_active = true
            AND (expires_at IS NULL OR expires_at > ?)
        """.trimIndent()
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId, Timestamp.valueOf(LocalDateTime.now())) ?: 0L
    }

    override fun isCharacterBanned(guildId: String, characterName: String): Boolean {
        val sql = """
            SELECT COUNT(*) FROM loot_bans
            WHERE guild_id = ? AND character_name = ? AND is_active = true
            AND (expires_at IS NULL OR expires_at > ?)
        """.trimIndent()
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, guildId, characterName, Timestamp.valueOf(LocalDateTime.now())) ?: 0
        return count > 0
    }

    override fun save(lootBan: LootBanEntity): LootBanEntity {
        return if (lootBan.id == null) {
            insertLootBan(lootBan)
        } else {
            updateLootBan(lootBan)
            lootBan
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM loot_bans WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertLootBan(lootBan: LootBanEntity): LootBanEntity {
        val sql = """
            INSERT INTO loot_bans (
                guild_id, character_name, reason, banned_by, banned_at, expires_at, is_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, lootBan.guildId)
            ps.setString(2, lootBan.characterName)
            ps.setString(3, lootBan.reason)
            ps.setString(4, lootBan.bannedBy)
            ps.setTimestamp(5, Timestamp.valueOf(lootBan.bannedAt))
            lootBan.expiresAt?.let { ps.setTimestamp(6, Timestamp.valueOf(it)) } ?: ps.setNull(6, java.sql.Types.TIMESTAMP)
            ps.setBoolean(7, lootBan.isActive)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return lootBan.copy(id = generatedId?.toLong())
    }

    private fun updateLootBan(lootBan: LootBanEntity) {
        val sql = """
            UPDATE loot_bans SET
                guild_id = ?,
                character_name = ?,
                reason = ?,
                banned_by = ?,
                banned_at = ?,
                expires_at = ?,
                is_active = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            lootBan.guildId,
            lootBan.characterName,
            lootBan.reason,
            lootBan.bannedBy,
            Timestamp.valueOf(lootBan.bannedAt),
            lootBan.expiresAt?.let { Timestamp.valueOf(it) },
            lootBan.isActive,
            lootBan.id,
        )
    }

    private val lootBanRowMapper = RowMapper { rs, _ ->
        val expiresAtTimestamp = rs.getTimestamp("expires_at")
        val expiresAt = expiresAtTimestamp?.toLocalDateTime()

        LootBanEntity(
            id = rs.getLong("id"),
            guildId = rs.getString("guild_id"),
            characterName = rs.getString("character_name"),
            reason = rs.getString("reason"),
            bannedBy = rs.getString("banned_by"),
            bannedAt = rs.getTimestamp("banned_at").toLocalDateTime(),
            expiresAt = expiresAt,
            isActive = rs.getBoolean("is_active"),
        )
    }
}
