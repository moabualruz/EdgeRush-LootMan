package com.edgerush.datasync.application.shared

import com.edgerush.datasync.domain.shared.model.Raider
import com.edgerush.datasync.domain.shared.model.RaiderId
import com.edgerush.datasync.domain.shared.repository.RaiderRepository
import org.springframework.stereotype.Service

/**
 * Use case for retrieving raider information.
 */
@Service
class GetRaiderUseCase(
    private val raiderRepository: RaiderRepository
) {
    /**
     * Get a raider by ID
     */
    fun execute(raiderId: RaiderId): Result<Raider> = runCatching {
        raiderRepository.findById(raiderId)
            ?: throw RaiderNotFoundException(raiderId)
    }
    
    /**
     * Get a raider by character name and realm
     */
    fun execute(characterName: String, realm: String): Result<Raider> = runCatching {
        raiderRepository.findByCharacterNameAndRealm(characterName, realm)
            ?: throw RaiderNotFoundException("$characterName-$realm")
    }
    
    /**
     * Get all raiders
     */
    fun executeAll(): Result<List<Raider>> = runCatching {
        raiderRepository.findAll()
    }
    
    /**
     * Get all active raiders
     */
    fun executeAllActive(): Result<List<Raider>> = runCatching {
        raiderRepository.findAllActive()
    }
}

/**
 * Exception thrown when a raider is not found
 */
class RaiderNotFoundException : Exception {
    constructor(raiderId: RaiderId) : super("Raider not found: ${raiderId.value}")
    constructor(identifier: String) : super("Raider not found: $identifier")
}
