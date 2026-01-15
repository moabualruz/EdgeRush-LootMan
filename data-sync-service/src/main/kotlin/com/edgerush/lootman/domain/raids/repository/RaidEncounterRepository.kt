package com.edgerush.lootman.domain.raids.repository

import com.edgerush.datasync.entity.RaidEncounterEntity

/**
 * Repository interface for RaidEncounter aggregate.
 *
 * Provides access to raid encounter data.
 */
interface RaidEncounterRepository {
    /**
     * Finds an encounter by its unique identifier.
     *
     * @param id The encounter's unique identifier
     * @return The encounter entity if found, null otherwise
     */
    fun findById(id: Long): RaidEncounterEntity?

    /**
     * Checks if an encounter exists by ID.
     *
     * @param id The encounter's unique identifier
     * @return true if the encounter exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all encounters with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of encounter entities
     */
    fun findAll(
        offset: Long,
        limit: Int,
    ): List<RaidEncounterEntity>

    /**
     * Counts all encounters.
     *
     * @return The total count of encounters
     */
    fun count(): Long

    /**
     * Finds encounters by raid with pagination.
     *
     * @param raidId The raid identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of encounter entities for the raid
     */
    fun findByRaidId(
        raidId: Long,
        offset: Long,
        limit: Int,
    ): List<RaidEncounterEntity>

    /**
     * Counts encounters for a raid.
     *
     * @param raidId The raid identifier
     * @return The count of encounters for the raid
     */
    fun countByRaidId(raidId: Long): Long

    /**
     * Finds enabled encounters by raid with pagination.
     *
     * @param raidId The raid identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of enabled encounter entities for the raid
     */
    fun findEnabledByRaidId(
        raidId: Long,
        offset: Long,
        limit: Int,
    ): List<RaidEncounterEntity>

    /**
     * Counts enabled encounters for a raid.
     *
     * @param raidId The raid identifier
     * @return The count of enabled encounters for the raid
     */
    fun countEnabledByRaidId(raidId: Long): Long

    /**
     * Saves an encounter entity.
     *
     * @param encounter The encounter to save
     * @return The saved encounter entity
     */
    fun save(encounter: RaidEncounterEntity): RaidEncounterEntity

    /**
     * Deletes an encounter by ID.
     *
     * @param id The encounter ID to delete
     */
    fun delete(id: Long)
}
