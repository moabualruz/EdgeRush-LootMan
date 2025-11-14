package com.edgerush.lootman.api.attendance

import com.edgerush.lootman.application.attendance.AttendanceReport
import com.edgerush.lootman.application.attendance.AttendanceTrackingResult
import com.edgerush.lootman.application.attendance.GuildAttendanceReport
import com.edgerush.lootman.domain.attendance.model.AttendanceStats
import java.time.Instant
import java.time.LocalDate

/**
 * Request DTO for tracking attendance.
 */
data class TrackAttendanceRequest(
    val raiderId: Long,
    val guildId: String,
    val instance: String,
    val encounter: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val attendedRaids: Int,
    val totalRaids: Int
)

/**
 * Response DTO for tracking attendance.
 */
data class TrackAttendanceResponse(
    val recordId: String,
    val raiderId: Long,
    val guildId: String,
    val instance: String,
    val encounter: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val attendedRaids: Int,
    val totalRaids: Int,
    val attendancePercentage: Double,
    val recordedAt: Instant
) {
    companion object {
        fun from(result: AttendanceTrackingResult): TrackAttendanceResponse {
            return TrackAttendanceResponse(
                recordId = result.recordId.value,
                raiderId = result.raiderId.value,
                guildId = result.guildId.value,
                instance = result.instance,
                encounter = result.encounter,
                startDate = result.startDate,
                endDate = result.endDate,
                attendedRaids = result.attendedRaids,
                totalRaids = result.totalRaids,
                attendancePercentage = result.attendancePercentage,
                recordedAt = result.recordedAt
            )
        }
    }
}

/**
 * Response DTO for attendance report.
 */
data class AttendanceReportResponse(
    val raiderId: Long,
    val guildId: String,
    val instance: String?,
    val encounter: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val stats: AttendanceStatsDto
) {
    companion object {
        fun from(report: AttendanceReport): AttendanceReportResponse {
            return AttendanceReportResponse(
                raiderId = report.raiderId.value,
                guildId = report.guildId.value,
                instance = report.instance,
                encounter = report.encounter,
                startDate = report.startDate,
                endDate = report.endDate,
                stats = AttendanceStatsDto.from(report.stats)
            )
        }
    }
}

/**
 * Response DTO for guild attendance report.
 */
data class GuildAttendanceReportResponse(
    val guildId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val stats: AttendanceStatsDto
) {
    companion object {
        fun from(report: GuildAttendanceReport): GuildAttendanceReportResponse {
            return GuildAttendanceReportResponse(
                guildId = report.guildId.value,
                startDate = report.startDate,
                endDate = report.endDate,
                stats = AttendanceStatsDto.from(report.stats)
            )
        }
    }
}

/**
 * DTO for attendance statistics.
 */
data class AttendanceStatsDto(
    val attendancePercentage: Double,
    val totalRaids: Int,
    val attendedRaids: Int,
    val missedRaids: Int
) {
    companion object {
        fun from(stats: AttendanceStats): AttendanceStatsDto {
            return AttendanceStatsDto(
                attendancePercentage = stats.attendancePercentage,
                totalRaids = stats.totalRaids,
                attendedRaids = stats.attendedRaids,
                missedRaids = stats.missedRaids
            )
        }
    }
}
