package com.edgerush.lootman.api.wishlist

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.application.wishlist.DeleteWishlistCommand
import com.edgerush.lootman.application.wishlist.DeleteWishlistUseCase
import com.edgerush.lootman.application.wishlist.GetWishlistQuery
import com.edgerush.lootman.application.wishlist.GetWishlistUseCase
import com.edgerush.lootman.application.wishlist.SaveWishlistCommand
import com.edgerush.lootman.application.wishlist.SaveWishlistUseCase
import com.edgerush.lootman.application.wishlist.WishlistItemCommand
import com.edgerush.lootman.domain.shared.GuildId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for Wishlist operations.
 *
 * Provides CRUD endpoints for managing raider wishlists.
 */
@RestController
@RequestMapping("/api/v1/wishlists")
class WishlistController(
    private val getWishlistUseCase: GetWishlistUseCase,
    private val saveWishlistUseCase: SaveWishlistUseCase,
    private val deleteWishlistUseCase: DeleteWishlistUseCase,
    private val currentUserService: CurrentUserService,
) {
    /**
     * Get current user's wishlist.
     *
     * @param guildId The guild's unique identifier
     * @param authenticatedUser The authenticated user from the JWT token
     * @return 200 OK with the wishlist
     */
    @GetMapping("/guilds/{guildId}/me")
    fun getMyWishlist(
        @PathVariable guildId: String,
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
    ): WishlistResponse {
        currentUserService.validateGuildAccess(authenticatedUser, GuildId(guildId))
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(authenticatedUser)

        return getWishlistUseCase.execute(GetWishlistQuery(raiderId.value))
            .map { wishlist -> WishlistResponse.from(wishlist) }
            .getOrThrow()
    }

    /**
     * Get a raider's wishlist.
     *
     * @param raiderId The raider's unique identifier
     * @return 200 OK with the wishlist, or 404 if not found
     */
    @GetMapping("/raider/{raiderId}")
    fun getWishlist(
        @PathVariable raiderId: Long,
    ): WishlistResponse {
        return getWishlistUseCase.execute(GetWishlistQuery(raiderId))
            .map { wishlist -> WishlistResponse.from(wishlist) }
            .getOrThrow()
    }

    /**
     * Create a new wishlist.
     *
     * @param request The wishlist creation request
     * @return 201 Created with the created wishlist
     */
    @PostMapping
    fun createWishlist(
        @RequestBody request: SaveWishlistRequest,
    ): ResponseEntity<WishlistResponse> {
        val command =
            SaveWishlistCommand(
                raiderId = request.raiderId,
                items =
                    request.items.map { item ->
                        WishlistItemCommand(
                            itemId = item.itemId,
                            itemName = item.itemName,
                            priority = item.priority,
                            upgradePercentage = item.upgradePercentage,
                            specName = item.specName,
                        )
                    },
            )

        return saveWishlistUseCase.execute(command)
            .map { wishlist ->
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(WishlistResponse.from(wishlist))
            }
            .getOrElse { exception -> throw exception }
    }

    /**
     * Update a raider's wishlist.
     *
     * @param raiderId The raider's unique identifier
     * @param request The update request with items
     * @return 200 OK with the updated wishlist
     */
    @PutMapping("/raider/{raiderId}")
    fun updateWishlist(
        @PathVariable raiderId: Long,
        @RequestBody request: SaveWishlistRequest,
    ): WishlistResponse {
        val command =
            SaveWishlistCommand(
                raiderId = raiderId,
                items =
                    request.items.map { item ->
                        WishlistItemCommand(
                            itemId = item.itemId,
                            itemName = item.itemName,
                            priority = item.priority,
                            upgradePercentage = item.upgradePercentage,
                            specName = item.specName,
                        )
                    },
            )

        return saveWishlistUseCase.execute(command)
            .map { wishlist -> WishlistResponse.from(wishlist) }
            .getOrThrow()
    }

    /**
     * Delete a raider's wishlist.
     *
     * @param raiderId The raider's unique identifier
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/raider/{raiderId}")
    fun deleteWishlist(
        @PathVariable raiderId: Long,
    ): ResponseEntity<Void> {
        return deleteWishlistUseCase.execute(DeleteWishlistCommand(raiderId))
            .map { ResponseEntity.noContent().build<Void>() }
            .getOrThrow()
    }
}
