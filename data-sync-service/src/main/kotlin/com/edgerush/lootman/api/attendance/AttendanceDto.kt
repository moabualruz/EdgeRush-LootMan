package com.edgerush.lootman.api.attendance

import com.edgerush.lootman.application.attendance.AttendanceReport
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
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
        fun from(record: AttendanceRecord): TrackAttendanceResponse {
            return TrackAttendanceResponse(
                recordId = record.id.value,
                raiderId = record.raiderId.value,
                guildId = record.guildId.value,
                instance = record.instance,
                encounter = record.encounter,
                startDate = record.startDate,
                endDate = record.endDate,
                attendedRaids = record.attendedRaids,
                totalRaids = record.totalRaids,
                attendancePercentage = record.attendancePercentage,
                recordedAt = record.recordedAt
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
