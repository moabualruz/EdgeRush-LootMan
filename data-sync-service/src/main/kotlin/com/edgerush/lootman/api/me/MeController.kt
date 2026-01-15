package com.edgerush.lootman.api.me

import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.lootman.api.auth.CurrentUserService
import com.edgerush.lootman.domain.shared.GuildId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for current user ("me") operations.
 *
 * Provides unified endpoints for retrieving personal data for the
 * authenticated user's primary linked character under /api/v1/me/.
 *
 * Note: Some resources also have /me endpoints under their resource controllers
 * (e.g., /api/v1/attendance/guilds/{guildId}/me). This controller provides an
 * alternative unified access point under /api/v1/me/.
 */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Me", description = "Current user data endpoints")
class MeController(
    private val currentUserService: CurrentUserService,
    private val meDataService: MeDataService,
) {
    /**
     * Get gear for the current user's primary character.
     */
    @GetMapping("/gear/guilds/{guildId}/me")
    @Operation(
        summary = "Get my gear",
        description = "Returns equipped gear for the current user's primary linked character",
    )
    fun getMyGear(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): PersonalGearResponse {
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(user)
        return meDataService.getGearForRaider(GuildId(guildId), raiderId)
    }

    /**
     * Get vault options for the current user's primary character.
     */
    @GetMapping("/vault/guilds/{guildId}/me")
    @Operation(
        summary = "Get my vault options",
        description = "Returns Great Vault options for the current user's primary linked character",
    )
    fun getMyVault(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): PersonalVaultResponse {
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(user)
        return meDataService.getVaultForRaider(GuildId(guildId), raiderId)
    }

    /**
     * Get attendance for the current user's primary character.
     */
    @GetMapping("/attendance/guilds/{guildId}/me")
    @Operation(
        summary = "Get my attendance",
        description = "Returns attendance records for the current user's primary linked character",
    )
    fun getMyAttendance(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): PersonalAttendanceResponse {
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(user)
        return meDataService.getAttendanceForRaider(GuildId(guildId), raiderId)
    }

    /**
     * Get performance metrics for the current user's primary character.
     */
    @GetMapping("/performance/guilds/{guildId}/me")
    @Operation(
        summary = "Get my performance",
        description = "Returns Warcraft Logs performance metrics for the current user's primary linked character",
    )
    fun getMyPerformance(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): PersonalPerformanceResponse {
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(user)
        return meDataService.getPerformanceForRaider(GuildId(guildId), raiderId)
    }

    /**
     * Get wishlist for the current user's primary character.
     */
    @GetMapping("/wishlist/guilds/{guildId}/me")
    @Operation(
        summary = "Get my wishlist",
        description = "Returns wishlist items for the current user's primary linked character",
    )
    fun getMyWishlist(
        @Parameter(description = "Guild ID")
        @PathVariable guildId: String,
        @AuthenticationPrincipal user: AuthenticatedUser,
    ): PersonalWishlistResponse {
        val raiderId = currentUserService.getCurrentUserPrimaryRaiderIdBlocking(user)
        return meDataService.getWishlistForRaider(GuildId(guildId), raiderId)
    }
}
