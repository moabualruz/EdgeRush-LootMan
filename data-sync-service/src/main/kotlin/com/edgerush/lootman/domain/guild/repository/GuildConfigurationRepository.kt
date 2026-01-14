package com.edgerush.lootman.domain.guild.repository

import com.edgerush.datasync.entity.GuildConfigurationEntity

/**
 * Repository interface for GuildConfigurationEntity CRUD operations.
 *
 * Provides access to guild configuration data.
 */
interface GuildConfigurationRepository {
    /**
     * Finds a guild configuration by its unique identifier.
     *
     * @param id The configuration's unique identifier
     * @return The guild configuration entity if found, null otherwise
     */
    fun findById(id: Long): GuildConfigurationEntity?

    /**
     * Finds a guild configuration by guild ID.
     *
     * @param guildId The guild identifier
     * @return The guild configuration entity if found, null otherwise
     */
    fun findByGuildId(guildId: String): GuildConfigurationEntity?

    /**
     * Checks if a guild configuration exists by ID.
     *
     * @param id The configuration's unique identifier
     * @return true if the configuration exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Finds all guild configurations with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of guild configuration entities
     */
    fun findAll(offset: Long, limit: Int): List<GuildConfigurationEntity>

    /**
     * Counts all guild configurations.
     *
     * @return The total count of guild configurations
     */
    fun count(): Long

    /**
     * Finds all active guild configurations with pagination.
     *
     * @param offset The number of records to skip
     * @param limit The maximum number of records to return
     * @return List of active guild configuration entities
     */
    fun findActive(offset: Long, limit: Int): List<GuildConfigurationEntity>

    /**
     * Counts all active guild configurations.
     *
     * @return The count of active guild configurations
     */
    fun countActive(): Long

    /**
     * Saves a guild configuration entity.
     *
     * @param entity The guild configuration to save
     * @return The saved guild configuration entity
     */
    fun save(entity: GuildConfigurationEntity): GuildConfigurationEntity

    /**
     * Deletes a guild configuration by ID.
     *
     * @param id The guild configuration ID to delete
     */
    fun delete(id: Long)
}
