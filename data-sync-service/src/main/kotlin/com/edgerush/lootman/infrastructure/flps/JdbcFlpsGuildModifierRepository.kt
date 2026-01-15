package com.edgerush.lootman.infrastructure.flps

import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import com.edgerush.lootman.domain.flps.repository.FlpsGuildModifierRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Statement
import java.time.OffsetDateTime

/**
 * JDBC implementation of FlpsGuildModifierRepository.
 */
@Repository
class JdbcFlpsGuildModifierRepository(
    private val jdbcTemplate: JdbcTemplate,
) : FlpsGuildModifierRepository {
    override fun findById(id: Long): FlpsGuildModifierEntity? {
        val sql =
            """
            SELECT id, guild_id, category, modifier_key, modifier_value, description, created_at, updated_at
            FROM flps_guild_modifiers
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, modifierRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM flps_guild_modifiers WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<FlpsGuildModifierEntity> {
        val sql =
            """
            SELECT id, guild_id, category, modifier_key, modifier_value, description, created_at, updated_at
            FROM flps_guild_modifiers
            ORDER BY guild_id, category, modifier_key
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, modifierRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM flps_guild_modifiers"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByGuildId(
        guildId: String,
        offset: Long,
        limit: Int,
    ): List<FlpsGuildModifierEntity> {
        val sql =
            """
            SELECT id, guild_id, category, modifier_key, modifier_value, description, created_at, updated_at
            FROM flps_guild_modifiers
            WHERE guild_id = ?
            ORDER BY category, modifier_key
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, modifierRowMapper, guildId, limit, offset)
    }

    override fun countByGuildId(guildId: String): Long {
        val sql = "SELECT COUNT(*) FROM flps_guild_modifiers WHERE guild_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId) ?: 0L
    }

    override fun findByGuildIdAndCategory(
        guildId: String,
        category: String,
        offset: Long,
        limit: Int,
    ): List<FlpsGuildModifierEntity> {
        val sql =
            """
            SELECT id, guild_id, category, modifier_key, modifier_value, description, created_at, updated_at
            FROM flps_guild_modifiers
            WHERE guild_id = ? AND category = ?
            ORDER BY modifier_key
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, modifierRowMapper, guildId, category, limit, offset)
    }

    override fun countByGuildIdAndCategory(
        guildId: String,
        category: String,
    ): Long {
        val sql = "SELECT COUNT(*) FROM flps_guild_modifiers WHERE guild_id = ? AND category = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId, category) ?: 0L
    }

    override fun save(modifier: FlpsGuildModifierEntity): FlpsGuildModifierEntity {
        return if (modifier.id == null) {
            insertModifier(modifier)
        } else {
            updateModifier(modifier)
            modifier
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM flps_guild_modifiers WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertModifier(modifier: FlpsGuildModifierEntity): FlpsGuildModifierEntity {
        val sql =
            """
            INSERT INTO flps_guild_modifiers (guild_id, category, modifier_key, modifier_value, description, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        val now = OffsetDateTime.now()
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, modifier.guildId)
            ps.setString(2, modifier.category)
            ps.setString(3, modifier.modifierKey)
            ps.setBigDecimal(4, modifier.modifierValue)
            modifier.description?.let { ps.setString(5, it) } ?: ps.setNull(5, java.sql.Types.VARCHAR)
            ps.setObject(6, now)
            ps.setObject(7, now)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return modifier.copy(id = generatedId?.toLong(), createdAt = now, updatedAt = now)
    }

    private fun updateModifier(modifier: FlpsGuildModifierEntity) {
        val sql =
            """
            UPDATE flps_guild_modifiers SET
                guild_id = ?,
                category = ?,
                modifier_key = ?,
                modifier_value = ?,
                description = ?,
                updated_at = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            modifier.guildId,
            modifier.category,
            modifier.modifierKey,
            modifier.modifierValue,
            modifier.description,
            OffsetDateTime.now(),
            modifier.id,
        )
    }

    private val modifierRowMapper =
        RowMapper { rs, _ ->
            FlpsGuildModifierEntity(
                id = rs.getLong("id"),
                guildId = rs.getString("guild_id"),
                category = rs.getString("category"),
                modifierKey = rs.getString("modifier_key"),
                modifierValue = rs.getBigDecimal("modifier_value") ?: BigDecimal.ZERO,
                description = rs.getString("description"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java) ?: OffsetDateTime.now(),
                updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java) ?: OffsetDateTime.now(),
            )
        }
}
