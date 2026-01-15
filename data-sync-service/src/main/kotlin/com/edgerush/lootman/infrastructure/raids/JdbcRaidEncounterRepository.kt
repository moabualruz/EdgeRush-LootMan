package com.edgerush.lootman.infrastructure.raids

import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.lootman.domain.raids.repository.RaidEncounterRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

/**
 * JDBC implementation of RaidEncounterRepository.
 *
 * Persists RaidEncounter entities to the raid_encounters table.
 */
@Repository
class JdbcRaidEncounterRepository(
    private val jdbcTemplate: JdbcTemplate,
) : RaidEncounterRepository {
    override fun findById(id: Long): RaidEncounterEntity? {
        val sql =
            """
            SELECT id, raid_id, encounter_id, name, enabled, extra, notes
            FROM raid_encounters
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, encounterRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM raid_encounters WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaidEncounterEntity> {
        val sql =
            """
            SELECT id, raid_id, encounter_id, name, enabled, extra, notes
            FROM raid_encounters
            ORDER BY raid_id, id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, encounterRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM raid_encounters"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByRaidId(
        raidId: Long,
        offset: Long,
        limit: Int,
    ): List<RaidEncounterEntity> {
        val sql =
            """
            SELECT id, raid_id, encounter_id, name, enabled, extra, notes
            FROM raid_encounters
            WHERE raid_id = ?
            ORDER BY id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, encounterRowMapper, raidId, limit, offset)
    }

    override fun countByRaidId(raidId: Long): Long {
        val sql = "SELECT COUNT(*) FROM raid_encounters WHERE raid_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, raidId) ?: 0L
    }

    override fun findEnabledByRaidId(
        raidId: Long,
        offset: Long,
        limit: Int,
    ): List<RaidEncounterEntity> {
        val sql =
            """
            SELECT id, raid_id, encounter_id, name, enabled, extra, notes
            FROM raid_encounters
            WHERE raid_id = ? AND enabled = true
            ORDER BY id
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, encounterRowMapper, raidId, limit, offset)
    }

    override fun countEnabledByRaidId(raidId: Long): Long {
        val sql = "SELECT COUNT(*) FROM raid_encounters WHERE raid_id = ? AND enabled = true"
        return jdbcTemplate.queryForObject(sql, Long::class.java, raidId) ?: 0L
    }

    override fun save(encounter: RaidEncounterEntity): RaidEncounterEntity {
        return if (encounter.id == null) {
            insertEncounter(encounter)
        } else {
            updateEncounter(encounter)
            encounter
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM raid_encounters WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertEncounter(encounter: RaidEncounterEntity): RaidEncounterEntity {
        val sql =
            """
            INSERT INTO raid_encounters (raid_id, encounter_id, name, enabled, extra, notes)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, encounter.raidId)
            encounter.encounterId?.let { ps.setLong(2, it) } ?: ps.setNull(2, java.sql.Types.BIGINT)
            encounter.name?.let { ps.setString(3, it) } ?: ps.setNull(3, java.sql.Types.VARCHAR)
            encounter.enabled?.let { ps.setBoolean(4, it) } ?: ps.setNull(4, java.sql.Types.BOOLEAN)
            encounter.extra?.let { ps.setBoolean(5, it) } ?: ps.setNull(5, java.sql.Types.BOOLEAN)
            encounter.notes?.let { ps.setString(6, it) } ?: ps.setNull(6, java.sql.Types.VARCHAR)
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return encounter.copy(id = generatedId?.toLong())
    }

    private fun updateEncounter(encounter: RaidEncounterEntity) {
        val sql =
            """
            UPDATE raid_encounters SET
                raid_id = ?,
                encounter_id = ?,
                name = ?,
                enabled = ?,
                extra = ?,
                notes = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            encounter.raidId,
            encounter.encounterId,
            encounter.name,
            encounter.enabled,
            encounter.extra,
            encounter.notes,
            encounter.id,
        )
    }

    private val encounterRowMapper =
        RowMapper { rs, _ ->
            val encounterIdValue = rs.getLong("encounter_id")
            val encounterId = if (rs.wasNull()) null else encounterIdValue

            val enabledValue = rs.getBoolean("enabled")
            val enabled = if (rs.wasNull()) null else enabledValue

            val extraValue = rs.getBoolean("extra")
            val extra = if (rs.wasNull()) null else extraValue

            RaidEncounterEntity(
                id = rs.getLong("id"),
                raidId = rs.getLong("raid_id"),
                encounterId = encounterId,
                name = rs.getString("name"),
                enabled = enabled,
                extra = extra,
                notes = rs.getString("notes"),
            )
        }
}
