package com.edgerush.lootman.domain.guild.repository

import com.edgerush.lootman.domain.guild.model.Guild
import com.edgerush.lootman.domain.shared.GuildId

/**
 * Repository interface for Guild aggregate persistence.
 */
interface GuildRepository {
    /**
     * Save a guild (create or update).
     *
     * @param guild The guild to save
     * @return The saved guild
     */
    fun save(guild: Guild): Guild

    /**
     * Find a guild by its ID.
     *
     * @param id The guild ID
     * @return The guild if found, null otherwise
     */
    fun findById(id: GuildId): Guild?

    /**
     * Find all active guilds.
     *
     * @return List of all active guilds
     */
    fun findAllActive(): List<Guild>

    /**
     * Find all guilds.
     *
     * @return List of all guilds
     */
    fun findAll(): List<Guild>

    /**
     * Delete a guild by its ID.
     *
     * @param id The guild ID
     * @return true if deleted, false if not found
     */
    fun deleteById(id: GuildId): Boolean

    /**
     * Check if a guild exists by ID.
     *
     * @param id The guild ID
     * @return true if exists, false otherwise
     */
    fun existsById(id: GuildId): Boolean
}
