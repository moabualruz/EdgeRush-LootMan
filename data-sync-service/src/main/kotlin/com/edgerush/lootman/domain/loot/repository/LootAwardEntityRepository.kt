package com.edgerush.lootman.domain.loot.repository

import com.edgerush.datasync.entity.LootAwardEntity

/**
 * Repository interface for LootAwardEntity CRUD operations.
 *
 * Provides access to loot award data at the entity level.
 */
interface LootAwardEntityRepository {
    /**
     * Finds a loot award by its unique identifier.
     *
     * @param id The loot award's unique identifier
     * @return The loot award entity if found, null otherwise
     */
    fun findById(id: Long): LootAwardEntity?

    /**
     * Checks if a loot award exists by ID.
     *
     * @param id The loot award's unique identifier
     * @return true if the loot award exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all loot awards with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of loot award entities
     */
    fun findAll(
        offset: Long,
        limit: Int,
    ): List<LootAwardEntity>

    /**
     * Counts all loot awards.
     *
     * @return The total count of loot awards
     */
    fun count(): Long

    /**
     * Finds loot awards by raider with pagination.
     *
     * @param raiderId The raider identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of loot award entities for the raider
     */
    fun findByRaiderId(
        raiderId: Long,
        offset: Long,
        limit: Int,
    ): List<LootAwardEntity>

    /**
     * Counts loot awards for a raider.
     *
     * @param raiderId The raider identifier
     * @return The count of loot awards for the raider
     */
    fun countByRaiderId(raiderId: Long): Long

    /**
     * Finds loot awards by item with pagination.
     *
     * @param itemId The item identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of loot award entities for the item
     */
    fun findByItemId(
        itemId: Long,
        offset: Long,
        limit: Int,
    ): List<LootAwardEntity>

    /**
     * Counts loot awards for an item.
     *
     * @param itemId The item identifier
     * @return The count of loot awards for the item
     */
    fun countByItemId(itemId: Long): Long

    /**
     * Finds loot awards by tier with pagination.
     *
     * @param tier The loot tier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of loot award entities for the tier
     */
    fun findByTier(
        tier: String,
        offset: Long,
        limit: Int,
    ): List<LootAwardEntity>

    /**
     * Counts loot awards for a tier.
     *
     * @param tier The loot tier
     * @return The count of loot awards for the tier
     */
    fun countByTier(tier: String): Long

    /**
     * Saves a loot award entity.
     *
     * @param entity The loot award to save
     * @return The saved loot award entity
     */
    fun save(entity: LootAwardEntity): LootAwardEntity

    /**
     * Deletes a loot award by ID.
     *
     * @param id The loot award ID to delete
     */
    fun delete(id: Long)
}
