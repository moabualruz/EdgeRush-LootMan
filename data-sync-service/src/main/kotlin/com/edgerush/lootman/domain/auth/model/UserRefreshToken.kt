package com.edgerush.lootman.domain.auth.model

import java.time.Instant

/**
 * Entity representing a refresh token for JWT authentication.
 *
 * Refresh tokens are stored as hashes and can be revoked.
 */
data class UserRefreshToken(
    val id: RefreshTokenId? = null,
    val userId: UserId,
    val tokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now(),
    val revokedAt: Instant? = null
) {
    init {
        require(tokenHash.isNotBlank()) { "Token hash must not be blank" }
    }

    /**
     * Checks if the token is valid (not expired and not revoked).
     */
    fun isValid(now: Instant = Instant.now()): Boolean =
        revokedAt == null && expiresAt.isAfter(now)

    /**
     * Checks if the token is expired.
     */
    fun isExpired(now: Instant = Instant.now()): Boolean =
        expiresAt.isBefore(now) || expiresAt == now

    /**
     * Checks if the token has been revoked.
     */
    fun isRevoked(): Boolean = revokedAt != null

    /**
     * Revokes this token.
     */
    fun revoke(): UserRefreshToken = copy(revokedAt = Instant.now())

    /**
     * Creates a copy with the given ID.
     */
    fun withId(id: RefreshTokenId): UserRefreshToken = copy(id = id)

    companion object {
        /**
         * Creates a new refresh token.
         *
         * @param userId The user this token belongs to
         * @param tokenHash SHA-256 hash of the raw token
         * @param validityDays Number of days the token is valid
         */
        fun create(
            userId: UserId,
            tokenHash: String,
            validityDays: Long = 30
        ): UserRefreshToken = UserRefreshToken(
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = Instant.now().plusSeconds(validityDays * 24 * 60 * 60)
        )
    }
}
