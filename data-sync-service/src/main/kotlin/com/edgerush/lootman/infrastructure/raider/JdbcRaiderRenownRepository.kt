package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderRenownEntity
import com.edgerush.lootman.domain.raider.repository.RaiderRenownRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcRaiderRenownRepository(private val jdbcTemplate: JdbcTemplate) : RaiderRenownRepository {
    override fun findById(id: Long): RaiderRenownEntity? =
        jdbcTemplate.query("SELECT * FROM raider_renown WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_renown WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderRenownEntity> = jdbcTemplate.query("SELECT * FROM raider_renown ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_renown", Long::class.java) ?: 0L

    override fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderRenownEntity> =
        jdbcTemplate.query(
            "SELECT * FROM raider_renown WHERE raider_id = ? ORDER BY faction LIMIT ? OFFSET ?",
            rowMapper,
            raiderId,
            limit,
            offset,
        )

    override fun countByRaiderId(raiderId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_renown WHERE raider_id = ?", Long::class.java, raiderId) ?: 0L

    override fun save(entity: RaiderRenownEntity): RaiderRenownEntity =
        if (entity.id == null) {
            insert(entity)
        } else {
            update(entity)
            entity
        }

    override fun delete(id: Long) {
        jdbcTemplate.update("DELETE FROM raider_renown WHERE id = ?", id)
    }

    private fun insert(entity: RaiderRenownEntity): RaiderRenownEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps =
                conn.prepareStatement(
                    "INSERT INTO raider_renown (raider_id, faction, level) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS,
                )
            ps.setLong(1, entity.raiderId)
            ps.setString(2, entity.faction)
            entity.level?.let { ps.setInt(3, it) } ?: ps.setNull(3, java.sql.Types.INTEGER)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: RaiderRenownEntity) {
        jdbcTemplate.update(
            "UPDATE raider_renown SET raider_id=?, faction=?, level=? WHERE id=?",
            entity.raiderId,
            entity.faction,
            entity.level,
            entity.id,
        )
    }

    private val rowMapper =
        RowMapper { rs, _ ->
            fun getIntOrNull(col: String): Int? {
                val v = rs.getInt(col)
                return if (rs.wasNull()) null else v
            }
            RaiderRenownEntity(rs.getLong("id"), rs.getLong("raider_id"), rs.getString("faction"), getIntOrNull("level"))
        }
}
