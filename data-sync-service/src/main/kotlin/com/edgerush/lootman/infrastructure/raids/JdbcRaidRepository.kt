package com.edgerush.lootman.infrastructure.raids

import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.lootman.domain.raids.repository.RaidRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * JDBC implementation of RaidRepository.
 *
 * Persists Raid entities to the raids table.
 */
@Repository
class JdbcRaidRepository(
    private val jdbcTemplate: JdbcTemplate,
) : RaidRepository {
    override fun findById(raidId: Long): RaidEntity? {
        val sql =
            """
            SELECT raid_id, date, start_time, end_time, instance, difficulty,
                   optional, status, present_size, total_size, notes,
                   selections_image, team_id, season_id, period_id,
                   created_at, updated_at, synced_at
            FROM raids
            WHERE raid_id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, raidRowMapper, raidId)
        return results.firstOrNull()
    }

    override fun existsById(raidId: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM raids WHERE raid_id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, raidId) ?: 0
        return count > 0
    }

    override fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaidEntity> {
        val sql =
            """
            SELECT raid_id, date, start_time, end_time, instance, difficulty,
                   optional, status, present_size, total_size, notes,
                   selections_image, team_id, season_id, period_id,
                   created_at, updated_at, synced_at
            FROM raids
            ORDER BY date DESC, raid_id DESC
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, raidRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM raids"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByTeamId(
        teamId: Long,
        offset: Long,
        limit: Int,
    ): List<RaidEntity> {
        val sql =
            """
            SELECT raid_id, date, start_time, end_time, instance, difficulty,
                   optional, status, present_size, total_size, notes,
                   selections_image, team_id, season_id, period_id,
                   created_at, updated_at, synced_at
            FROM raids
            WHERE team_id = ?
            ORDER BY date DESC, raid_id DESC
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, raidRowMapper, teamId, limit, offset)
    }

    override fun countByTeamId(teamId: Long): Long {
        val sql = "SELECT COUNT(*) FROM raids WHERE team_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, teamId) ?: 0L
    }

    override fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
        offset: Long,
        limit: Int,
    ): List<RaidEntity> {
        val sql =
            """
            SELECT raid_id, date, start_time, end_time, instance, difficulty,
                   optional, status, present_size, total_size, notes,
                   selections_image, team_id, season_id, period_id,
                   created_at, updated_at, synced_at
            FROM raids
            WHERE date >= ? AND date <= ?
            ORDER BY date DESC, raid_id DESC
            LIMIT ? OFFSET ?
            """.trimIndent()

        return jdbcTemplate.query(sql, raidRowMapper, startDate, endDate, limit, offset)
    }

    override fun countByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Long {
        val sql = "SELECT COUNT(*) FROM raids WHERE date >= ? AND date <= ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, startDate, endDate) ?: 0L
    }

    override fun save(raid: RaidEntity): RaidEntity {
        val exists = existsById(raid.raidId)

        if (exists) {
            updateRaid(raid)
        } else {
            insertRaid(raid)
        }

        return raid
    }

    override fun delete(raidId: Long) {
        val sql = "DELETE FROM raids WHERE raid_id = ?"
        jdbcTemplate.update(sql, raidId)
    }

    override fun findUpcomingByGuildId(
        guildId: Long,
        limit: Int,
    ): List<RaidEntity> {
        val sql =
            """
            SELECT r.raid_id, r.date, r.start_time, r.end_time, r.instance, r.difficulty,
                   r.optional, r.status, r.present_size, r.total_size, r.notes,
                   r.selections_image, r.team_id, r.season_id, r.period_id,
                   r.created_at, r.updated_at, r.synced_at
            FROM raids r
            INNER JOIN team_metadata tm ON r.team_id = tm.team_id
            WHERE tm.guild_id = ? AND r.date >= CURRENT_DATE
            ORDER BY r.date ASC, r.start_time ASC
            LIMIT ?
            """.trimIndent()

        return jdbcTemplate.query(sql, raidRowMapper, guildId, limit)
    }

    override fun findPastByGuildId(
        guildId: Long,
        limit: Int,
    ): List<RaidEntity> {
        val sql =
            """
            SELECT r.raid_id, r.date, r.start_time, r.end_time, r.instance, r.difficulty,
                   r.optional, r.status, r.present_size, r.total_size, r.notes,
                   r.selections_image, r.team_id, r.season_id, r.period_id,
                   r.created_at, r.updated_at, r.synced_at
            FROM raids r
            INNER JOIN team_metadata tm ON r.team_id = tm.team_id
            WHERE tm.guild_id = ? AND r.date < CURRENT_DATE
            ORDER BY r.date DESC, r.start_time DESC
            LIMIT ?
            """.trimIndent()

        return jdbcTemplate.query(sql, raidRowMapper, guildId, limit)
    }

    private fun insertRaid(raid: RaidEntity) {
        val sql =
            """
            INSERT INTO raids (
                raid_id, date, start_time, end_time, instance, difficulty,
                optional, status, present_size, total_size, notes,
                selections_image, team_id, season_id, period_id,
                created_at, updated_at, synced_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            raid.raidId,
            raid.date,
            raid.startTime,
            raid.endTime,
            raid.instance,
            raid.difficulty,
            raid.optional,
            raid.status,
            raid.presentSize,
            raid.totalSize,
            raid.notes,
            raid.selectionsImage,
            raid.teamId,
            raid.seasonId,
            raid.periodId,
            raid.createdAt,
            raid.updatedAt,
            raid.syncedAt,
        )
    }

    private fun updateRaid(raid: RaidEntity) {
        val sql =
            """
            UPDATE raids SET
                date = ?,
                start_time = ?,
                end_time = ?,
                instance = ?,
                difficulty = ?,
                optional = ?,
                status = ?,
                present_size = ?,
                total_size = ?,
                notes = ?,
                selections_image = ?,
                team_id = ?,
                season_id = ?,
                period_id = ?,
                updated_at = ?,
                synced_at = ?
            WHERE raid_id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            raid.date,
            raid.startTime,
            raid.endTime,
            raid.instance,
            raid.difficulty,
            raid.optional,
            raid.status,
            raid.presentSize,
            raid.totalSize,
            raid.notes,
            raid.selectionsImage,
            raid.teamId,
            raid.seasonId,
            raid.periodId,
            raid.updatedAt,
            raid.syncedAt,
            raid.raidId,
        )
    }

    private val raidRowMapper =
        RowMapper { rs, _ ->
            val teamIdValue = rs.getLong("team_id")
            val teamId = if (rs.wasNull()) null else teamIdValue

            val seasonIdValue = rs.getLong("season_id")
            val seasonId = if (rs.wasNull()) null else seasonIdValue

            val periodIdValue = rs.getLong("period_id")
            val periodId = if (rs.wasNull()) null else periodIdValue

            val presentSizeValue = rs.getInt("present_size")
            val presentSize = if (rs.wasNull()) null else presentSizeValue

            val totalSizeValue = rs.getInt("total_size")
            val totalSize = if (rs.wasNull()) null else totalSizeValue

            val optionalValue = rs.getBoolean("optional")
            val optional = if (rs.wasNull()) null else optionalValue

            val createdAtObj = rs.getObject("created_at", OffsetDateTime::class.java)
            val updatedAtObj = rs.getObject("updated_at", OffsetDateTime::class.java)
            val syncedAtObj = rs.getObject("synced_at", OffsetDateTime::class.java)

            RaidEntity(
                raidId = rs.getLong("raid_id"),
                date = rs.getObject("date", LocalDate::class.java),
                startTime = rs.getTime("start_time")?.toLocalTime(),
                endTime = rs.getTime("end_time")?.toLocalTime(),
                instance = rs.getString("instance"),
                difficulty = rs.getString("difficulty"),
                optional = optional,
                status = rs.getString("status"),
                presentSize = presentSize,
                totalSize = totalSize,
                notes = rs.getString("notes"),
                selectionsImage = rs.getString("selections_image"),
                teamId = teamId,
                seasonId = seasonId,
                periodId = periodId,
                createdAt = createdAtObj,
                updatedAt = updatedAtObj,
                syncedAt = syncedAtObj ?: OffsetDateTime.now(),
            )
        }
}
