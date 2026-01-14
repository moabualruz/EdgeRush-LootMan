package com.edgerush.lootman.domain.raider.repository

import com.edgerush.datasync.entity.RaiderVaultSlotEntity

/**
 * Repository interface for RaiderVaultSlotEntity CRUD operations.
 *
 * Provides access to raider vault slot data.
 */
interface RaiderVaultSlotRepository {
    /**
     * Finds a vault slot by its unique identifier.
     *
     * @param id The vault slot's unique identifier
     * @return The vault slot entity if found, null otherwise
     */
    fun findById(id: Long): RaiderVaultSlotEntity?

    /**
     * Checks if a vault slot exists by ID.
     *
     * @param id The vault slot's unique identifier
     * @return true if the vault slot exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all vault slots with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of vault slot entities
     */
    fun findAll(offset: Long, limit: Int): List<RaiderVaultSlotEntity>

    /**
     * Counts all vault slots.
     *
     * @return The total count of vault slots
     */
    fun count(): Long

    /**
     * Finds vault slots by raider with pagination.
     *
     * @param raiderId The raider identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of vault slot entities for the raider
     */
    fun findByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderVaultSlotEntity>

    /**
     * Counts vault slots for a raider.
     *
     * @param raiderId The raider identifier
     * @return The count of vault slots for the raider
     */
    fun countByRaiderId(raiderId: Long): Long

    /**
     * Finds unlocked vault slots by raider with pagination.
     *
     * @param raiderId The raider identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of unlocked vault slot entities for the raider
     */
    fun findUnlockedByRaiderId(raiderId: Long, offset: Long, limit: Int): List<RaiderVaultSlotEntity>

    /**
     * Counts unlocked vault slots for a raider.
     *
     * @param raiderId The raider identifier
     * @return The count of unlocked vault slots for the raider
     */
    fun countUnlockedByRaiderId(raiderId: Long): Long

    /**
     * Saves a vault slot entity.
     *
     * @param entity The vault slot to save
     * @return The saved vault slot entity
     */
    fun save(entity: RaiderVaultSlotEntity): RaiderVaultSlotEntity

    /**
     * Deletes a vault slot by ID.
     *
     * @param id The vault slot ID to delete
     */
    fun delete(id: Long)
}
