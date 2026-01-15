package com.edgerush.lootman.api.character

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
 * REST controller for CharacterHistory entity CRUD operations.
 *
 * Provides endpoints for managing character history snapshots.
 */
@RestController
@RequestMapping("/api/character-history")
@Tag(name = "CharacterHistory", description = "Character history management endpoints")
class CharacterHistoryController(
    private val characterHistoryService: CharacterHistoryCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all character history with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<CharacterHistoryResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return characterHistoryService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find character history by ID")
    fun findById(
        @Parameter(description = "Character history ID")
        @PathVariable id: Long,
    ): CharacterHistoryResponse {
        return characterHistoryService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create new character history")
    fun create(
        @Valid @RequestBody request: CreateCharacterHistoryRequest,
    ): ResponseEntity<CharacterHistoryResponse> {
        val created = characterHistoryService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing character history")
    fun update(
        @Parameter(description = "Character history ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCharacterHistoryRequest,
    ): CharacterHistoryResponse {
        return characterHistoryService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete character history")
    fun delete(
        @Parameter(description = "Character history ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        characterHistoryService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if character history exists")
    fun exists(
        @Parameter(description = "Character history ID")
        @PathVariable id: Long,
    ): CharacterHistoryExistsResponse {
        return CharacterHistoryExistsResponse(exists = characterHistoryService.existsById(id))
    }

    @GetMapping("/character/{characterId}")
    @Operation(summary = "Find character history by character with pagination")
    fun findByCharacterId(
        @Parameter(description = "Character ID")
        @PathVariable characterId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<CharacterHistoryResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return characterHistoryService.findByCharacterId(characterId, pageRequest)
    }

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Find character history by team with pagination")
    fun findByTeamId(
        @Parameter(description = "Team ID")
        @PathVariable teamId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<CharacterHistoryResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return characterHistoryService.findByTeamId(teamId, pageRequest)
    }

    @GetMapping("/character/{characterId}/count")
    @Operation(summary = "Count character history for a character")
    fun countByCharacterId(
        @Parameter(description = "Character ID")
        @PathVariable characterId: Long,
    ): CharacterHistoryCountResponse {
        return CharacterHistoryCountResponse(count = characterHistoryService.countByCharacterId(characterId))
    }
}
