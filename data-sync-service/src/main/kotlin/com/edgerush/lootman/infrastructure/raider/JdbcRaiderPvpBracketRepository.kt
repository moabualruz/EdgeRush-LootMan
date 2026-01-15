package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import com.edgerush.lootman.domain.raider.repository.RaiderPvpBracketRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcRaiderPvpBracketRepository(private val jdbcTemplate: JdbcTemplate) : RaiderPvpBracketRepository {
    override fun findById(id: Long): RaiderPvpBracketEntity? =
        jdbcTemplate.query("SELECT * FROM raider_pvp_bracket_stats WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_pvp_bracket_stats WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaiderPvpBracketEntity> =
        jdbcTemplate.query("SELECT * FROM raider_pvp_bracket_stats ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_pvp_bracket_stats", Long::class.java) ?: 0L

    override fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<RaiderPvpBracketEntity> =
        jdbcTemplate.query(
            "SELECT * FROM raider_pvp_bracket_stats WHERE raider_id = ? ORDER BY bracket LIMIT ? OFFSET ?",
            rowMapper,
            raiderId,
            limit,
            offset,
        )

    override fun countByRaiderId(raiderId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_pvp_bracket_stats WHERE raider_id = ?", Long::class.java, raiderId) ?: 0L

    override fun save(entity: RaiderPvpBracketEntity): RaiderPvpBracketEntity =
        if (entity.id == null) {
            insert(entity)
        } else {
            update(entity)
            entity
        }

    override fun delete(id: Long) {
        jdbcTemplate.update("DELETE FROM raider_pvp_bracket_stats WHERE id = ?", id)
    }

    private fun insert(entity: RaiderPvpBracketEntity): RaiderPvpBracketEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps =
                conn.prepareStatement(
                    "INSERT INTO raider_pvp_bracket_stats (raider_id, bracket, rating, season_played, week_played, max_rating) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS,
                )
            ps.setLong(1, entity.raiderId)
            ps.setString(2, entity.bracket)
            entity.rating?.let { ps.setInt(3, it) } ?: ps.setNull(3, java.sql.Types.INTEGER)
            entity.seasonPlayed?.let { ps.setInt(4, it) } ?: ps.setNull(4, java.sql.Types.INTEGER)
            entity.weekPlayed?.let { ps.setInt(5, it) } ?: ps.setNull(5, java.sql.Types.INTEGER)
            entity.maxRating?.let { ps.setInt(6, it) } ?: ps.setNull(6, java.sql.Types.INTEGER)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: RaiderPvpBracketEntity) {
        jdbcTemplate.update(
            "UPDATE raider_pvp_bracket_stats SET raider_id=?, bracket=?, rating=?, season_played=?, week_played=?, max_rating=? WHERE id=?",
            entity.raiderId,
            entity.bracket,
            entity.rating,
            entity.seasonPlayed,
            entity.weekPlayed,
            entity.maxRating,
            entity.id,
        )
    }

    private val rowMapper =
        RowMapper { rs, _ ->
            fun getIntOrNull(col: String): Int? {
                val v = rs.getInt(col)
                return if (rs.wasNull()) null else v
            }
            RaiderPvpBracketEntity(
                rs.getLong("id"),
                rs.getLong("raider_id"),
                rs.getString("bracket"),
                getIntOrNull("rating"),
                getIntOrNull("season_played"),
                getIntOrNull("week_played"),
                getIntOrNull("max_rating"),
            )
        }
}
