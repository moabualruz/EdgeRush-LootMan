package com.edgerush.lootman.api.attendance

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.application.attendance.DeleteAttendanceCommand
import com.edgerush.lootman.application.attendance.DeleteAttendanceUseCase
import com.edgerush.lootman.application.attendance.GetAttendanceRecordQuery
import com.edgerush.lootman.application.attendance.GetAttendanceRecordUseCase
import com.edgerush.lootman.application.attendance.GetAttendanceReportQuery
import com.edgerush.lootman.application.attendance.GetAttendanceReportUseCase
import com.edgerush.lootman.application.attendance.GetGuildAttendanceSummaryQuery
import com.edgerush.lootman.application.attendance.GetGuildAttendanceSummaryUseCase
import com.edgerush.lootman.application.attendance.ListRaiderAttendanceQuery
import com.edgerush.lootman.application.attendance.ListRaiderAttendanceUseCase
import com.edgerush.lootman.application.attendance.TrackAttendanceCommand
import com.edgerush.lootman.application.attendance.TrackAttendanceUseCase
import com.edgerush.lootman.application.attendance.UpdateAttendanceCommand
import com.edgerush.lootman.application.attendance.UpdateAttendanceUseCase
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
    private val getAttendanceReportUseCase: GetAttendanceReportUseCase,
    private val getAttendanceRecordUseCase: GetAttendanceRecordUseCase,
    private val updateAttendanceUseCase: UpdateAttendanceUseCase,
    private val deleteAttendanceUseCase: DeleteAttendanceUseCase,
    private val listRaiderAttendanceUseCase: ListRaiderAttendanceUseCase,
    private val getGuildAttendanceSummaryUseCase: GetGuildAttendanceSummaryUseCase,
    private val currentUserService: CurrentUserService,
) {
    /**
     * Get attendance report for the current user.
     *
     * Returns attendance statistics for the current user's primary raider
     * for the last 90 days by default.
     *
     * @param guildId The guild's unique identifier
     * @param authenticatedUser The authenticated user from the JWT token
     * @return 200 OK with the attendance report
     */
    @GetMapping("/guilds/{guildId}/me")
    fun getMyAttendance(
        @PathVariable guildId: String,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): AttendanceReportResponse {
        currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId))
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser)

        val now = LocalDate.now()
        val startDate = now.minusDays(90)

        val query = GetAttendanceReportQuery(
            raiderId = raiderId.value,
            guildId = guildId,
            startDate = startDate,
            endDate = now,
            instance = null,
            encounter = null,
        )

        return getAttendanceReportUseCase.execute(query)
            .map { report -> AttendanceReportResponse.from(report) }
            .getOrThrow()
    }

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
        @RequestBody request: TrackAttendanceRequest,
    ): ResponseEntity<TrackAttendanceResponse> {
        val command =
            TrackAttendanceCommand(
                raiderId = request.raiderId,
                guildId = request.guildId,
                instance = request.instance,
                encounter = request.encounter,
                startDate = request.startDate,
                endDate = request.endDate,
                attendedRaids = request.attendedRaids,
                totalRaids = request.totalRaids,
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
        @RequestParam(required = false) encounter: String?,
    ): AttendanceReportResponse {
        val query =
            GetAttendanceReportQuery(
                raiderId = raiderId,
                guildId = guildId,
                startDate = startDate,
                endDate = endDate,
                instance = instance,
                encounter = encounter,
            )

        return getAttendanceReportUseCase.execute(query)
            .map { report -> AttendanceReportResponse.from(report) }
            .getOrThrow()
    }

    /**
     * Get a specific attendance record by ID.
     *
     * @param recordId The attendance record's unique identifier
     * @return 200 OK with the attendance record, or 404 if not found
     */
    @GetMapping("/{recordId}")
    fun getAttendanceRecord(@PathVariable recordId: String): TrackAttendanceResponse {
        return getAttendanceRecordUseCase.execute(GetAttendanceRecordQuery(recordId))
            .map { record -> TrackAttendanceResponse.from(record) }
            .getOrThrow()
    }

    /**
     * Update an existing attendance record.
     *
     * @param recordId The attendance record's unique identifier
     * @param request The update request with fields to modify
     * @return 200 OK with the updated attendance record, or 404 if not found
     */
    @PutMapping("/{recordId}")
    fun updateAttendanceRecord(
        @PathVariable recordId: String,
        @RequestBody request: UpdateAttendanceRequest
    ): TrackAttendanceResponse {
        val command = UpdateAttendanceCommand(
            recordId = recordId,
            instance = request.instance,
            encounter = request.encounter,
            startDate = request.startDate,
            endDate = request.endDate,
            attendedRaids = request.attendedRaids,
            totalRaids = request.totalRaids
        )

        return updateAttendanceUseCase.execute(command)
            .map { record -> TrackAttendanceResponse.from(record) }
            .getOrThrow()
    }

    /**
     * Delete an attendance record.
     *
     * @param recordId The attendance record's unique identifier
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{recordId}")
    fun deleteAttendanceRecord(@PathVariable recordId: String): ResponseEntity<Void> {
        return deleteAttendanceUseCase.execute(DeleteAttendanceCommand(recordId))
            .map { ResponseEntity.noContent().build<Void>() }
            .getOrThrow()
    }

    /**
     * Get attendance history for a raider.
     *
     * @param raiderId The raider's unique identifier
     * @param guildId The guild's unique identifier
     * @param startDate The start date of the period
     * @param endDate The end date of the period
     * @return 200 OK with the raider's attendance history
     */
    @GetMapping("/raider/{raiderId}")
    fun getRaiderAttendanceHistory(
        @PathVariable raiderId: Long,
        @RequestParam guildId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): RaiderAttendanceHistoryResponse {
        return listRaiderAttendanceUseCase.execute(
            ListRaiderAttendanceQuery(
                raiderId = raiderId,
                guildId = guildId,
                startDate = startDate,
                endDate = endDate
            )
        )
            .map { records ->
                RaiderAttendanceHistoryResponse(
                    raiderId = raiderId,
                    guildId = guildId,
                    startDate = startDate,
                    endDate = endDate,
                    records = records.map { TrackAttendanceResponse.from(it) },
                    totalRecords = records.size
                )
            }
            .getOrThrow()
    }

    /**
     * Get attendance summary for a guild.
     *
     * @param guildId The guild's unique identifier
     * @param startDate The start date of the period
     * @param endDate The end date of the period
     * @return 200 OK with the guild attendance summary
     */
    @GetMapping("/guild/{guildId}/summary")
    fun getGuildAttendanceSummary(
        @PathVariable guildId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): GuildAttendanceSummaryResponse {
        return getGuildAttendanceSummaryUseCase.execute(
            GetGuildAttendanceSummaryQuery(
                guildId = guildId,
                startDate = startDate,
                endDate = endDate
            )
        )
            .map { summary -> GuildAttendanceSummaryResponse.from(summary) }
            .getOrThrow()
    }
}
