package com.edgerush.lootman.api.behavioral

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
 * REST controller for BehavioralAction CRUD operations.
 *
 * Provides endpoints for managing behavioral actions (deductions/restorations).
 */
@RestController
@RequestMapping("/api/behavioral-actions")
@Tag(name = "BehavioralAction", description = "Behavioral action management endpoints")
class BehavioralActionController(
    private val behavioralActionService: BehavioralActionCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all behavioral actions with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<BehavioralActionResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return behavioralActionService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find behavioral action by ID")
    fun findById(
        @Parameter(description = "Behavioral action ID")
        @PathVariable id: Long,
    ): BehavioralActionResponse {
        return behavioralActionService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create a new behavioral action")
    fun create(
        @Valid @RequestBody request: CreateBehavioralActionRequest,
    ): ResponseEntity<BehavioralActionResponse> {
        val created = behavioralActionService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing behavioral action")
    fun update(
        @Parameter(description = "Behavioral action ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateBehavioralActionRequest,
    ): BehavioralActionResponse {
        return behavioralActionService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a behavioral action")
    fun delete(
        @Parameter(description = "Behavioral action ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        behavioralActionService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if behavioral action exists")
    fun exists(
        @Parameter(description = "Behavioral action ID")
        @PathVariable id: Long,
    ): BehavioralActionExistsResponse {
        return BehavioralActionExistsResponse(exists = behavioralActionService.existsById(id))
    }

    @GetMapping("/guild/{guildId}")
    @Operation(summary = "Find behavioral actions by guild with pagination")
    fun findByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<BehavioralActionResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return behavioralActionService.findByGuild(guildId, pageRequest)
    }

    @GetMapping("/guild/{guildId}/active")
    @Operation(summary = "Find active behavioral actions by guild with pagination")
    fun findActiveByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<BehavioralActionResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return behavioralActionService.findActiveByGuild(guildId, pageRequest)
    }

    @GetMapping("/guild/{guildId}/character/{characterName}")
    @Operation(summary = "Find behavioral actions for a character")
    fun findByCharacter(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Character name")
        @PathVariable characterName: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<BehavioralActionResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return behavioralActionService.findByCharacter(guildId, characterName, pageRequest)
    }

    @GetMapping("/guild/{guildId}/character/{characterName}/total-deduction")
    @Operation(summary = "Get total deduction amount for a character")
    fun getTotalDeduction(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Character name")
        @PathVariable characterName: String,
    ): TotalDeductionResponse {
        return TotalDeductionResponse(totalDeduction = behavioralActionService.getTotalDeduction(guildId, characterName))
    }

    @GetMapping("/guild/{guildId}/count")
    @Operation(summary = "Count behavioral actions for a guild")
    fun countByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): BehavioralActionCountResponse {
        return BehavioralActionCountResponse(count = behavioralActionService.countByGuild(guildId))
    }
}
