package com.edgerush.lootman.api.gear

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
@RequestMapping("/api/raider-gear-items")
@Tag(name = "RaiderGearItem", description = "Raider gear item CRUD endpoints")
class RaiderGearItemController(private val service: RaiderGearItemCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all raider gear items")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<RaiderGearItemResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find raider gear item by ID")
    fun findById(@PathVariable id: Long): RaiderGearItemResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a raider gear item")
    fun create(@Valid @RequestBody request: CreateRaiderGearItemRequest): ResponseEntity<RaiderGearItemResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a raider gear item")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateRaiderGearItemRequest): RaiderGearItemResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a raider gear item")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if raider gear item exists")
    fun exists(@PathVariable id: Long): RaiderGearItemExistsResponse = RaiderGearItemExistsResponse(service.existsById(id))

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find raider gear items by raider ID")
    fun findByRaiderId(@PathVariable raiderId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<RaiderGearItemResponse> =
        service.findByRaiderId(raiderId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/raider/{raiderId}/gear-set/{gearSet}")
    @Operation(summary = "Find raider gear items by raider ID and gear set")
    fun findByRaiderIdAndGearSet(@PathVariable raiderId: Long, @PathVariable gearSet: String, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<RaiderGearItemResponse> =
        service.findByRaiderIdAndGearSet(raiderId, gearSet, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
