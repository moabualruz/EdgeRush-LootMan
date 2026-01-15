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
@RequestMapping("/api/raider-crest-counts")
@Tag(name = "RaiderCrestCount", description = "Raider crest count CRUD endpoints")
class RaiderCrestCountController(private val service: RaiderCrestCountCrudService, private val paginationProperties: PaginationProperties) {
    @GetMapping
    @Operation(summary = "Find all raider crest counts")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderCrestCountResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find raider crest count by ID")
    fun findById(
        @PathVariable id: Long,
    ): RaiderCrestCountResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a raider crest count")
    fun create(
        @Valid @RequestBody request: CreateRaiderCrestCountRequest,
    ): ResponseEntity<RaiderCrestCountResponse> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a raider crest count")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRaiderCrestCountRequest,
    ): RaiderCrestCountResponse = service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a raider crest count")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if raider crest count exists")
    fun exists(
        @PathVariable id: Long,
    ): RaiderCrestCountExistsResponse = RaiderCrestCountExistsResponse(service.existsById(id))

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find raider crest counts by raider ID")
    fun findByRaiderId(
        @PathVariable raiderId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderCrestCountResponse> =
        service.findByRaiderId(
            raiderId,
            PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize),
        )
}
