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
 * REST controller for TeamMetadata entity CRUD operations.
 *
 * Provides endpoints for managing team metadata.
 */
@RestController
@RequestMapping("/api/team-metadata")
@Tag(name = "TeamMetadata", description = "Team metadata management endpoints")
class TeamMetadataController(
    private val teamMetadataService: TeamMetadataCrudService,
    private val paginationProperties: PaginationProperties,
) {

    @GetMapping
    @Operation(summary = "Find all team metadata with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<TeamMetadataResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return teamMetadataService.findAll(pageRequest)
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Find team metadata by team ID")
    fun findById(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
    ): TeamMetadataResponse {
        return teamMetadataService.findById(teamId)
    }

    @PostMapping
    @Operation(summary = "Create new team metadata")
    fun create(
        @Valid @RequestBody request: CreateTeamMetadataRequest,
    ): ResponseEntity<TeamMetadataResponse> {
        val created = teamMetadataService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{teamId}")
    @Operation(summary = "Update existing team metadata")
    fun update(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
        @Valid @RequestBody request: UpdateTeamMetadataRequest,
    ): TeamMetadataResponse {
        return teamMetadataService.update(teamId, request)
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Delete team metadata")
    fun delete(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
    ): ResponseEntity<Unit> {
        teamMetadataService.delete(teamId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{teamId}/exists")
    @Operation(summary = "Check if team metadata exists")
    fun exists(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
    ): TeamMetadataExistsResponse {
        return TeamMetadataExistsResponse(exists = teamMetadataService.existsById(teamId))
    }

    @GetMapping("/guild/{guildId}")
    @Operation(summary = "Find team metadata by guild with pagination")
    fun findByGuildId(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<TeamMetadataResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return teamMetadataService.findByGuildId(guildId, pageRequest)
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "Find team metadata by region with pagination")
    fun findByRegion(
        @Parameter(description = "Region (e.g., EU, US, KR)")
        @PathVariable region: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<TeamMetadataResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return teamMetadataService.findByRegion(region, pageRequest)
    }

    @GetMapping("/guild/{guildId}/count")
    @Operation(summary = "Count team metadata for a guild")
    fun countByGuildId(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: Long,
    ): TeamMetadataCountResponse {
        return TeamMetadataCountResponse(count = teamMetadataService.countByGuildId(guildId))
    }
}
