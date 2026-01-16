package com.edgerush.lootman.api.guild

import com.edgerush.lootman.application.guild.GuildContext
import com.edgerush.lootman.application.guild.GuildContextService
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing user guild context.
 *
 * Provides endpoints for:
 * - Getting user's guilds
 * - Getting/setting active character
 */
@RestController
@RequestMapping("/api/v1/user/guilds")
@Tag(name = "Guild Context", description = "User guild context management")
class GuildContextController(
    private val guildContextService: GuildContextService,
    private val userIdExtractor: UserIdExtractor,
) {
    @GetMapping
    @Operation(
        summary = "Get user's guilds",
        description = "Returns all guilds the user has characters in, with their permissions",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Guild list returned"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun getUserGuilds(
        @RequestHeader("Authorization") authorization: String,
    ): ResponseEntity<List<GuildContextResponse>> {
        val userId = userIdExtractor.extractUserId(authorization)
        val contexts = guildContextService.getUserGuilds(userId)
        return ResponseEntity.ok(contexts.map { it.toResponse() })
    }

    @GetMapping("/active")
    @Operation(
        summary = "Get active guild context",
        description = "Returns the user's currently active guild/character context",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Active context returned"),
        ApiResponse(responseCode = "204", description = "No active context (user has no linked characters)"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun getActiveGuildContext(
        @RequestHeader("Authorization") authorization: String,
    ): ResponseEntity<GuildContextResponse> {
        val userId = userIdExtractor.extractUserId(authorization)
        val context = guildContextService.getActiveGuildContext(userId)
        return if (context != null) {
            ResponseEntity.ok(context.toResponse())
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @PutMapping("/active")
    @Operation(
        summary = "Set active character",
        description = "Sets the user's active character/guild context",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Active character updated"),
        ApiResponse(responseCode = "400", description = "Invalid character mapping ID"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun setActiveCharacter(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: SetActiveCharacterRequest,
    ): ResponseEntity<GuildContextResponse> {
        val userId = userIdExtractor.extractUserId(authorization)
        val context = guildContextService.setActiveCharacter(userId, UserCharacterMappingId(request.characterMappingId))
        return ResponseEntity.ok(context.toResponse())
    }

    private fun GuildContext.toResponse() =
        GuildContextResponse(
            guildId = guildId,
            guildName = guildName,
            characterName = characterName,
            characterRealm = characterRealm,
            characterClass = characterClass,
            characterMappingId = characterMappingId,
            raiderId = raiderId,
            rank = rank,
            permissions = permissions.map { it.name },
            isActive = isActive,
        )
}

/**
 * Response DTO for guild context.
 */
data class GuildContextResponse(
    val guildId: String,
    val guildName: String,
    val characterName: String,
    val characterRealm: String,
    val characterClass: String,
    val characterMappingId: Long,
    val raiderId: Long,
    val rank: String?,
    val permissions: List<String>,
    val isActive: Boolean,
)

/**
 * Request DTO for setting active character.
 */
data class SetActiveCharacterRequest(
    val characterMappingId: Long,
)

/**
 * Interface for extracting user ID from authorization header.
 */
interface UserIdExtractor {
    fun extractUserId(authorization: String): UserId
}
