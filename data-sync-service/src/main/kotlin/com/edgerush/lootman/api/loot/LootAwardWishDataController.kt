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
@RequestMapping("/api/loot-award-wish-data")
@Tag(name = "LootAwardWishData", description = "Loot award wish data CRUD endpoints")
class LootAwardWishDataController(
    private val service: LootAwardWishDataCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all loot award wish data")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootAwardWishDataResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find loot award wish data by ID")
    fun findById(
        @PathVariable id: Long,
    ): LootAwardWishDataResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create loot award wish data")
    fun create(
        @Valid @RequestBody request: CreateLootAwardWishDataRequest,
    ): ResponseEntity<LootAwardWishDataResponse> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update loot award wish data")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateLootAwardWishDataRequest,
    ): LootAwardWishDataResponse = service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete loot award wish data")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if loot award wish data exists")
    fun exists(
        @PathVariable id: Long,
    ): LootAwardWishDataExistsResponse = LootAwardWishDataExistsResponse(service.existsById(id))

    @GetMapping("/loot-award/{lootAwardId}")
    @Operation(summary = "Find loot award wish data by loot award ID")
    fun findByLootAwardId(
        @PathVariable lootAwardId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootAwardWishDataResponse> =
        service.findByLootAwardId(
            lootAwardId,
            PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize),
        )
}
