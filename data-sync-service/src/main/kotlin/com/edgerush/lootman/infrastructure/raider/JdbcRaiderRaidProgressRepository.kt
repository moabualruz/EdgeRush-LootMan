package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import com.edgerush.lootman.domain.raider.repository.RaiderRaidProgressRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcRaiderRaidProgressRepository(private val jdbcTemplate: JdbcTemplate) : RaiderRaidProgressRepository {

    override fun findById(id: Long): RaiderRaidProgressEntity? =
        jdbcTemplate.query("SELECT * FROM raider_raid_progress WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_raid_progress WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<RaiderRaidProgressEntity> =
        jdbcTemplate.query("SELECT * FROM raider_raid_progress ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_raid_progress", Long::class.java) ?: 0L

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderRaidProgressEntity> =
        jdbcTemplate.query("SELECT * FROM raider_raid_progress WHERE raider_id = ? ORDER BY raid, difficulty LIMIT ? OFFSET ?", rowMapper, raiderId, limit, offset)

    override fun countByRaiderId(raiderId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_raid_progress WHERE raider_id = ?", Long::class.java, raiderId) ?: 0L

    override fun save(entity: RaiderRaidProgressEntity): RaiderRaidProgressEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM raider_raid_progress WHERE id = ?", id) }

    private fun insert(entity: RaiderRaidProgressEntity): RaiderRaidProgressEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO raider_raid_progress (raider_id, raid, difficulty, bosses_defeated) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.raiderId)
            ps.setString(2, entity.raid)
            ps.setString(3, entity.difficulty)
            entity.bossesDefeated?.let { ps.setInt(4, it) } ?: ps.setNull(4, java.sql.Types.INTEGER)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: RaiderRaidProgressEntity) {
        jdbcTemplate.update(
            "UPDATE raider_raid_progress SET raider_id=?, raid=?, difficulty=?, bosses_defeated=? WHERE id=?",
            entity.raiderId, entity.raid, entity.difficulty, entity.bossesDefeated, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }
        RaiderRaidProgressEntity(rs.getLong("id"), rs.getLong("raider_id"), rs.getString("raid"), rs.getString("difficulty"), getIntOrNull("bosses_defeated"))
    }
}
