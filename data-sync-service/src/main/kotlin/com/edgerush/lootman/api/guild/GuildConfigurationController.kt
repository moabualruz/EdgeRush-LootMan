package com.edgerush.lootman.api.guild

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
 * REST controller for GuildConfiguration CRUD operations.
 *
 * Provides endpoints for managing guild configurations.
 */
@RestController
@RequestMapping("/api/guild-configurations")
@Tag(name = "GuildConfiguration", description = "Guild configuration management endpoints")
class GuildConfigurationController(
    private val guildConfigurationService: GuildConfigurationCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all guild configurations with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<GuildConfigurationResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return guildConfigurationService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find guild configuration by ID")
    fun findById(
        @Parameter(description = "Guild configuration ID")
        @PathVariable id: Long,
    ): GuildConfigurationResponse {
        return guildConfigurationService.findById(id)
    }

    @GetMapping("/guild/{guildId}")
    @Operation(summary = "Find guild configuration by guild ID")
    fun findByGuildId(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): GuildConfigurationResponse {
        return guildConfigurationService.findByGuildId(guildId)
    }

    @PostMapping
    @Operation(summary = "Create a new guild configuration")
    fun create(
        @Valid @RequestBody request: CreateGuildConfigurationRequest,
    ): ResponseEntity<GuildConfigurationResponse> {
        val created = guildConfigurationService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing guild configuration")
    fun update(
        @Parameter(description = "Guild configuration ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateGuildConfigurationRequest,
    ): GuildConfigurationResponse {
        return guildConfigurationService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a guild configuration")
    fun delete(
        @Parameter(description = "Guild configuration ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        guildConfigurationService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if guild configuration exists")
    fun exists(
        @Parameter(description = "Guild configuration ID")
        @PathVariable id: Long,
    ): GuildConfigurationExistsResponse {
        return GuildConfigurationExistsResponse(exists = guildConfigurationService.existsById(id))
    }

    @GetMapping("/active")
    @Operation(summary = "Find all active guild configurations with pagination")
    fun findActive(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<GuildConfigurationResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return guildConfigurationService.findActive(pageRequest)
    }

    @PutMapping("/{id}/benchmark")
    @Operation(summary = "Update benchmark configuration for a guild")
    fun updateBenchmark(
        @Parameter(description = "Guild configuration ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBenchmarkRequest,
    ): GuildConfigurationResponse {
        return guildConfigurationService.updateBenchmark(id, request)
    }

    @PutMapping("/guild/{guildId}/sync-status")
    @Operation(summary = "Update sync status for a guild")
    fun updateSyncStatus(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Sync status")
        @RequestParam status: String,
        @Parameter(description = "Error message if any")
        @RequestParam(required = false) error: String?,
    ): GuildConfigurationResponse {
        return guildConfigurationService.updateSyncStatus(guildId, status, error)
    }
}
