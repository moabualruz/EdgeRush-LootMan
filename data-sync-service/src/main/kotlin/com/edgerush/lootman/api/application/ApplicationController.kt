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
@RequestMapping("/api/applications")
@Tag(name = "Application", description = "Guild application CRUD endpoints")
class ApplicationController(private val service: ApplicationCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all applications")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<ApplicationResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find application by ID")
    fun findById(@PathVariable id: Long): ApplicationResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create an application")
    fun create(@Valid @RequestBody request: CreateApplicationRequest): ResponseEntity<ApplicationResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update an application")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateApplicationRequest): ApplicationResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an application")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if application exists")
    fun exists(@PathVariable id: Long): ApplicationExistsResponse = ApplicationExistsResponse(service.existsById(id))

    @GetMapping("/status/{status}")
    @Operation(summary = "Find applications by status")
    fun findByStatus(@PathVariable status: String, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<ApplicationResponse> =
        service.findByStatus(status, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
