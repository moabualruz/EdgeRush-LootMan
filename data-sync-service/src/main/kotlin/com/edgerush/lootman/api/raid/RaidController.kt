package com.edgerush.lootman.api.raid

import com.edgerush.lootman.api.common.CountResponse
import com.edgerush.lootman.api.common.ExistsResponse
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
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
import java.time.LocalDate

/**
 * REST controller for Raid operations.
 *
 * Provides CRUD endpoints for managing raids.
 */
@RestController
@RequestMapping("/api/v1/raids")
@Tag(name = "Raids", description = "Raid management operations")
class RaidController(
    private val raidService: RaidCrudService,
    private val paginationProperties: PaginationProperties,
) {

    @Operation(summary = "Get all raids", description = "Returns a paginated list of all raids")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully retrieved raids"),
    )
    @GetMapping
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return raidService.findAll(pageRequest)
    }

    @Operation(summary = "Get raid by ID", description = "Returns a single raid by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Raid found"),
        ApiResponse(responseCode = "404", description = "Raid not found"),
    )
    @GetMapping("/{id}")
    fun findById(
        @Parameter(description = "Raid ID")
        @PathVariable id: Long,
    ): RaidResponse = raidService.findById(id)

    @Operation(summary = "Create a new raid", description = "Creates a new raid and returns it")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Raid created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
    )
    @PostMapping
    fun create(
        @RequestBody request: CreateRaidRequest,
    ): ResponseEntity<RaidResponse> {
        val created = raidService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @Operation(summary = "Update a raid", description = "Updates an existing raid")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Raid updated successfully"),
        ApiResponse(responseCode = "404", description = "Raid not found"),
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "Raid ID")
        @PathVariable id: Long,
        @RequestBody request: UpdateRaidRequest,
    ): RaidResponse = raidService.update(id, request)

    @Operation(summary = "Delete a raid", description = "Deletes a raid by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Raid deleted successfully"),
        ApiResponse(responseCode = "404", description = "Raid not found"),
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "Raid ID")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        raidService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Check if raid exists", description = "Returns whether a raid with the given ID exists")
    @GetMapping("/{id}/exists")
    fun exists(
        @Parameter(description = "Raid ID")
        @PathVariable id: Long,
    ): ExistsResponse = ExistsResponse(exists = raidService.existsById(id))

    @Operation(summary = "Get raids by team", description = "Returns paginated raids for a specific team")
    @GetMapping("/team/{teamId}")
    fun findByTeam(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return raidService.findByTeam(teamId, pageRequest)
    }

    @Operation(summary = "Get raids by date range", description = "Returns paginated raids within a date range")
    @GetMapping("/date-range")
    fun findByDateRange(
        @Parameter(description = "Start date (inclusive)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "End date (inclusive)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return raidService.findByDateRange(startDate, endDate, pageRequest)
    }

    @Operation(summary = "Count raids by team", description = "Returns the count of raids for a team")
    @GetMapping("/team/{teamId}/count")
    fun countByTeam(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
    ): CountResponse = CountResponse(count = raidService.countByTeam(teamId))

    @Operation(summary = "Get upcoming raids for a guild", description = "Returns upcoming raids for teams in a guild")
    @GetMapping("/guilds/{guildId}/upcoming")
    fun findUpcomingByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: Long,
        @Parameter(description = "Maximum number of raids to return")
        @RequestParam(defaultValue = "10") limit: Int,
    ): List<RaidResponse> = raidService.findUpcomingByGuild(guildId, limit)

    @Operation(summary = "Get past raids for a guild", description = "Returns past raids for teams in a guild")
    @GetMapping("/guilds/{guildId}/past")
    fun findPastByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: Long,
        @Parameter(description = "Maximum number of raids to return")
        @RequestParam(defaultValue = "10") limit: Int,
    ): List<RaidResponse> = raidService.findPastByGuild(guildId, limit)
}
