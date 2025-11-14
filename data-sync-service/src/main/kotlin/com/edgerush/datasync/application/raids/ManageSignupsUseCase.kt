package com.edgerush.datasync.application.raids

import com.edgerush.datasync.domain.raids.model.Raid
import com.edgerush.datasync.domain.raids.model.RaidId
import com.edgerush.datasync.domain.raids.model.RaidRole
import com.edgerush.datasync.domain.raids.model.RaidSignup
import com.edgerush.datasync.domain.raids.model.RaiderId
import com.edgerush.datasync.domain.raids.repository.RaidRepository
import org.springframework.stereotype.Service

/**
 * Use case for managing raid signups.
 * 
 * This use case handles:
 * - Adding signups to raids
 * - Removing signups from raids
 * - Updating signup status
 * - Selecting signups for the raid roster
 */
@Service
class ManageSignupsUseCase(
    private val raidRepository: RaidRepository
) {
    
    /**
     * Add a signup to a raid.
     * 
     * @param command The command containing signup details
     * @return Result containing the updated Raid or an exception
     */
    fun addSignup(command: AddSignupCommand): Result<Raid> = runCatching {
        val raidId = RaidId(command.raidId)
        val raid = raidRepository.findById(raidId)
            ?: throw IllegalArgumentException("Raid not found: ${command.raidId}")
        
        // Parse role
        val role = RaidRole.fromString(command.role)
            ?: throw IllegalArgumentException("Invalid role: ${command.role}")
        
        // Add signup to raid
        val updatedRaid = raid.addSignup(
            raider = RaiderId(command.raiderId),
            role = role,
            comment = command.comment
        )
        
        // Persist the updated raid
        raidRepository.save(updatedRaid)
    }
    
    /**
     * Remove a signup from a raid.
     * 
     * @param command The command containing signup removal details
     * @return Result containing the updated Raid or an exception
     */
    fun removeSignup(command: RemoveSignupCommand): Result<Raid> = runCatching {
        val raidId = RaidId(command.raidId)
        val raid = raidRepository.findById(raidId)
            ?: throw IllegalArgumentException("Raid not found: ${command.raidId}")
        
        // Remove signup from raid
        val updatedRaid = raid.removeSignup(RaiderId(command.raiderId))
        
        // Persist the updated raid
        raidRepository.save(updatedRaid)
    }
    
    /**
     * Update a signup status.
     * 
     * @param command The command containing status update details
     * @return Result containing the updated Raid or an exception
     */
    fun updateSignupStatus(command: UpdateSignupStatusCommand): Result<Raid> = runCatching {
        val raidId = RaidId(command.raidId)
        val raid = raidRepository.findById(raidId)
            ?: throw IllegalArgumentException("Raid not found: ${command.raidId}")
        
        // Parse status
        val status = RaidSignup.SignupStatus.fromString(command.status)
            ?: throw IllegalArgumentException("Invalid status: ${command.status}")
        
        // Update signup status
        val updatedRaid = raid.updateSignupStatus(
            raiderId = RaiderId(command.raiderId),
            newStatus = status
        )
        
        // Persist the updated raid
        raidRepository.save(updatedRaid)
    }
    
    /**
     * Select a signup for the raid roster.
     * 
     * @param command The command containing selection details
     * @return Result containing the updated Raid or an exception
     */
    fun selectSignup(command: SelectSignupCommand): Result<Raid> = runCatching {
        val raidId = RaidId(command.raidId)
        val raid = raidRepository.findById(raidId)
            ?: throw IllegalArgumentException("Raid not found: ${command.raidId}")
        
        // Select signup
        val updatedRaid = raid.selectSignup(RaiderId(command.raiderId))
        
        // Persist the updated raid
        raidRepository.save(updatedRaid)
    }
}
