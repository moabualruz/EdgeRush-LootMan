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
 * This use case orchestrates the process of:
 * 1. Validating the query parameters
 * 2. Calculating attendance statistics based on the query scope
 * 3. Returning an aggregated attendance report
 */
@Service
class GetAttendanceReportUseCase(
    private val attendanceCalculationService: AttendanceCalculationService,
) {
    /**
     * Execute the get attendance report use case.
     *
     * @param query The query containing report parameters
     * @return Result containing the AttendanceReport or an exception
     */
    fun execute(query: GetAttendanceReportQuery): Result<AttendanceReport> =
        runCatching {
            // Validate that encounter is not specified without instance
            if (query.encounter != null && query.instance == null) {
                throw IllegalArgumentException("Cannot query encounter attendance without specifying an instance")
            }

            val raiderId = RaiderId(query.raiderId)
            val guildId = GuildId(query.guildId)

            // Calculate stats based on query scope
            val stats =
                when {
                    query.encounter != null && query.instance != null -> {
                        // Encounter-specific stats
                        attendanceCalculationService.calculateAttendanceStatsForEncounter(
                            raiderId = raiderId,
                            guildId = guildId,
                            instance = query.instance,
                            encounter = query.encounter,
                            startDate = query.startDate,
                            endDate = query.endDate,
                        )
                    }
                    query.instance != null -> {
                        // Instance-specific stats
                        attendanceCalculationService.calculateAttendanceStatsForInstance(
                            raiderId = raiderId,
                            guildId = guildId,
                            instance = query.instance,
                            startDate = query.startDate,
                            endDate = query.endDate,
                        )
                    }
                    else -> {
                        // Overall stats
                        attendanceCalculationService.calculateAttendanceStats(
                            raiderId = raiderId,
                            guildId = guildId,
                            startDate = query.startDate,
                            endDate = query.endDate,
                        )
                    }
                }

            // Create the report
            AttendanceReport(
                raiderId = raiderId,
                guildId = guildId,
                startDate = query.startDate,
                endDate = query.endDate,
                instance = query.instance,
                encounter = query.encounter,
                stats = stats,
            )
        }
}

/**
 * Query for getting an attendance report.
 *
 * @property raiderId The raider's unique identifier
 * @property guildId The guild's unique identifier
 * @property startDate The start date of the report period
 * @property endDate The end date of the report period
 * @property instance Optional raid instance name for instance-specific report
 * @property encounter Optional encounter name for encounter-specific report
 */
data class GetAttendanceReportQuery(
    val raiderId: Long,
    val guildId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val instance: String? = null,
    val encounter: String? = null,
)

/**
 * Attendance report containing aggregated statistics.
 *
 * @property raiderId The raider's unique identifier
 * @property guildId The guild's unique identifier
 * @property startDate The start date of the report period
 * @property endDate The end date of the report period
 * @property instance The raid instance name (null for overall report)
 * @property encounter The encounter name (null for instance or overall report)
 * @property stats The aggregated attendance statistics
 */
data class AttendanceReport(
    val raiderId: RaiderId,
    val guildId: GuildId,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val instance: String?,
    val encounter: String?,
    val stats: AttendanceStats,
)
