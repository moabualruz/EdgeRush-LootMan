package com.edgerush.lootman.api.loot

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
 * REST controller for LootAward entity CRUD operations.
 *
 * Provides endpoints for managing loot awards.
 */
@RestController
@RequestMapping("/api/loot-awards")
@Tag(name = "LootAward", description = "Loot award management endpoints")
class LootAwardController(
    private val lootAwardService: LootAwardCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all loot awards with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootAwardEntityResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return lootAwardService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find loot award by ID")
    fun findById(
        @Parameter(description = "Loot award ID")
        @PathVariable id: Long,
    ): LootAwardEntityResponse {
        return lootAwardService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create a new loot award")
    fun create(
        @Valid @RequestBody request: CreateLootAwardEntityRequest,
    ): ResponseEntity<LootAwardEntityResponse> {
        val created = lootAwardService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing loot award")
    fun update(
        @Parameter(description = "Loot award ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateLootAwardEntityRequest,
    ): LootAwardEntityResponse {
        return lootAwardService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loot award")
    fun delete(
        @Parameter(description = "Loot award ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        lootAwardService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if loot award exists")
    fun exists(
        @Parameter(description = "Loot award ID")
        @PathVariable id: Long,
    ): LootAwardExistsResponse {
        return LootAwardExistsResponse(exists = lootAwardService.existsById(id))
    }

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find loot awards by raider with pagination")
    fun findByRaider(
        @Parameter(description = "Raider ID")
        @PathVariable raiderId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootAwardEntityResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return lootAwardService.findByRaider(raiderId, pageRequest)
    }

    @GetMapping("/item/{itemId}")
    @Operation(summary = "Find loot awards by item with pagination")
    fun findByItem(
        @Parameter(description = "Item ID")
        @PathVariable itemId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootAwardEntityResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return lootAwardService.findByItem(itemId, pageRequest)
    }

    @GetMapping("/tier/{tier}")
    @Operation(summary = "Find loot awards by tier with pagination")
    fun findByTier(
        @Parameter(description = "Loot tier")
        @PathVariable tier: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootAwardEntityResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return lootAwardService.findByTier(tier, pageRequest)
    }

    @GetMapping("/raider/{raiderId}/count")
    @Operation(summary = "Count loot awards for a raider")
    fun countByRaider(
        @Parameter(description = "Raider ID")
        @PathVariable raiderId: Long,
    ): LootAwardCountResponse {
        return LootAwardCountResponse(count = lootAwardService.countByRaider(raiderId))
    }
}
