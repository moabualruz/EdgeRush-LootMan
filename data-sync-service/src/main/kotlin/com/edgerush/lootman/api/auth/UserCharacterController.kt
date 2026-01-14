package com.edgerush.lootman.api.auth

import com.edgerush.lootman.domain.auth.model.UserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for managing user-character mappings.
 *
 * Allows authenticated users to link and manage their WoW characters.
 */
@RestController
@RequestMapping("/api/v1/users/me/characters")
@Tag(name = "User Characters", description = "Manage linked WoW characters for the current user")
class UserCharacterController(
    private val authenticationService: AuthenticationService,
    private val characterMappingService: UserCharacterMappingService
) {

    @GetMapping
    @Operation(
        summary = "Get linked characters",
        description = "Returns all WoW characters linked to the current user"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "List of linked characters"),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun getCharacters(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String
    ): List<UserCharacterMappingResponse> {
        val userId = extractUserId(authorization)
        return characterMappingService.getCharactersForUser(userId)
    }

    @PostMapping
    @Operation(
        summary = "Link a character",
        description = "Links a new WoW character to the current user"
    )
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Character linked successfully"),
        ApiResponse(responseCode = "400", description = "Character already linked"),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun linkCharacter(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: LinkCharacterRequest
    ): ResponseEntity<UserCharacterMappingResponse> {
        val userId = extractUserId(authorization)
        val response = characterMappingService.linkCharacter(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Unlink a character",
        description = "Removes the link between the current user and a character"
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Character unlinked successfully"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
        ApiResponse(responseCode = "404", description = "Mapping not found")
    )
    fun unlinkCharacter(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String,
        @Parameter(description = "Character mapping ID")
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        val userId = extractUserId(authorization)
        characterMappingService.unlinkCharacter(userId, id)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}/primary")
    @Operation(
        summary = "Set primary character",
        description = "Sets a linked character as the user's primary character"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Primary character updated"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
        ApiResponse(responseCode = "404", description = "Mapping not found")
    )
    fun setPrimaryCharacter(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String,
        @Parameter(description = "Character mapping ID")
        @PathVariable id: Long
    ): UserCharacterMappingResponse {
        val userId = extractUserId(authorization)
        return characterMappingService.setPrimaryCharacter(userId, id)
    }

    @GetMapping("/primary")
    @Operation(
        summary = "Get primary character",
        description = "Returns the user's primary character if one is set"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Primary character returned"),
        ApiResponse(responseCode = "204", description = "No primary character set"),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun getPrimaryCharacter(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<UserCharacterMappingResponse> {
        val userId = extractUserId(authorization)
        val primary = characterMappingService.getPrimaryCharacterForUser(userId)
        return if (primary != null) {
            ResponseEntity.ok(primary)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping("/count")
    @Operation(
        summary = "Get character count",
        description = "Returns the number of characters linked to the current user"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Character count returned"),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun getCharacterCount(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String
    ): CharacterCountResponse {
        val userId = extractUserId(authorization)
        return characterMappingService.getCharacterCount(userId)
    }

    private fun extractUserId(authorization: String): UserId {
        val token = extractBearerToken(authorization)
        return authenticationService.validateToken(token)
            ?: throw IllegalArgumentException("Invalid token")
    }

    private fun extractBearerToken(authorization: String): String {
        if (!authorization.startsWith("Bearer ", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid Authorization header format")
        }
        return authorization.substring(7)
    }
}
