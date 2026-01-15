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
 * REST controller for LootBan CRUD operations.
 *
 * Provides endpoints for managing loot bans.
 */
@RestController
@RequestMapping("/api/loot-bans")
@Tag(name = "LootBan", description = "Loot ban management endpoints")
class LootBanController(
    private val lootBanService: LootBanCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all loot bans with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootBanResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return lootBanService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find loot ban by ID")
    fun findById(
        @Parameter(description = "Loot ban ID")
        @PathVariable id: Long,
    ): LootBanResponse {
        return lootBanService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create a new loot ban")
    fun create(
        @Valid @RequestBody request: CreateLootBanEntityRequest,
    ): ResponseEntity<LootBanResponse> {
        val created = lootBanService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing loot ban")
    fun update(
        @Parameter(description = "Loot ban ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateLootBanEntityRequest,
    ): LootBanResponse {
        return lootBanService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loot ban")
    fun delete(
        @Parameter(description = "Loot ban ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        lootBanService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if loot ban exists")
    fun exists(
        @Parameter(description = "Loot ban ID")
        @PathVariable id: Long,
    ): LootBanExistsResponse {
        return LootBanExistsResponse(exists = lootBanService.existsById(id))
    }

    @GetMapping("/guild/{guildId}")
    @Operation(summary = "Find loot bans by guild with pagination")
    fun findByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootBanResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return lootBanService.findByGuild(guildId, pageRequest)
    }

    @GetMapping("/guild/{guildId}/active")
    @Operation(summary = "Find active loot bans by guild with pagination")
    fun findActiveByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<LootBanResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return lootBanService.findActiveByGuild(guildId, pageRequest)
    }

    @GetMapping("/guild/{guildId}/check/{characterName}")
    @Operation(summary = "Check if a character is banned from loot")
    fun checkBan(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Character name")
        @PathVariable characterName: String,
    ): BannedResponse {
        return BannedResponse(banned = lootBanService.isCharacterBanned(guildId, characterName))
    }

    @GetMapping("/guild/{guildId}/count")
    @Operation(summary = "Count loot bans for a guild")
    fun countByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): LootBanCountResponse {
        return LootBanCountResponse(count = lootBanService.countByGuild(guildId))
    }
}
