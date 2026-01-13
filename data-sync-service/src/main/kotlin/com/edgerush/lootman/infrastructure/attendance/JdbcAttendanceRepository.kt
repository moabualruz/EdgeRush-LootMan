package com.edgerush.lootman.infrastructure.attendance

import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate

/**
 * JDBC implementation of AttendanceRepository.
 *
 * Persists AttendanceRecord aggregates to the attendance_stats table.
 * Column names follow the JPA naming conventions from V0019 migration.
 *
 * Database column mappings:
 * - id -> AttendanceRecordId (String)
 * - character_id -> raiderId (Long)
 * - team_id -> guildId (String)
 * - instance -> instance (String)
 * - encounter -> encounter (String?)
 * - startDate -> startDate (LocalDate)
 * - endDate -> endDate (LocalDate)
 * - attendedAmount -> attendedRaids (Int)
 * - totalAmount -> totalRaids (Int)
 * - syncedAt -> recordedAt (Instant)
 */
@Repository
class JdbcAttendanceRepository(
    private val jdbcTemplate: JdbcTemplate
) : AttendanceRepository {

    override fun findById(id: AttendanceRecordId): AttendanceRecord? {
        val sql = """
            SELECT id, character_id, team_id, instance, encounter,
                   startDate, endDate, attendedAmount, totalAmount, syncedAt
            FROM attendance_stats
            WHERE id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, attendanceRecordRowMapper, id.value)
        return results.firstOrNull()
    }

    override fun findByRaiderIdAndGuildIdAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> {
        val sql = """
            SELECT id, character_id, team_id, instance, encounter,
                   startDate, endDate, attendedAmount, totalAmount, syncedAt
            FROM attendance_stats
            WHERE character_id = ? AND team_id = ?
            AND NOT (endDate < ? OR startDate > ?)
            ORDER BY startDate DESC
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            attendanceRecordRowMapper,
            raiderId.value,
            guildId.value,
            Date.valueOf(startDate),
            Date.valueOf(endDate)
        )
    }

    override fun findByRaiderIdAndGuildIdAndInstanceAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> {
        val sql = """
            SELECT id, character_id, team_id, instance, encounter,
                   startDate, endDate, attendedAmount, totalAmount, syncedAt
            FROM attendance_stats
            WHERE character_id = ? AND team_id = ? AND instance = ?
            AND NOT (endDate < ? OR startDate > ?)
            ORDER BY startDate DESC
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            attendanceRecordRowMapper,
            raiderId.value,
            guildId.value,
            instance,
            Date.valueOf(startDate),
            Date.valueOf(endDate)
        )
    }

    override fun findByRaiderIdAndGuildIdAndEncounterAndDateRange(
        raiderId: RaiderId,
        guildId: GuildId,
        instance: String,
        encounter: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> {
        val sql = """
            SELECT id, character_id, team_id, instance, encounter,
                   startDate, endDate, attendedAmount, totalAmount, syncedAt
            FROM attendance_stats
            WHERE character_id = ? AND team_id = ? AND instance = ? AND encounter = ?
            AND NOT (endDate < ? OR startDate > ?)
            ORDER BY startDate DESC
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            attendanceRecordRowMapper,
            raiderId.value,
            guildId.value,
            instance,
            encounter,
            Date.valueOf(startDate),
            Date.valueOf(endDate)
        )
    }

    override fun findByGuildIdAndDateRange(
        guildId: GuildId,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AttendanceRecord> {
        val sql = """
            SELECT id, character_id, team_id, instance, encounter,
                   startDate, endDate, attendedAmount, totalAmount, syncedAt
            FROM attendance_stats
            WHERE team_id = ?
            AND NOT (endDate < ? OR startDate > ?)
            ORDER BY startDate DESC
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            attendanceRecordRowMapper,
            guildId.value,
            Date.valueOf(startDate),
            Date.valueOf(endDate)
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
        jdbcTemplate.update(sql, id.value)
    }

    private fun existsById(id: AttendanceRecordId): Boolean {
        val sql = "SELECT COUNT(*) FROM attendance_stats WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id.value) ?: 0
        return count > 0
    }

    private fun insertAttendanceRecord(record: AttendanceRecord) {
        val sql = """
            INSERT INTO attendance_stats (
                id, character_id, team_id, instance, encounter,
                startDate, endDate, attendedAmount, totalAmount, syncedAt
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            record.id.value,
            record.raiderId.value,
            record.guildId.value,
            record.instance,
            record.encounter,
            Date.valueOf(record.startDate),
            Date.valueOf(record.endDate),
            record.attendedRaids,
            record.totalRaids,
            Timestamp.from(record.recordedAt)
        )
    }

    private fun updateAttendanceRecord(record: AttendanceRecord) {
        val sql = """
            UPDATE attendance_stats SET
                character_id = ?,
                team_id = ?,
                instance = ?,
                encounter = ?,
                startDate = ?,
                endDate = ?,
                attendedAmount = ?,
                totalAmount = ?,
                syncedAt = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            record.raiderId.value,
            record.guildId.value,
            record.instance,
            record.encounter,
            Date.valueOf(record.startDate),
            Date.valueOf(record.endDate),
            record.attendedRaids,
            record.totalRaids,
            Timestamp.from(record.recordedAt),
            record.id.value
        )
    }

    private val attendanceRecordRowMapper = RowMapper { rs, _ ->
        val encounter = rs.getString("encounter")

        // Use reflection to create AttendanceRecord with specific values
        // since the domain model uses a private constructor
        createAttendanceRecordFromDb(
            id = AttendanceRecordId(rs.getString("id")),
            raiderId = RaiderId(rs.getLong("character_id")),
            guildId = GuildId(rs.getString("team_id") ?: "default"),
            instance = rs.getString("instance") ?: "",
            encounter = encounter,
            startDate = rs.getDate("startDate").toLocalDate(),
            endDate = rs.getDate("endDate").toLocalDate(),
            attendedRaids = rs.getInt("attendedAmount"),
            totalRaids = rs.getInt("totalAmount"),
            recordedAt = rs.getTimestamp("syncedAt")?.toInstant() ?: Instant.now()
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
        recordedAt: Instant
    ): AttendanceRecord {
        val constructor = AttendanceRecord::class.java.getDeclaredConstructor(
            AttendanceRecordId::class.java,
            RaiderId::class.java,
            GuildId::class.java,
            String::class.java,
            String::class.java,
            LocalDate::class.java,
            LocalDate::class.java,
            Int::class.java,
            Int::class.java,
            Instant::class.java
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
            recordedAt
        )
    }
}
