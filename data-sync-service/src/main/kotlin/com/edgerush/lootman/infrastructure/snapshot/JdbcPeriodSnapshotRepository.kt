package com.edgerush.lootman.infrastructure.snapshot

import com.edgerush.datasync.entity.PeriodSnapshotEntity
import com.edgerush.lootman.domain.snapshot.repository.PeriodSnapshotRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class JdbcPeriodSnapshotRepository(private val jdbcTemplate: JdbcTemplate) : PeriodSnapshotRepository {

    override fun findById(id: Long): PeriodSnapshotEntity? =
        jdbcTemplate.query("SELECT * FROM period_snapshots WHERE id = ?", rowMapper, id).firstOrNull()

    override fun existsById(id: Long): Boolean =
        (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM period_snapshots WHERE id = ?", Int::class.java, id) ?: 0) > 0

    override fun findAll(offset: Long, limit: Int): List<PeriodSnapshotEntity> =
        jdbcTemplate.query("SELECT * FROM period_snapshots ORDER BY fetched_at DESC LIMIT ? OFFSET ?", rowMapper, limit, offset)

    override fun count(): Long = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM period_snapshots", Long::class.java) ?: 0L

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<PeriodSnapshotEntity> =
        jdbcTemplate.query("SELECT * FROM period_snapshots WHERE team_id = ? ORDER BY fetched_at DESC LIMIT ? OFFSET ?", rowMapper, teamId, limit, offset)

    override fun countByTeamId(teamId: Long): Long =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM period_snapshots WHERE team_id = ?", Long::class.java, teamId) ?: 0L

    override fun save(entity: PeriodSnapshotEntity): PeriodSnapshotEntity = if (entity.id == null) insert(entity) else { update(entity); entity }

    override fun delete(id: Long) { jdbcTemplate.update("DELETE FROM period_snapshots WHERE id = ?", id) }

    private fun insert(entity: PeriodSnapshotEntity): PeriodSnapshotEntity {
        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ conn ->
            val ps = conn.prepareStatement("INSERT INTO period_snapshots (team_id, season_id, period_id, current_period, fetched_at) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)
            entity.teamId?.let { ps.setLong(1, it) } ?: ps.setNull(1, java.sql.Types.BIGINT)
            entity.seasonId?.let { ps.setLong(2, it) } ?: ps.setNull(2, java.sql.Types.BIGINT)
            entity.periodId?.let { ps.setLong(3, it) } ?: ps.setNull(3, java.sql.Types.BIGINT)
            entity.currentPeriod?.let { ps.setLong(4, it) } ?: ps.setNull(4, java.sql.Types.BIGINT)
            ps.setTimestamp(5, Timestamp.from(entity.fetchedAt.toInstant()))
            ps
        }, keyHolder)
        return entity.copy(id = (keyHolder.keys?.get("id") as? Number)?.toLong())
    }

    private fun update(entity: PeriodSnapshotEntity) {
        jdbcTemplate.update("UPDATE period_snapshots SET team_id=?, season_id=?, period_id=?, current_period=?, fetched_at=? WHERE id=?",
            entity.teamId, entity.seasonId, entity.periodId, entity.currentPeriod, Timestamp.from(entity.fetchedAt.toInstant()), entity.id)
    }

    private val rowMapper = RowMapper { rs, _ ->
        fun getLongOrNull(col: String): Long? { val v = rs.getLong(col); return if (rs.wasNull()) null else v }
        PeriodSnapshotEntity(rs.getLong("id"), getLongOrNull("team_id"), getLongOrNull("season_id"),
            getLongOrNull("period_id"), getLongOrNull("current_period"),
            rs.getTimestamp("fetched_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now())
    }
}
