package com.edgerush.lootman.api.raider

import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.api.common.PaginationProperties
import com.edgerush.lootman.application.raider.CreateRaiderCommand
import com.edgerush.lootman.application.raider.CreateRaiderUseCase
import com.edgerush.lootman.application.raider.DeleteRaiderCommand
import com.edgerush.lootman.application.raider.DeleteRaiderUseCase
import com.edgerush.lootman.application.raider.GetRaiderQuery
import com.edgerush.lootman.application.raider.GetRaiderUseCase
import com.edgerush.lootman.application.raider.ListRaidersByGuildPaginatedQuery
import com.edgerush.lootman.application.raider.ListRaidersByGuildQuery
import com.edgerush.lootman.application.raider.ListRaidersUseCase
import com.edgerush.lootman.application.raider.UpdateRaiderCommand
import com.edgerush.lootman.application.raider.UpdateRaiderUseCase
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
 * REST controller for Raider operations.
 *
 * Provides CRUD endpoints for managing guild raiders.
 */
@RestController
@RequestMapping("/api/v1/raiders")
class RaiderController(
    private val createRaiderUseCase: CreateRaiderUseCase,
    private val updateRaiderUseCase: UpdateRaiderUseCase,
    private val deleteRaiderUseCase: DeleteRaiderUseCase,
    private val getRaiderUseCase: GetRaiderUseCase,
    private val listRaidersUseCase: ListRaidersUseCase,
    private val paginationProperties: PaginationProperties,
) {
    /**
     * Create a new raider.
     *
     * @param request The raider creation request
     * @return 201 Created with the created raider
     */
    @PostMapping
    fun createRaider(
        @RequestBody request: CreateRaiderRequest,
    ): ResponseEntity<RaiderResponse> {
        val command =
            CreateRaiderCommand(
                id = request.id,
                characterId = request.characterId,
                guildId = request.guildId,
                characterName = request.characterName,
                realm = request.realm,
                region = request.region,
                characterClass = request.characterClass,
                role = request.role,
                rank = request.rank,
                status = request.status,
                joinDate = request.joinDate,
                wowauditId = request.wowauditId,
                blizzardId = request.blizzardId,
                accountId = request.accountId,
            )

        return createRaiderUseCase.execute(command)
            .map { raider ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(RaiderResponse.from(raider))
            }
            .getOrElse { exception -> throw exception }
    }

    /**
     * Get a raider by ID.
     *
     * @param id The raider's unique identifier
     * @return 200 OK with the raider, or 404 if not found
     */
    @GetMapping("/{id}")
    fun getRaider(
        @PathVariable id: Long,
    ): RaiderResponse {
        return getRaiderUseCase.execute(GetRaiderQuery(id))
            .map { raider -> RaiderResponse.from(raider) }
            .getOrThrow()
    }

    /**
     * Update an existing raider.
     *
     * @param id The raider's unique identifier
     * @param request The update request with fields to modify
     * @return 200 OK with the updated raider, or 404 if not found
     */
    @PutMapping("/{id}")
    fun updateRaider(
        @PathVariable id: Long,
        @RequestBody request: UpdateRaiderRequest,
    ): RaiderResponse {
        val command =
            UpdateRaiderCommand(
                id = id,
                characterName = request.characterName,
                realm = request.realm,
                characterClass = request.characterClass,
                role = request.role,
                rank = request.rank,
                status = request.status,
            )

        return updateRaiderUseCase.execute(command)
            .map { raider -> RaiderResponse.from(raider) }
            .getOrThrow()
    }

    /**
     * Delete a raider.
     *
     * @param id The raider's unique identifier
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    fun deleteRaider(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        return deleteRaiderUseCase.execute(DeleteRaiderCommand(id))
            .map { ResponseEntity.noContent().build<Void>() }
            .getOrThrow()
    }

    /**
     * Get all raiders for a guild (non-paginated, for backwards compatibility).
     *
     * @param guildId The guild's unique identifier
     * @return 200 OK with the list of raiders
     */
    @GetMapping("/guild/{guildId}/all")
    fun getAllRaidersByGuild(
        @PathVariable guildId: String,
    ): RaiderListResponse {
        return listRaidersUseCase.executeByGuild(ListRaidersByGuildQuery(guildId))
            .map { raiders -> RaiderListResponse.from(raiders) }
            .getOrThrow()
    }

    /**
     * Get raiders for a guild with pagination.
     *
     * @param guildId The guild's unique identifier
     * @param page The page number (0-indexed)
     * @param size The page size (optional, uses default from configuration)
     * @return 200 OK with paginated list of raiders
     */
    @GetMapping("/guild/{guildId}")
    fun getRaidersByGuild(
        @PathVariable guildId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(required = false) size: Int?,
    ): PagedResponse<RaiderResponse> {
        val pageRequest = paginationProperties.createPageRequest(page, size)
        val query =
            ListRaidersByGuildPaginatedQuery(
                guildId = guildId,
                offset = pageRequest.offset,
                limit = pageRequest.size,
            )

        return listRaidersUseCase.executeByGuildPaginated(query)
            .map { result ->
                PagedResponse.of(
                    content = result.raiders.map { RaiderResponse.from(it) },
                    pageRequest = pageRequest,
                    totalElements = result.totalCount,
                )
            }
            .getOrThrow()
    }
}
