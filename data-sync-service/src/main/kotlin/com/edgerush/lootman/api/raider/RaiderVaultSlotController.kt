package com.edgerush.lootman.api.raider

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
 * REST controller for RaiderVaultSlot CRUD operations.
 *
 * Provides endpoints for managing raider vault slots.
 */
@RestController
@RequestMapping("/api/raider-vault-slots")
@Tag(name = "RaiderVaultSlot", description = "Raider vault slot management endpoints")
class RaiderVaultSlotController(
    private val vaultSlotService: RaiderVaultSlotCrudService,
    private val paginationProperties: PaginationProperties,
) {

    @GetMapping
    @Operation(summary = "Find all vault slots with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderVaultSlotResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return vaultSlotService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find vault slot by ID")
    fun findById(
        @Parameter(description = "Vault slot ID")
        @PathVariable id: Long,
    ): RaiderVaultSlotResponse {
        return vaultSlotService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create a new vault slot")
    fun create(
        @Valid @RequestBody request: CreateRaiderVaultSlotRequest,
    ): ResponseEntity<RaiderVaultSlotResponse> {
        val created = vaultSlotService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing vault slot")
    fun update(
        @Parameter(description = "Vault slot ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRaiderVaultSlotRequest,
    ): RaiderVaultSlotResponse {
        return vaultSlotService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vault slot")
    fun delete(
        @Parameter(description = "Vault slot ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        vaultSlotService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if vault slot exists")
    fun exists(
        @Parameter(description = "Vault slot ID")
        @PathVariable id: Long,
    ): RaiderVaultSlotExistsResponse {
        return RaiderVaultSlotExistsResponse(exists = vaultSlotService.existsById(id))
    }

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find vault slots by raider with pagination")
    fun findByRaider(
        @Parameter(description = "Raider ID")
        @PathVariable raiderId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderVaultSlotResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return vaultSlotService.findByRaider(raiderId, pageRequest)
    }

    @GetMapping("/raider/{raiderId}/unlocked")
    @Operation(summary = "Find unlocked vault slots by raider with pagination")
    fun findUnlockedByRaider(
        @Parameter(description = "Raider ID")
        @PathVariable raiderId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderVaultSlotResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return vaultSlotService.findUnlockedByRaider(raiderId, pageRequest)
    }

    @GetMapping("/raider/{raiderId}/count")
    @Operation(summary = "Count vault slots for a raider")
    fun countByRaider(
        @Parameter(description = "Raider ID")
        @PathVariable raiderId: Long,
    ): RaiderVaultSlotCountResponse {
        return RaiderVaultSlotCountResponse(count = vaultSlotService.countByRaider(raiderId))
    }
}
