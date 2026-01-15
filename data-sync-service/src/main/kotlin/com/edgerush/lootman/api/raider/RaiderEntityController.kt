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
@RequestMapping("/api/raider-entities")
@Tag(name = "RaiderEntity", description = "Raider entity CRUD endpoints")
class RaiderEntityController(
    private val raiderEntityService: RaiderEntityCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all raiders with pagination")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderEntityResponse> {
        val pageRequest = PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize)
        return raiderEntityService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find raider by ID")
    fun findById(
        @PathVariable id: Long,
    ): RaiderEntityResponse = raiderEntityService.findById(id)

    @PostMapping
    @Operation(summary = "Create a new raider")
    fun create(
        @Valid @RequestBody request: CreateRaiderEntityRequest,
    ): ResponseEntity<RaiderEntityResponse> = ResponseEntity.status(HttpStatus.CREATED).body(raiderEntityService.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing raider")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRaiderEntityRequest,
    ): RaiderEntityResponse = raiderEntityService.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a raider")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        raiderEntityService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if raider exists")
    fun exists(
        @PathVariable id: Long,
    ): RaiderEntityExistsResponse = RaiderEntityExistsResponse(raiderEntityService.existsById(id))

    @GetMapping("/realm/{realm}")
    @Operation(summary = "Find raiders by realm")
    fun findByRealm(
        @PathVariable realm: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderEntityResponse> {
        val pageRequest = PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize)
        return raiderEntityService.findByRealm(realm, pageRequest)
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "Find raiders by region")
    fun findByRegion(
        @PathVariable region: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderEntityResponse> {
        val pageRequest = PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize)
        return raiderEntityService.findByRegion(region, pageRequest)
    }

    @GetMapping("/realm/{realm}/count")
    @Operation(summary = "Count raiders for a realm")
    fun countByRealm(
        @PathVariable realm: String,
    ): RaiderEntityCountResponse = RaiderEntityCountResponse(raiderEntityService.countByRealm(realm))
}
