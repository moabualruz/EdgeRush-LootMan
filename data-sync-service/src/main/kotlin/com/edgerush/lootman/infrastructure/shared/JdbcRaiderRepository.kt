package com.edgerush.lootman.infrastructure.shared

import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.CharacterClass
import com.edgerush.lootman.domain.shared.model.Raider
import com.edgerush.lootman.domain.shared.model.RaiderStatus
import com.edgerush.lootman.domain.shared.model.Role
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * JDBC implementation of RaiderRepository.
 *
 * Persists Raider aggregates to the raiders table.
 *
 * Note: This implementation assumes the raiders table has a guild_id column.
 * The column uses JPA naming conventions (camelCase) as per V0019 migration.
 */
@Repository
class JdbcRaiderRepository(
    private val jdbcTemplate: JdbcTemplate,
) : RaiderRepository {
    override fun findById(id: RaiderId): Raider? {
        val sql =
            """
            SELECT id, guild_id, "characterName", realm, "characterClass", role,
                   rank, status, "joinDate", "wowauditId"
            FROM raiders
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, raiderRowMapper, id.value)
        return results.firstOrNull()
    }

    override fun findByGuildId(guildId: GuildId): List<Raider> {
        val sql =
            """
            SELECT id, guild_id, "characterName", realm, "characterClass", role,
                   rank, status, "joinDate", "wowauditId"
            FROM raiders
            WHERE guild_id = ?
            ORDER BY "characterName"
            """.trimIndent()

        return jdbcTemplate.query(sql, raiderRowMapper, guildId.value)
    }

    override fun findByGuildId(
        guildId: GuildId,
        offset: Long,
        limit: Int,
    ): List<Raider> {
        val sql =
            """
            SELECT id, guild_id, "characterName", realm, "characterClass", role,
                   rank, status, "joinDate", "wowauditId"
            FROM raiders
            WHERE guild_id = ?
            ORDER BY "characterName"
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, raiderRowMapper, guildId.value, limit, offset)
    }

    override fun countByGuildId(guildId: GuildId): Long {
        val sql = "SELECT COUNT(*) FROM raiders WHERE guild_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId.value) ?: 0L
    }

    override fun findByCharacterNameAndRealm(
        characterName: String,
        realm: String,
    ): Raider? {
        val sql =
            """
            SELECT id, guild_id, "characterName", realm, "characterClass", role,
                   rank, status, "joinDate", "wowauditId"
            FROM raiders
            WHERE "characterName" = ? AND realm = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, raiderRowMapper, characterName, realm)
        return results.firstOrNull()
    }

    override fun save(raider: Raider): Raider {
        val exists = existsById(raider.id)

        if (exists) {
            updateRaider(raider)
        } else {
            insertRaider(raider)
        }

        return raider
    }

    override fun delete(id: RaiderId) {
        val sql = "DELETE FROM raiders WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    override fun findByIds(ids: List<RaiderId>): List<Raider> {
        if (ids.isEmpty()) return emptyList()

        val placeholders = ids.joinToString(", ") { "?" }
        val sql =
            """
            SELECT id, guild_id, "characterName", realm, "characterClass", role,
                   rank, status, "joinDate", "wowauditId"
            FROM raiders
            WHERE id IN ($placeholders)
            """.trimIndent()

        return jdbcTemplate.query(sql, raiderRowMapper, *ids.map { it.value }.toTypedArray())
    }

    private fun existsById(id: RaiderId): Boolean {
        val sql = "SELECT COUNT(*) FROM raiders WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id.value) ?: 0
        return count > 0
    }

    private fun insertRaider(raider: Raider) {
        val sql =
            """
            INSERT INTO raiders (
                guild_id, "characterName", realm, "characterClass", role,
                rank, status, "joinDate", "wowauditId"
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            raider.guildId.value,
            raider.characterName,
            raider.realm,
            raider.characterClass.name,
            raider.role.name,
            raider.rank,
            raider.status.name,
            raider.joinDate?.let { Timestamp.valueOf(it) },
            raider.wowauditId,
        )
    }

    private fun updateRaider(raider: Raider) {
        val sql =
            """
            UPDATE raiders SET
                guild_id = ?,
                "characterName" = ?,
                realm = ?,
                "characterClass" = ?,
                role = ?,
                rank = ?,
                status = ?,
                "joinDate" = ?,
                "wowauditId" = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            raider.guildId.value,
            raider.characterName,
            raider.realm,
            raider.characterClass.name,
            raider.role.name,
            raider.rank,
            raider.status.name,
            raider.joinDate?.let { Timestamp.valueOf(it) },
            raider.wowauditId,
            raider.id.value,
        )
    }

    private val raiderRowMapper =
        RowMapper { rs, _ ->
            val wowauditIdValue = rs.getLong("wowauditId")
            val wowauditId = if (rs.wasNull()) null else wowauditIdValue

            val joinDateTimestamp = rs.getTimestamp("joinDate")
            val joinDate: LocalDateTime? = joinDateTimestamp?.toLocalDateTime()

            val classStr = rs.getString("characterClass") ?: "WARRIOR"
            val characterClass =
                try {
                    CharacterClass.valueOf(classStr.uppercase().replace(" ", "_"))
                } catch (e: IllegalArgumentException) {
                    CharacterClass.WARRIOR
                }

            val roleStr = rs.getString("role") ?: "DPS"
            val role =
                try {
                    Role.valueOf(roleStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    Role.DPS
                }

            val statusStr = rs.getString("status") ?: "ACTIVE"
            val status = RaiderStatus.fromString(statusStr) ?: RaiderStatus.ACTIVE

            val guildIdStr = rs.getString("guild_id") ?: "default"

            Raider(
                id = RaiderId(rs.getLong("id")),
                guildId = GuildId(guildIdStr),
                characterName = rs.getString("characterName"),
                realm = rs.getString("realm"),
                characterClass = characterClass,
                role = role,
                rank = rs.getString("rank"),
                status = status,
                joinDate = joinDate,
                wowauditId = wowauditId,
            )
        }
}
