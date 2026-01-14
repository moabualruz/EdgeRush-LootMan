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
@RequestMapping("/api/application-question-files")
@Tag(name = "ApplicationQuestionFile", description = "Application question file CRUD endpoints")
class ApplicationQuestionFileController(private val service: ApplicationQuestionFileCrudService, private val paginationProperties: PaginationProperties) {

    @GetMapping
    @Operation(summary = "Find all application question files")
    fun findAll(@RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<ApplicationQuestionFileResponse> =
        service.findAll(PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))

    @GetMapping("/{id}")
    @Operation(summary = "Find application question file by ID")
    fun findById(@PathVariable id: Long): ApplicationQuestionFileResponse = service.findById(id)

    @PostMapping
    @Operation(summary = "Create an application question file")
    fun create(@Valid @RequestBody request: CreateApplicationQuestionFileRequest): ResponseEntity<ApplicationQuestionFileResponse> =
        ResponseEntity.status(HttpStatus.CREATED).body(service.create(request))

    @PutMapping("/{id}")
    @Operation(summary = "Update an application question file")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateApplicationQuestionFileRequest): ApplicationQuestionFileResponse =
        service.update(id, request)

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an application question file")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> { service.delete(id); return ResponseEntity.noContent().build() }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if application question file exists")
    fun exists(@PathVariable id: Long): ApplicationQuestionFileExistsResponse = ApplicationQuestionFileExistsResponse(service.existsById(id))

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Find application question files by application ID")
    fun findByApplicationId(@PathVariable applicationId: Long, @RequestParam(defaultValue = "0") page: Int, @RequestParam(required = false) size: Int?): PagedResponse<ApplicationQuestionFileResponse> =
        service.findByApplicationId(applicationId, PageRequest.withDefaults(page, size, paginationProperties.defaultPageSize, paginationProperties.maxPageSize))
}
