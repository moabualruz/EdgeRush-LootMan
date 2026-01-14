package com.edgerush.lootman.api.sync

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
 * REST controller for SyncRun entity CRUD operations.
 *
 * Provides endpoints for managing data sync run records.
 */
@RestController
@RequestMapping("/api/sync-runs")
@Tag(name = "SyncRun", description = "Sync run management endpoints")
class SyncRunController(
    private val syncRunService: SyncRunCrudService,
    private val paginationProperties: PaginationProperties,
) {

    @GetMapping
    @Operation(summary = "Find all sync runs with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<SyncRunResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return syncRunService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find sync run by ID")
    fun findById(
        @Parameter(description = "Sync run ID")
        @PathVariable id: Long,
    ): SyncRunResponse {
        return syncRunService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create a new sync run")
    fun create(
        @Valid @RequestBody request: CreateSyncRunRequest,
    ): ResponseEntity<SyncRunResponse> {
        val created = syncRunService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing sync run")
    fun update(
        @Parameter(description = "Sync run ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateSyncRunRequest,
    ): SyncRunResponse {
        return syncRunService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sync run")
    fun delete(
        @Parameter(description = "Sync run ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        syncRunService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if sync run exists")
    fun exists(
        @Parameter(description = "Sync run ID")
        @PathVariable id: Long,
    ): SyncRunExistsResponse {
        return SyncRunExistsResponse(exists = syncRunService.existsById(id))
    }

    @GetMapping("/source/{source}")
    @Operation(summary = "Find sync runs by source with pagination")
    fun findBySource(
        @Parameter(description = "Sync source (e.g., WoWAudit, WarcraftLogs)")
        @PathVariable source: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<SyncRunResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return syncRunService.findBySource(source, pageRequest)
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Find sync runs by status with pagination")
    fun findByStatus(
        @Parameter(description = "Sync status (e.g., RUNNING, COMPLETED, FAILED)")
        @PathVariable status: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<SyncRunResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return syncRunService.findByStatus(status, pageRequest)
    }

    @GetMapping("/source/{source}/count")
    @Operation(summary = "Count sync runs for a source")
    fun countBySource(
        @Parameter(description = "Sync source")
        @PathVariable source: String,
    ): SyncRunCountResponse {
        return SyncRunCountResponse(count = syncRunService.countBySource(source))
    }
}
