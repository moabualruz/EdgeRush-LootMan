package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaidEncounterEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaidEncounterRepository : CrudRepository<RaidEncounterEntity, Long> {
    /**
     * Find all encounters for a specific raid.
     * 
     * @param raidId The raid ID to search for
     * @return List of encounters for the specified raid
     */
    fun findByRaidId(raidId: Long): List<RaidEncounterEntity>
    
    /**
     * Delete all encounters for a specific raid.
     * 
     * @param raidId The raid ID whose encounters should be deleted
     */
    fun deleteByRaidId(raidId: Long)
}
