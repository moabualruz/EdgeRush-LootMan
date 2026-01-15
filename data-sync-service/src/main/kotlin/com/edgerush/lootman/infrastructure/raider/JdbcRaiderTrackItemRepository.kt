package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderTrackItemEntity
import com.edgerush.lootman.domain.raider.repository.RaiderTrackItemRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcRaiderTrackItemRepository(private val jdbcTemplate: JdbcTemplate) : RaiderTrackItemRepository {
    override fun findById(id: Long): RaiderTrackItemEntity? =
        jdbcTemplate.query("SELECT * FROM raider_track_items WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_track_items WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderTrackItemEntity> =
        jdbcTemplate.query("SELECT * FROM raider_track_items ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_track_items", Long::class.java) ?: 0L

    override fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderTrackItemEntity> =
        jdbcTemplate.query(
            "SELECT * FROM raider_track_items WHERE raider_id = ? ORDER BY tier LIMIT ? OFFSET ?",
            rowMapper,
            raiderId,
            limit,
            offset,
        )

    override fun countByRaiderId(raiderId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_track_items WHERE raider_id = ?", Long::class.java, raiderId) ?: 0L

    override fun save(entity: RaiderTrackItemEntity): RaiderTrackItemEntity =
        if (entity.id == null) {
            insert(entity)
        } else {
            update(entity)
            entity
        }

    override fun delete(id: Long) {
        jdbcTemplate.update("DELETE FROM raider_track_items WHERE id = ?", id)
    }

    private fun insert(entity: RaiderTrackItemEntity): RaiderTrackItemEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps =
                conn.prepareStatement(
                    "INSERT INTO raider_track_items (raider_id, tier, item_count) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS,
                )
            ps.setLong(1, entity.raiderId)
            ps.setString(2, entity.tier)
            entity.itemCount?.let { ps.setInt(3, it) } ?: ps.setNull(3, java.sql.Types.INTEGER)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: RaiderTrackItemEntity) {
        jdbcTemplate.update(
            "UPDATE raider_track_items SET raider_id=?, tier=?, item_count=? WHERE id=?",
            entity.raiderId,
            entity.tier,
            entity.itemCount,
            entity.id,
        )
    }

    private val rowMapper =
        RowMapper { rs, _ ->
            fun getIntOrNull(col: String): Int? {
                val v = rs.getInt(col)
                return if (rs.wasNull()) null else v
            }
            RaiderTrackItemEntity(rs.getLong("id"), rs.getLong("raider_id"), rs.getString("tier"), getIntOrNull("item_count"))
        }
}
