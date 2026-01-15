package com.edgerush.lootman.api.statistics

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
@RequestMapping("/api/raider-statistics")
@Tag(name = "RaiderStatistics", description = "Raider statistics CRUD endpoints")
class RaiderStatisticsController(private val service: RaiderStatisticsCrudService, private val paginationProperties: PaginationProperties) {
    @GetMapping
    @Operation(summary = "Find all raider statistics")
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderStatisticsResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find raider statistics by ID")
    fun findById(
        @PathVariable id: Long,
    ): RaiderStatisticsResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create raider statistics")
    fun create(
        @Valid @RequestBody request: CreateRaiderStatisticsRequest,
    ): ResponseEntity<RaiderStatisticsResponse> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update raider statistics")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateRaiderStatisticsRequest,
    ): RaiderStatisticsResponse = service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete raider statistics")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if raider statistics exists")
    fun exists(
        @PathVariable id: Long,
    ): RaiderStatisticsExistsResponse = RaiderStatisticsExistsResponse(service.existsById(id))

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find raider statistics by raider ID")
    fun findByRaiderId(
        @PathVariable raiderId: Long,
    ): RaiderStatisticsResponse = service.findByRaiderId(raiderId)
}
