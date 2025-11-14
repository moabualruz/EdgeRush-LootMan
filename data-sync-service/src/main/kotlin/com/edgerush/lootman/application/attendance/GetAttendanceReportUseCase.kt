package com.edgerush.lootman.application.attendance

import com.edgerush.lootman.domain.attendance.model.AttendanceStats
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.service.AttendanceCalculationService
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Use case for getting attendance reports.
 *
 * This use case orchestrates attendance reporting by:
 * 1. Determining the scope (overall, instance, or encounter)
 * 2. Calculating attendance statistics
 * 3. Returning formatted report
 */
@Service
class GetAttendanceReportUseCase(
    private val attendanceCalculationService: AttendanceCalculationService
) {
    /**
     * Executes the attendance report generation for a raider.
     *
     * @param query The report query with scope and filters
     * @return Result containing AttendanceReport or error
     */
    fun execute(query: GetAttendanceReportQuery): Result<AttendanceReport> = runCatching {
        val stats = when {
            // Specific encounter
            query.instance != null && query.encounter != null -> {
                attendanceCalculationService.calculateAttendanceStatsForEncounter(
                    raiderId = query.raiderId,
                    guildId = query.guildId,
                    instance = query.instance,
                    encounter = query.encounter,
                    startDate = query.startDate,
                    endDate = query.endDate
                )
            }
            // Specific instance
            query.instance != null -> {
                attendanceCalculationService.calculateAttendanceStatsForInstance(
                    raiderId = query.raiderId,
                    guildId = query.guildId,
                    instance = query.instance,
                    startDate = query.startDate,
                    endDate = query.endDate
                )
            }
            // Overall attendance
            else -> {
                attendanceCalculationService.calculateAttendanceStats(
                    raiderId = query.raiderId,
                    guildId = query.guildId,
                    startDate = query.startDate,
                    endDate = query.endDate
                )
            }
        }

        AttendanceReport(
            raiderId = query.raiderId,
            guildId = query.guildId,
            instance = query.instance,
            encounter = query.encounter,
            startDate = query.startDate,
            endDate = query.endDate,
            stats = stats
        )
    }

    /**
     * Executes the attendance report generation for an entire guild.
     *
     * @param query The guild report query
     * @return Result containing GuildAttendanceReport or error
     */
    fun executeGuildReport(query: GetGuildAttendanceReportQuery): Result<GuildAttendanceReport> = runCatching {
        val stats = attendanceCalculationService.calculateGuildAttendanceStats(
            guildId = query.guildId,
            startDate = query.startDate,
            endDate = query.endDate
        )

        GuildAttendanceReport(
            guildId = query.guildId,
            startDate = query.startDate,
            endDate = query.endDate,
            stats = stats
        )
    }
}

/**
 * Query for getting attendance report for a raider.
 */
data class GetAttendanceReportQuery(
    val raiderId: RaiderId,
    val guildId: GuildId,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val instance: String? = null,
    val encounter: String? = null
)

/**
 * Query for getting guild-wide attendance report.
 */
data class GetGuildAttendanceReportQuery(
    val guildId: GuildId,
    val startDate: LocalDate,
    val endDate: LocalDate
)

/**
 * Attendance report for a raider.
 */
data class AttendanceReport(
    val raiderId: RaiderId,
    val guildId: GuildId,
    val instance: String?,
    val encounter: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val stats: AttendanceStats
)

/**
 * Guild-wide attendance report.
 */
data class GuildAttendanceReport(
    val guildId: GuildId,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val stats: AttendanceStats
)
