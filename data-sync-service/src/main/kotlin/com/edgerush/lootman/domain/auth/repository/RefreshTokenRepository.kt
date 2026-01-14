package com.edgerush.lootman.domain.auth.repository

import com.edgerush.lootman.domain.auth.model.RefreshTokenId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserRefreshToken

/**
 * Repository interface for UserRefreshToken operations.
 */
interface RefreshTokenRepository {

    /**
     * Finds a refresh token by its ID.
     *
     * @param id The token's unique identifier
     * @return The token if found, null otherwise
     */
    fun findById(id: RefreshTokenId): UserRefreshToken?

    /**
     * Finds a refresh token by its hash.
     *
     * @param tokenHash SHA-256 hash of the raw token
     * @return The token if found, null otherwise
     */
    fun findByTokenHash(tokenHash: String): UserRefreshToken?

    /**
     * Finds all refresh tokens for a user.
     *
     * @param userId The user's ID
     * @return List of refresh tokens for the user
     */
    fun findByUserId(userId: UserId): List<UserRefreshToken>

    /**
     * Finds all valid (non-expired, non-revoked) tokens for a user.
     *
     * @param userId The user's ID
     * @return List of valid refresh tokens
     */
    fun findValidByUserId(userId: UserId): List<UserRefreshToken>

    /**
     * Saves a refresh token (creates or updates).
     *
     * @param token The token to save
     * @return The saved token with ID assigned
     */
    fun save(token: UserRefreshToken): UserRefreshToken

    /**
     * Deletes a refresh token by its ID.
     *
     * @param id The token ID to delete
     */
    fun deleteById(id: RefreshTokenId)

    /**
     * Deletes all refresh tokens for a user.
     *
     * @param userId The user's ID
     * @return Number of tokens deleted
     */
    fun deleteByUserId(userId: UserId): Int

    /**
     * Revokes all tokens for a user.
     *
     * @param userId The user's ID
     * @return Number of tokens revoked
     */
    fun revokeAllByUserId(userId: UserId): Int

    /**
     * Deletes all expired tokens (cleanup).
     *
     * @return Number of tokens deleted
     */
    fun deleteExpired(): Int
}
