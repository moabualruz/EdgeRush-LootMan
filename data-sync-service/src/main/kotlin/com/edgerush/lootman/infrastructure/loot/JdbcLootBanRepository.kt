package com.edgerush.lootman.infrastructure.loot

import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * JDBC implementation of LootBanRepository.
 *
 * Persists LootBan aggregates to the loot_bans table.
 * Column names follow the JPA naming conventions from V0019 migration.
 */
@Repository
class JdbcLootBanRepository(
    private val jdbcTemplate: JdbcTemplate,
) : LootBanRepository {
    override fun findById(id: LootBanId): LootBan? {
        val sql =
            """
            SELECT id, raiderId, guild_id, reason, bannedAt, expiresAt, is_active
            FROM loot_bans
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, lootBanRowMapper, id.value)
        return results.firstOrNull()
    }

    override fun findActiveByRaiderId(
        raiderId: RaiderId,
        guildId: GuildId,
    ): List<LootBan> {
        val sql =
            """
            SELECT id, raiderId, guild_id, reason, bannedAt, expiresAt, is_active
            FROM loot_bans
            WHERE raiderId = ? AND guild_id = ? AND is_active = true
            AND (expiresAt IS NULL OR expiresAt > NOW())
            ORDER BY bannedAt DESC
            """.trimIndent()

        return jdbcTemplate.query(sql, lootBanRowMapper, raiderId.value.toString(), guildId.value)
    }

    override fun save(lootBan: LootBan): LootBan {
        val exists = existsById(lootBan.id)

        if (exists) {
            updateLootBan(lootBan)
        } else {
            insertLootBan(lootBan)
        }

        return lootBan
    }

    override fun delete(id: LootBanId) {
        val sql = "DELETE FROM loot_bans WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    private fun existsById(id: LootBanId): Boolean {
        val sql = "SELECT COUNT(*) FROM loot_bans WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id.value) ?: 0
        return count > 0
    }

    private fun insertLootBan(ban: LootBan) {
        val sql =
            """
            INSERT INTO loot_bans (
                id, raiderId, guild_id, reason, bannedAt, expiresAt, is_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            ban.id.value,
            ban.raiderId.value.toString(),
            ban.guildId.value,
            ban.reason,
            Timestamp.from(ban.bannedAt),
            ban.expiresAt?.let { Timestamp.from(it) },
            ban.isActive(),
        )
    }

    private fun updateLootBan(ban: LootBan) {
        val sql =
            """
            UPDATE loot_bans SET
                raiderId = ?,
                guild_id = ?,
                reason = ?,
                bannedAt = ?,
                expiresAt = ?,
                is_active = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            ban.raiderId.value.toString(),
            ban.guildId.value,
            ban.reason,
            Timestamp.from(ban.bannedAt),
            ban.expiresAt?.let { Timestamp.from(it) },
            ban.isActive(),
            ban.id.value,
        )
    }

    private val lootBanRowMapper =
        RowMapper { rs, _ ->
            val expiresAtTimestamp = rs.getTimestamp("expiresAt")
            val expiresAt = expiresAtTimestamp?.toInstant()

            LootBan(
                id = LootBanId(rs.getString("id")),
                raiderId = RaiderId(rs.getString("raiderId").toLong()),
                guildId = GuildId(rs.getString("guild_id")),
                reason = rs.getString("reason"),
                bannedAt = rs.getTimestamp("bannedAt").toInstant(),
                expiresAt = expiresAt,
            )
        }
}
