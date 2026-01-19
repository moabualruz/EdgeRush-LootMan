package com.edgerush.lootman.api.auth

import com.edgerush.lootman.application.guild.AllUsersLinkageRefreshResult
import com.edgerush.lootman.application.guild.CharacterGuildDataRepairService
import com.edgerush.lootman.application.guild.CharacterGuildRepairResult
import com.edgerush.lootman.application.guild.UserCharacterGuildRepairResult
import com.edgerush.lootman.application.guild.UserLinkageRefreshResult
import com.edgerush.lootman.application.guild.UserLinkageRefreshService
import com.edgerush.lootman.application.guild.UserLinkageValidationResult
import com.edgerush.lootman.domain.auth.model.UserCharacter
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for user-centric data operations.
 */
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "User data and preferences")
class UserController(
    private val authenticationService: AuthenticationService,
    private val userCharacterRepository: UserCharacterRepository,
    private val userLinkageRefreshService: UserLinkageRefreshService,
    private val characterGuildDataRepairService: CharacterGuildDataRepairService,
) {

    @GetMapping("/characters")
    @Operation(summary = "Get user characters", description = "Returns list of synced WoW characters")
    fun getCharacters(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String
    ): List<UserCharacter> {
        val token = authorization.substring(7)
        val userId = authenticationService.validateToken(token)
            ?: throw IllegalArgumentException("Invalid token")

        return userCharacterRepository.findAllByUserId(userId)
    }

    @GetMapping("/linkage/validate")
    @Operation(
        summary = "Validate user linkages",
        description = "Checks if user's character-raider-guild links are valid without making changes"
    )
    fun validateLinkages(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<UserLinkageValidationResult> {
        val token = authorization.substring(7)
        val userId = authenticationService.validateToken(token)
            ?: throw IllegalArgumentException("Invalid token")

        val result = userLinkageRefreshService.validateUserLinkages(userId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/linkage/refresh")
    @Operation(
        summary = "Refresh user linkages",
        description = "Repairs user's character-raider-guild links: removes orphaned mappings, auto-links characters to raiders, fixes preferences"
    )
    fun refreshLinkages(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<UserLinkageRefreshResult> {
        val token = authorization.substring(7)
        val userId = authenticationService.validateToken(token)
            ?: throw IllegalArgumentException("Invalid token")

        val result = userLinkageRefreshService.refreshUserLinkages(userId)
        return ResponseEntity.ok(result)
    }

    // ============= Admin Endpoints =============

    @GetMapping("/admin/{userId}/linkage/validate")
    @Operation(
        summary = "Admin: Validate user linkages by ID",
        description = "Admin endpoint to check a specific user's character-raider-guild links"
    )
    fun adminValidateLinkages(
        @Parameter(description = "User ID to validate")
        @PathVariable userId: Long
    ): ResponseEntity<UserLinkageValidationResult> {
        val result = userLinkageRefreshService.validateUserLinkages(UserId(userId))
        return ResponseEntity.ok(result)
    }

    @PostMapping("/admin/{userId}/linkage/refresh")
    @Operation(
        summary = "Admin: Refresh user linkages by ID",
        description = "Admin endpoint to repair a specific user's character-raider-guild links"
    )
    fun adminRefreshLinkages(
        @Parameter(description = "User ID to refresh")
        @PathVariable userId: Long
    ): ResponseEntity<UserLinkageRefreshResult> {
        val result = userLinkageRefreshService.refreshUserLinkages(UserId(userId))
        return ResponseEntity.ok(result)
    }

    @PostMapping("/admin/linkage/refresh-all")
    @Operation(
        summary = "Admin: Refresh all users' linkages",
        description = "Admin endpoint to repair all users' character-raider-guild links. Use with caution on large databases."
    )
    fun adminRefreshAllLinkages(): ResponseEntity<AllUsersLinkageRefreshResult> {
        val result = userLinkageRefreshService.refreshAllUserLinkages()
        return ResponseEntity.ok(result)
    }

    // ============= Data Repair Endpoints =============

    @GetMapping("/admin/{userId}/repair-guild-data")
    @Operation(
        summary = "Admin: Repair user's character guild data",
        description = "Updates user's characters with guild info from matching raiders, then refreshes linkages. " +
            "Use this to fix characters synced before guild info was added."
    )
    fun adminRepairUserGuildData(
        @Parameter(description = "User ID to repair")
        @PathVariable userId: Long
    ): ResponseEntity<UserCharacterGuildRepairResult> {
        val result = characterGuildDataRepairService.repairUserCharacterGuildData(UserId(userId))
        return ResponseEntity.ok(result)
    }

    @GetMapping("/admin/repair-all-guild-data")
    @Operation(
        summary = "Admin: Repair all users' character guild data",
        description = "Updates ALL users' characters with guild info from matching raiders, then refreshes linkages. " +
            "This backfills guild_id for characters synced before guild info feature was added. " +
            "WARNING: This may take a long time on large databases!"
    )
    fun adminRepairAllGuildData(): ResponseEntity<CharacterGuildRepairResult> {
        val result = characterGuildDataRepairService.repairAllCharacterGuildData()
        return ResponseEntity.ok(result)
    }
}
