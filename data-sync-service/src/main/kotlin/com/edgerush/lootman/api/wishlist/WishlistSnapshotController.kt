package com.edgerush.lootman.api.wishlist

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
@RequestMapping("/api/wishlist-snapshots")
@Tag(name = "WishlistSnapshot", description = "Wishlist snapshot CRUD endpoints")
class WishlistSnapshotController(private val service: WishlistSnapshotCrudService, private val paginationProperties: PaginationProperties) {
    @GetMapping
    @Operation(summary = "Find all wishlist snapshots")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<WishlistSnapshotResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find wishlist snapshot by ID")
    fun findById(
        @PathVariable id: Long,
    ): WishlistSnapshotResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a wishlist snapshot")
    fun create(
        @Valid @RequestBody request: CreateWishlistSnapshotRequest,
    ): ResponseEntity<WishlistSnapshotResponse> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a wishlist snapshot")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateWishlistSnapshotRequest,
    ): WishlistSnapshotResponse = service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a wishlist snapshot")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if wishlist snapshot exists")
    fun exists(
        @PathVariable id: Long,
    ): WishlistSnapshotExistsResponse = WishlistSnapshotExistsResponse(service.existsById(id))

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find wishlist snapshots by raider ID")
    fun findByRaiderId(
        @PathVariable raiderId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<WishlistSnapshotResponse> =
        service.findByRaiderId(
            raiderId,
            PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize),
        )

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Find wishlist snapshots by team ID")
    fun findByTeamId(
        @PathVariable teamId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<WishlistSnapshotResponse> =
        service.findByTeamId(
            teamId,
            PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize),
        )
}
