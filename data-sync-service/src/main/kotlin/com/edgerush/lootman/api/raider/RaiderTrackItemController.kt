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
@RequestMapping("/api/raider-track-items")
@Tag(name = "RaiderTrackItem", description = "Raider track item CRUD endpoints")
class RaiderTrackItemController(private val service: RaiderTrackItemCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all raider track items")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<RaiderTrackItemResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find raider track item by ID")
    fun findById(@PathVariable id: Long): RaiderTrackItemResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a raider track item")
    fun create(@Valid @RequestBody request: CreateRaiderTrackItemRequest): ResponseEntity<RaiderTrackItemResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a raider track item")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateRaiderTrackItemRequest): RaiderTrackItemResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a raider track item")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if raider track item exists")
    fun exists(@PathVariable id: Long): RaiderTrackItemExistsResponse = RaiderTrackItemExistsResponse(service.existsById(id))

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find raider track items by raider ID")
    fun findByRaiderId(@PathVariable raiderId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<RaiderTrackItemResponse> =
        service.findByRaiderId(raiderId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
