package com.edgerush.lootman.domain.team.repository

import com.edgerush.datasync.entity.TeamMetadataEntity

/**
 * Repository interface for TeamMetadataEntity CRUD operations.
 *
 * Provides access to team metadata data at the entity level.
 */
interface TeamMetadataRepository {
    /**
     * Finds team metadata by its unique identifier (teamId).
     *
     * @param teamId The team's unique identifier
     * @return The team metadata entity if found, null otherwise
     */
    fun findById(teamId: Long): TeamMetadataEntity?

    /**
     * Checks if team metadata exists by ID.
     *
     * @param teamId The team's unique identifier
     * @return true if the team metadata exists, false otherwise
     */
    fun existsById(teamId: Long): Boolean

    /**
     * Finds all team metadata with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of team metadata entities
     */
    fun findAll(
        offset: Long,
        limit: Int,
    ): List<TeamMetadataEntity>

    /**
     * Counts all team metadata.
     *
     * @return The total count of team metadata
     */
    fun count(): Long

    /**
     * Finds team metadata by guild with pagination.
     *
     * @param guildId The guild identifier
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of team metadata entities for the guild
     */
    fun findByGuildId(
        guildId: Long,
        offset: Long,
        limit: Int,
    ): List<TeamMetadataEntity>

    /**
     * Counts team metadata for a guild.
     *
     * @param guildId The guild identifier
     * @return The count of team metadata for the guild
     */
    fun countByGuildId(guildId: Long): Long

    /**
     * Finds team metadata by region with pagination.
     *
     * @param region The region
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of team metadata entities for the region
     */
    fun findByRegion(
        region: String,
        offset: Long,
        limit: Int,
    ): List<TeamMetadataEntity>

    /**
     * Counts team metadata for a region.
     *
     * @param region The region
     * @return The count of team metadata for the region
     */
    fun countByRegion(region: String): Long

    /**
     * Saves a team metadata entity.
     *
     * @param entity The team metadata to save
     * @return The saved team metadata entity
     */
    fun save(entity: TeamMetadataEntity): TeamMetadataEntity

    /**
     * Deletes team metadata by ID.
     *
     * @param teamId The team ID to delete
     */
    fun delete(teamId: Long)
}
