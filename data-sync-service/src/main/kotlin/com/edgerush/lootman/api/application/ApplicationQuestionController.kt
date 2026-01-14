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

@RestController @RequestMapping("/api/application-questions") @Tag(name = "ApplicationQuestion", description = "Application question CRUD endpoints")
class ApplicationQuestionController(private val service: ApplicationQuestionCrudService, private val paginationProperties: PaginationProperties) {
    @GetMapping @Operation(summary = "Find all application questions") fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<ApplicationQuestionResponse> = service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
    @GetMapping("/{id}") @Operation(summary = "Find by ID") fun findById(@PathVariable id: Long) = service.findById(id)
    @PostMapping @Operation(summary = "Create") fun create(@Valid @RequestBody request: CreateApplicationQuestionRequest): ResponseEntity<ApplicationQuestionResponse> = ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))
    @PutMapping("/{id}") @Operation(summary = "Update") fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateApplicationQuestionRequest) = service.update(id, request)
    @DeleteMapping("/{id}") @Operation(summary = "Delete") fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }
    @GetMapping("/{id}/exists") @Operation(summary = "Check exists") fun exists(@PathVariable id: Long) = ApplicationQuestionExistsResponse(service.existsById(id))
    @GetMapping("/application/{applicationId}") @Operation(summary = "Find by application") fun findByApplicationId(@PathVariable applicationId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?) = service.findByApplicationId(applicationId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
