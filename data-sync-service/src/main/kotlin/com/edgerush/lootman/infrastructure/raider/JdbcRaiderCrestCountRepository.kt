package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderCrestCountEntity
import com.edgerush.lootman.domain.raider.repository.RaiderCrestCountRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcRaiderCrestCountRepository(private val jdbcTemplate: JdbcTemplate) : RaiderCrestCountRepository {

    override fun findById(id: Long): RaiderCrestCountEntity? =
        jdbcTemplate.query("SELECT * FROM raider_crest_counts WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_crest_counts WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<RaiderCrestCountEntity> =
        jdbcTemplate.query("SELECT * FROM raider_crest_counts ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_crest_counts", Long::class.java) ?: 0L

    override fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderCrestCountEntity> =
        jdbcTemplate.query("SELECT * FROM raider_crest_counts WHERE raider_id = ? ORDER BY crest_type LIMIT ? OFFSET ?", rowMapper, raiderId, limit, offset)

    override fun countByRaiderId(raiderId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_crest_counts WHERE raider_id = ?", Long::class.java, raiderId) ?: 0L

    override fun save(entity: RaiderCrestCountEntity): RaiderCrestCountEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM raider_crest_counts WHERE id = ?", id) }

    private fun insert(entity: RaiderCrestCountEntity): RaiderCrestCountEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                "INSERT INTO raider_crest_counts (raider_id, crest_type, crest_count) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.raiderId)
            ps.setString(2, entity.crestType)
            entity.crestCount?.let { ps.setInt(3, it) } ?: ps.setNull(3, java.sql.Types.INTEGER)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: RaiderCrestCountEntity) {
        jdbcTemplate.update(
            "UPDATE raider_crest_counts SET raider_id=?, crest_type=?, crest_count=? WHERE id=?",
            entity.raiderId, entity.crestType, entity.crestCount, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }
        RaiderCrestCountEntity(rs.getLong("id"), rs.getLong("raider_id"), rs.getString("crest_type"), getIntOrNull("crest_count"))
    }
}
