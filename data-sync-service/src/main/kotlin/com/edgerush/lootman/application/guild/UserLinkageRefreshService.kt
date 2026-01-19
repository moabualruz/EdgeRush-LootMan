package com.edgerush.lootman.application.guild

import com.edgerush.lootman.domain.auth.model.UserCharacterMapping
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterMappingRepository
import com.edgerush.lootman.domain.auth.repository.UserCharacterRepository
import com.edgerush.lootman.domain.auth.repository.UserPreferencesRepository
import com.edgerush.lootman.domain.auth.repository.UserRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.repository.RaiderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to refresh and repair user linkages.
 *
 * This service handles:
 * - Removing orphaned character mappings (raider was deleted)
 * - Auto-linking user characters to matching guild raiders
 * - Fixing broken preferences (active mapping no longer exists)
 * - Ensuring one character is marked as primary
 * - Verifying guild associations are correct
 *
 * Use this service to repair data inconsistencies after migrations,
 * roster syncs, or to fix user account issues.
 */
@Service
@Transactional
class UserLinkageRefreshService(
    private val userRepository: UserRepository,
    private val userCharacterRepository: UserCharacterRepository,
    private val userCharacterMappingRepository: UserCharacterMappingRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val raiderRepository: RaiderRepository,
    private val bnetCharacterToRaiderSyncService: BnetCharacterToRaiderSyncService,
) {
    private val logger = LoggerFactory.getLogger(UserLinkageRefreshService::class.java)

    companion object {
        private const val DEFAULT_GUILD_ID = "default"
    }

    /**
     * Refreshes all linkages for a specific user.
     *
     * @param userId The user ID to refresh
     * @return Result of the refresh operation
     */
    fun refreshUserLinkages(userId: UserId): UserLinkageRefreshResult {
        logger.info("Starting linkage refresh for user ${userId.value}")

        var orphanedMappingsRemoved = 0
        var newLinksCreated = 0
        var preferencesFixed = false
        var primaryFixed = false
        val errors = mutableListOf<String>()

        try {
            // Step 0: Create raiders from Battle.net characters if they don't exist
            // This ensures all user characters can be linked even if not in WoWAudit
            val syncResult = bnetCharacterToRaiderSyncService.syncCharactersToRaiders(userId, DEFAULT_GUILD_ID)
            if (syncResult.raidersCreated > 0) {
                logger.info("Created ${syncResult.raidersCreated} raiders from Battle.net characters for user ${userId.value}")
            }

            // Step 1: Remove orphaned mappings (where raider no longer exists)
            orphanedMappingsRemoved = removeOrphanedMappings(userId)

            // Step 2: Auto-link characters to matching raiders
            newLinksCreated = autoLinkCharacters(userId)

            // Step 3: Fix preferences if active mapping is invalid
            preferencesFixed = fixBrokenPreferences(userId)

            // Step 4: Ensure at least one character is primary
            primaryFixed = ensurePrimaryCharacter(userId)

        } catch (e: Exception) {
            logger.error("Error refreshing linkages for user ${userId.value}: ${e.message}", e)
            errors.add("Unexpected error: ${e.message}")
        }

        val result = UserLinkageRefreshResult(
            userId = userId.value,
            orphanedMappingsRemoved = orphanedMappingsRemoved,
            newLinksCreated = newLinksCreated,
            preferencesFixed = preferencesFixed,
            primaryFixed = primaryFixed,
            errors = errors
        )

        logger.info("Linkage refresh completed for user ${userId.value}: $result")
        return result
    }

    /**
     * Refreshes linkages for all users.
     * Use with caution on large databases.
     *
     * @return Aggregate result of all refresh operations
     */
    fun refreshAllUserLinkages(batchSize: Int = 100): AllUsersLinkageRefreshResult {
        logger.info("Starting linkage refresh for all users")

        var usersProcessed = 0
        var totalOrphanedRemoved = 0
        var totalLinksCreated = 0
        var totalPreferencesFixed = 0
        var totalPrimaryFixed = 0
        val userErrors = mutableMapOf<Long, List<String>>()

        var offset = 0L
        var batch: List<com.edgerush.lootman.domain.auth.model.User>

        do {
            batch = userRepository.findAll(offset, batchSize)

            for (user in batch) {
                val userId = user.id ?: continue

                try {
                    val result = refreshUserLinkages(userId)
                    usersProcessed++
                    totalOrphanedRemoved += result.orphanedMappingsRemoved
                    totalLinksCreated += result.newLinksCreated
                    if (result.preferencesFixed) totalPreferencesFixed++
                    if (result.primaryFixed) totalPrimaryFixed++
                    if (result.errors.isNotEmpty()) {
                        userErrors[userId.value] = result.errors
                    }
                } catch (e: Exception) {
                    logger.error("Failed to refresh user ${userId.value}: ${e.message}", e)
                    userErrors[userId.value] = listOf("Failed: ${e.message}")
                }
            }

            offset += batchSize
        } while (batch.size == batchSize)

        val result = AllUsersLinkageRefreshResult(
            usersProcessed = usersProcessed,
            totalOrphanedMappingsRemoved = totalOrphanedRemoved,
            totalNewLinksCreated = totalLinksCreated,
            totalPreferencesFixed = totalPreferencesFixed,
            totalPrimaryFixed = totalPrimaryFixed,
            userErrors = userErrors
        )

        logger.info("All users linkage refresh completed: $result")
        return result
    }

    /**
     * Removes character mappings where the linked raider no longer exists.
     */
    private fun removeOrphanedMappings(userId: UserId): Int {
        val mappings = userCharacterMappingRepository.findByUserId(userId)
        var removed = 0

        for (mapping in mappings) {
            val raider = raiderEntityRepository.findById(mapping.raiderId.value)
            if (raider == null) {
                logger.info("Removing orphaned mapping for user ${userId.value}: raiderId=${mapping.raiderId.value} no longer exists")
                mapping.id?.let { userCharacterMappingRepository.deleteById(it) }
                removed++
            }
        }

        return removed
    }

    /**
     * Auto-links user's Battle.net characters to matching raiders in guild rosters.
     */
    private fun autoLinkCharacters(userId: UserId): Int {
        val characters = userCharacterRepository.findAllByUserId(userId)
        val existingMappings = userCharacterMappingRepository.findByUserId(userId)
        val linkedRaiderIds = existingMappings.map { it.raiderId.value }.toSet()

        var linked = 0

        for (character in characters) {
            try {
                // Find matching raider by name and realm
                val raider = raiderRepository.findByCharacterNameAndRealm(character.name, character.realm)

                if (raider != null && raider.id.value !in linkedRaiderIds) {
                    // Check if mapping already exists (double-check)
                    if (!userCharacterMappingRepository.existsByUserIdAndRaiderId(userId, raider.id)) {
                        val isPrimary = existingMappings.isEmpty() && linked == 0

                        val mapping = UserCharacterMapping.create(
                            userId = userId,
                            raiderId = raider.id,
                            isPrimary = isPrimary
                        )

                        userCharacterMappingRepository.save(mapping)
                        linked++

                        logger.info(
                            "Auto-linked character ${character.name}-${character.realm} to raider ${raider.id.value} " +
                                "(guild: ${raider.guildId.value}, rank: ${raider.rank})"
                        )
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to auto-link character ${character.name}-${character.realm}: ${e.message}")
            }
        }

        return linked
    }

    /**
     * Fixes preferences when active character mapping no longer exists.
     */
    private fun fixBrokenPreferences(userId: UserId): Boolean {
        val preferences = userPreferencesRepository.findByUserId(userId) ?: return false
        val activeMappingId = preferences.activeCharacterMappingId ?: return false

        // Check if active mapping still exists
        val activeMapping = userCharacterMappingRepository.findById(activeMappingId)
        if (activeMapping != null) {
            // Verify the raider still exists
            val raider = raiderEntityRepository.findById(activeMapping.raiderId.value)
            if (raider != null && !raider.guildId.isNullOrBlank()) {
                return false // Preferences are valid
            }
        }

        // Active mapping is broken, try to find a valid alternative
        val validMappings = userCharacterMappingRepository.findByUserId(userId)
            .filter { mapping ->
                val raider = raiderEntityRepository.findById(mapping.raiderId.value)
                raider != null && !raider.guildId.isNullOrBlank()
            }

        if (validMappings.isNotEmpty()) {
            val newActive = validMappings.find { it.isPrimary } ?: validMappings.first()
            val raider = raiderEntityRepository.findById(newActive.raiderId.value)!!
            val guildId = GuildId(raider.guildId!!)

            userPreferencesRepository.updateActiveCharacter(userId, newActive.id, guildId)
            logger.info("Fixed preferences for user ${userId.value}: new active mapping=${newActive.id?.value}")
            return true
        } else {
            // No valid mappings, clear active character
            userPreferencesRepository.updateActiveCharacter(userId, null, null)
            logger.info("Cleared broken preferences for user ${userId.value}: no valid mappings available")
            return true
        }
    }

    /**
     * Ensures at least one character mapping is marked as primary.
     */
    private fun ensurePrimaryCharacter(userId: UserId): Boolean {
        val mappings = userCharacterMappingRepository.findByUserId(userId)
        if (mappings.isEmpty()) return false

        val hasPrimary = mappings.any { it.isPrimary }
        if (hasPrimary) return false

        // No primary set, mark the first valid one as primary
        val validMappings = mappings.filter { mapping ->
            val raider = raiderEntityRepository.findById(mapping.raiderId.value)
            raider != null && !raider.guildId.isNullOrBlank()
        }

        if (validMappings.isNotEmpty()) {
            val newPrimary = validMappings.first()
            userCharacterMappingRepository.save(newPrimary.markAsPrimary())
            logger.info("Set primary character for user ${userId.value}: mappingId=${newPrimary.id?.value}")
            return true
        }

        return false
    }

    /**
     * Validates and reports on a user's current linkage state without making changes.
     */
    @Transactional(readOnly = true)
    fun validateUserLinkages(userId: UserId): UserLinkageValidationResult {
        val characters = userCharacterRepository.findAllByUserId(userId)
        val mappings = userCharacterMappingRepository.findByUserId(userId)
        val preferences = userPreferencesRepository.findByUserId(userId)

        val orphanedMappings = mutableListOf<Long>()
        val validMappings = mutableListOf<ValidMappingInfo>()
        val unlinkableCharacters = mutableListOf<UnlinkableCharacterInfo>()
        var preferencesValid = true
        var hasPrimary = false

        // Check each mapping
        for (mapping in mappings) {
            val raider = raiderEntityRepository.findById(mapping.raiderId.value)
            if (raider == null) {
                orphanedMappings.add(mapping.raiderId.value)
            } else {
                validMappings.add(
                    ValidMappingInfo(
                        mappingId = mapping.id!!.value,
                        raiderId = mapping.raiderId.value,
                        characterName = raider.characterName,
                        realm = raider.realm,
                        guildId = raider.guildId,
                        rank = raider.rank,
                        isPrimary = mapping.isPrimary
                    )
                )
                if (mapping.isPrimary) hasPrimary = true
            }
        }

        // Check which characters could be linked but aren't
        val linkedRaiderIds = validMappings.map { it.raiderId }.toSet()
        for (character in characters) {
            val raider = raiderRepository.findByCharacterNameAndRealm(character.name, character.realm)
            if (raider != null && raider.id.value !in linkedRaiderIds) {
                unlinkableCharacters.add(
                    UnlinkableCharacterInfo(
                        characterName = character.name,
                        realm = character.realm,
                        matchingRaiderId = raider.id.value,
                        matchingGuildId = raider.guildId.value,
                        matchingRank = raider.rank
                    )
                )
            }
        }

        // Check preferences
        if (preferences != null && preferences.activeCharacterMappingId != null) {
            val activeExists = mappings.any { it.id == preferences.activeCharacterMappingId }
            if (!activeExists) {
                preferencesValid = false
            } else {
                // Check if the active mapping's raider still exists with a guild
                val activeMapping = mappings.find { it.id == preferences.activeCharacterMappingId }
                if (activeMapping != null) {
                    val raider = raiderEntityRepository.findById(activeMapping.raiderId.value)
                    if (raider == null || raider.guildId.isNullOrBlank()) {
                        preferencesValid = false
                    }
                }
            }
        }

        return UserLinkageValidationResult(
            userId = userId.value,
            totalCharacters = characters.size,
            totalMappings = mappings.size,
            orphanedMappings = orphanedMappings,
            validMappings = validMappings,
            unlinkableCharacters = unlinkableCharacters,
            preferencesValid = preferencesValid,
            hasPrimaryCharacter = hasPrimary,
            needsRefresh = orphanedMappings.isNotEmpty() ||
                unlinkableCharacters.isNotEmpty() ||
                !preferencesValid ||
                (!hasPrimary && validMappings.isNotEmpty())
        )
    }
}

// Result DTOs

data class UserLinkageRefreshResult(
    val userId: Long,
    val orphanedMappingsRemoved: Int,
    val newLinksCreated: Int,
    val preferencesFixed: Boolean,
    val primaryFixed: Boolean,
    val errors: List<String>
) {
    val success: Boolean get() = errors.isEmpty()
}

data class AllUsersLinkageRefreshResult(
    val usersProcessed: Int,
    val totalOrphanedMappingsRemoved: Int,
    val totalNewLinksCreated: Int,
    val totalPreferencesFixed: Int,
    val totalPrimaryFixed: Int,
    val userErrors: Map<Long, List<String>>
) {
    val success: Boolean get() = userErrors.isEmpty()
}

data class UserLinkageValidationResult(
    val userId: Long,
    val totalCharacters: Int,
    val totalMappings: Int,
    val orphanedMappings: List<Long>,
    val validMappings: List<ValidMappingInfo>,
    val unlinkableCharacters: List<UnlinkableCharacterInfo>,
    val preferencesValid: Boolean,
    val hasPrimaryCharacter: Boolean,
    val needsRefresh: Boolean
)

data class ValidMappingInfo(
    val mappingId: Long,
    val raiderId: Long,
    val characterName: String,
    val realm: String,
    val guildId: String?,
    val rank: String?,
    val isPrimary: Boolean
)

data class UnlinkableCharacterInfo(
    val characterName: String,
    val realm: String,
    val matchingRaiderId: Long,
    val matchingGuildId: String,
    val matchingRank: String?
)
