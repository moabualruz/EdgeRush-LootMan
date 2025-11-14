package com.edgerush.datasync.repository

import com.edgerush.datasync.entity.RaidSignupEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RaidSignupRepository : CrudRepository<RaidSignupEntity, Long> {
    /**
     * Find all signups for a specific raid.
     * 
     * @param raidId The raid ID to search for
     * @return List of signups for the specified raid
     */
    fun findByRaidId(raidId: Long): List<RaidSignupEntity>
    
    /**
     * Delete all signups for a specific raid.
     * 
     * @param raidId The raid ID whose signups should be deleted
     */
    fun deleteByRaidId(raidId: Long)
}
