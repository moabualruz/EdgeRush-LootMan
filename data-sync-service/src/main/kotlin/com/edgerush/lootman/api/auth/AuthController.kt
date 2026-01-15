package com.edgerush.lootman.api.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST controller for authentication endpoints.
 *
 * Handles OAuth2 authentication flows for Discord and Battle.net,
 * token refresh, and user profile retrieval.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "OAuth2 authentication and token management")
class AuthController(
    private val authenticationService: AuthenticationService,
) {
    // ============= Discord OAuth2 =============

    @GetMapping("/discord/url")
    @Operation(
        summary = "Get Discord OAuth2 URL",
        description = "Returns the Discord OAuth2 authorization URL for initiating login",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Authorization URL returned"),
        ApiResponse(responseCode = "503", description = "Discord OAuth2 not configured"),
    )
    fun getDiscordAuthUrl(
        @Parameter(description = "Optional state parameter for CSRF protection")
        @RequestParam(required = false) state: String?,
    ): OAuth2UrlResponse {
        return authenticationService.getDiscordAuthUrl(state)
    }

    @PostMapping("/discord/callback")
    @Operation(
        summary = "Discord OAuth2 callback",
        description = "Exchanges the Discord authorization code for JWT tokens",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Authentication successful, tokens returned"),
        ApiResponse(responseCode = "400", description = "Invalid authorization code"),
        ApiResponse(responseCode = "503", description = "Discord OAuth2 not configured"),
    )
    fun discordCallback(
        @RequestBody request: OAuth2CallbackRequest,
    ): ResponseEntity<TokenResponse> {
        val tokens = authenticationService.authenticateWithDiscord(request.code)
        return ResponseEntity.ok(tokens)
    }

    // ============= Battle.net OAuth2 =============

    @GetMapping("/battlenet/url")
    @Operation(
        summary = "Get Battle.net OAuth2 URL",
        description = "Returns the Battle.net OAuth2 authorization URL for initiating login",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Authorization URL returned"),
        ApiResponse(responseCode = "503", description = "Battle.net OAuth2 not configured"),
    )
    fun getBattlenetAuthUrl(
        @Parameter(description = "Optional state parameter for CSRF protection")
        @RequestParam(required = false) state: String?,
    ): OAuth2UrlResponse {
        return authenticationService.getBattlenetAuthUrl(state)
    }

    @PostMapping("/battlenet/callback")
    @Operation(
        summary = "Battle.net OAuth2 callback",
        description = "Exchanges the Battle.net authorization code for JWT tokens",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Authentication successful, tokens returned"),
        ApiResponse(responseCode = "400", description = "Invalid authorization code"),
        ApiResponse(responseCode = "503", description = "Battle.net OAuth2 not configured"),
    )
    fun battlenetCallback(
        @RequestBody request: OAuth2CallbackRequest,
    ): ResponseEntity<TokenResponse> {
        val tokens = authenticationService.authenticateWithBattlenet(request.code)
        return ResponseEntity.ok(tokens)
    }

    // ============= Token Management =============

    @GetMapping("/me")
    @Operation(
        summary = "Get current user",
        description = "Returns the profile of the currently authenticated user",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User profile returned"),
        ApiResponse(responseCode = "401", description = "Not authenticated or invalid token"),
    )
    fun getCurrentUser(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String,
    ): UserProfileResponse {
        val token = extractBearerToken(authorization)
        return authenticationService.getCurrentUser(token)
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        description = "Uses a refresh token to obtain a new access token",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "New tokens returned"),
        ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
    )
    fun refreshToken(
        @RequestBody request: RefreshTokenRequest,
    ): ResponseEntity<TokenResponse> {
        val tokens = authenticationService.refreshAccessToken(request.refreshToken)
        return ResponseEntity.ok(tokens)
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Logout",
        description = "Revokes all refresh tokens for the current user",
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Logout successful"),
        ApiResponse(responseCode = "401", description = "Not authenticated"),
    )
    fun logout(
        @Parameter(description = "JWT access token", required = true)
        @RequestHeader("Authorization") authorization: String,
    ): LogoutResponse {
        val token = extractBearerToken(authorization)
        val userId =
            authenticationService.validateToken(token)
                ?: throw IllegalArgumentException("Invalid token")
        return authenticationService.logout(userId)
    }

    // ============= Helper Methods =============

    private fun extractBearerToken(authorization: String): String {
        if (!authorization.startsWith("Bearer ", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid Authorization header format")
        }
        return authorization.substring(7)
    }
}
