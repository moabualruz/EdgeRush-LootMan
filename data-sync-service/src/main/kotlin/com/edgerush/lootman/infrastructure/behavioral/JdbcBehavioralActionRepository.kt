package com.edgerush.lootman.infrastructure.behavioral

import com.edgerush.datasync.entity.BehavioralActionEntity
import com.edgerush.lootman.domain.behavioral.repository.BehavioralActionRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

/**
 * JDBC implementation of BehavioralActionRepository.
 *
 * Persists BehavioralActionEntity to the behavioral_actions table.
 */
@Repository
class JdbcBehavioralActionRepository(
    private val jdbcTemplate: JdbcTemplate,
) : BehavioralActionRepository {

    override fun findById(id: Long): BehavioralActionEntity? {
        val sql = """
            SELECT id, guild_id, character_name, action_type, deduction_amount,
                   reason, applied_by, applied_at, expires_at, is_active
            FROM behavioral_actions
            WHERE id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, behavioralActionRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM behavioral_actions WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(offset: Long, limit: Int): List<BehavioralActionEntity> {
        val sql = """
            SELECT id, guild_id, character_name, action_type, deduction_amount,
                   reason, applied_by, applied_at, expires_at, is_active
            FROM behavioral_actions
            ORDER BY applied_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, behavioralActionRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM behavioral_actions"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByGuildId(guildId: String, offset: Long, limit: Int): List<BehavioralActionEntity> {
        val sql = """
            SELECT id, guild_id, character_name, action_type, deduction_amount,
                   reason, applied_by, applied_at, expires_at, is_active
            FROM behavioral_actions
            WHERE guild_id = ?
            ORDER BY applied_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, behavioralActionRowMapper, guildId, limit, offset)
    }

    override fun countByGuildId(guildId: String): Long {
        val sql = "SELECT COUNT(*) FROM behavioral_actions WHERE guild_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId) ?: 0L
    }

    override fun findActiveByGuildId(guildId: String, offset: Long, limit: Int): List<BehavioralActionEntity> {
        val sql = """
            SELECT id, guild_id, character_name, action_type, deduction_amount,
                   reason, applied_by, applied_at, expires_at, is_active
            FROM behavioral_actions
            WHERE guild_id = ? AND is_active = true
            AND (expires_at IS NULL OR expires_at > ?)
            ORDER BY applied_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, behavioralActionRowMapper, guildId, Timestamp.valueOf(LocalDateTime.now()), limit, offset)
    }

    override fun countActiveByGuildId(guildId: String): Long {
        val sql = """
            SELECT COUNT(*) FROM behavioral_actions
            WHERE guild_id = ? AND is_active = true
            AND (expires_at IS NULL OR expires_at > ?)
        """.trimIndent()
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId, Timestamp.valueOf(LocalDateTime.now())) ?: 0L
    }

    override fun findByCharacter(guildId: String, characterName: String, offset: Long, limit: Int): List<BehavioralActionEntity> {
        val sql = """
            SELECT id, guild_id, character_name, action_type, deduction_amount,
                   reason, applied_by, applied_at, expires_at, is_active
            FROM behavioral_actions
            WHERE guild_id = ? AND character_name = ?
            ORDER BY applied_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, behavioralActionRowMapper, guildId, characterName, limit, offset)
    }

    override fun countByCharacter(guildId: String, characterName: String): Long {
        val sql = "SELECT COUNT(*) FROM behavioral_actions WHERE guild_id = ? AND character_name = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId, characterName) ?: 0L
    }

    override fun getTotalActiveDeduction(guildId: String, characterName: String): Double {
        val sql = """
            SELECT COALESCE(SUM(
                CASE WHEN action_type = 'DEDUCTION' THEN deduction_amount
                     WHEN action_type = 'RESTORATION' THEN -deduction_amount
                     ELSE 0
                END
            ), 0.0) as total
            FROM behavioral_actions
            WHERE guild_id = ? AND character_name = ? AND is_active = true
            AND (expires_at IS NULL OR expires_at > ?)
        """.trimIndent()
        val total = jdbcTemplate.queryForObject(sql, Double::class.java, guildId, characterName, Timestamp.valueOf(LocalDateTime.now())) ?: 0.0
        return maxOf(0.0, minOf(1.0, total))
    }

    override fun save(entity: BehavioralActionEntity): BehavioralActionEntity {
        return if (entity.id == null) {
            insertBehavioralAction(entity)
        } else {
            updateBehavioralAction(entity)
            entity
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM behavioral_actions WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertBehavioralAction(entity: BehavioralActionEntity): BehavioralActionEntity {
        val sql = """
            INSERT INTO behavioral_actions (
                guild_id, character_name, action_type, deduction_amount,
                reason, applied_by, applied_at, expires_at, is_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setString(1, entity.guildId)
            ps.setString(2, entity.characterName)
            ps.setString(3, entity.actionType)
            ps.setDouble(4, entity.deductionAmount)
            ps.setString(5, entity.reason)
            ps.setString(6, entity.appliedBy)
            ps.setTimestamp(7, Timestamp.valueOf(entity.appliedAt))
            entity.expiresAt?.let { ps.setTimestamp(8, Timestamp.valueOf(it)) } ?: ps.setNull(8, java.sql.Types.TIMESTAMP)
            ps.setBoolean(9, entity.isActive)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = generatedId?.toLong())
    }

    private fun updateBehavioralAction(entity: BehavioralActionEntity) {
        val sql = """
            UPDATE behavioral_actions SET
                guild_id = ?,
                character_name = ?,
                action_type = ?,
                deduction_amount = ?,
                reason = ?,
                applied_by = ?,
                applied_at = ?,
                expires_at = ?,
                is_active = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.guildId,
            entity.characterName,
            entity.actionType,
            entity.deductionAmount,
            entity.reason,
            entity.appliedBy,
            Timestamp.valueOf(entity.appliedAt),
            entity.expiresAt?.let { Timestamp.valueOf(it) },
            entity.isActive,
            entity.id,
        )
    }

    private val behavioralActionRowMapper = RowMapper { rs, _ ->
        val expiresAtTimestamp = rs.getTimestamp("expires_at")
        val expiresAt = expiresAtTimestamp?.toLocalDateTime()

        BehavioralActionEntity(
            id = rs.getLong("id"),
            guildId = rs.getString("guild_id"),
            characterName = rs.getString("character_name"),
            actionType = rs.getString("action_type"),
            deductionAmount = rs.getDouble("deduction_amount"),
            reason = rs.getString("reason"),
            appliedBy = rs.getString("applied_by"),
            appliedAt = rs.getTimestamp("applied_at").toLocalDateTime(),
            expiresAt = expiresAt,
            isActive = rs.getBoolean("is_active"),
        )
    }
}
