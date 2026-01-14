package com.edgerush.lootman.api.snapshot

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
@RequestMapping("/api/period-snapshots")
@Tag(name = "PeriodSnapshot", description = "Period snapshot CRUD endpoints")
class PeriodSnapshotController(private val service: PeriodSnapshotCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all period snapshots")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<PeriodSnapshotResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find period snapshot by ID")
    fun findById(@PathVariable id: Long): PeriodSnapshotResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a period snapshot")
    fun create(@Valid @RequestBody request: CreatePeriodSnapshotRequest): ResponseEntity<PeriodSnapshotResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a period snapshot")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdatePeriodSnapshotRequest): PeriodSnapshotResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a period snapshot")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if period snapshot exists")
    fun exists(@PathVariable id: Long): PeriodSnapshotExistsResponse = PeriodSnapshotExistsResponse(service.existsById(id))

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Find period snapshots by team ID")
    fun findByTeamId(@PathVariable teamId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<PeriodSnapshotResponse> =
        service.findByTeamId(teamId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
