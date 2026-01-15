package com.edgerush.lootman.infrastructure.trial

import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.trial.model.Trial
import com.edgerush.lootman.domain.trial.model.TrialId
import com.edgerush.lootman.domain.trial.model.TrialOutcome
import com.edgerush.lootman.domain.trial.model.TrialStatus
import com.edgerush.lootman.domain.trial.repository.TrialRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp

/**
 * JDBC implementation of TrialRepository.
 *
 * Persists Trial entities to the trials table.
 */
@Repository
class JdbcTrialRepository(
    private val jdbcTemplate: JdbcTemplate,
) : TrialRepository {

    override fun findById(id: TrialId): Trial? {
        val sql = """
            SELECT id, application_id, raider_id, guild_id, status, start_date, end_date,
                   expected_end_date, raids_attended, raids_required, attendance_rate,
                   average_performance, deaths_per_raid, outcome, outcome_reason,
                   promoted_by, promoted_at, created_at, last_updated
            FROM trials
            WHERE id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, trialRowMapper, id.value).firstOrNull()
    }

    override fun findByApplicationId(applicationId: ApplicationId): Trial? {
        val sql = """
            SELECT id, application_id, raider_id, guild_id, status, start_date, end_date,
                   expected_end_date, raids_attended, raids_required, attendance_rate,
                   average_performance, deaths_per_raid, outcome, outcome_reason,
                   promoted_by, promoted_at, created_at, last_updated
            FROM trials
            WHERE application_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, trialRowMapper, applicationId.value).firstOrNull()
    }

    override fun findByGuildId(guildId: GuildId, offset: Long, limit: Int): List<Trial> {
        val sql = """
            SELECT id, application_id, raider_id, guild_id, status, start_date, end_date,
                   expected_end_date, raids_attended, raids_required, attendance_rate,
                   average_performance, deaths_per_raid, outcome, outcome_reason,
                   promoted_by, promoted_at, created_at, last_updated
            FROM trials
            WHERE guild_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, trialRowMapper, guildId.value, limit, offset)
    }

    override fun findByGuildIdAndStatus(
        guildId: GuildId,
        status: TrialStatus,
        offset: Long,
        limit: Int,
    ): List<Trial> {
        val sql = """
            SELECT id, application_id, raider_id, guild_id, status, start_date, end_date,
                   expected_end_date, raids_attended, raids_required, attendance_rate,
                   average_performance, deaths_per_raid, outcome, outcome_reason,
                   promoted_by, promoted_at, created_at, last_updated
            FROM trials
            WHERE guild_id = ? AND status = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, trialRowMapper, guildId.value, status.name, limit, offset)
    }

    override fun findActiveTrialsByGuildId(guildId: GuildId): List<Trial> {
        val sql = """
            SELECT id, application_id, raider_id, guild_id, status, start_date, end_date,
                   expected_end_date, raids_attended, raids_required, attendance_rate,
                   average_performance, deaths_per_raid, outcome, outcome_reason,
                   promoted_by, promoted_at, created_at, last_updated
            FROM trials
            WHERE guild_id = ? AND status IN ('ACTIVE', 'EXTENDED')
            ORDER BY created_at DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, trialRowMapper, guildId.value)
    }

    override fun findByRaiderId(raiderId: Long): Trial? {
        val sql = """
            SELECT id, application_id, raider_id, guild_id, status, start_date, end_date,
                   expected_end_date, raids_attended, raids_required, attendance_rate,
                   average_performance, deaths_per_raid, outcome, outcome_reason,
                   promoted_by, promoted_at, created_at, last_updated
            FROM trials
            WHERE raider_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, trialRowMapper, raiderId).firstOrNull()
    }

    override fun countByGuildId(guildId: GuildId): Long {
        val sql = "SELECT COUNT(*) FROM trials WHERE guild_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId.value) ?: 0L
    }

    override fun countByGuildIdAndStatus(guildId: GuildId, status: TrialStatus): Long {
        val sql = "SELECT COUNT(*) FROM trials WHERE guild_id = ? AND status = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, guildId.value, status.name) ?: 0L
    }

    override fun existsById(id: TrialId): Boolean {
        val sql = "SELECT COUNT(*) FROM trials WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id.value) ?: 0
        return count > 0
    }

    override fun save(trial: Trial): Trial {
        return if (existsById(trial.id)) {
            update(trial)
        } else {
            insert(trial)
        }
    }

    override fun deleteById(id: TrialId) {
        val sql = "DELETE FROM trials WHERE id = ?"
        jdbcTemplate.update(sql, id.value)
    }

    private fun insert(trial: Trial): Trial {
        val sql = """
            INSERT INTO trials (
                id, application_id, raider_id, guild_id, status, start_date, end_date,
                expected_end_date, raids_attended, raids_required, attendance_rate,
                average_performance, deaths_per_raid, outcome, outcome_reason,
                promoted_by, promoted_at, created_at, last_updated
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            trial.id.value,
            trial.applicationId.value,
            trial.raiderId,
            trial.guildId.value,
            trial.status.name,
            Timestamp.from(trial.startDate),
            trial.endDate?.let { Timestamp.from(it) },
            Timestamp.from(trial.expectedEndDate),
            trial.raidsAttended,
            trial.raidsRequired,
            trial.attendanceRate,
            trial.averagePerformance,
            trial.deathsPerRaid,
            trial.outcome?.name,
            trial.outcomeReason,
            trial.promotedBy,
            trial.promotedAt?.let { Timestamp.from(it) },
            Timestamp.from(trial.createdAt),
            Timestamp.from(trial.lastUpdated),
        )

        return trial
    }

    private fun update(trial: Trial): Trial {
        val sql = """
            UPDATE trials SET
                application_id = ?, raider_id = ?, guild_id = ?, status = ?,
                start_date = ?, end_date = ?, expected_end_date = ?,
                raids_attended = ?, raids_required = ?, attendance_rate = ?,
                average_performance = ?, deaths_per_raid = ?, outcome = ?,
                outcome_reason = ?, promoted_by = ?, promoted_at = ?, last_updated = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            trial.applicationId.value,
            trial.raiderId,
            trial.guildId.value,
            trial.status.name,
            Timestamp.from(trial.startDate),
            trial.endDate?.let { Timestamp.from(it) },
            Timestamp.from(trial.expectedEndDate),
            trial.raidsAttended,
            trial.raidsRequired,
            trial.attendanceRate,
            trial.averagePerformance,
            trial.deathsPerRaid,
            trial.outcome?.name,
            trial.outcomeReason,
            trial.promotedBy,
            trial.promotedAt?.let { Timestamp.from(it) },
            Timestamp.from(trial.lastUpdated),
            trial.id.value,
        )

        return trial
    }

    private val trialRowMapper = RowMapper { rs, _ ->
        fun getDoubleOrNull(col: String): Double? {
            val value = rs.getDouble(col)
            return if (rs.wasNull()) null else value
        }

        fun getLongOrNull(col: String): Long? {
            val value = rs.getLong(col)
            return if (rs.wasNull()) null else value
        }

        Trial.reconstruct(
            id = TrialId(rs.getString("id")),
            applicationId = ApplicationId(rs.getString("application_id")),
            raiderId = getLongOrNull("raider_id"),
            guildId = GuildId(rs.getString("guild_id")),
            status = TrialStatus.valueOf(rs.getString("status")),
            startDate = rs.getTimestamp("start_date").toInstant(),
            endDate = rs.getTimestamp("end_date")?.toInstant(),
            expectedEndDate = rs.getTimestamp("expected_end_date").toInstant(),
            raidsAttended = rs.getInt("raids_attended"),
            raidsRequired = rs.getInt("raids_required"),
            attendanceRate = getDoubleOrNull("attendance_rate"),
            averagePerformance = getDoubleOrNull("average_performance"),
            deathsPerRaid = getDoubleOrNull("deaths_per_raid"),
            outcome = rs.getString("outcome")?.let { TrialOutcome.valueOf(it) },
            outcomeReason = rs.getString("outcome_reason"),
            promotedBy = rs.getString("promoted_by"),
            promotedAt = rs.getTimestamp("promoted_at")?.toInstant(),
            createdAt = rs.getTimestamp("created_at").toInstant(),
            lastUpdated = rs.getTimestamp("last_updated").toInstant(),
        )
    }
}
