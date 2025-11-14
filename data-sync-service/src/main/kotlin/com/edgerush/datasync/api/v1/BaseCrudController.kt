package com.edgerush.datasync.api.v1

import com.edgerush.datasync.api.dto.response.PagedResponse
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.crud.CrudService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

abstract class BaseCrudController<T : Any, ID : Any, CreateReq : Any, UpdateReq : Any, Resp : Any>(
    protected val service: CrudService<T, ID, CreateReq, UpdateReq, Resp>,
) {
    @GetMapping
    @Operation(
        summary = "List all entities with pagination",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
        ],
    )
    open fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "Sort field and direction (e.g., 'name,asc')")
        @RequestParam(required = false) sort: String?,
    ): ResponseEntity<PagedResponse<Resp>> {
        val pageable = createPageable(page, size, sort)
        val result = service.findAll(pageable)

        val response =
            PagedResponse(
                content = result.content,
                page = result.number,
                size = result.size,
                totalElements = result.totalElements,
                totalPages = result.totalPages,
                isFirst = result.isFirst,
                isLast = result.isLast,
            )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get entity by ID",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved entity"),
            ApiResponse(responseCode = "404", description = "Entity not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
        ],
    )
    open fun findById(
        @Parameter(description = "Entity ID")
        @PathVariable id: ID,
    ): ResponseEntity<Resp> {
        val result = service.findById(id)
        return ResponseEntity.ok(result)
    }

    @PostMapping
    @Operation(
        summary = "Create new entity",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Successfully created entity"),
            ApiResponse(responseCode = "400", description = "Invalid input"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
        ],
    )
    open fun create(
        @Valid @RequestBody request: CreateReq,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Resp> {
        val result = service.create(request, user)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update existing entity",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Successfully updated entity"),
            ApiResponse(responseCode = "400", description = "Invalid input"),
            ApiResponse(responseCode = "404", description = "Entity not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
        ],
    )
    open fun update(
        @Parameter(description = "Entity ID")
        @PathVariable id: ID,
        @Valid @RequestBody request: UpdateReq,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Resp> {
        val result = service.update(id, request, user)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete entity",
        security = [SecurityRequirement(name = "bearerAuth")],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Successfully deleted entity"),
            ApiResponse(responseCode = "404", description = "Entity not found"),
            ApiResponse(responseCode = "401", description = "Unauthorized"),
            ApiResponse(responseCode = "403", description = "Forbidden"),
        ],
    )
    open fun delete(
        @Parameter(description = "Entity ID")
        @PathVariable id: ID,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): ResponseEntity<Void> {
        service.delete(id, user)
        return ResponseEntity.noContent().build()
    }

    protected fun createPageable(
        page: Int,
        size: Int,
        sort: String?,
    ): PageRequest {
        val validatedSize = size.coerceIn(1, 100)

        return if (sort != null) {
            val parts = sort.split(",")
            val field = parts[0]
            val direction =
                if (parts.size > 1 && parts[1].equals("desc", ignoreCase = true)) {
                    Sort.Direction.DESC
                } else {
                    Sort.Direction.ASC
                }
            PageRequest.of(page, validatedSize, Sort.by(direction, field))
        } else {
            PageRequest.of(page, validatedSize)
        }
    }
}
