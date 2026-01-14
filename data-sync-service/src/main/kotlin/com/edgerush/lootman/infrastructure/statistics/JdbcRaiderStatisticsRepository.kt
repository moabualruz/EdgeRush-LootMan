package com.edgerush.lootman.infrastructure.statistics

import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.lootman.domain.statistics.repository.RaiderStatisticsRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement

@Repository
class JdbcRaiderStatisticsRepository(private val jdbcTemplate: JdbcTemplate) : RaiderStatisticsRepository {

    override fun findById(id: Long): RaiderStatisticsEntity? =
        jdbcTemplate.query("SELECT * FROM raider_statistics WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_statistics WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<RaiderStatisticsEntity> =
        jdbcTemplate.query("SELECT * FROM raider_statistics ORDER BY id LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_statistics", Long::class.java) ?: 0L

    override fun findByRaiderId(raiderId: Long): RaiderStatisticsEntity? =
        jdbcTemplate.query("SELECT * FROM raider_statistics WHERE raider_id = ?", rowMapper, raiderId).firstOrNull()

    override fun existsByRaiderId(raiderId: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM raider_statistics WHERE raider_id = ?", Int::class.java, raiderId) ?: 0) > 0

    override fun save(entity: RaiderStatisticsEntity): RaiderStatisticsEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM raider_statistics WHERE id = ?", id) }

    private fun insert(entity: RaiderStatisticsEntity): RaiderStatisticsEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement(
                """INSERT INTO raider_statistics (raider_id, mythic_plus_score, weekly_highest_mplus, season_highest_mplus,
                   world_quests_total, world_quests_this_week, collectibles_mounts, collectibles_toys,
                   collectibles_unique_pets, collectibles_level_25_pets, honor_level) VALUES (?,?,?,?,?,?,?,?,?,?,?)""",
                Statement.RETURN_GENERATED_KEYS
            )
            ps.setLong(1, entity.raiderId)
            entity.mythicPlusScore?.let { ps.setDouble(2, it) } ?: ps.setNull(2, java.sql.Types.DOUBLE)
            entity.weeklyHighestMplus?.let { ps.setInt(3, it) } ?: ps.setNull(3, java.sql.Types.INTEGER)
            entity.seasonHighestMplus?.let { ps.setInt(4, it) } ?: ps.setNull(4, java.sql.Types.INTEGER)
            entity.worldQuestsTotal?.let { ps.setInt(5, it) } ?: ps.setNull(5, java.sql.Types.INTEGER)
            entity.worldQuestsThisWeek?.let { ps.setInt(6, it) } ?: ps.setNull(6, java.sql.Types.INTEGER)
            entity.collectiblesMounts?.let { ps.setInt(7, it) } ?: ps.setNull(7, java.sql.Types.INTEGER)
            entity.collectiblesToys?.let { ps.setInt(8, it) } ?: ps.setNull(8, java.sql.Types.INTEGER)
            entity.collectiblesUniquePets?.let { ps.setInt(9, it) } ?: ps.setNull(9, java.sql.Types.INTEGER)
            entity.collectiblesLevel25Pets?.let { ps.setInt(10, it) } ?: ps.setNull(10, java.sql.Types.INTEGER)
            entity.honorLevel?.let { ps.setInt(11, it) } ?: ps.setNull(11, java.sql.Types.INTEGER)
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: RaiderStatisticsEntity) {
        jdbcTemplate.update(
            """UPDATE raider_statistics SET raider_id=?, mythic_plus_score=?, weekly_highest_mplus=?, season_highest_mplus=?,
               world_quests_total=?, world_quests_this_week=?, collectibles_mounts=?, collectibles_toys=?,
               collectibles_unique_pets=?, collectibles_level_25_pets=?, honor_level=? WHERE id=?""",
            entity.raiderId, entity.mythicPlusScore, entity.weeklyHighestMplus, entity.seasonHighestMplus,
            entity.worldQuestsTotal, entity.worldQuestsThisWeek, entity.collectiblesMounts, entity.collectiblesToys,
            entity.collectiblesUniquePets, entity.collectiblesLevel25Pets, entity.honorLevel, entity.id
        )
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getIntOrNull(col: String): Int? { val v = rs.getInt(col); return if (rs.wasNull()) null else v }
        fun getDoubleOrNull(col: String): Double? { val v = rs.getDouble(col); return if (rs.wasNull()) null else v }
        RaiderStatisticsEntity(
            rs.getLong("id"), rs.getLong("raider_id"), getDoubleOrNull("mythic_plus_score"),
            getIntOrNull("weekly_highest_mplus"), getIntOrNull("season_highest_mplus"),
            getIntOrNull("world_quests_total"), getIntOrNull("world_quests_this_week"),
            getIntOrNull("collectibles_mounts"), getIntOrNull("collectibles_toys"),
            getIntOrNull("collectibles_unique_pets"), getIntOrNull("collectibles_level_25_pets"),
            getIntOrNull("honor_level")
        )
    }
}
