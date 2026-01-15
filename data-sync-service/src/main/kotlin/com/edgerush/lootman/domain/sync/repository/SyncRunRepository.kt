package com.edgerush.lootman.domain.sync.repository

import com.edgerush.datasync.entity.SyncRunEntity

/**
 * Repository interface for SyncRunEntity CRUD operations.
 *
 * Provides access to sync run data at the entity level.
 */
interface SyncRunRepository {
    /**
     * Finds a sync run by its unique identifier.
     *
     * @param id The sync run's unique identifier
     * @return The sync run entity if found, null otherwise
     */
    fun findById(id: Long): SyncRunEntity?

    /**
     * Checks if a sync run exists by ID.
     *
     * @param id The sync run's unique identifier
     * @return true if the sync run exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all sync runs with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of sync run entities
     */
    fun findAll(
        offset: Long,
        limit: Int,
    ): List<SyncRunEntity>

    /**
     * Counts all sync runs.
     *
     * @return The total count of sync runs
     */
    fun count(): Long

    /**
     * Finds sync runs by source with pagination.
     *
     * @param source The sync source
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of sync run entities for the source
     */
    fun findBySource(
        source: String,
        offset: Long,
        limit: Int,
    ): List<SyncRunEntity>

    /**
     * Counts sync runs for a source.
     *
     * @param source The sync source
     * @return The count of sync runs for the source
     */
    fun countBySource(source: String): Long

    /**
     * Finds sync runs by status with pagination.
     *
     * @param status The sync status
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of sync run entities with the status
     */
    fun findByStatus(
        status: String,
        offset: Long,
        limit: Int,
    ): List<SyncRunEntity>

    /**
     * Counts sync runs for a status.
     *
     * @param status The sync status
     * @return The count of sync runs with the status
     */
    fun countByStatus(status: String): Long

    /**
     * Saves a sync run entity.
     *
     * @param entity The sync run to save
     * @return The saved sync run entity
     */
    fun save(entity: SyncRunEntity): SyncRunEntity

    /**
     * Deletes a sync run by ID.
     *
     * @param id The sync run ID to delete
     */
    fun delete(id: Long)
}
