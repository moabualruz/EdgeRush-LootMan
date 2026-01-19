package com.edgerush.lootman.infrastructure.attendance

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate

/**
 * JDBC implementation of AttendanceRepository.
 *
 * Persists AttendanceRecord aggregates to the attendance_stats table.
 *
 * Actual database column mappings (V0045 snake_case):
 * - id -> AttendanceRecordId (int)
 * - character_id -> raiderId (bigint)
 * - team_id -> guildId (bigint) - note: stored as number, not string
 * - instance -> instance (text)
 * - encounter -> encounter (text)
 * - start_date -> startDate (date)
 * - end_date -> endDate (date)
 * - attended_amount_of_raids -> attendedRaids (int)
 * - total_amount_of_raids -> totalRaids (int)
 * - synced_at -> recordedAt (timestamptz)
 */
@Repository
@Primary
class JdbcAttendanceRepository(
    private val jdbcTemplate: JdbcTemplate,
) : AttendanceRepository {
    override fun findById(id: AttendanceRecordId): AttendanceRecord? {
        val sql =
            """
            SELECT id, character_id, team_id, instance, encounter,
                   start_date, end_date, attended_amount_of_raids, total_amount_of_raids, synced_at
            FROM attendance_stats
            WHERE id = ?
            """.trimIndent()

        val results = jdbcTemplate.query(sql, attendanceRecordRowMapper, id.value.toIntOrNull() ?: 0)
        return results.firstOrNull()
    }

    override fun findByRaiderIdAndGuildIdAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        // The attendance_stats.character_id contains wowaudit_id, not local raider id
        // Join with raiders table to find attendance by raider's wowaudit_id
        // Handle NULL dates by including records where dates are NULL (considered always valid)
        val sql =
            """
            SELECT a.id, a.character_id, a.team_id, a.instance, a.encounter,
                   a.start_date, a.end_date, a.attended_amount_of_raids, a.total_amount_of_raids, a.synced_at
            FROM attendance_stats a
            INNER JOIN raiders r ON a.character_id = r.wowaudit_id
            WHERE r.id = ?
            AND (a.start_date IS NULL OR a.end_date IS NULL
                 OR NOT (a.end_date < ? OR a.start_date > ?))
            ORDER BY a.start_date DESC NULLS LAST
            """.trimIndent()

        return jdbcTemplate.query(
            sql,
            attendanceRecordRowMapper,
            raiderId.value,
            Date.valueOf(startDate),
            Date.valueOf(endDate),
        )
    }

    override fun findByRaiderIdAndGuildIdAndInstanceAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        val sql =
            """
            SELECT id, character_id, team_id, instance, encounter,
                   start_date, end_date, attended_amount_of_raids, total_amount_of_raids, synced_at
            FROM attendance_stats
            WHERE character_id = ? AND team_id = ? AND instance = ?
            AND NOT (end_date < ? OR start_date > ?)
            ORDER BY start_date DESC
            """.trimIndent()

        return jdbcTemplate.query(
            sql,
            attendanceRecordRowMapper,
            raiderId.value,
            guildId.value.toLongOrNull() ?: 0L,
            instance,
            Date.valueOf(startDate),
            Date.valueOf(endDate),
        )
    }

    override fun findByRaiderIdAndGuildIdAndEncounterAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        val sql =
            """
            SELECT id, character_id, team_id, instance, encounter,
                   start_date, end_date, attended_amount_of_raids, total_amount_of_raids, synced_at
            FROM attendance_stats
            WHERE character_id = ? AND team_id = ? AND instance = ? AND encounter = ?
            AND NOT (end_date < ? OR start_date > ?)
            ORDER BY start_date DESC
            """.trimIndent()

        return jdbcTemplate.query(
            sql,
            attendanceRecordRowMapper,
            raiderId.value,
            guildId.value.toLongOrNull() ?: 0L,
            instance,
            encounter,
            Date.valueOf(startDate),
            Date.valueOf(endDate),
        )
    }

    override fun findByGuildIdAndDateRange(
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<AttendanceRecord> {
        val sql =
            """
            SELECT id, character_id, team_id, instance, encounter,
                   start_date, end_date, attended_amount_of_raids, total_amount_of_raids, synced_at
            FROM attendance_stats
            WHERE team_id = ?
            AND NOT (end_date < ? OR start_date > ?)
            ORDER BY start_date DESC
            """.trimIndent()

        return jdbcTemplate.query(
            sql,
            attendanceRecordRowMapper,
            guildId.value.toLongOrNull() ?: 0L,
            Date.valueOf(startDate),
            Date.valueOf(endDate),
        )
    }

    override fun save(record: AttendanceRecord): AttendanceRecord {
        val exists = existsById(record.id)

        if (exists) {
            updateAttendanceRecord(record)
        } else {
            insertAttendanceRecord(record)
        }

        return record
    }

    override fun delete(id: AttendanceRecordId) {
        val sql = "DELETE FROM attendance_stats WHERE id = ?"
        jdbcTemplate.update(sql, id.value.toIntOrNull() ?: 0)
    }

    private fun existsById(id: AttendanceRecordId): Boolean {
        val sql = "SELECT COUNT(*) FROM attendance_stats WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id.value.toIntOrNull() ?: 0) ?: 0
        return count > 0
    }

    private fun insertAttendanceRecord(record: AttendanceRecord) {
        // Note: attendance_stats requires character_name which we don't have
        // This is a workaround - should be resolved via character lookup
        val sql =
            """
            INSERT INTO attendance_stats (
                character_id, team_id, instance, encounter,
                start_date, end_date, attended_amount_of_raids, total_amount_of_raids, synced_at,
                character_name
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            record.raiderId.value,
            record.guildId.value.toLongOrNull() ?: 0L,
            record.instance,
            record.encounter,
            Date.valueOf(record.startDate),
            Date.valueOf(record.endDate),
            record.attendedRaids,
            record.totalRaids,
            Timestamp.from(record.recordedAt),
            "Unknown", // character_name is NOT NULL in schema
        )
    }

    private fun updateAttendanceRecord(record: AttendanceRecord) {
        val sql =
            """
            UPDATE attendance_stats SET
                character_id = ?,
                team_id = ?,
                instance = ?,
                encounter = ?,
                start_date = ?,
                end_date = ?,
                attended_amount_of_raids = ?,
                total_amount_of_raids = ?,
                synced_at = ?
            WHERE id = ?
            """.trimIndent()

        jdbcTemplate.update(
            sql,
            record.raiderId.value,
            record.guildId.value.toLongOrNull() ?: 0L,
            record.instance,
            record.encounter,
            Date.valueOf(record.startDate),
            Date.valueOf(record.endDate),
            record.attendedRaids,
            record.totalRaids,
            Timestamp.from(record.recordedAt),
            record.id.value.toIntOrNull() ?: 0,
        )
    }

    private val attendanceRecordRowMapper =
        RowMapper { rs, _ ->
            val encounter = rs.getString("encounter")
            val teamIdLong = rs.getLong("team_id")
            val teamId = if (rs.wasNull()) "0" else teamIdLong.toString()

            // Use reflection to create AttendanceRecord with specific values
            // since the domain model uses a private constructor
            createAttendanceRecordFromDb(
                id = AttendanceRecordId(rs.getInt("id").toString()),
                raiderId = RaiderId(rs.getLong("character_id")),
                guildId = GuildId(teamId),
                instance = rs.getString("instance") ?: "",
                encounter = encounter,
                startDate = rs.getDate("start_date")?.toLocalDate() ?: LocalDate.now(),
                endDate = rs.getDate("end_date")?.toLocalDate() ?: LocalDate.now(),
                attendedRaids = rs.getInt("attended_amount_of_raids"),
                totalRaids = rs.getInt("total_amount_of_raids").let { if (it == 0) 1 else it }, // Avoid division by zero
                recordedAt = rs.getTimestamp("synced_at")?.toInstant() ?: Instant.now(),
            )
        }

    /**
     * Creates an AttendanceRecord from database values.
     *
     * Uses reflection to access the private constructor since the domain model
     * is designed to be created through the companion object's create() method.
     */
    private fun createAttendanceRecordFromDb(
        id: AttendanceRecordId,
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String?,
        startDate: LocalDate,
        endDate: LocalDate,
        attendedRaids: Int,
        totalRaids: Int,
        recordedAt: Instant,
    ): AttendanceRecord {
        val constructor =
            AttendanceRecord::class.java.getDeclaredConstructor(
                AttendanceRecordId::class.java,
                RaiderId::class.java,
                GuildId::class.java,
                String::class.java,
                String::class.java,
                LocalDate::class.java,
                LocalDate::class.java,
                Int::class.java,
                Int::class.java,
                Instant::class.java,
            )
        constructor.isAccessible = true
        return constructor.newInstance(
            id,
            raiderId,
            guildId,
            instance,
            encounter,
            startDate,
            endDate,
            attendedRaids,
            totalRaids,
            recordedAt,
        )
    }
}
