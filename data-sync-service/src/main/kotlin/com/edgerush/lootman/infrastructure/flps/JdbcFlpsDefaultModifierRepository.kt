package com.edgerush.lootman.infrastructure.flps

import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import com.edgerush.lootman.domain.flps.repository.FlpsDefaultModifierRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Statement
import java.time.OffsetDateTime

/**
 * JDBC implementation of FlpsDefaultModifierRepository.
 */
@Repository
class JdbcFlpsDefaultModifierRepository(
    private val jdbcTemplate: JdbcTemplate,
) : FlpsDefaultModifierRepository {
    override fun findById(id: Long): FlpsDefaultModifierEntity? {
        val sql =
            """
            SELECT id, category, modifier_key, modifier_value, description, created_at, updated_at
            FROM flps_default_modifiers
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, modifierRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM flps_default_modifiers WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<FlpsDefaultModifierEntity> {
        val sql =
            """
            SELECT id, category, modifier_key, modifier_value, description, created_at, updated_at
            FROM flps_default_modifiers
            ORDER BY category, modifier_key
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, modifierRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM flps_default_modifiers"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByCategory(
        category: String,
        offset: Long,
        limit: Int,
    ): List<FlpsDefaultModifierEntity> {
        val sql =
            """
            SELECT id, category, modifier_key, modifier_value, description, created_at, updated_at
            FROM flps_default_modifiers
            WHERE category = ?
            ORDER BY modifier_key
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, modifierRowMapper, category, limit, offset)
    }

    override fun countByCategory(category: String): Long {
        val sql = "SELECT COUNT(*) FROM flps_default_modifiers WHERE category = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, category) ?: 0L
    }

    override fun save(modifier: FlpsDefaultModifierEntity): FlpsDefaultModifierEntity {
        return if (modifier.id == null) {
            insertModifier(modifier)
        } else {
            updateModifier(modifier)
            modifier
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM flps_default_modifiers WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertModifier(modifier: FlpsDefaultModifierEntity): FlpsDefaultModifierEntity {
        val sql =
            """
            INSERT INTO flps_default_modifiers (category, modifier_key, modifier_value, description, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

        val now = OffsetDateTime.now()
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, modifier.category)
            ps.setString(2, modifier.modifierKey)
            ps.setBigDecimal(3, modifier.modifierValue)
            modifier.description?.let { ps.setString(4, it) } ?: ps.setNull(4, java.sql.Types.VARCHAR)
            ps.setObject(5, now)
            ps.setObject(6, now)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return modifier.copy(id = generatedId?.toLong(), createdAt = now, updatedAt = now)
    }

    private fun updateModifier(modifier: FlpsDefaultModifierEntity) {
        val sql =
            """
            UPDATE flps_default_modifiers SET
                category = ?,
                modifier_key = ?,
                modifier_value = ?,
                description = ?,
                updated_at = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
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
            FlpsDefaultModifierEntity(
                id = rs.getLong("id"),
                category = rs.getString("category"),
                modifierKey = rs.getString("modifier_key"),
                modifierValue = rs.getBigDecimal("modifier_value") ?: BigDecimal.ZERO,
                description = rs.getString("description"),
                createdAt = rs.getObject("created_at", OffsetDateTime::class.java) ?: OffsetDateTime.now(),
                updatedAt = rs.getObject("updated_at", OffsetDateTime::class.java) ?: OffsetDateTime.now(),
            )
        }
}
