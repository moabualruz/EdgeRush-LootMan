package com.edgerush.lootman.api.flps

import com.edgerush.lootman.api.common.CountResponse
import com.edgerush.lootman.api.common.ExistsResponse
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for FLPS Modifier operations.
 *
 * Provides CRUD endpoints for managing both default and guild-specific FLPS modifiers.
 */
@RestController
@RequestMapping("/api/v1/flps-modifiers")
@Tag(name = "FLPS Modifiers", description = "FLPS modifier management operations")
class FlpsModifierController(
    private val defaultModifierService: FlpsDefaultModifierCrudService,
    private val guildModifierService: FlpsGuildModifierCrudService,
    private val paginationProperties: PaginationProperties,
) {

    // ============== Default Modifier Endpoints ==============

    @Operation(summary = "Get all default modifiers", description = "Returns a paginated list of all default FLPS modifiers")
    @GetMapping("/defaults")
    fun findAllDefaults(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<FlpsDefaultModifierResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return defaultModifierService.findAll(pageRequest)
    }

    @Operation(summary = "Get default modifier by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Modifier found"),
        ApiResponse(responseCode = "404", description = "Modifier not found"),
    )
    @GetMapping("/defaults/{id}")
    fun findDefaultById(
        @Parameter(description = "Modifier ID")
        @PathVariable id: Long,
    ): FlpsDefaultModifierResponse = defaultModifierService.findById(id)

    @Operation(summary = "Create a new default modifier")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Modifier created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
    )
    @PostMapping("/defaults")
    fun createDefault(
        @RequestBody request: CreateFlpsDefaultModifierRequest,
    ): ResponseEntity<FlpsDefaultModifierResponse> {
        val created = defaultModifierService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @Operation(summary = "Update a default modifier")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Modifier updated successfully"),
        ApiResponse(responseCode = "404", description = "Modifier not found"),
    )
    @PutMapping("/defaults/{id}")
    fun updateDefault(
        @Parameter(description = "Modifier ID")
        @PathVariable id: Long,
        @RequestBody request: UpdateFlpsDefaultModifierRequest,
    ): FlpsDefaultModifierResponse = defaultModifierService.update(id, request)

    @Operation(summary = "Delete a default modifier")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Modifier deleted successfully"),
        ApiResponse(responseCode = "404", description = "Modifier not found"),
    )
    @DeleteMapping("/defaults/{id}")
    fun deleteDefault(
        @Parameter(description = "Modifier ID")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        defaultModifierService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Check if default modifier exists")
    @GetMapping("/defaults/{id}/exists")
    fun defaultExists(
        @Parameter(description = "Modifier ID")
        @PathVariable id: Long,
    ): ExistsResponse = ExistsResponse(exists = defaultModifierService.existsById(id))

    @Operation(summary = "Get default modifiers by category")
    @GetMapping("/defaults/category/{category}")
    fun findDefaultsByCategory(
        @Parameter(description = "Category name (e.g., rms, ipi)")
        @PathVariable category: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<FlpsDefaultModifierResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return defaultModifierService.findByCategory(category, pageRequest)
    }

    // ============== Guild Modifier Endpoints ==============

    @Operation(summary = "Get all guild modifiers", description = "Returns a paginated list of all guild-specific FLPS modifiers")
    @GetMapping("/guilds")
    fun findAllGuildModifiers(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<FlpsGuildModifierResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return guildModifierService.findAll(pageRequest)
    }

    @Operation(summary = "Get guild modifier by ID")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Modifier found"),
        ApiResponse(responseCode = "404", description = "Modifier not found"),
    )
    @GetMapping("/guilds/{id}")
    fun findGuildModifierById(
        @Parameter(description = "Modifier ID")
        @PathVariable id: Long,
    ): FlpsGuildModifierResponse = guildModifierService.findById(id)

    @Operation(summary = "Create a new guild modifier")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Modifier created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
    )
    @PostMapping("/guilds")
    fun createGuildModifier(
        @RequestBody request: CreateFlpsGuildModifierRequest,
    ): ResponseEntity<FlpsGuildModifierResponse> {
        val created = guildModifierService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @Operation(summary = "Update a guild modifier")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Modifier updated successfully"),
        ApiResponse(responseCode = "404", description = "Modifier not found"),
    )
    @PutMapping("/guilds/{id}")
    fun updateGuildModifier(
        @Parameter(description = "Modifier ID")
        @PathVariable id: Long,
        @RequestBody request: UpdateFlpsGuildModifierRequest,
    ): FlpsGuildModifierResponse = guildModifierService.update(id, request)

    @Operation(summary = "Delete a guild modifier")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Modifier deleted successfully"),
        ApiResponse(responseCode = "404", description = "Modifier not found"),
    )
    @DeleteMapping("/guilds/{id}")
    fun deleteGuildModifier(
        @Parameter(description = "Modifier ID")
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        guildModifierService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "Check if guild modifier exists")
    @GetMapping("/guilds/{id}/exists")
    fun guildModifierExists(
        @Parameter(description = "Modifier ID")
        @PathVariable id: Long,
    ): ExistsResponse = ExistsResponse(exists = guildModifierService.existsById(id))

    @Operation(summary = "Get modifiers by guild", description = "Returns paginated modifiers for a specific guild")
    @GetMapping("/guilds/guild/{guildId}")
    fun findByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<FlpsGuildModifierResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return guildModifierService.findByGuild(guildId, pageRequest)
    }

    @Operation(summary = "Get modifiers by guild and category")
    @GetMapping("/guilds/guild/{guildId}/category/{category}")
    fun findByGuildAndCategory(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @Parameter(description = "Category name")
        @PathVariable category: String,
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<FlpsGuildModifierResponse> {
        val pageRequest = PageRequest.withDefaults(
            page = page,
            size = size,
            defaultSize = paginationProperties.defaultPageSize,
            maxPageSize = paginationProperties.maxPageSize,
        )
        return guildModifierService.findByGuildAndCategory(guildId, category, pageRequest)
    }

    @Operation(summary = "Count modifiers by guild")
    @GetMapping("/guilds/guild/{guildId}/count")
    fun countByGuild(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
    ): CountResponse = CountResponse(count = guildModifierService.countByGuild(guildId))
}
