package com.edgerush.lootman.api.common

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

/**
 * Abstract base controller providing standard CRUD operations.
 *
 * Extend this class to quickly create REST controllers with consistent
 * behavior and OpenAPI documentation.
 *
 * @param ID The type of the entity identifier
 * @param CreateReq The request type for creating entities
 * @param UpdateReq The request type for updating entities
 * @param Resp The response type for entity representation
 */
abstract class BaseCrudController<ID : Any, CreateReq, UpdateReq, Resp>(
    protected val service: CrudService<ID, CreateReq, UpdateReq, Resp>,
    private val paginationProperties: PaginationProperties,
) {
    /**
     * Get all entities with pagination.
     *
     * @param page The page number (0-indexed)
     * @param size The page size
     * @return Paged response of entities
     */
    @GetMapping
    @Operation(summary = "Get all entities", description = "Retrieve all entities with pagination support")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved entities",
            ),
        ],
    )
    open fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0")
        page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false)
        size: Int?,
    ): PagedResponse<Resp> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return service.findAll(pageRequest)
    }

    /**
     * Get an entity by ID.
     *
     * @param id The entity identifier
     * @return The entity response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID", description = "Retrieve a specific entity by its identifier")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Entity found",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Entity not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    open fun findById(
        @Parameter(description = "Entity identifier")
        @PathVariable
        id: ID,
    ): Resp = service.findById(id)

    /**
     * Create a new entity.
     *
     * @param request The creation request
     * @return Response with created entity and 201 status
     */
    @PostMapping
    @Operation(summary = "Create entity", description = "Create a new entity")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Entity created successfully",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    open fun create(
        @Parameter(description = "Entity creation request")
        @Valid
        @RequestBody
        request: CreateReq,
    ): ResponseEntity<Resp> {
        val created = service.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    /**
     * Update an existing entity.
     *
     * @param id The entity identifier
     * @param request The update request
     * @return The updated entity response
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update entity", description = "Update an existing entity")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Entity updated successfully",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request body",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Entity not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    open fun update(
        @Parameter(description = "Entity identifier")
        @PathVariable
        id: ID,
        @Parameter(description = "Entity update request")
        @Valid
        @RequestBody
        request: UpdateReq,
    ): Resp = service.update(id, request)

    /**
     * Delete an entity.
     *
     * @param id The entity identifier
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete entity", description = "Delete an entity by its identifier")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Entity deleted successfully",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Entity not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    open fun delete(
        @Parameter(description = "Entity identifier")
        @PathVariable
        id: ID,
    ): ResponseEntity<Unit> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    /**
     * Check if an entity exists.
     *
     * @param id The entity identifier
     * @return 200 OK with exists flag
     */
    @GetMapping("/{id}/exists")
    @Operation(summary = "Check entity exists", description = "Check if an entity exists by its identifier")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Existence check completed",
            ),
        ],
    )
    open fun exists(
        @Parameter(description = "Entity identifier")
        @PathVariable
        id: ID,
    ): ExistsResponse = ExistsResponse(exists = service.existsById(id))
}

/**
 * Response for existence checks.
 */
data class ExistsResponse(
    val exists: Boolean,
)

/**
 * Abstract base controller for guild-scoped entities.
 *
 * Extends BaseCrudController with additional endpoints for guild-based filtering.
 */
abstract class GuildScopedCrudController<ID : Any, CreateReq, UpdateReq, Resp>(
    private val guildScopedService: GuildScopedCrudService<ID, CreateReq, UpdateReq, Resp>,
    paginationProperties: PaginationProperties,
) : BaseCrudController<ID, CreateReq, UpdateReq, Resp>(guildScopedService, paginationProperties) {
    /**
     * Get all entities for a specific guild with pagination.
     *
     * @param guildId The guild identifier
     * @param page The page number (0-indexed)
     * @param size The page size
     * @return Paged response of entities for the guild
     */
    @GetMapping("/guild/{guildId}")
    @Operation(summary = "Get entities by guild", description = "Retrieve all entities for a specific guild")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved entities",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Guild not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    open fun findByGuild(
        @Parameter(description = "Guild identifier")
        @PathVariable
        guildId: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0")
        page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false)
        size: Int?,
        paginationProperties: PaginationProperties,
    ): PagedResponse<Resp> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return guildScopedService.findByGuild(guildId, pageRequest)
    }

    /**
     * Get count of entities for a specific guild.
     *
     * @param guildId The guild identifier
     * @return Count response
     */
    @GetMapping("/guild/{guildId}/count")
    @Operation(summary = "Count entities by guild", description = "Get count of entities for a specific guild")
    open fun countByGuild(
        @Parameter(description = "Guild identifier")
        @PathVariable
        guildId: String,
    ): CountResponse = CountResponse(count = guildScopedService.countByGuild(guildId))
}

/**
 * Abstract base controller for raider-scoped entities.
 *
 * Extends BaseCrudController with additional endpoints for raider-based filtering.
 */
abstract class RaiderScopedCrudController<ID : Any, CreateReq, UpdateReq, Resp>(
    private val raiderScopedService: RaiderScopedCrudService<ID, CreateReq, UpdateReq, Resp>,
    paginationProperties: PaginationProperties,
) : BaseCrudController<ID, CreateReq, UpdateReq, Resp>(raiderScopedService, paginationProperties) {
    /**
     * Get all entities for a specific raider with pagination.
     *
     * @param raiderId The raider identifier
     * @param page The page number (0-indexed)
     * @param size The page size
     * @return Paged response of entities for the raider
     */
    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Get entities by raider", description = "Retrieve all entities for a specific raider")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved entities",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Raider not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))],
            ),
        ],
    )
    open fun findByRaider(
        @Parameter(description = "Raider identifier")
        @PathVariable
        raiderId: Long,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0")
        page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false)
        size: Int?,
        paginationProperties: PaginationProperties,
    ): PagedResponse<Resp> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return raiderScopedService.findByRaider(raiderId, pageRequest)
    }

    /**
     * Get count of entities for a specific raider.
     *
     * @param raiderId The raider identifier
     * @return Count response
     */
    @GetMapping("/raider/{raiderId}/count")
    @Operation(summary = "Count entities by raider", description = "Get count of entities for a specific raider")
    open fun countByRaider(
        @Parameter(description = "Raider identifier")
        @PathVariable
        raiderId: Long,
    ): CountResponse = CountResponse(count = raiderScopedService.countByRaider(raiderId))
}

/**
 * Response for count operations.
 */
data class CountResponse(
    val count: Long,
)
