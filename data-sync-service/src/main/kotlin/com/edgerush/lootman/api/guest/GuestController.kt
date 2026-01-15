package com.edgerush.lootman.api.guest

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
@RequestMapping("/api/guests")
@Tag(name = "Guest", description = "Guest CRUD endpoints")
class GuestController(private val guestService: GuestCrudService, private val paginationProperties: PaginationProperties) {
    @GetMapping
    @Operation(summary = "Find all guests")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<GuestResponse> =
        guestService.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find guest by ID")
    fun findById(
        @PathVariable id: Long,
    ): GuestResponse = guestService.findById(id)

    @PostMapping
    @Operation(summary = "Create a guest")
    fun create(
        @Valid @RequestBody request: CreateGuestRequest,
    ): ResponseEntity<GuestResponse> = ResponseEntity.status(HttpStatus.CREATED).body(guestService.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a guest")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateGuestRequest,
    ): GuestResponse = guestService.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a guest")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        guestService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if guest exists")
    fun exists(
        @PathVariable id: Long,
    ): GuestExistsResponse = GuestExistsResponse(guestService.existsById(id))
}
