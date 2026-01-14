package com.edgerush.lootman.api.loot

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
@RequestMapping("/api/loot-award-old-items")
@Tag(name = "LootAwardOldItem", description = "Loot award old item CRUD endpoints")
class LootAwardOldItemController(private val service: LootAwardOldItemCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all loot award old items")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<LootAwardOldItemResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find loot award old item by ID")
    fun findById(@PathVariable id: Long): LootAwardOldItemResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a loot award old item")
    fun create(@Valid @RequestBody request: CreateLootAwardOldItemRequest): ResponseEntity<LootAwardOldItemResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a loot award old item")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateLootAwardOldItemRequest): LootAwardOldItemResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loot award old item")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if loot award old item exists")
    fun exists(@PathVariable id: Long): LootAwardOldItemExistsResponse = LootAwardOldItemExistsResponse(service.existsById(id))

    @GetMapping("/loot-award/{lootAwardId}")
    @Operation(summary = "Find loot award old items by loot award ID")
    fun findByLootAwardId(@PathVariable lootAwardId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<LootAwardOldItemResponse> =
        service.findByLootAwardId(lootAwardId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
