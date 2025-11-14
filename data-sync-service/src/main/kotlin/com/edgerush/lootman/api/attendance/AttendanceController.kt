package com.edgerush.lootman.api.attendance

import com.edgerush.lootman.application.attendance.GetAttendanceReportQuery
import com.edgerush.lootman.application.attendance.GetAttendanceReportUseCase
import com.edgerush.lootman.application.attendance.GetGuildAttendanceReportQuery
import com.edgerush.lootman.application.attendance.TrackAttendanceCommand
import com.edgerush.lootman.application.attendance.TrackAttendanceUseCase
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * REST controller for Attendance operations.
 *
 * Provides endpoints for:
 * - Tracking raid attendance
 * - Querying attendance reports
 * - Viewing attendance statistics
 */
@RestController
@RequestMapping("/api/v1/attendance")
class AttendanceController(
    private val trackAttendanceUseCase: TrackAttendanceUseCase,
    private val getAttendanceReportUseCase: GetAttendanceReportUseCase
) {

    /**
     * Track attendance for a raider.
     */
    @PostMapping("/track")
    fun trackAttendance(
        @RequestBody request: TrackAttendanceRequest
    ): ResponseEntity<TrackAttendanceResponse> {
        val command = TrackAttendanceCommand(
            raiderId = RaiderId(request.raiderId),
            guildId = GuildId(request.guildId),
            instance = request.instance,
            encounter = request.encounter,
            startDate = request.startDate,
            endDate = request.endDate,
            attendedRaids = request.attendedRaids,
            totalRaids = request.totalRaids
        )

        return trackAttendanceUseCase.execute(command)
            .map { result ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(TrackAttendanceResponse.from(result))
            }
            .getOrElse { exception ->
                ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build()
            }
    }

    /**
     * Get attendance report for a raider.
     */
    @GetMapping("/raiders/{raiderId}")
    fun getRaiderAttendanceReport(
        @PathVariable raiderId: Long,
        @RequestParam guildId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(required = false) instance: String?,
        @RequestParam(required = false) encounter: String?
    ): AttendanceReportResponse {
        val query = GetAttendanceReportQuery(
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            startDate = startDate,
            endDate = endDate,
            instance = instance,
            encounter = encounter
        )

        return getAttendanceReportUseCase.execute(query)
            .map { report -> AttendanceReportResponse.from(report) }
            .getOrThrow()
    }

    /**
     * Get guild-wide attendance report.
     */
    @GetMapping("/guilds/{guildId}")
    fun getGuildAttendanceReport(
        @PathVariable guildId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): GuildAttendanceReportResponse {
        val query = GetGuildAttendanceReportQuery(
            guildId = GuildId(guildId),
            startDate = startDate,
            endDate = endDate
        )

        return getAttendanceReportUseCase.executeGuildReport(query)
            .map { report -> GuildAttendanceReportResponse.from(report) }
            .getOrThrow()
    }
}
