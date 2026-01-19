package com.edgerush.lootman.application.guild

import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * Service to create raiders from Battle.net characters.
 *
 * This ensures that users can link to their characters even if they're not
 * tracked in WoWAudit. Battle.net characters are the source of truth for
 * character ownership.
 *
 * Flow:
 * 1. User logs in with Battle.net OAuth
 * 2. Battle.net characters are synced to user_characters table (with guild info)
 * 3. This service creates raider entries for max-level characters that don't have one
 * 4. Raiders are assigned to their actual guild (from Battle.net) if tracked
 * 5. User can now link to the raider via user_character_mappings
 * 6. WoWAudit sync will enrich the raider with additional data if tracked
 */
@Service
@Transactional
class BnetCharacterToRaiderSyncService(
    private val userCharacterRepository: UserCharacterRepository,
    private val raiderEntityRepository: RaiderEntityRepository,
) {
    private val logger = LoggerFactory.getLogger(BnetCharacterToRaiderSyncService::class.java)

    companion object {
        private const val MIN_LEVEL_FOR_RAIDER = 70  // Only create raiders for near-max level characters
    }

    /**
     * Ensures all max-level Battle.net characters for a user have corresponding raider entries.
     * Raiders are assigned to the character's actual guild if it's tracked, otherwise to the default guild.
     *
     * @param userId The user whose characters should be synced
     * @param defaultGuildId The fallback guild ID for characters not in a tracked guild
     * @return Result with counts of created/existing raiders
     */
    fun syncCharactersToRaiders(userId: UserId, defaultGuildId: String): BnetCharacterSyncResult {
        val characters = userCharacterRepository.findAllByUserId(userId)
            .filter { it.level >= MIN_LEVEL_FOR_RAIDER }  // Only process high-level characters

        var created = 0
        var existing = 0
        var updated = 0
        var errors = 0

        for (character in characters) {
            try {
                // Check if raider already exists
                val existingRaider = raiderEntityRepository.findByCharacterNameAndRealm(
                    character.name,
                    character.realm
                )

                // Use character's actual guild if tracked, otherwise use default
                val targetGuildId = character.guildId ?: defaultGuildId

                if (existingRaider != null) {
                    existing++

                    // Update raider if needed (blizzardId, guildId if changed)
                    var needsUpdate = false
                    var updatedRaider = existingRaider

                    // Update blizzardId if we have it and raider doesn't
                    if (character.blizzardId != null && existingRaider.blizzardId == null) {
                        updatedRaider = updatedRaider.copy(blizzardId = character.blizzardId)
                        needsUpdate = true
                    }

                    // Update guildId if character moved to a tracked guild and raider was BNET_ONLY
                    if (existingRaider.status == "BNET_ONLY" && character.guildId != null && existingRaider.guildId != character.guildId) {
                        updatedRaider = updatedRaider.copy(guildId = character.guildId)
                        needsUpdate = true
                        logger.info("Updating raider ${character.name}-${character.realm} guild from ${existingRaider.guildId} to ${character.guildId}")
                    }

                    if (needsUpdate) {
                        raiderEntityRepository.save(updatedRaider)
                        updated++
                    }
                } else {
                    // Create new raider from Battle.net character
                    val newRaider = RaiderEntity(
                        characterName = character.name,
                        realm = character.realm,
                        region = "eu", // Default to EU, could be derived from realm
                        guildId = targetGuildId,
                        wowauditId = null, // Not from WoWAudit
                        clazz = mapClassName(character.className),
                        spec = "", // Not available from Battle.net basic character data
                        role = guessRoleFromClass(character.className),
                        rank = null, // No rank from Battle.net
                        status = "BNET_ONLY", // Mark as Battle.net only (not tracked in WoWAudit)
                        note = null,
                        blizzardId = character.blizzardId,
                        trackingSince = OffsetDateTime.now(),
                        joinDate = null,
                        blizzardLastModified = null,
                        lastSync = OffsetDateTime.now()
                    )

                    raiderEntityRepository.save(newRaider)
                    created++

                    logger.info("Created raider from Battle.net character: ${character.name}-${character.realm} (guild: $targetGuildId)")
                }
            } catch (e: Exception) {
                logger.error("Failed to sync character ${character.name}-${character.realm}: ${e.message}", e)
                errors++
            }
        }

        val result = BnetCharacterSyncResult(
            userId = userId.value,
            totalCharacters = characters.size,
            raidersCreated = created,
            raidersExisting = existing,
            raidersUpdated = updated,
            errors = errors
        )

        logger.info("Battle.net character sync completed for user ${userId.value}: $result")
        return result
    }

    /**
     * Maps Battle.net class name to our internal format.
     */
    private fun mapClassName(className: String?): String {
        return when (className?.lowercase()) {
            "death knight" -> "Death Knight"
            "demon hunter" -> "Demon Hunter"
            else -> className ?: "Unknown"
        }
    }

    /**
     * Guesses role from class name (can be refined later).
     */
    private fun guessRoleFromClass(className: String?): String {
        return when (className?.lowercase()) {
            "warrior", "paladin", "death knight", "monk", "druid", "demon hunter" -> "Melee"
            "mage", "warlock", "hunter", "priest", "shaman", "evoker" -> "Ranged"
            "rogue" -> "Melee"
            else -> "DPS"
        }
    }
}

data class BnetCharacterSyncResult(
    val userId: Long,
    val totalCharacters: Int,
    val raidersCreated: Int,
    val raidersExisting: Int,
    val raidersUpdated: Int,
    val errors: Int
) {
    val success: Boolean get() = errors == 0
}
