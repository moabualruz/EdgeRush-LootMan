package com.edgerush.lootman.domain.flps.repository

import com.edgerush.datasync.entity.FlpsDefaultModifierEntity

/**
 * Repository interface for FlpsDefaultModifier aggregate.
 *
 * Provides access to default FLPS modifier data.
 */
interface FlpsDefaultModifierRepository {
    /**
     * Finds a modifier by its unique identifier.
     *
     * @param id The modifier's unique identifier
     * @return The modifier entity if found, null otherwise
     */
    fun findById(id: Long): FlpsDefaultModifierEntity?

    /**
     * Checks if a modifier exists by ID.
     *
     * @param id The modifier's unique identifier
     * @return true if the modifier exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all modifiers with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of modifier entities
     */
    fun findAll(offset: Long, limit: Int): List<FlpsDefaultModifierEntity>

    /**
     * Counts all modifiers.
     *
     * @return The total count of modifiers
     */
    fun count(): Long

    /**
     * Finds modifiers by category with pagination.
     *
     * @param category The modifier category
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of modifier entities for the category
     */
    fun findByCategory(category: String, offset: Long, limit: Int): List<FlpsDefaultModifierEntity>

    /**
     * Counts modifiers for a category.
     *
     * @param category The modifier category
     * @return The count of modifiers for the category
     */
    fun countByCategory(category: String): Long

    /**
     * Saves a modifier entity.
     *
     * @param modifier The modifier to save
     * @return The saved modifier entity
     */
    fun save(modifier: FlpsDefaultModifierEntity): FlpsDefaultModifierEntity

    /**
     * Deletes a modifier by ID.
     *
     * @param id The modifier ID to delete
     */
    fun delete(id: Long)
}
