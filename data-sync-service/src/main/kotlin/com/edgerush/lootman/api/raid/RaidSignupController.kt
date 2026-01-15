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
 * REST controller for RaidSignup operations.
 *
 * Provides CRUD endpoints for managing raid signups.
 */
@RestController
@RequestMapping("/api/v1/raid-signups")
@Tag(name = "Raid Signups", description = "Raid signup management operations")
class RaidSignupController(
    private val signupService: RaidSignupCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @Operation(summary = "Get all signups", description = "Returns a paginated list of all raid signups")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successfully retrieved signups"),
    )
    @GetMapping
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidSignupResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return signupService.findAll(pageRequest)
    }

    @Operation(summary = "Get signup by ID", description = "Returns a single signup by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Signup found"),
        ApiResponse(responseCode = "404", description = "Signup not found"),
    )
    @GetMapping("/{id}")
    fun findById(
        @Parameter(description = "Signup ID")
        @PathVariable id: Long,
    ): RaidSignupResponse = signupService.findById(id)

    @Operation(summary = "Create a new signup", description = "Creates a new raid signup and returns it")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Signup created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
    )
    @PostMapping
    fun create(
        @RequestBody request: CreateRaidSignupRequest,
    ): ResponseEntity<RaidSignupResponse> {
        val created = signupService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @Operation(summary = "Update a signup", description = "Updates an existing raid signup")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Signup updated successfully"),
        ApiResponse(responseCode = "404", description = "Signup not found"),
    )
    @PutMapping("/{id}")
    fun update(
        @Parameter(description = "Signup ID")
        @PathVariable id: Long,
        @RequestBody request: UpdateRaidSignupRequest,
    ): RaidSignupResponse = signupService.update(id, request)

    @Operation(summary = "Delete a signup", description = "Deletes a signup by its ID")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Signup deleted successfully"),
        ApiResponse(responseCode = "404", description = "Signup not found"),
    )
    @DeleteMapping("/{id}")
    fun delete(
        @Parameter(description = "Signup ID")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        signupService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Check if signup exists", description = "Returns whether a signup with the given ID exists")
    @GetMapping("/{id}/exists")
    fun exists(
        @Parameter(description = "Signup ID")
        @PathVariable id: Long,
    ): ExistsResponse = ExistsResponse(exists = signupService.existsById(id))

    @Operation(summary = "Get signups by raid", description = "Returns paginated signups for a specific raid")
    @GetMapping("/raid/{raidId}")
    fun findByRaid(
        @Parameter(description = "Raid ID")
        @PathVariable raidId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidSignupResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return signupService.findByRaid(raidId, pageRequest)
    }

    @Operation(summary = "Get selected signups by raid", description = "Returns paginated selected signups for a specific raid")
    @GetMapping("/raid/{raidId}/selected")
    fun findSelectedByRaid(
        @Parameter(description = "Raid ID")
        @PathVariable raidId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidSignupResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return signupService.findSelectedByRaid(raidId, pageRequest)
    }

    @Operation(summary = "Get signups by character", description = "Returns paginated signups for a specific character")
    @GetMapping("/character/{characterId}")
    fun findByCharacter(
        @Parameter(description = "Character ID")
        @PathVariable characterId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaidSignupResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return signupService.findByCharacter(characterId, pageRequest)
    }

    @Operation(summary = "Count signups by raid", description = "Returns the count of signups for a raid")
    @GetMapping("/raid/{raidId}/count")
    fun countByRaid(
        @Parameter(description = "Raid ID")
        @PathVariable raidId: Long,
    ): CountResponse = CountResponse(count = signupService.countByRaid(raidId))
}
