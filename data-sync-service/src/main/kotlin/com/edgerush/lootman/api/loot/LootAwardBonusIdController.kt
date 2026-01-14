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
@RequestMapping("/api/loot-award-bonus-ids")
@Tag(name = "LootAwardBonusId", description = "Loot award bonus ID CRUD endpoints")
class LootAwardBonusIdController(private val service: LootAwardBonusIdCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all loot award bonus IDs")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<LootAwardBonusIdResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find loot award bonus ID by ID")
    fun findById(@PathVariable id: Long): LootAwardBonusIdResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a loot award bonus ID")
    fun create(@Valid @RequestBody request: CreateLootAwardBonusIdRequest): ResponseEntity<LootAwardBonusIdResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a loot award bonus ID")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateLootAwardBonusIdRequest): LootAwardBonusIdResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loot award bonus ID")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if loot award bonus ID exists")
    fun exists(@PathVariable id: Long): LootAwardBonusIdExistsResponse = LootAwardBonusIdExistsResponse(service.existsById(id))

    @GetMapping("/loot-award/{lootAwardId}")
    @Operation(summary = "Find loot award bonus IDs by loot award ID")
    fun findByLootAwardId(@PathVariable lootAwardId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<LootAwardBonusIdResponse> =
        service.findByLootAwardId(lootAwardId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
