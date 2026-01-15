package com.edgerush.lootman.api.discord

import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
 * REST controller for Discord user link CRUD operations.
 *
 * Provides endpoints for managing links between Discord users and WoW characters.
 * Used by both the Discord bot and web frontend for user identification.
 */
@RestController
@RequestMapping("/api/v1/discord/links")
@Tag(name = "Discord User Links", description = "Discord to WoW character linking endpoints")
class DiscordUserLinkController(
    private val discordUserLinkService: DiscordUserLinkCrudService,
    private val paginationProperties: PaginationProperties,
) {
    @GetMapping
    @Operation(summary = "Find all Discord user links with pagination")
    fun findAll(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Page size")
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<DiscordUserLinkResponse> {
        val pageRequest =
            PageRequest.withDefaults(
                page = page,
                size = size,
                defaultSize = paginationProperties.defaultPageSize,
                maxPageSize = paginationProperties.maxPageSize,
            )
        return discordUserLinkService.findAll(pageRequest)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find Discord user link by ID")
    fun findById(
        @Parameter(description = "Link ID")
        @PathVariable id: Long,
    ): DiscordUserLinkResponse {
        return discordUserLinkService.findById(id)
    }

    @PostMapping
    @Operation(summary = "Create a new Discord user link")
    fun create(
        @Valid @RequestBody request: CreateDiscordUserLinkRequest,
    ): ResponseEntity<DiscordUserLinkResponse> {
        val created = discordUserLinkService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing Discord user link")
    fun update(
        @Parameter(description = "Link ID")
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateDiscordUserLinkRequest,
    ): DiscordUserLinkResponse {
        return discordUserLinkService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Discord user link")
    fun delete(
        @Parameter(description = "Link ID")
        @PathVariable id: Long,
    ): ResponseEntity<Unit> {
        discordUserLinkService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if Discord user link exists")
    fun exists(
        @Parameter(description = "Link ID")
        @PathVariable id: Long,
    ): DiscordUserLinkExistsResponse {
        return DiscordUserLinkExistsResponse(exists = discordUserLinkService.existsById(id))
    }

    @GetMapping("/user/{discordUserId}")
    @Operation(summary = "Find all links for a Discord user")
    fun findByDiscordUserId(
        @Parameter(description = "Discord user ID (snowflake)")
        @PathVariable discordUserId: String,
    ): List<DiscordUserLinkResponse> {
        return discordUserLinkService.findByDiscordUserId(discordUserId)
    }

    @GetMapping("/user/{discordUserId}/primary")
    @Operation(summary = "Find the primary link for a Discord user")
    fun findPrimaryByDiscordUserId(
        @Parameter(description = "Discord user ID (snowflake)")
        @PathVariable discordUserId: String,
    ): DiscordUserLinkResponse {
        return discordUserLinkService.findPrimaryByDiscordUserId(discordUserId)
    }

    @GetMapping("/user/{discordUserId}/count")
    @Operation(summary = "Count links for a Discord user")
    fun countByDiscordUserId(
        @Parameter(description = "Discord user ID (snowflake)")
        @PathVariable discordUserId: String,
    ): DiscordUserLinkCountResponse {
        return DiscordUserLinkCountResponse(count = discordUserLinkService.countByDiscordUserId(discordUserId))
    }

    @DeleteMapping("/user/{discordUserId}")
    @Operation(summary = "Delete all links for a Discord user")
    fun deleteByDiscordUserId(
        @Parameter(description = "Discord user ID (snowflake)")
        @PathVariable discordUserId: String,
    ): DiscordUserLinkCountResponse {
        val deletedCount = discordUserLinkService.deleteByDiscordUserId(discordUserId)
        return DiscordUserLinkCountResponse(count = deletedCount.toLong())
    }

    @GetMapping("/raider/{raiderId}")
    @Operation(summary = "Find all Discord users linked to a raider")
    fun findByRaiderId(
        @Parameter(description = "Raider ID")
        @PathVariable raiderId: Long,
    ): List<DiscordUserLinkResponse> {
        return discordUserLinkService.findByRaiderId(raiderId)
    }

    @PutMapping("/{id}/primary")
    @Operation(summary = "Set a link as the primary link for its Discord user")
    fun setPrimary(
        @Parameter(description = "Link ID")
        @PathVariable id: Long,
    ): DiscordUserLinkResponse {
        return discordUserLinkService.setPrimary(id)
    }
}
