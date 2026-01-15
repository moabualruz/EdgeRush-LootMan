package com.edgerush.lootman.domain.team.repository

import com.edgerush.datasync.entity.TeamRaidDayEntity

/**
 * Repository interface for TeamRaidDayEntity CRUD operations.
 *
 * Provides access to team raid day data at the entity level.
 */
interface TeamRaidDayRepository {
    /**
     * Finds a team raid day by its unique identifier.
     *
     * @param id The team raid day's unique identifier
     * @return The team raid day entity if found, null otherwise
     */
    fun findById(id: Long): TeamRaidDayEntity?

    /**
     * Checks if a team raid day exists by ID.
     *
     * @param id The team raid day's unique identifier
     * @return true if the team raid day exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all team raid days with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of team raid day entities
     */
    fun findAll(
        offset: Long,
        limit: Int,
    ): List<TeamRaidDayEntity>

    /**
     * Counts all team raid days.
     *
     * @return The total count of team raid days
     */
    fun count(): Long

    /**
     * Finds team raid days by team with pagination.
     *
     * @param teamId The team identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of team raid day entities for the team
     */
    fun findByTeamId(
        teamId: Long,
        offset: Long,
        limit: Int,
    ): List<TeamRaidDayEntity>

    /**
     * Counts team raid days for a team.
     *
     * @param teamId The team identifier
     * @return The count of team raid days for the team
     */
    fun countByTeamId(teamId: Long): Long

    /**
     * Saves a team raid day entity.
     *
     * @param entity The team raid day to save
     * @return The saved team raid day entity
     */
    fun save(entity: TeamRaidDayEntity): TeamRaidDayEntity

    /**
     * Deletes a team raid day by ID.
     *
     * @param id The team raid day ID to delete
     */
    fun delete(id: Long)
}
