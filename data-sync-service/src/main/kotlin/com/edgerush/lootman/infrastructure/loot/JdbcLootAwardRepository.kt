package com.edgerush.lootman.infrastructure.loot

import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * JDBC implementation of LootAwardRepository.
 *
 * Persists LootAward aggregates to the loot_awards table.
 * Note: guildId is obtained via JOIN with raiders table since loot_awards
 * doesn't have a direct guild_id column.
 *
 * Uses snake_case column names as per V0045 migration.
 */
@Repository
class JdbcLootAwardRepository(
    private val jdbcTemplate: JdbcTemplate,
) : LootAwardRepository {
    override fun findById(id: LootAwardId): LootAward? {
        val sql =
            """
            SELECT la.id, la.item_id, la.raider_id, r.guild_id, la.awarded_at, la.flps, la.tier, la.discarded
            FROM loot_awards la
            JOIN raiders r ON la.raider_id = r.id
            WHERE la.id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, lootAwardRowMapper, id.value.toIntOrNull() ?: 0)
        return results.firstOrNull()
    }

    override fun findByRaiderId(raiderId: RaiderId): List<LootAward> {
        val sql =
            """
            SELECT la.id, la.item_id, la.raider_id, r.guild_id, la.awarded_at, la.flps, la.tier, la.discarded
            FROM loot_awards la
            JOIN raiders r ON la.raider_id = r.id
            WHERE la.raider_id = ?
            ORDER BY la.awarded_at DESC
            """.trimIndent()

        return jdbcTemplate.query(sql, lootAwardRowMapper, raiderId.value)
    }

    override fun findByGuildId(guildId: GuildId): List<LootAward> {
        val sql =
            """
            SELECT la.id, la.item_id, la.raider_id, r.guild_id, la.awarded_at, la.flps, la.tier, la.discarded
            FROM loot_awards la
            JOIN raiders r ON la.raider_id = r.id
            WHERE r.guild_id = ?
            ORDER BY la.awarded_at DESC
            """.trimIndent()

        return jdbcTemplate.query(sql, lootAwardRowMapper, guildId.value)
    }

    override fun findByGuildId(
        guildId: GuildId,
        offset: Long,
        limit: Int,
    ): List<LootAward> {
        val sql =
            """
            SELECT la.id, la.item_id, la.raider_id, r.guild_id, la.awarded_at, la.flps, la.tier, la.discarded
            FROM loot_awards la
            JOIN raiders r ON la.raider_id = r.id
            WHERE r.guild_id = ?
            ORDER BY la.awarded_at DESC
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, lootAwardRowMapper, guildId.value, limit, offset)
    }

    override fun countByGuildId(guildId: GuildId): Long {
        val sql =
            """
            SELECT COUNT(*) FROM loot_awards la
            JOIN raiders r ON la.raider_id = r.id
            WHERE r.guild_id = ?
            """.trimIndent()
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId.value) ?: 0L
    }

    override fun save(lootAward: LootAward): LootAward {
        val exists = existsById(lootAward.id)

        if (exists) {
            updateLootAward(lootAward)
        } else {
            insertLootAward(lootAward)
        }

        return lootAward
    }

    override fun delete(id: LootAwardId) {
        val sql = "DELETE FROM loot_awards WHERE id = ?"
        jdbcTemplate.update(sql, id.value.toIntOrNull() ?: 0)
    }

    override fun findByRaiderIds(raiderIds: List<RaiderId>): List<LootAward> {
        if (raiderIds.isEmpty()) return emptyList()

        val placeholders = raiderIds.joinToString(", ") { "?" }
        val sql =
            """
            SELECT la.id, la.item_id, la.raider_id, r.guild_id, la.awarded_at, la.flps, la.tier, la.discarded
            FROM loot_awards la
            JOIN raiders r ON la.raider_id = r.id
            WHERE la.raider_id IN ($placeholders)
            ORDER BY la.awarded_at DESC
            """.trimIndent()

        return jdbcTemplate.query(sql, lootAwardRowMapper, *raiderIds.map { it.value }.toTypedArray())
    }

    private fun existsById(id: LootAwardId): Boolean {
        val sql = "SELECT COUNT(*) FROM loot_awards WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id.value.toIntOrNull() ?: 0) ?: 0
        return count > 0
    }

    private fun insertLootAward(award: LootAward) {
        // Note: loot_awards table requires item_name which we don't have in domain model
        // Using a placeholder for now - in production this should be resolved via item lookup
        val sql =
            """
            INSERT INTO loot_awards (
                item_id, raider_id, item_name, tier, flps, rdf, awarded_at, discarded
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            award.itemId.value,
            award.raiderId.value,
            "Unknown Item", // item_name is NOT NULL in schema
            award.tier.name,
            award.flpsScore.value,
            0.0, // rdf is NOT NULL in schema
            Timestamp.from(award.awardedAt),
            !award.isActive(),
        )
    }

    private fun updateLootAward(award: LootAward) {
        val sql =
            """
            UPDATE loot_awards SET
                item_id = ?,
                raider_id = ?,
                tier = ?,
                flps = ?,
                awarded_at = ?,
                discarded = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            award.itemId.value,
            award.raiderId.value,
            award.tier.name,
            award.flpsScore.value,
            Timestamp.from(award.awardedAt),
            !award.isActive(),
            award.id.value.toIntOrNull() ?: 0,
        )
    }

    private val lootAwardRowMapper =
        RowMapper { rs, _ ->
            val tierStr = rs.getString("tier") ?: "MYTHIC"
            val tier =
                try {
                    LootTier.valueOf(tierStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    LootTier.MYTHIC
                }

            // Use discarded column to determine status (discarded = true means REVOKED)
            val discarded = rs.getBoolean("discarded")

            val guildIdStr = rs.getString("guild_id") ?: "default"

            LootAward(
                id = LootAwardId(rs.getInt("id").toString()),
                itemId = ItemId(rs.getLong("item_id")),
                raiderId = RaiderId(rs.getLong("raider_id")),
                guildId = GuildId(guildIdStr),
                awardedAt = rs.getTimestamp("awarded_at").toInstant(),
                flpsScore = run {
                    val rawFlps = rs.getDouble("flps")
                    // DB stores FLPS on 0-100 scale (from WoWAudit sync), normalize to 0.0-1.0
                    val normalized = if (rawFlps > 1.0) rawFlps / 100.0 else rawFlps
                    FlpsScore.of(normalized.coerceIn(0.0, 1.0))
                },
                tier = tier,
            )
        }
}
