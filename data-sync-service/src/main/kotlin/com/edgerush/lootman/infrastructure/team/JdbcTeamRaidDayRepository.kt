package com.edgerush.lootman.infrastructure.team

import com.edgerush.datasync.entity.TeamRaidDayEntity
import com.edgerush.lootman.domain.team.repository.TeamRaidDayRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.Statement
import java.sql.Time
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * JDBC implementation of TeamRaidDayRepository.
 *
 * Persists TeamRaidDayEntity to the team_raid_days table.
 */
@Repository
class JdbcTeamRaidDayRepository(
    private val jdbcTemplate: JdbcTemplate,
) : TeamRaidDayRepository {

    override fun findById(id: Long): TeamRaidDayEntity? {
        val sql = """
            SELECT id, team_id, week_day, start_time, end_time, current_instance,
                   difficulty, active_from, synced_at
            FROM team_raid_days
            WHERE id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, teamRaidDayRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM team_raid_days WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(offset: Long, limit: Int): List<TeamRaidDayEntity> {
        val sql = """
            SELECT id, team_id, week_day, start_time, end_time, current_instance,
                   difficulty, active_from, synced_at
            FROM team_raid_days
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, teamRaidDayRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM team_raid_days"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<TeamRaidDayEntity> {
        val sql = """
            SELECT id, team_id, week_day, start_time, end_time, current_instance,
                   difficulty, active_from, synced_at
            FROM team_raid_days
            WHERE team_id = ?
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, teamRaidDayRowMapper, teamId, limit, offset)
    }

    override fun countByTeamId(teamId: Long): Long {
        val sql = "SELECT COUNT(*) FROM team_raid_days WHERE team_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, teamId) ?: 0L
    }

    override fun save(entity: TeamRaidDayEntity): TeamRaidDayEntity {
        return if (entity.id == null) {
            insertTeamRaidDay(entity)
        } else {
            updateTeamRaidDay(entity)
            entity
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM team_raid_days WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertTeamRaidDay(entity: TeamRaidDayEntity): TeamRaidDayEntity {
        val sql = """
            INSERT INTO team_raid_days (
                team_id, week_day, start_time, end_time, current_instance,
                difficulty, active_from, synced_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ps.setLong(1, entity.teamId)
            entity.weekDay?.let { ps.setString(2, it) } ?: ps.setNull(2, java.sql.Types.VARCHAR)
            entity.startTime?.let { ps.setTime(3, Time.valueOf(it)) } ?: ps.setNull(3, java.sql.Types.TIME)
            entity.endTime?.let { ps.setTime(4, Time.valueOf(it)) } ?: ps.setNull(4, java.sql.Types.TIME)
            entity.currentInstance?.let { ps.setString(5, it) } ?: ps.setNull(5, java.sql.Types.VARCHAR)
            entity.difficulty?.let { ps.setString(6, it) } ?: ps.setNull(6, java.sql.Types.VARCHAR)
            entity.activeFrom?.let { ps.setDate(7, Date.valueOf(it)) } ?: ps.setNull(7, java.sql.Types.DATE)
            ps.setTimestamp(8, Timestamp.from(entity.syncedAt.toInstant()))
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = generatedId?.toLong())
    }

    private fun updateTeamRaidDay(entity: TeamRaidDayEntity) {
        val sql = """
            UPDATE team_raid_days SET
                team_id = ?, week_day = ?, start_time = ?, end_time = ?, current_instance = ?,
                difficulty = ?, active_from = ?, synced_at = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.teamId,
            entity.weekDay,
            entity.startTime?.let { Time.valueOf(it) },
            entity.endTime?.let { Time.valueOf(it) },
            entity.currentInstance,
            entity.difficulty,
            entity.activeFrom?.let { Date.valueOf(it) },
            Timestamp.from(entity.syncedAt.toInstant()),
            entity.id,
        )
    }

    private val teamRaidDayRowMapper = RowMapper { rs, _ ->
        TeamRaidDayEntity(
            id = rs.getLong("id"),
            teamId = rs.getLong("team_id"),
            weekDay = rs.getString("week_day"),
            startTime = rs.getTime("start_time")?.toLocalTime(),
            endTime = rs.getTime("end_time")?.toLocalTime(),
            currentInstance = rs.getString("current_instance"),
            difficulty = rs.getString("difficulty"),
            activeFrom = rs.getDate("active_from")?.toLocalDate(),
            syncedAt = rs.getTimestamp("synced_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
        )
    }
}
