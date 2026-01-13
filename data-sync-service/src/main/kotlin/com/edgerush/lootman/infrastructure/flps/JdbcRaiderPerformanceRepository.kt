package com.edgerush.lootman.infrastructure.flps

import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.repository.RaiderPerformanceRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import java.time.Instant

/**
 * JDBC implementation of RaiderPerformanceRepository.
 *
 * Aggregates performance data from Warcraft Logs tables:
 * - warcraft_logs_performance: Per-fight character metrics
 * - warcraft_logs_fights: Fight metadata and timestamps
 * - warcraft_logs_reports: Report metadata and guild association
 *
 * Queries join these tables to aggregate deaths, fights, and avoidable damage
 * by character within a specified time period.
 */
@Repository
class JdbcRaiderPerformanceRepository(
    private val jdbcTemplate: JdbcTemplate
) : RaiderPerformanceRepository {

    override fun findByRaiderAndPeriod(
        raiderId: RaiderId,
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant
    ): RaiderPerformanceData? {
        val sql = """
            SELECT
                r.id as raider_id,
                wlp.character_name,
                wlp.character_realm,
                COALESCE(SUM(wlp.deaths), 0) as total_deaths,
                COUNT(wlp.id) as total_fights,
                COALESCE(AVG(wlp.avoidable_damage_percentage), 0) as avg_avoidable_damage,
                MIN(wlr.start_time) as period_start,
                MAX(wlr.end_time) as period_end
            FROM raiders r
            JOIN warcraft_logs_performance wlp
                ON LOWER(r.characterName) = LOWER(wlp.character_name)
                AND LOWER(r.realm) = LOWER(wlp.character_realm)
            JOIN warcraft_logs_fights wlf ON wlp.fight_id = wlf.id
            JOIN warcraft_logs_reports wlr ON wlf.report_id = wlr.id
            WHERE r.id = ?
                AND wlr.guild_id = ?
                AND wlr.start_time >= ?
                AND wlr.end_time <= ?
            GROUP BY r.id, wlp.character_name, wlp.character_realm
        """.trimIndent()

        val results = jdbcTemplate.query(
            sql,
            performanceRowMapper(startDate, endDate),
            raiderId.value,
            guildId.value,
            Timestamp.from(startDate),
            Timestamp.from(endDate)
        )

        return results.firstOrNull()
    }

    override fun findByCharacterAndPeriod(
        characterName: String,
        characterRealm: String,
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant
    ): RaiderPerformanceData? {
        val sql = """
            SELECT
                COALESCE(r.id, 0) as raider_id,
                wlp.character_name,
                wlp.character_realm,
                COALESCE(SUM(wlp.deaths), 0) as total_deaths,
                COUNT(wlp.id) as total_fights,
                COALESCE(AVG(wlp.avoidable_damage_percentage), 0) as avg_avoidable_damage,
                MIN(wlr.start_time) as period_start,
                MAX(wlr.end_time) as period_end
            FROM warcraft_logs_performance wlp
            JOIN warcraft_logs_fights wlf ON wlp.fight_id = wlf.id
            JOIN warcraft_logs_reports wlr ON wlf.report_id = wlr.id
            LEFT JOIN raiders r
                ON LOWER(r.characterName) = LOWER(wlp.character_name)
                AND LOWER(r.realm) = LOWER(wlp.character_realm)
            WHERE wlp.character_name = ?
                AND wlp.character_realm = ?
                AND wlr.guild_id = ?
                AND wlr.start_time >= ?
                AND wlr.end_time <= ?
            GROUP BY r.id, wlp.character_name, wlp.character_realm
        """.trimIndent()

        val results = jdbcTemplate.query(
            sql,
            performanceRowMapper(startDate, endDate),
            characterName,
            characterRealm,
            guildId.value,
            Timestamp.from(startDate),
            Timestamp.from(endDate)
        )

        return results.firstOrNull()
    }

    override fun findAllByGuildAndPeriod(
        guildId: GuildId,
        startDate: Instant,
        endDate: Instant
    ): List<RaiderPerformanceData> {
        val sql = """
            SELECT
                COALESCE(r.id, 0) as raider_id,
                wlp.character_name,
                wlp.character_realm,
                COALESCE(SUM(wlp.deaths), 0) as total_deaths,
                COUNT(wlp.id) as total_fights,
                COALESCE(AVG(wlp.avoidable_damage_percentage), 0) as avg_avoidable_damage,
                MIN(wlr.start_time) as period_start,
                MAX(wlr.end_time) as period_end
            FROM warcraft_logs_performance wlp
            JOIN warcraft_logs_fights wlf ON wlp.fight_id = wlf.id
            JOIN warcraft_logs_reports wlr ON wlf.report_id = wlr.id
            LEFT JOIN raiders r
                ON LOWER(r.characterName) = LOWER(wlp.character_name)
                AND LOWER(r.realm) = LOWER(wlp.character_realm)
            WHERE wlr.guild_id = ?
                AND wlr.start_time >= ?
                AND wlr.end_time <= ?
            GROUP BY r.id, wlp.character_name, wlp.character_realm
            ORDER BY wlp.character_name
        """.trimIndent()

        return jdbcTemplate.query(
            sql,
            performanceRowMapper(startDate, endDate),
            guildId.value,
            Timestamp.from(startDate),
            Timestamp.from(endDate)
        )
    }

    private fun performanceRowMapper(
        periodStart: Instant,
        periodEnd: Instant
    ) = RowMapper { rs, _ ->
        val totalDeaths = rs.getInt("total_deaths")
        val totalFights = rs.getInt("total_fights")
        val deathsPerAttempt = if (totalFights > 0) {
            totalDeaths.toDouble() / totalFights
        } else {
            0.0
        }

        var avgAvoidableDamage = rs.getDouble("avg_avoidable_damage")
        if (rs.wasNull()) {
            avgAvoidableDamage = 0.0
        }

        // Use provided period bounds if DB returns null
        val dbPeriodStart = rs.getTimestamp("period_start")?.toInstant() ?: periodStart
        val dbPeriodEnd = rs.getTimestamp("period_end")?.toInstant() ?: periodEnd

        RaiderPerformanceData(
            raiderId = RaiderId(rs.getLong("raider_id")),
            characterName = rs.getString("character_name") ?: "",
            characterRealm = rs.getString("character_realm") ?: "",
            totalDeaths = totalDeaths,
            totalFights = totalFights,
            deathsPerAttempt = deathsPerAttempt,
            avoidableDamagePercentage = avgAvoidableDamage,
            periodStart = dbPeriodStart,
            periodEnd = dbPeriodEnd
        )
    }
}
