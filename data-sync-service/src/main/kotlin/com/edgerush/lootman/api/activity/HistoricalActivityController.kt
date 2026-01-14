package com.edgerush.lootman.api.activity

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
@RequestMapping("/api/historical-activities")
@Tag(name = "HistoricalActivity", description = "Historical activity CRUD endpoints")
class HistoricalActivityController(private val service: HistoricalActivityCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all historical activities")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<HistoricalActivityResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find historical activity by ID")
    fun findById(@PathVariable id: Long): HistoricalActivityResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create a historical activity")
    fun create(@Valid @RequestBody request: CreateHistoricalActivityRequest): ResponseEntity<HistoricalActivityResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update a historical activity")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateHistoricalActivityRequest): HistoricalActivityResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a historical activity")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if historical activity exists")
    fun exists(@PathVariable id: Long): HistoricalActivityExistsResponse = HistoricalActivityExistsResponse(service.existsById(id))

    @GetMapping("/character/{characterId}")
    @Operation(summary = "Find historical activities by character ID")
    fun findByCharacterId(@PathVariable characterId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<HistoricalActivityResponse> =
        service.findByCharacterId(characterId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/team/{teamId}")
    @Operation(summary = "Find historical activities by team ID")
    fun findByTeamId(@PathVariable teamId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<HistoricalActivityResponse> =
        service.findByTeamId(teamId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
