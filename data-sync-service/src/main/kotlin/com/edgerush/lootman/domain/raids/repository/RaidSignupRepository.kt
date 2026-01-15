package com.edgerush.lootman.domain.raids.repository

import com.edgerush.datasync.entity.RaidSignupEntity

/**
 * Repository interface for RaidSignup aggregate.
 *
 * Provides access to raid signup data.
 */
interface RaidSignupRepository {
    /**
     * Finds a signup by its unique identifier.
     *
     * @param id The signup's unique identifier
     * @return The signup entity if found, null otherwise
     */
    fun findById(id: Long): RaidSignupEntity?

    /**
     * Checks if a signup exists by ID.
     *
     * @param id The signup's unique identifier
     * @return true if the signup exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all signups with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of signup entities
     */
    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaidSignupEntity>

    /**
     * Counts all signups.
     *
     * @return The total count of signups
     */
    fun count(): Long

    /**
     * Finds signups by raid with pagination.
     *
     * @param raidId The raid identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of signup entities for the raid
     */
    fun findByRaidId(
        raidId: Long,
        offset: Long,
        limit: Int,
    ): List<RaidSignupEntity>

    /**
     * Counts signups for a raid.
     *
     * @param raidId The raid identifier
     * @return The count of signups for the raid
     */
    fun countByRaidId(raidId: Long): Long

    /**
     * Finds selected signups by raid with pagination.
     *
     * @param raidId The raid identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of selected signup entities for the raid
     */
    fun findSelectedByRaidId(
        raidId: Long,
        offset: Long,
        limit: Int,
    ): List<RaidSignupEntity>

    /**
     * Counts selected signups for a raid.
     *
     * @param raidId The raid identifier
     * @return The count of selected signups for the raid
     */
    fun countSelectedByRaidId(raidId: Long): Long

    /**
     * Finds signups by character with pagination.
     *
     * @param characterId The character identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of signup entities for the character
     */
    fun findByCharacterId(
        characterId: Long,
        offset: Long,
        limit: Int,
    ): List<RaidSignupEntity>

    /**
     * Counts signups for a character.
     *
     * @param characterId The character identifier
     * @return The count of signups for the character
     */
    fun countByCharacterId(characterId: Long): Long

    /**
     * Saves a signup entity.
     *
     * @param signup The signup to save
     * @return The saved signup entity
     */
    fun save(signup: RaidSignupEntity): RaidSignupEntity

    /**
     * Deletes a signup by ID.
     *
     * @param id The signup ID to delete
     */
    fun delete(id: Long)
}
