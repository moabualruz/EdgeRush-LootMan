package com.edgerush.datasync.domain.shared.repository

import com.edgerush.datasync.domain.shared.model.Guild
import com.edgerush.datasync.domain.shared.model.GuildId

/**
 * Repository interface for Guild aggregate.
 */
interface GuildRepository {
    /**
     * Find a guild by ID
     */
    fun findById(id: GuildId): Guild?
    
    /**
     * Find an active guild by ID
     */
    fun findActiveById(id: GuildId): Guild?
    
    /**
     * Find all guilds
     */
    fun findAll(): List<Guild>
    
    /**
     * Find all active guilds
     */
    fun findAllActive(): List<Guild>
    
    /**
     * Save a guild (insert or update)
     */
    fun save(guild: Guild): Guild
    
    /**
     * Delete a guild by ID
     */
    fun deleteById(id: GuildId)
}
