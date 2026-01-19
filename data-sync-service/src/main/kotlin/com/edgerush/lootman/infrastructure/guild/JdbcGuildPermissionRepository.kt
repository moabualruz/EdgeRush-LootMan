package com.edgerush.lootman.infrastructure.guild

import com.edgerush.lootman.domain.guild.model.GuildPermission
import com.edgerush.lootman.domain.guild.model.GuildPermissionId
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import com.edgerush.lootman.domain.guild.repository.GuildPermissionRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.ZoneOffset

/**
 * JDBC implementation of GuildPermissionRepository.
 */
@Repository
class JdbcGuildPermissionRepository(
    private val jdbcTemplate: JdbcTemplate,
) : GuildPermissionRepository {
    override fun findByGuildId(guildId: GuildId): List<GuildPermission> {
        val sql =
            """
            SELECT id, guild_id, rank_name, permission_type, created_at
            FROM guild_permissions
            WHERE guild_id = ?
            ORDER BY rank_name, permission_type
            """.trimIndent()

        return jdbcTemplate.query(sql, permissionRowMapper, guildId.value)
    }

    override fun findByGuildIdAndRankName(
        guildId: GuildId,
        rankName: String,
    ): List<GuildPermission> {
        val sql =
            """
            SELECT id, guild_id, rank_name, permission_type, created_at
            FROM guild_permissions
            WHERE guild_id = ? AND rank_name = ?
            ORDER BY permission_type
            """.trimIndent()

        return jdbcTemplate.query(sql, permissionRowMapper, guildId.value, rankName)
    }

    override fun hasPermission(
        guildId: GuildId,
        rankName: String,
        permissionType: GuildPermissionType,
    ): Boolean {
        val sql =
            """
            SELECT COUNT(*) FROM guild_permissions
            WHERE guild_id = ? AND rank_name = ? AND permission_type = ?
            """.trimIndent()

        val count = jdbcTemplate.queryForObject(sql, Int::class.java, guildId.value, rankName, permissionType.name) ?: 0
        return count > 0
    }

    override fun findById(id: GuildPermissionId): GuildPermission? {
        val sql =
            """
            SELECT id, guild_id, rank_name, permission_type, created_at
            FROM guild_permissions
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, permissionRowMapper, id.value)
        return results.firstOrNull()
    }

    override fun save(permission: GuildPermission): GuildPermission {
        return if (permission.id == null) {
            insert(permission)
        } else {
            update(permission)
            permission
        }
    }

    override fun deleteById(id: GuildPermissionId) {
        val sql = "DELETE FROM guild_permissions WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    override fun deleteByGuildId(guildId: GuildId) {
        val sql = "DELETE FROM guild_permissions WHERE guild_id = ?"
        jdbcTemplate.update(sql, guildId.value)
    }

    override fun findDistinctRankNamesByGuildId(guildId: GuildId): List<String> {
        val sql =
            """
            SELECT DISTINCT rank_name FROM guild_permissions
            WHERE guild_id = ?
            ORDER BY rank_name
            """.trimIndent()

        return jdbcTemplate.queryForList(sql, String::class.java, guildId.value)
    }

    override fun findByGuildIdAndRankNames(
        guildRanks: List<Pair<GuildId, String>>
    ): Map<Pair<String, String>, List<GuildPermissionType>> {
        if (guildRanks.isEmpty()) return emptyMap()

        // Build a WHERE clause with OR conditions for each (guild_id, rank_name) pair
        val conditions = guildRanks.joinToString(" OR ") { "(guild_id = ? AND rank_name = ?)" }
        val sql = """
            SELECT guild_id, rank_name, permission_type
            FROM guild_permissions
            WHERE $conditions
            ORDER BY guild_id, rank_name, permission_type
        """.trimIndent()

        // Flatten the pairs into a list of parameters
        val params = guildRanks.flatMap { listOf(it.first.value, it.second) }.toTypedArray()

        val results = jdbcTemplate.query(sql, { rs, _ ->
            Triple(
                rs.getString("guild_id"),
                rs.getString("rank_name"),
                GuildPermissionType.valueOf(rs.getString("permission_type"))
            )
        }, *params)

        // Group by (guildId, rankName)
        return results.groupBy(
            keySelector = { Pair(it.first, it.second) },
            valueTransform = { it.third }
        )
    }

    private fun insert(permission: GuildPermission): GuildPermission {
        val sql =
            """
            INSERT INTO guild_permissions (guild_id, rank_name, permission_type, created_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT ON CONSTRAINT uq_guild_rank_permission DO NOTHING
            """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, permission.guildId.value)
            ps.setString(2, permission.rankName)
            ps.setString(3, permission.permissionType.name)
            ps.setTimestamp(4, Timestamp.from(permission.createdAt))
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return if (generatedId != null) {
            permission.copy(id = GuildPermissionId(generatedId.toLong()))
        } else {
            // Permission already exists, find and return it
            findByGuildIdAndRankName(permission.guildId, permission.rankName)
                .find { it.permissionType == permission.permissionType }
                ?: permission
        }
    }

    private fun update(permission: GuildPermission) {
        val sql =
            """
            UPDATE guild_permissions SET
                guild_id = ?,
                rank_name = ?,
                permission_type = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            permission.guildId.value,
            permission.rankName,
            permission.permissionType.name,
            permission.id?.value,
        )
    }

    private val permissionRowMapper =
        RowMapper { rs, _ ->
            GuildPermission(
                id = GuildPermissionId(rs.getLong("id")),
                guildId = GuildId(rs.getString("guild_id")),
                rankName = rs.getString("rank_name"),
                permissionType = GuildPermissionType.valueOf(rs.getString("permission_type")),
                createdAt = rs.getTimestamp("created_at")?.toInstant() ?: java.time.Instant.now(),
            )
        }
}
