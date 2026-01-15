package com.edgerush.lootman.api.team

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
 * REST controller for TeamRaidDay entity CRUD operations.
 *
 * Provides endpoints for managing team raid day schedules.
 */
@RestController
@RequestMapping("/api/team-raid-days")
@Tag(name = "TeamRaidDay", description = "Team raid day management endpoints")
class TeamRaidDayController(
    private val teamRaidDayService: TeamRaidDayCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all team raid days with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<TeamRaidDayResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return teamRaidDayService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find team raid day by ID")
    fun findById(
        @Parameter(description = "Team raid day ID")
        @PathVariable id: Long,
    ): TeamRaidDayResponse {
        return teamRaidDayService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create a new team raid day")
    fun create(
        @Valid @RequestBody request: CreateTeamRaidDayRequest,
    ): ResponseEntity<TeamRaidDayResponse> {
        val created = teamRaidDayService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing team raid day")
    fun update(
        @Parameter(description = "Team raid day ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTeamRaidDayRequest,
    ): TeamRaidDayResponse {
        return teamRaidDayService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a team raid day")
    fun delete(
        @Parameter(description = "Team raid day ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        teamRaidDayService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if team raid day exists")
    fun exists(
        @Parameter(description = "Team raid day ID")
        @PathVariable id: Long,
    ): TeamRaidDayExistsResponse {
        return TeamRaidDayExistsResponse(exists = teamRaidDayService.existsById(id))
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Find team raid days by team with pagination")
    fun findByTeamId(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<TeamRaidDayResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return teamRaidDayService.findByTeamId(teamId, pageRequest)
    }

    @GetMapping("/team/{teamId}/count")
    @Operation(summary = "Count team raid days for a team")
    fun countByTeamId(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
    ): TeamRaidDayCountResponse {
        return TeamRaidDayCountResponse(count = teamRaidDayService.countByTeamId(teamId))
    }
}
