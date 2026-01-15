package com.edgerush.lootman.api.auth

import com.edgerush.lootman.domain.auth.model.User
import java.time.Instant

/**
 * Response containing OAuth2 authorization URL.
 */
data class OAuth2UrlResponse(
    val url: String,
    val provider: String,
)

/**
 * Request to exchange OAuth2 authorization code for tokens.
 */
data class OAuth2CallbackRequest(
    val code: String,
    val state: String? = null,
)

/**
 * Response containing JWT tokens.
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
)

/**
 * Request to refresh an access token.
 */
data class RefreshTokenRequest(
    val refreshToken: String,
)

/**
 * Response for the current authenticated user.
 */
data class UserProfileResponse(
    val id: Long,
    val discordId: String?,
    val battlenetId: String?,
    val username: String,
    val email: String?,
    val avatarUrl: String?,
    val role: String,
    val guildId: String?,
    val createdAt: Instant,
    val lastLogin: Instant?,
) {
    companion object {
        fun from(user: User): UserProfileResponse =
            UserProfileResponse(
                id = user.id!!.value,
                discordId = user.discordId,
                battlenetId = user.battlenetId,
                username = user.username,
                email = user.email,
                avatarUrl = user.avatarUrl,
                role = user.role.name,
                guildId = user.guildId?.value,
                createdAt = user.createdAt,
                lastLogin = user.lastLogin,
            )
    }
}

/**
 * Response confirming logout.
 */
data class LogoutResponse(
    val success: Boolean,
    val message: String = "Successfully logged out",
)

/**
 * Discord user info from OAuth2.
 */
data class DiscordUserInfo(
    val id: String,
    val username: String,
    val discriminator: String,
    val avatar: String?,
    val email: String?,
    val verified: Boolean?,
) {
    val avatarUrl: String?
        get() = avatar?.let { "https://cdn.discordapp.com/avatars/$id/$it.png" }
}

/**
 * Battle.net user info from OAuth2.
 */
data class BattlenetUserInfo(
    val sub: String,
    val id: Long,
    val battletag: String,
)
