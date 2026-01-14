package com.edgerush.lootman.api.attendance

import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for AttendanceStat entity CRUD operations.
 *
 * Provides endpoints for managing attendance statistics.
 */
@RestController
@RequestMapping("/api/attendance-stats")
@Tag(name = "AttendanceStat", description = "Attendance statistic management endpoints")
class AttendanceStatController(
    private val attendanceStatService: AttendanceStatCrudService,
    private val paginationProperties: PaginationProperties,
) {

    @GetMapping
    @Operation(summary = "Find all attendance stats with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<AttendanceStatResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return attendanceStatService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find attendance stat by ID")
    fun findById(
        @Parameter(description = "Attendance stat ID")
        @PathVariable id: Long,
    ): AttendanceStatResponse {
        return attendanceStatService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create a new attendance stat")
    fun create(
        @Valid @RequestBody request: CreateAttendanceStatRequest,
    ): ResponseEntity<AttendanceStatResponse> {
        val created = attendanceStatService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing attendance stat")
    fun update(
        @Parameter(description = "Attendance stat ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateAttendanceStatRequest,
    ): AttendanceStatResponse {
        return attendanceStatService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an attendance stat")
    fun delete(
        @Parameter(description = "Attendance stat ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        attendanceStatService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if attendance stat exists")
    fun exists(
        @Parameter(description = "Attendance stat ID")
        @PathVariable id: Long,
    ): AttendanceStatExistsResponse {
        return AttendanceStatExistsResponse(exists = attendanceStatService.existsById(id))
    }

    @GetMapping("/character/{characterId}")
    @Operation(summary = "Find attendance stats by character with pagination")
    fun findByCharacterId(
        @Parameter(description = "Character ID")
        @PathVariable characterId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<AttendanceStatResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return attendanceStatService.findByCharacterId(characterId, pageRequest)
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Find attendance stats by team with pagination")
    fun findByTeamId(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<AttendanceStatResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return attendanceStatService.findByTeamId(teamId, pageRequest)
    }

    @GetMapping("/season/{seasonId}")
    @Operation(summary = "Find attendance stats by season with pagination")
    fun findBySeasonId(
        @Parameter(description = "Season ID")
        @PathVariable seasonId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<AttendanceStatResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return attendanceStatService.findBySeasonId(seasonId, pageRequest)
    }

    @GetMapping("/character/{characterId}/count")
    @Operation(summary = "Count attendance stats for a character")
    fun countByCharacterId(
        @Parameter(description = "Character ID")
        @PathVariable characterId: Long,
    ): AttendanceStatCountResponse {
        return AttendanceStatCountResponse(count = attendanceStatService.countByCharacterId(characterId))
    }
}
