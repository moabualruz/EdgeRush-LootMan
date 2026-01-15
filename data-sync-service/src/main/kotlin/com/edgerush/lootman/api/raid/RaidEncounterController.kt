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
 * REST controller for RaidEncounter operations.
 *
 * Provides CRUD endpoints for managing raid encounters.
 */
@RestController
@RequestMapping("/api/v1/raid-encounters")
@Tag(name = "Raid Encounters", description = "Raid encounter management operations")
class RaidEncounterController(
    private val encounterService: RaidEncounterCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @Operation(summary = "Get all encounters", description = "Returns a paginated list of all raid encounters")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully retrieved encounters"),
    )
    @GetMapping
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidEncounterResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return encounterService.findAll(pageRequest)
    }

    @Operation(summary = "Get encounter by ID", description = "Returns a single encounter by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Encounter found"),
        ApiResponse(responseCode = "404", description = "Encounter not found"),
    )
    @GetMapping("/{id}")
    fun findById(
        @Parameter(description = "Encounter ID")
        @PathVariable id: Long,
    ): RaidEncounterResponse = encounterService.findById(id)

    @Operation(summary = "Create a new encounter", description = "Creates a new raid encounter and returns it")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Encounter created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
    )
    @PostMapping
    fun create(
        @RequestBody request: CreateRaidEncounterRequest,
    ): ResponseEntity<RaidEncounterResponse> {
        val created = encounterService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @Operation(summary = "Update an encounter", description = "Updates an existing raid encounter")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Encounter updated successfully"),
        ApiResponse(responseCode = "404", description = "Encounter not found"),
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "Encounter ID")
        @PathVariable id: Long,
        @RequestBody request: UpdateRaidEncounterRequest,
    ): RaidEncounterResponse = encounterService.update(id, request)

    @Operation(summary = "Delete an encounter", description = "Deletes an encounter by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Encounter deleted successfully"),
        ApiResponse(responseCode = "404", description = "Encounter not found"),
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "Encounter ID")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        encounterService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Check if encounter exists", description = "Returns whether an encounter with the given ID exists")
    @GetMapping("/{id}/exists")
    fun exists(
        @Parameter(description = "Encounter ID")
        @PathVariable id: Long,
    ): ExistsResponse = ExistsResponse(exists = encounterService.existsById(id))

    @Operation(summary = "Get encounters by raid", description = "Returns paginated encounters for a specific raid")
    @GetMapping("/raid/{raidId}")
    fun findByRaid(
        @Parameter(description = "Raid ID")
        @PathVariable raidId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidEncounterResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return encounterService.findByRaid(raidId, pageRequest)
    }

    @Operation(summary = "Get enabled encounters by raid", description = "Returns paginated enabled encounters for a specific raid")
    @GetMapping("/raid/{raidId}/enabled")
    fun findEnabledByRaid(
        @Parameter(description = "Raid ID")
        @PathVariable raidId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidEncounterResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return encounterService.findEnabledByRaid(raidId, pageRequest)
    }

    @Operation(summary = "Count encounters by raid", description = "Returns the count of encounters for a raid")
    @GetMapping("/raid/{raidId}/count")
    fun countByRaid(
        @Parameter(description = "Raid ID")
        @PathVariable raidId: Long,
    ): CountResponse = CountResponse(count = encounterService.countByRaid(raidId))
}
