package com.edgerush.lootman.domain.raids.repository

import com.edgerush.datasync.entity.RaidEntity
import java.time.LocalDate

/**
 * Repository interface for Raid aggregate.
 *
 * Provides access to raid data.
 */
interface RaidRepository {
    /**
     * Finds a raid by its unique identifier.
     *
     * @param raidId The raid's unique identifier
     * @return The raid entity if found, null otherwise
     */
    fun findById(raidId: Long): RaidEntity?

    /**
     * Checks if a raid exists by ID.
     *
     * @param raidId The raid's unique identifier
     * @return true if the raid exists, false otherwise
     */
    fun existsById(raidId: Long): Boolean

    /**
     * Finds all raids with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of raid entities
     */
    fun findAll(offset: Long, limit: Int): List<RaidEntity>

    /**
     * Counts all raids.
     *
     * @return The total count of raids
     */
    fun count(): Long

    /**
     * Finds raids by team with pagination.
     *
     * @param teamId The team identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of raid entities for the team
     */
    fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<RaidEntity>

    /**
     * Counts raids for a team.
     *
     * @param teamId The team identifier
     * @return The count of raids for the team
     */
    fun countByTeamId(teamId: Long): Long

    /**
     * Finds raids within a date range with pagination.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of raid entities within the date range
     */
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate, offset: Long, limit: Int): List<RaidEntity>

    /**
     * Counts raids within a date range.
     *
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return The count of raids within the date range
     */
    fun countByDateRange(startDate: LocalDate, endDate: LocalDate): Long

    /**
     * Saves a raid entity.
     *
     * @param raid The raid to save
     * @return The saved raid entity
     */
    fun save(raid: RaidEntity): RaidEntity

    /**
     * Deletes a raid by ID.
     *
     * @param raidId The raid ID to delete
     */
    fun delete(raidId: Long)
}
