package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import com.edgerush.lootman.domain.raider.repository.RaiderVaultSlotRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

/**
 * JDBC implementation of RaiderVaultSlotRepository.
 *
 * Persists RaiderVaultSlotEntity to the raider_vault_slots table.
 */
@Repository
class JdbcRaiderVaultSlotRepository(
    private val jdbcTemplate: JdbcTemplate,
) : RaiderVaultSlotRepository {

    override fun findById(id: Long): RaiderVaultSlotEntity? {
        val sql = """
            SELECT id, raider_id, slot, unlocked
            FROM raider_vault_slots
            WHERE id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, vaultSlotRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM raider_vault_slots WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(offset: Long, limit: Int): List<RaiderVaultSlotEntity> {
        val sql = """
            SELECT id, raider_id, slot, unlocked
            FROM raider_vault_slots
            ORDER BY raider_id, slot, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, vaultSlotRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM raider_vault_slots"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderVaultSlotEntity> {
        val sql = """
            SELECT id, raider_id, slot, unlocked
            FROM raider_vault_slots
            WHERE raider_id = ?
            ORDER BY slot, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, vaultSlotRowMapper, raiderId, limit, offset)
    }

    override fun countByRaiderId(raiderId: Long): Long {
        val sql = "SELECT COUNT(*) FROM raider_vault_slots WHERE raider_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, raiderId) ?: 0L
    }

    override fun findUnlockedByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderVaultSlotEntity> {
        val sql = """
            SELECT id, raider_id, slot, unlocked
            FROM raider_vault_slots
            WHERE raider_id = ? AND unlocked = true
            ORDER BY slot, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, vaultSlotRowMapper, raiderId, limit, offset)
    }

    override fun countUnlockedByRaiderId(raiderId: Long): Long {
        val sql = "SELECT COUNT(*) FROM raider_vault_slots WHERE raider_id = ? AND unlocked = true"
        return jdbcTemplate.queryForObject(sql, Long::class.java, raiderId) ?: 0L
    }

    override fun save(entity: RaiderVaultSlotEntity): RaiderVaultSlotEntity {
        return if (entity.id == null) {
            insertVaultSlot(entity)
        } else {
            updateVaultSlot(entity)
            entity
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM raider_vault_slots WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertVaultSlot(entity: RaiderVaultSlotEntity): RaiderVaultSlotEntity {
        val sql = """
            INSERT INTO raider_vault_slots (raider_id, slot, unlocked)
            VALUES (?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, entity.raiderId)
            ps.setString(2, entity.slot)
            entity.unlocked?.let { ps.setBoolean(3, it) } ?: ps.setNull(3, java.sql.Types.BOOLEAN)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = generatedId?.toLong())
    }

    private fun updateVaultSlot(entity: RaiderVaultSlotEntity) {
        val sql = """
            UPDATE raider_vault_slots SET
                raider_id = ?,
                slot = ?,
                unlocked = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.raiderId,
            entity.slot,
            entity.unlocked,
            entity.id,
        )
    }

    private val vaultSlotRowMapper = RowMapper { rs, _ ->
        val unlockedValue = rs.getBoolean("unlocked")
        val unlocked = if (rs.wasNull()) null else unlockedValue

        RaiderVaultSlotEntity(
            id = rs.getLong("id"),
            raiderId = rs.getLong("raider_id"),
            slot = rs.getString("slot"),
            unlocked = unlocked,
        )
    }
}
