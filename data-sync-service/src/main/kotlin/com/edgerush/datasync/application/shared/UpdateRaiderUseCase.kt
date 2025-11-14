package com.edgerush.datasync.application.shared

import com.edgerush.datasync.domain.shared.model.Raider
import com.edgerush.datasync.domain.shared.model.RaiderId
import com.edgerush.datasync.domain.shared.model.RaiderStatus
import com.edgerush.datasync.domain.shared.repository.RaiderRepository
import org.springframework.stereotype.Service

/**
 * Use case for updating raider information.
 */
@Service
class UpdateRaiderUseCase(
    private val raiderRepository: RaiderRepository
) {
    /**
     * Update raider status
     */
    fun execute(command: UpdateRaiderStatusCommand): Result<Raider> = runCatching {
        val raider = raiderRepository.findById(command.raiderId)
            ?: throw RaiderNotFoundException(command.raiderId)
        
        val updated = raider.updateStatus(command.newStatus)
        raiderRepository.save(updated)
    }
    
    /**
     * Update raider sync timestamp
     */
    fun updateSync(raiderId: RaiderId): Result<Raider> = runCatching {
        val raider = raiderRepository.findById(raiderId)
            ?: throw RaiderNotFoundException(raiderId)
        
        val updated = raider.updateSync()
        raiderRepository.save(updated)
    }
}

/**
 * Command to update raider status
 */
data class UpdateRaiderStatusCommand(
    val raiderId: RaiderId,
    val newStatus: RaiderStatus
)
