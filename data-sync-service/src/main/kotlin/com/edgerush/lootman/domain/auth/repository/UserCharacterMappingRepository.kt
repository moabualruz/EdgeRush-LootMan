package com.edgerush.lootman.domain.auth.repository

import com.edgerush.lootman.domain.auth.model.UserCharacterMapping
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.shared.RaiderId

/**
 * Repository interface for UserCharacterMapping operations.
 */
interface UserCharacterMappingRepository {
    /**
     * Finds a mapping by its unique identifier.
     */
    fun findById(id: UserCharacterMappingId): UserCharacterMapping?

    /**
     * Finds all character mappings for a user.
     */
    fun findByUserId(userId: UserId): List<UserCharacterMapping>

    /**
     * Finds the primary character mapping for a user.
     */
    fun findPrimaryByUserId(userId: UserId): UserCharacterMapping?

    /**
     * Finds all user mappings for a raider.
     */
    fun findByRaiderId(raiderId: RaiderId): List<UserCharacterMapping>

    /**
     * Checks if a specific mapping exists.
     */
    fun existsByUserIdAndRaiderId(
        userId: UserId,
        raiderId: RaiderId,
    ): Boolean

    /**
     * Saves a mapping (creates or updates).
     */
    fun save(mapping: UserCharacterMapping): UserCharacterMapping

    /**
     * Deletes a mapping by its ID.
     */
    fun deleteById(id: UserCharacterMappingId)

    /**
     * Deletes all mappings for a user.
     */
    fun deleteByUserId(userId: UserId): Int

    /**
     * Clears the primary flag for all mappings of a user.
     */
    fun clearPrimaryForUser(userId: UserId)

    /**
     * Counts all mappings for a user.
     */
    fun countByUserId(userId: UserId): Long
}
