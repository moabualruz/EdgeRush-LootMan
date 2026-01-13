package com.edgerush.lootman.api.guild

import com.edgerush.lootman.application.guild.CreateGuildCommand
import com.edgerush.lootman.application.guild.CreateGuildUseCase
import com.edgerush.lootman.application.guild.DeleteGuildCommand
import com.edgerush.lootman.application.guild.DeleteGuildUseCase
import com.edgerush.lootman.application.guild.GetGuildQuery
import com.edgerush.lootman.application.guild.GetGuildUseCase
import com.edgerush.lootman.application.guild.ListGuildsUseCase
import com.edgerush.lootman.application.guild.UpdateGuildCommand
import com.edgerush.lootman.application.guild.UpdateGuildUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for Guild operations.
 *
 * Provides CRUD endpoints for managing guilds.
 */
@RestController
@RequestMapping("/api/v1/guilds")
class GuildController(
    private val createGuildUseCase: CreateGuildUseCase,
    private val updateGuildUseCase: UpdateGuildUseCase,
    private val deleteGuildUseCase: DeleteGuildUseCase,
    private val getGuildUseCase: GetGuildUseCase,
    private val listGuildsUseCase: ListGuildsUseCase
) {
    /**
     * Create a new guild.
     *
     * @param request The guild creation request
     * @return 201 Created with the created guild
     */
    @PostMapping
    fun createGuild(@RequestBody request: CreateGuildRequest): ResponseEntity<GuildResponse> {
        val command = CreateGuildCommand(
            id = request.id,
            name = request.name,
            description = request.description,
            realm = request.realm,
            region = request.region,
            syncEnabled = request.syncEnabled,
            syncCronExpression = request.syncCronExpression,
            timezone = request.timezone,
            benchmarkMode = request.benchmarkMode
        )

        return createGuildUseCase.execute(command)
            .map { guild ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(GuildResponse.from(guild))
            }
            .getOrElse { exception -> throw exception }
    }

    /**
     * Get a guild by ID.
     *
     * @param id The guild's unique identifier
     * @return 200 OK with the guild, or 404 if not found
     */
    @GetMapping("/{id}")
    fun getGuild(@PathVariable id: String): GuildResponse {
        return getGuildUseCase.execute(GetGuildQuery(id))
            .map { guild -> GuildResponse.from(guild) }
            .getOrThrow()
    }

    /**
     * Update an existing guild.
     *
     * @param id The guild's unique identifier
     * @param request The update request with fields to modify
     * @return 200 OK with the updated guild, or 404 if not found
     */
    @PutMapping("/{id}")
    fun updateGuild(
        @PathVariable id: String,
        @RequestBody request: UpdateGuildRequest
    ): GuildResponse {
        val command = UpdateGuildCommand(
            id = id,
            name = request.name,
            description = request.description,
            realm = request.realm,
            region = request.region,
            syncEnabled = request.syncEnabled,
            syncCronExpression = request.syncCronExpression,
            timezone = request.timezone,
            benchmarkMode = request.benchmarkMode,
            customBenchmarkRms = request.customBenchmarkRms,
            customBenchmarkIpi = request.customBenchmarkIpi,
            isActive = request.isActive
        )

        return updateGuildUseCase.execute(command)
            .map { guild -> GuildResponse.from(guild) }
            .getOrThrow()
    }

    /**
     * Delete a guild.
     *
     * @param id The guild's unique identifier
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    fun deleteGuild(@PathVariable id: String): ResponseEntity<Void> {
        return deleteGuildUseCase.execute(DeleteGuildCommand(id))
            .map { ResponseEntity.noContent().build<Void>() }
            .getOrThrow()
    }

    /**
     * List all guilds.
     *
     * @return 200 OK with the list of all guilds
     */
    @GetMapping
    fun listGuilds(): GuildListResponse {
        return listGuildsUseCase.execute()
            .map { guilds -> GuildListResponse.from(guilds) }
            .getOrThrow()
    }

    /**
     * List active guilds only.
     *
     * @return 200 OK with the list of active guilds
     */
    @GetMapping("/active")
    fun listActiveGuilds(): GuildListResponse {
        return listGuildsUseCase.executeActiveOnly()
            .map { guilds -> GuildListResponse.from(guilds) }
            .getOrThrow()
    }
}
