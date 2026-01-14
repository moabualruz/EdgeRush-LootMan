package com.edgerush.lootman.infrastructure.attendance

import com.edgerush.datasync.entity.AttendanceStatEntity
import com.edgerush.lootman.domain.attendance.repository.AttendanceStatRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.Date
import java.sql.Statement
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * JDBC implementation of AttendanceStatRepository.
 *
 * Persists AttendanceStatEntity to the attendance_stats table.
 */
@Repository
class JdbcAttendanceStatRepository(
    private val jdbcTemplate: JdbcTemplate,
) : AttendanceStatRepository {

    override fun findById(id: Long): AttendanceStatEntity? {
        val sql = """
            SELECT id, instance, encounter, start_date, end_date, character_id, character_name,
                   character_realm, character_class, character_role, character_region,
                   attended_amount_of_raids, total_amount_of_raids, attended_percentage,
                   selected_amount_of_encounters, total_amount_of_encounters, selected_percentage,
                   team_id, season_id, period_id, synced_at
            FROM attendance_stats
            WHERE id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, attendanceStatRowMapper, id)
        return results.firstOrNull()
    }

    override fun existsById(id: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM attendance_stats WHERE id = ?"
        val count = jdbcTemplate.queryForObject(sql, Int::class.java, id) ?: 0
        return count > 0
    }

    override fun findAll(offset: Long, limit: Int): List<AttendanceStatEntity> {
        val sql = """
            SELECT id, instance, encounter, start_date, end_date, character_id, character_name,
                   character_realm, character_class, character_role, character_region,
                   attended_amount_of_raids, total_amount_of_raids, attended_percentage,
                   selected_amount_of_encounters, total_amount_of_encounters, selected_percentage,
                   team_id, season_id, period_id, synced_at
            FROM attendance_stats
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, attendanceStatRowMapper, limit, offset)
    }

    override fun count(): Long {
        val sql = "SELECT COUNT(*) FROM attendance_stats"
        return jdbcTemplate.queryForObject(sql, Long::class.java) ?: 0L
    }

    override fun findByCharacterId(characterId: Long, offset: Long, limit: Int): List<AttendanceStatEntity> {
        val sql = """
            SELECT id, instance, encounter, start_date, end_date, character_id, character_name,
                   character_realm, character_class, character_role, character_region,
                   attended_amount_of_raids, total_amount_of_raids, attended_percentage,
                   selected_amount_of_encounters, total_amount_of_encounters, selected_percentage,
                   team_id, season_id, period_id, synced_at
            FROM attendance_stats
            WHERE character_id = ?
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, attendanceStatRowMapper, characterId, limit, offset)
    }

    override fun countByCharacterId(characterId: Long): Long {
        val sql = "SELECT COUNT(*) FROM attendance_stats WHERE character_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, characterId) ?: 0L
    }

    override fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<AttendanceStatEntity> {
        val sql = """
            SELECT id, instance, encounter, start_date, end_date, character_id, character_name,
                   character_realm, character_class, character_role, character_region,
                   attended_amount_of_raids, total_amount_of_raids, attended_percentage,
                   selected_amount_of_encounters, total_amount_of_encounters, selected_percentage,
                   team_id, season_id, period_id, synced_at
            FROM attendance_stats
            WHERE team_id = ?
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, attendanceStatRowMapper, teamId, limit, offset)
    }

    override fun countByTeamId(teamId: Long): Long {
        val sql = "SELECT COUNT(*) FROM attendance_stats WHERE team_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, teamId) ?: 0L
    }

    override fun findBySeasonId(seasonId: Long, offset: Long, limit: Int): List<AttendanceStatEntity> {
        val sql = """
            SELECT id, instance, encounter, start_date, end_date, character_id, character_name,
                   character_realm, character_class, character_role, character_region,
                   attended_amount_of_raids, total_amount_of_raids, attended_percentage,
                   selected_amount_of_encounters, total_amount_of_encounters, selected_percentage,
                   team_id, season_id, period_id, synced_at
            FROM attendance_stats
            WHERE season_id = ?
            ORDER BY synced_at DESC, id
            LIMIT ? OFFSET ?
        """.trimIndent()

        return jdbcTemplate.query(sql, attendanceStatRowMapper, seasonId, limit, offset)
    }

    override fun countBySeasonId(seasonId: Long): Long {
        val sql = "SELECT COUNT(*) FROM attendance_stats WHERE season_id = ?"
        return jdbcTemplate.queryForObject(sql, Long::class.java, seasonId) ?: 0L
    }

    override fun save(entity: AttendanceStatEntity): AttendanceStatEntity {
        return if (entity.id == null) {
            insertAttendanceStat(entity)
        } else {
            updateAttendanceStat(entity)
            entity
        }
    }

    override fun delete(id: Long) {
        val sql = "DELETE FROM attendance_stats WHERE id = ?"
        jdbcTemplate.update(sql, id)
    }

    private fun insertAttendanceStat(entity: AttendanceStatEntity): AttendanceStatEntity {
        val sql = """
            INSERT INTO attendance_stats (
                instance, encounter, start_date, end_date, character_id, character_name,
                character_realm, character_class, character_role, character_region,
                attended_amount_of_raids, total_amount_of_raids, attended_percentage,
                selected_amount_of_encounters, total_amount_of_encounters, selected_percentage,
                team_id, season_id, period_id, synced_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            entity.instance?.let { ps.setString(1, it) } ?: ps.setNull(1, java.sql.Types.VARCHAR)
            entity.encounter?.let { ps.setString(2, it) } ?: ps.setNull(2, java.sql.Types.VARCHAR)
            entity.startDate?.let { ps.setDate(3, Date.valueOf(it)) } ?: ps.setNull(3, java.sql.Types.DATE)
            entity.endDate?.let { ps.setDate(4, Date.valueOf(it)) } ?: ps.setNull(4, java.sql.Types.DATE)
            entity.characterId?.let { ps.setLong(5, it) } ?: ps.setNull(5, java.sql.Types.BIGINT)
            ps.setString(6, entity.characterName)
            entity.characterRealm?.let { ps.setString(7, it) } ?: ps.setNull(7, java.sql.Types.VARCHAR)
            entity.characterClass?.let { ps.setString(8, it) } ?: ps.setNull(8, java.sql.Types.VARCHAR)
            entity.characterRole?.let { ps.setString(9, it) } ?: ps.setNull(9, java.sql.Types.VARCHAR)
            entity.characterRegion?.let { ps.setString(10, it) } ?: ps.setNull(10, java.sql.Types.VARCHAR)
            entity.attendedAmountOfRaids?.let { ps.setInt(11, it) } ?: ps.setNull(11, java.sql.Types.INTEGER)
            entity.totalAmountOfRaids?.let { ps.setInt(12, it) } ?: ps.setNull(12, java.sql.Types.INTEGER)
            entity.attendedPercentage?.let { ps.setDouble(13, it) } ?: ps.setNull(13, java.sql.Types.DOUBLE)
            entity.selectedAmountOfEncounters?.let { ps.setInt(14, it) } ?: ps.setNull(14, java.sql.Types.INTEGER)
            entity.totalAmountOfEncounters?.let { ps.setInt(15, it) } ?: ps.setNull(15, java.sql.Types.INTEGER)
            entity.selectedPercentage?.let { ps.setDouble(16, it) } ?: ps.setNull(16, java.sql.Types.DOUBLE)
            entity.teamId?.let { ps.setLong(17, it) } ?: ps.setNull(17, java.sql.Types.BIGINT)
            entity.seasonId?.let { ps.setLong(18, it) } ?: ps.setNull(18, java.sql.Types.BIGINT)
            entity.periodId?.let { ps.setLong(19, it) } ?: ps.setNull(19, java.sql.Types.BIGINT)
            ps.setTimestamp(20, Timestamp.from(entity.syncedAt.toInstant()))
            ps
        }, keyHolder)

        val generatedId = keyHolder.keys?.get("id") as? Number ?: keyHolder.key?.toLong()
        return entity.copy(id = generatedId?.toLong())
    }

    private fun updateAttendanceStat(entity: AttendanceStatEntity) {
        val sql = """
            UPDATE attendance_stats SET
                instance = ?, encounter = ?, start_date = ?, end_date = ?, character_id = ?,
                character_name = ?, character_realm = ?, character_class = ?, character_role = ?,
                character_region = ?, attended_amount_of_raids = ?, total_amount_of_raids = ?,
                attended_percentage = ?, selected_amount_of_encounters = ?, total_amount_of_encounters = ?,
                selected_percentage = ?, team_id = ?, season_id = ?, period_id = ?, synced_at = ?
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(
            sql,
            entity.instance,
            entity.encounter,
            entity.startDate?.let { Date.valueOf(it) },
            entity.endDate?.let { Date.valueOf(it) },
            entity.characterId,
            entity.characterName,
            entity.characterRealm,
            entity.characterClass,
            entity.characterRole,
            entity.characterRegion,
            entity.attendedAmountOfRaids,
            entity.totalAmountOfRaids,
            entity.attendedPercentage,
            entity.selectedAmountOfEncounters,
            entity.totalAmountOfEncounters,
            entity.selectedPercentage,
            entity.teamId,
            entity.seasonId,
            entity.periodId,
            Timestamp.from(entity.syncedAt.toInstant()),
            entity.id,
        )
    }

    private val attendanceStatRowMapper = RowMapper { rs, _ ->
        val characterIdValue = rs.getLong("character_id")
        val characterId = if (rs.wasNull()) null else characterIdValue

        val attendedAmountOfRaidsValue = rs.getInt("attended_amount_of_raids")
        val attendedAmountOfRaids = if (rs.wasNull()) null else attendedAmountOfRaidsValue

        val totalAmountOfRaidsValue = rs.getInt("total_amount_of_raids")
        val totalAmountOfRaids = if (rs.wasNull()) null else totalAmountOfRaidsValue

        val attendedPercentageValue = rs.getDouble("attended_percentage")
        val attendedPercentage = if (rs.wasNull()) null else attendedPercentageValue

        val selectedAmountOfEncountersValue = rs.getInt("selected_amount_of_encounters")
        val selectedAmountOfEncounters = if (rs.wasNull()) null else selectedAmountOfEncountersValue

        val totalAmountOfEncountersValue = rs.getInt("total_amount_of_encounters")
        val totalAmountOfEncounters = if (rs.wasNull()) null else totalAmountOfEncountersValue

        val selectedPercentageValue = rs.getDouble("selected_percentage")
        val selectedPercentage = if (rs.wasNull()) null else selectedPercentageValue

        val teamIdValue = rs.getLong("team_id")
        val teamId = if (rs.wasNull()) null else teamIdValue

        val seasonIdValue = rs.getLong("season_id")
        val seasonId = if (rs.wasNull()) null else seasonIdValue

        val periodIdValue = rs.getLong("period_id")
        val periodId = if (rs.wasNull()) null else periodIdValue

        AttendanceStatEntity(
            id = rs.getLong("id"),
            instance = rs.getString("instance"),
            encounter = rs.getString("encounter"),
            startDate = rs.getDate("start_date")?.toLocalDate(),
            endDate = rs.getDate("end_date")?.toLocalDate(),
            characterId = characterId,
            characterName = rs.getString("character_name"),
            characterRealm = rs.getString("character_realm"),
            characterClass = rs.getString("character_class"),
            characterRole = rs.getString("character_role"),
            characterRegion = rs.getString("character_region"),
            attendedAmountOfRaids = attendedAmountOfRaids,
            totalAmountOfRaids = totalAmountOfRaids,
            attendedPercentage = attendedPercentage,
            selectedAmountOfEncounters = selectedAmountOfEncounters,
            totalAmountOfEncounters = totalAmountOfEncounters,
            selectedPercentage = selectedPercentage,
            teamId = teamId,
            seasonId = seasonId,
            periodId = periodId,
            syncedAt = rs.getTimestamp("synced_at")?.toInstant()?.atOffset(ZoneOffset.UTC) ?: OffsetDateTime.now(),
        )
    }
}
