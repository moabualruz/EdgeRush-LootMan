package com.edgerush.lootman.domain.attendance.repository

import com.edgerush.datasync.entity.AttendanceStatEntity

/**
 * Repository interface for AttendanceStatEntity CRUD operations.
 *
 * Provides access to attendance stat data at the entity level.
 */
interface AttendanceStatRepository {
    /**
     * Finds an attendance stat by its unique identifier.
     *
     * @param id The attendance stat's unique identifier
     * @return The attendance stat entity if found, null otherwise
     */
    fun findById(id: Long): AttendanceStatEntity?

    /**
     * Checks if an attendance stat exists by ID.
     *
     * @param id The attendance stat's unique identifier
     * @return true if the attendance stat exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all attendance stats with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of attendance stat entities
     */
    fun findAll(offset: Long, limit: Int): List<AttendanceStatEntity>

    /**
     * Counts all attendance stats.
     *
     * @return The total count of attendance stats
     */
    fun count(): Long

    /**
     * Finds attendance stats by character with pagination.
     *
     * @param characterId The character identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of attendance stat entities for the character
     */
    fun findByCharacterId(characterId: Long, offset: Long, limit: Int): List<AttendanceStatEntity>

    /**
     * Counts attendance stats for a character.
     *
     * @param characterId The character identifier
     * @return The count of attendance stats for the character
     */
    fun countByCharacterId(characterId: Long): Long

    /**
     * Finds attendance stats by team with pagination.
     *
     * @param teamId The team identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of attendance stat entities for the team
     */
    fun findByTeamId(teamId: Long, offset: Long, limit: Int): List<AttendanceStatEntity>

    /**
     * Counts attendance stats for a team.
     *
     * @param teamId The team identifier
     * @return The count of attendance stats for the team
     */
    fun countByTeamId(teamId: Long): Long

    /**
     * Finds attendance stats by season with pagination.
     *
     * @param seasonId The season identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of attendance stat entities for the season
     */
    fun findBySeasonId(seasonId: Long, offset: Long, limit: Int): List<AttendanceStatEntity>

    /**
     * Counts attendance stats for a season.
     *
     * @param seasonId The season identifier
     * @return The count of attendance stats for the season
     */
    fun countBySeasonId(seasonId: Long): Long

    /**
     * Saves an attendance stat entity.
     *
     * @param entity The attendance stat to save
     * @return The saved attendance stat entity
     */
    fun save(entity: AttendanceStatEntity): AttendanceStatEntity

    /**
     * Deletes an attendance stat by ID.
     *
     * @param id The attendance stat ID to delete
     */
    fun delete(id: Long)
}
