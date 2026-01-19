package com.edgerush.lootman.application.guild

import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterRepository
import com.edgerush.lootman.domain.auth.repository.UserRepository
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service to repair existing character data by populating guild info.
 *
 * This service is used to backfill guild_id for user_characters that were
 * synced before the guild info feature was added.
 */
@Service
@Transactional
class CharacterGuildDataRepairService(
    private val userRepository: UserRepository,
    private val userCharacterRepository: UserCharacterRepository,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
    private val userLinkageRefreshService: UserLinkageRefreshService,
) {
    private val logger = LoggerFactory.getLogger(CharacterGuildDataRepairService::class.java)

    /**
     * Repairs guild data for all user characters by matching them to existing raiders.
     * This is useful for backfilling guild_id on characters synced before this feature existed.
     *
     * @return Result with counts of updated characters and refreshed linkages
     */
    fun repairAllCharacterGuildData(): CharacterGuildRepairResult {
        logger.info("Starting character guild data repair for all users")

        // Build a map of tracked guilds for quick lookup
        val trackedGuilds = guildConfigurationRepository.findAll(offset = 0, limit = 100)
            .filter { it.syncEnabled }
            .associateBy { it.guildId to "${it.guildName.lowercase()}-${it.bnetRealmSlug?.lowercase()}" }

        var usersProcessed = 0
        var charactersUpdated = 0
        var linkagesRefreshed = 0
        var totalNewLinks = 0
        val errors = mutableListOf<String>()

        var offset = 0L
        val batchSize = 100

        do {
            val users = userRepository.findAll(offset, batchSize)

            for (user in users) {
                val userId = user.id ?: continue

                try {
                    // Get all characters for this user
                    val characters = userCharacterRepository.findAllByUserId(userId)

                    for (character in characters) {
                        // Skip if already has guild_id set
                        if (character.guildId != null) continue

                        // Try to find matching raider to get guild info
                        val raider = raiderEntityRepository.findByCharacterNameAndRealm(
                            character.name,
                            character.realm
                        )

                        if (raider != null && !raider.guildId.isNullOrBlank()) {
                            // Check if raider's guild is one we track
                            val guildConfig = guildConfigurationRepository.findByGuildId(raider.guildId!!)

                            if (guildConfig != null) {
                                // Update character with guild info from raider
                                val updatedCharacter = character.copy(
                                    guildName = guildConfig.guildName,
                                    guildRealm = guildConfig.bnetRealmSlug,
                                    guildId = raider.guildId
                                )
                                userCharacterRepository.save(updatedCharacter)
                                charactersUpdated++
                                logger.debug("Updated guild info for ${character.name}-${character.realm}: guild=${raider.guildId}")
                            }
                        }
                    }

                    // Refresh linkages to create raiders and links for characters
                    val refreshResult = userLinkageRefreshService.refreshUserLinkages(userId)
                    if (refreshResult.newLinksCreated > 0 || refreshResult.orphanedMappingsRemoved > 0) {
                        linkagesRefreshed++
                        totalNewLinks += refreshResult.newLinksCreated
                    }

                    usersProcessed++

                } catch (e: Exception) {
                    logger.error("Failed to repair data for user ${userId.value}: ${e.message}", e)
                    errors.add("User ${userId.value}: ${e.message}")
                }
            }

            offset += batchSize
        } while (users.size == batchSize)

        val result = CharacterGuildRepairResult(
            usersProcessed = usersProcessed,
            charactersUpdated = charactersUpdated,
            linkagesRefreshed = linkagesRefreshed,
            totalNewLinksCreated = totalNewLinks,
            errors = errors
        )

        logger.info("Character guild data repair completed: $result")
        return result
    }

    /**
     * Repairs guild data for a specific user's characters.
     *
     * @param userId The user ID to repair
     * @return Result of the repair operation
     */
    fun repairUserCharacterGuildData(userId: UserId): UserCharacterGuildRepairResult {
        logger.info("Starting character guild data repair for user ${userId.value}")

        var charactersUpdated = 0
        val errors = mutableListOf<String>()

        try {
            val characters = userCharacterRepository.findAllByUserId(userId)

            for (character in characters) {
                try {
                    // Try to find matching raider to get guild info
                    val raider = raiderEntityRepository.findByCharacterNameAndRealm(
                        character.name,
                        character.realm
                    )

                    if (raider != null && !raider.guildId.isNullOrBlank()) {
                        val guildConfig = guildConfigurationRepository.findByGuildId(raider.guildId!!)

                        if (guildConfig != null) {
                            // Update character with guild info
                            val needsUpdate = character.guildId != raider.guildId ||
                                character.guildName != guildConfig.guildName ||
                                character.guildRealm != guildConfig.bnetRealmSlug

                            if (needsUpdate) {
                                val updatedCharacter = character.copy(
                                    guildName = guildConfig.guildName,
                                    guildRealm = guildConfig.bnetRealmSlug,
                                    guildId = raider.guildId
                                )
                                userCharacterRepository.save(updatedCharacter)
                                charactersUpdated++
                                logger.info("Updated guild info for ${character.name}-${character.realm}: guild=${raider.guildId}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Failed to repair character ${character.name}-${character.realm}: ${e.message}")
                    errors.add("${character.name}-${character.realm}: ${e.message}")
                }
            }

            // Refresh linkages
            val refreshResult = userLinkageRefreshService.refreshUserLinkages(userId)

            return UserCharacterGuildRepairResult(
                userId = userId.value,
                totalCharacters = characters.size,
                charactersUpdated = charactersUpdated,
                newLinksCreated = refreshResult.newLinksCreated,
                orphanedMappingsRemoved = refreshResult.orphanedMappingsRemoved,
                errors = errors
            )

        } catch (e: Exception) {
            logger.error("Failed to repair data for user ${userId.value}: ${e.message}", e)
            return UserCharacterGuildRepairResult(
                userId = userId.value,
                totalCharacters = 0,
                charactersUpdated = 0,
                newLinksCreated = 0,
                orphanedMappingsRemoved = 0,
                errors = listOf("Fatal error: ${e.message}")
            )
        }
    }
}

data class CharacterGuildRepairResult(
    val usersProcessed: Int,
    val charactersUpdated: Int,
    val linkagesRefreshed: Int,
    val totalNewLinksCreated: Int,
    val errors: List<String>
) {
    val success: Boolean get() = errors.isEmpty()
}

data class UserCharacterGuildRepairResult(
    val userId: Long,
    val totalCharacters: Int,
    val charactersUpdated: Int,
    val newLinksCreated: Int,
    val orphanedMappingsRemoved: Int,
    val errors: List<String>
) {
    val success: Boolean get() = errors.isEmpty()
}
