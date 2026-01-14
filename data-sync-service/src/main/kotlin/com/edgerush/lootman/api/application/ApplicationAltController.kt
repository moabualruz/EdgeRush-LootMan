package com.edgerush.lootman.api.application

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
@RequestMapping("/api/application-alts")
@Tag(name = "ApplicationAlt", description = "Application alt character CRUD endpoints")
class ApplicationAltController(private val service: ApplicationAltCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all application alts")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<ApplicationAltResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find application alt by ID")
    fun findById(@PathVariable id: Long): ApplicationAltResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create an application alt")
    fun create(@Valid @RequestBody request: CreateApplicationAltRequest): ResponseEntity<ApplicationAltResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update an application alt")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateApplicationAltRequest): ApplicationAltResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an application alt")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if application alt exists")
    fun exists(@PathVariable id: Long): ApplicationAltExistsResponse = ApplicationAltExistsResponse(service.existsById(id))

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Find alts by application ID")
    fun findByApplicationId(@PathVariable applicationId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<ApplicationAltResponse> =
        service.findByApplicationId(applicationId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
