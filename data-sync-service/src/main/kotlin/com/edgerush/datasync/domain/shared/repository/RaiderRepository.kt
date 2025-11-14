package com.edgerush.datasync.domain.shared.repository

import com.edgerush.datasync.domain.shared.model.Raider
import com.edgerush.datasync.domain.shared.model.RaiderId

/**
 * Repository interface for Raider aggregate.
 */
interface RaiderRepository {
    /**
     * Find a raider by ID
     */
    fun findById(id: RaiderId): Raider?
    
    /**
     * Find a raider by character name and realm
     */
    fun findByCharacterNameAndRealm(characterName: String, realm: String): Raider?
    
    /**
     * Find a raider by WoWAudit ID
     */
    fun findByWowauditId(wowauditId: Long): Raider?
    
    /**
     * Find all raiders
     */
    fun findAll(): List<Raider>
    
    /**
     * Find all active raiders
     */
    fun findAllActive(): List<Raider>
    
    /**
     * Save a raider (insert or update)
     */
    fun save(raider: Raider): Raider
    
    /**
     * Delete a raider by ID
     */
    fun deleteById(id: RaiderId)
}
