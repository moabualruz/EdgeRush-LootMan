package com.edgerush.lootman.api.gear

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.application.gear.GearItemCommand
import com.edgerush.lootman.application.gear.GetCurrentGearQuery
import com.edgerush.lootman.application.gear.GetCurrentGearUseCase
import com.edgerush.lootman.application.gear.GetGearByTypeQuery
import com.edgerush.lootman.application.gear.GetGearByTypeUseCase
import com.edgerush.lootman.application.gear.SaveGearCommand
import com.edgerush.lootman.application.gear.SaveGearUseCase
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for Gear operations.
 *
 * Provides endpoints for managing raider gear sets.
 */
@RestController
@RequestMapping("/api/v1/gear")
class GearController(
    private val getCurrentGearUseCase: GetCurrentGearUseCase,
    private val getGearByTypeUseCase: GetGearByTypeUseCase,
    private val saveGearUseCase: SaveGearUseCase,
    private val currentUserService: CurrentUserService,
) {
    /**
     * Get current user's equipped gear.
     *
     * @param guildId The guild's unique identifier
     * @param authenticatedUser The authenticated user from the JWT token
     * @return 200 OK with the gear set
     */
    @GetMapping("/guilds/{guildId}/me")
    fun getMyGear(
        @PathVariable guildId: String,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): GearSetResponse {
        currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId))
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser)

        return getCurrentGearUseCase.execute(GetCurrentGearQuery(raiderId.value))
            .map { gearSet -> GearSetResponse.from(gearSet) }
            .getOrThrow()
    }

    /**
     * Get a raider's current equipped gear.
     *
     * @param raiderId The raider's unique identifier
     * @return 200 OK with the gear set, or 404 if not found
     */
    @GetMapping("/raider/{raiderId}")
    fun getCurrentGear(@PathVariable raiderId: Long): GearSetResponse {
        return getCurrentGearUseCase.execute(GetCurrentGearQuery(raiderId))
            .map { gearSet -> GearSetResponse.from(gearSet) }
            .getOrThrow()
    }

    /**
     * Get a raider's gear by type.
     *
     * @param raiderId The raider's unique identifier
     * @param type The gear set type (EQUIPPED or BEST)
     * @return 200 OK with the gear set, or 404 if not found
     */
    @GetMapping("/raider/{raiderId}/type/{type}")
    fun getGearByType(
        @PathVariable raiderId: Long,
        @PathVariable type: String
    ): GearSetResponse {
        return getGearByTypeUseCase.execute(GetGearByTypeQuery(raiderId, type))
            .map { gearSet -> GearSetResponse.from(gearSet) }
            .getOrThrow()
    }

    /**
     * Create gear for a raider.
     *
     * @param raiderId The raider's unique identifier
     * @param request The gear creation request
     * @return 201 Created with the created gear set
     */
    @PostMapping("/raider/{raiderId}")
    fun createGear(
        @PathVariable raiderId: Long,
        @RequestBody request: SaveGearRequest
    ): ResponseEntity<GearSetResponse> {
        val command = SaveGearCommand(
            raiderId = raiderId,
            gearSetType = request.gearSetType,
            items = request.items.map { item ->
                GearItemCommand(
                    itemId = item.itemId,
                    name = item.name,
                    itemLevel = item.itemLevel,
                    quality = item.quality,
                    slot = item.slot,
                    isTierPiece = item.isTierPiece,
                    enchant = item.enchant,
                    sockets = item.sockets
                )
            }
        )

        return saveGearUseCase.execute(command)
            .map { gearSet ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(GearSetResponse.from(gearSet))
            }
            .getOrElse { exception -> throw exception }
    }

    /**
     * Update a raider's gear.
     *
     * @param raiderId The raider's unique identifier
     * @param request The gear update request
     * @return 200 OK with the updated gear set
     */
    @PutMapping("/raider/{raiderId}")
    fun updateGear(
        @PathVariable raiderId: Long,
        @RequestBody request: SaveGearRequest
    ): GearSetResponse {
        val command = SaveGearCommand(
            raiderId = raiderId,
            gearSetType = request.gearSetType,
            items = request.items.map { item ->
                GearItemCommand(
                    itemId = item.itemId,
                    name = item.name,
                    itemLevel = item.itemLevel,
                    quality = item.quality,
                    slot = item.slot,
                    isTierPiece = item.isTierPiece,
                    enchant = item.enchant,
                    sockets = item.sockets
                )
            }
        )

        return saveGearUseCase.execute(command)
            .map { gearSet -> GearSetResponse.from(gearSet) }
            .getOrThrow()
    }
}
