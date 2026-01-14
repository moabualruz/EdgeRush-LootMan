package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import com.edgerush.lootman.domain.raider.repository.RaiderWarcraftLogRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcRaiderWarcraftLogRepository(private val jdbcTemplate: JdbcTemplate) : RaiderWarcraftLogRepository {

    override fun findById(id: Long): RaiderWarcraftLogEntity? =
        jdbcTemplate.query("SELECT * FROM raider_warcraft_logs WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_warcraft_logs WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<RaiderWarcraftLogEntity> =
        jdbcTemplate.query("SELECT * FROM raider_warcraft_logs ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_warcraft_logs", Long::class.java) ?: 0L

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderWarcraftLogEntity> =
        jdbcTemplate.query("SELECT * FROM raider_warcraft_logs WHERE raider_id = ? ORDER BY difficulty LIMIT ? OFFSET ?", rowMapper, raiderId, limit, offset)

    override fun countByRaiderId(raiderId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_warcraft_logs WHERE raider_id = ?", Long::class.java, raiderId) ?: 0L

    override fun save(entity: RaiderWarcraftLogEntity): RaiderWarcraftLogEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM raider_warcraft_logs WHERE id = ?", id) }

    private fun insert(entity: RaiderWarcraftLogEntity): RaiderWarcraftLogEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO raider_warcraft_logs (raider_id, difficulty, score) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.raiderId)
            ps.setString(2, entity.difficulty)
            entity.score?.let { ps.setInt(3, it) } ?: ps.setNull(3, java.sql.Types.INTEGER)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: RaiderWarcraftLogEntity) {
        jdbcTemplate.update(
            "UPDATE raider_warcraft_logs SET raider_id=?, difficulty=?, score=? WHERE id=?",
            entity.raiderId, entity.difficulty, entity.score, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }
        RaiderWarcraftLogEntity(rs.getLong("id"), rs.getLong("raider_id"), rs.getString("difficulty"), getIntOrNull("score"))
    }
}
