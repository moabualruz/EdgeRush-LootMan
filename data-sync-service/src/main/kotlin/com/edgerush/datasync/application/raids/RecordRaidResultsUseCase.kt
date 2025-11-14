package com.edgerush.datasync.application.raids

import com.edgerush.datasync.domain.raids.model.Raid
import com.edgerush.datasync.domain.raids.model.RaidId
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import org.springframework.stereotype.Service

/**
 * Use case for recording raid results and managing raid lifecycle.
 * 
 * This use case handles:
 * - Starting raids
 * - Completing raids
 * - Cancelling raids
 */
@Service
class RecordRaidResultsUseCase(
    private val raidRepository: RaidRepository
) {
    
    /**
     * Start a raid.
     * 
     * @param command The command containing raid start details
     * @return Result containing the updated Raid or an exception
     */
    fun startRaid(command: StartRaidCommand): Result<Raid> = runCatching {
        val raidId = RaidId(command.raidId)
        val raid = raidRepository.findById(raidId)
            ?: throw IllegalArgumentException("Raid not found: ${command.raidId}")
        
        // Start the raid
        val startedRaid = raid.start()
        
        // Persist the updated raid
        raidRepository.save(startedRaid)
    }
    
    /**
     * Complete a raid.
     * 
     * @param command The command containing raid completion details
     * @return Result containing the updated Raid or an exception
     */
    fun completeRaid(command: CompleteRaidCommand): Result<Raid> = runCatching {
        val raidId = RaidId(command.raidId)
        val raid = raidRepository.findById(raidId)
            ?: throw IllegalArgumentException("Raid not found: ${command.raidId}")
        
        // Complete the raid
        val completedRaid = raid.complete()
        
        // Persist the updated raid
        raidRepository.save(completedRaid)
    }
    
    /**
     * Cancel a raid.
     * 
     * @param command The command containing raid cancellation details
     * @return Result containing the updated Raid or an exception
     */
    fun cancelRaid(command: CancelRaidCommand): Result<Raid> = runCatching {
        val raidId = RaidId(command.raidId)
        val raid = raidRepository.findById(raidId)
            ?: throw IllegalArgumentException("Raid not found: ${command.raidId}")
        
        // Cancel the raid
        val cancelledRaid = raid.cancel()
        
        // Persist the updated raid
        raidRepository.save(cancelledRaid)
    }
}
