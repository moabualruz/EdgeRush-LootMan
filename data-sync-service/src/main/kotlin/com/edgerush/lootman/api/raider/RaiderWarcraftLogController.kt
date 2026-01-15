package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/raider-warcraft-logs")
@Tag(name = "RaiderWarcraftLog", description = "Raider Warcraft Logs score CRUD endpoints")
class RaiderWarcraftLogController(
    private val service: RaiderWarcraftLogCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all raider Warcraft Logs scores")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderWarcraftLogResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find raider Warcraft Logs score by ID")
    fun findById(
        @PathVariable id: Long,
    ): RaiderWarcraftLogResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a raider Warcraft Logs score")
    fun create(
        @Valid @RequestBody request: CreateRaiderWarcraftLogRequest,
    ): ResponseEntity<RaiderWarcraftLogResponse> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a raider Warcraft Logs score")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRaiderWarcraftLogRequest,
    ): RaiderWarcraftLogResponse = service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a raider Warcraft Logs score")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if raider Warcraft Logs score exists")
    fun exists(
        @PathVariable id: Long,
    ): RaiderWarcraftLogExistsResponse = RaiderWarcraftLogExistsResponse(service.existsById(id))

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find raider Warcraft Logs scores by raider ID")
    fun findByRaiderId(
        @PathVariable raiderId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderWarcraftLogResponse> =
        service.findByRaiderId(
            raiderId,
            PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize),
        )
}
