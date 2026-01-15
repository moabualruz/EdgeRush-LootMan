package com.edgerush.lootman.api.raider

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
@RequestMapping("/api/raider-renown")
@Tag(name = "RaiderRenown", description = "Raider renown CRUD endpoints")
class RaiderRenownController(private val service: RaiderRenownCrudService, private val paginationProperties: PaginationProperties) {
    @GetMapping
    @Operation(summary = "Find all raider renown")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderRenownResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find raider renown by ID")
    fun findById(
        @PathVariable id: Long,
    ): RaiderRenownResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create raider renown")
    fun create(
        @Valid @RequestBody request: CreateRaiderRenownRequest,
    ): ResponseEntity<RaiderRenownResponse> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update raider renown")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRaiderRenownRequest,
    ): RaiderRenownResponse = service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete raider renown")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if raider renown exists")
    fun exists(
        @PathVariable id: Long,
    ): RaiderRenownExistsResponse = RaiderRenownExistsResponse(service.existsById(id))

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find raider renown by raider ID")
    fun findByRaiderId(
        @PathVariable raiderId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderRenownResponse> =
        service.findByRaiderId(
            raiderId,
            PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize),
        )
}
