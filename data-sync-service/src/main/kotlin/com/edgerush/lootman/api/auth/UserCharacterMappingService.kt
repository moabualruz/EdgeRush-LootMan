package com.edgerush.lootman.api.auth

import com.edgerush.lootman.domain.auth.model.UserCharacterMapping
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterMappingRepository
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Exception thrown when a character mapping is not found.
 */
class CharacterMappingNotFoundException(val mappingId: Long) :
    RuntimeException("Character mapping not found: $mappingId")

/**
 * Exception thrown when a character is already linked to the user.
 */
class CharacterAlreadyLinkedException(val userId: Long, val raiderId: Long) :
    RuntimeException("Character $raiderId is already linked to user $userId")

/**
 * Service for managing user-character mappings.
 */
@Service
@Transactional
class UserCharacterMappingService(
    private val repository: UserCharacterMappingRepository,
) {
    /**
     * Gets all character mappings for a user.
     */
    @Transactional(readOnly = true)
    fun getCharactersForUser(userId: UserId): List<UserCharacterMappingResponse> {
        return repository.findByUserId(userId).map { UserCharacterMappingResponse.from(it) }
    }

    /**
     * Gets the primary character mapping for a user.
     */
    @Transactional(readOnly = true)
    fun getPrimaryCharacterForUser(userId: UserId): UserCharacterMappingResponse? {
        return repository.findPrimaryByUserId(userId)?.let { UserCharacterMappingResponse.from(it) }
    }

    /**
     * Links a character to a user.
     */
    fun linkCharacter(
        userId: UserId,
        request: LinkCharacterRequest,
    ): UserCharacterMappingResponse {
        val raiderId = RaiderId(request.raiderId)

        // Check for duplicate
        if (repository.existsByUserIdAndRaiderId(userId, raiderId)) {
            throw CharacterAlreadyLinkedException(userId.value, raiderId.value)
        }

        // If this is set as primary, clear existing primary
        if (request.isPrimary) {
            repository.clearPrimaryForUser(userId)
        }

        // If no characters exist for this user, make it primary automatically
        val isPrimary = request.isPrimary || repository.countByUserId(userId) == 0L

        val mapping =
            UserCharacterMapping.create(
                userId = userId,
                raiderId = raiderId,
                isPrimary = isPrimary,
            )

        val savedMapping = repository.save(mapping)
        return UserCharacterMappingResponse.from(savedMapping)
    }

    /**
     * Unlinks a character from a user.
     */
    fun unlinkCharacter(
        userId: UserId,
        mappingId: Long,
    ) {
        val mapping =
            repository.findById(UserCharacterMappingId(mappingId))
                ?: throw CharacterMappingNotFoundException(mappingId)

        // Verify ownership
        if (mapping.userId != userId) {
            throw CharacterMappingNotFoundException(mappingId)
        }

        repository.deleteById(mapping.id!!)

        // If we deleted the primary character, promote another one
        if (mapping.isPrimary) {
            val remainingMappings = repository.findByUserId(userId)
            if (remainingMappings.isNotEmpty()) {
                val newPrimary = remainingMappings.first().markAsPrimary()
                repository.save(newPrimary)
            }
        }
    }

    /**
     * Sets a character as the primary for a user.
     */
    fun setPrimaryCharacter(
        userId: UserId,
        mappingId: Long,
    ): UserCharacterMappingResponse {
        val mapping =
            repository.findById(UserCharacterMappingId(mappingId))
                ?: throw CharacterMappingNotFoundException(mappingId)

        // Verify ownership
        if (mapping.userId != userId) {
            throw CharacterMappingNotFoundException(mappingId)
        }

        // Clear other primary mappings
        repository.clearPrimaryForUser(userId)

        // Set this one as primary
        val updatedMapping = repository.save(mapping.markAsPrimary())
        return UserCharacterMappingResponse.from(updatedMapping)
    }

    /**
     * Gets the count of linked characters for a user.
     */
    @Transactional(readOnly = true)
    fun getCharacterCount(userId: UserId): CharacterCountResponse {
        return CharacterCountResponse(repository.countByUserId(userId))
    }
}
