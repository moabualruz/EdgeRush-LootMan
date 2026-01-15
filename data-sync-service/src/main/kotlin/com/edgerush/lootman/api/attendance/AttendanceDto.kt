package com.edgerush.lootman.api.attendance

import com.edgerush.lootman.application.attendance.AttendanceReport
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceStats
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate

/**
 * Request DTO for tracking attendance.
 */
data class TrackAttendanceRequest(
    @field:Min(value = 1, message = "Raider ID must be positive")
    val raiderId: Long,
    @field:NotBlank(message = "Guild ID is required")
    val guildId: String,
    @field:NotBlank(message = "Instance is required")
    val instance: String,
    val encounter: String? = null,
    @field:NotNull(message = "Start date is required")
    val startDate: LocalDate,
    @field:NotNull(message = "End date is required")
    val endDate: LocalDate,
    @field:Min(value = 0, message = "Attended raids must be non-negative")
    val attendedRaids: Int,
    @field:Min(value = 1, message = "Total raids must be at least 1")
    val totalRaids: Int,
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
    val recordedAt: Instant,
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
                recordedAt = record.recordedAt,
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
    val stats: AttendanceStatsDto,
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
                stats = AttendanceStatsDto.from(report.stats),
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
    val missedRaids: Int,
) {
    companion object {
        fun from(stats: AttendanceStats): AttendanceStatsDto {
            return AttendanceStatsDto(
                attendancePercentage = stats.attendancePercentage,
                totalRaids = stats.totalRaids,
                attendedRaids = stats.attendedRaids,
                missedRaids = stats.missedRaids,
            )
        }
    }
}

/**
 * Request DTO for updating attendance.
 */
data class UpdateAttendanceRequest(
    val instance: String? = null,
    val encounter: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val attendedRaids: Int? = null,
    val totalRaids: Int? = null,
)

/**
 * Response DTO for raider attendance history.
 */
data class RaiderAttendanceHistoryResponse(
    val raiderId: Long,
    val guildId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val records: List<TrackAttendanceResponse>,
    val totalRecords: Int,
)

/**
 * Response DTO for guild attendance summary.
 */
data class GuildAttendanceSummaryResponse(
    val guildId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalRecords: Int,
    val uniqueRaiders: Int,
    val overallAttendancePercentage: Double,
    val raiderSummaries: List<RaiderAttendanceSummaryResponse>,
) {
    companion object {
        fun from(summary: com.edgerush.lootman.application.attendance.GuildAttendanceSummary): GuildAttendanceSummaryResponse {
            return GuildAttendanceSummaryResponse(
                guildId = summary.guildId,
                startDate = summary.startDate,
                endDate = summary.endDate,
                totalRecords = summary.totalRecords,
                uniqueRaiders = summary.uniqueRaiders,
                overallAttendancePercentage = summary.overallAttendancePercentage,
                raiderSummaries = summary.raiderSummaries.map { RaiderAttendanceSummaryResponse.from(it) },
            )
        }
    }
}

/**
 * Response DTO for raider attendance summary within a guild.
 */
data class RaiderAttendanceSummaryResponse(
    val raiderId: Long,
    val totalRecords: Int,
    val totalAttendedRaids: Int,
    val totalRaids: Int,
    val averageAttendancePercentage: Double,
) {
    companion object {
        fun from(summary: com.edgerush.lootman.application.attendance.RaiderAttendanceSummary): RaiderAttendanceSummaryResponse {
            return RaiderAttendanceSummaryResponse(
                raiderId = summary.raiderId,
                totalRecords = summary.totalRecords,
                totalAttendedRaids = summary.totalAttendedRaids,
                totalRaids = summary.totalRaids,
                averageAttendancePercentage = summary.averageAttendancePercentage,
            )
        }
    }
}
