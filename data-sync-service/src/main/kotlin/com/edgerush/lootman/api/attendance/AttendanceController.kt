package com.edgerush.lootman.api.attendance

import com.edgerush.lootman.application.attendance.GetAttendanceReportQuery
import com.edgerush.lootman.application.attendance.GetAttendanceReportUseCase
import com.edgerush.lootman.application.attendance.TrackAttendanceCommand
import com.edgerush.lootman.application.attendance.TrackAttendanceUseCase
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
 * - Tracking raid attendance records
 * - Retrieving attendance reports (overall, instance-specific, encounter-specific)
 * - Querying attendance statistics
 *
 * This controller maintains backward compatibility with existing attendance endpoints
 * while using the new domain-driven architecture.
 */
@RestController
@RequestMapping("/api/v1/attendance")
class AttendanceController(
    private val trackAttendanceUseCase: TrackAttendanceUseCase,
    private val getAttendanceReportUseCase: GetAttendanceReportUseCase
) {

    /**
     * Track attendance for a raider.
     *
     * Records attendance data for a specific raider in a raid instance or encounter.
     * Can track overall instance attendance or specific encounter attendance.
     *
     * @param request The attendance tracking request
     * @return 201 Created with the created attendance record
     */
    @PostMapping("/track")
    fun trackAttendance(
        @RequestBody request: TrackAttendanceRequest
    ): ResponseEntity<TrackAttendanceResponse> {
        val command = TrackAttendanceCommand(
            raiderId = request.raiderId,
            guildId = request.guildId,
            instance = request.instance,
            encounter = request.encounter,
            startDate = request.startDate,
            endDate = request.endDate,
            attendedRaids = request.attendedRaids,
            totalRaids = request.totalRaids
        )

        return trackAttendanceUseCase.execute(command)
            .map { record ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(TrackAttendanceResponse.from(record))
            }
            .getOrElse { exception ->
                throw exception
            }
    }

    /**
     * Get attendance report for a raider.
     *
     * Retrieves aggregated attendance statistics for a raider within a date range.
     * Supports three levels of granularity:
     * - Overall: All raids across all instances
     * - Instance-specific: All raids in a specific instance
     * - Encounter-specific: Specific encounter in a specific instance
     *
     * @param raiderId The raider's unique identifier
     * @param guildId The guild's unique identifier
     * @param startDate The start date of the report period (ISO format: yyyy-MM-dd)
     * @param endDate The end date of the report period (ISO format: yyyy-MM-dd)
     * @param instance Optional raid instance name for instance-specific report
     * @param encounter Optional encounter name for encounter-specific report (requires instance)
     * @return 200 OK with the attendance report
     */
    @GetMapping("/raiders/{raiderId}/report")
    fun getAttendanceReport(
        @PathVariable raiderId: Long,
        @RequestParam guildId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(required = false) instance: String?,
        @RequestParam(required = false) encounter: String?
    ): AttendanceReportResponse {
        val query = GetAttendanceReportQuery(
            raiderId = raiderId,
            guildId = guildId,
            startDate = startDate,
            endDate = endDate,
            instance = instance,
            encounter = encounter
        )

        return getAttendanceReportUseCase.execute(query)
            .map { report -> AttendanceReportResponse.from(report) }
            .getOrThrow()
    }
}
