package com.edgerush.lootman.application.guild

import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.repository.UserCharacterMappingRepository
import com.edgerush.lootman.domain.auth.repository.UserPreferencesRepository
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import com.edgerush.lootman.domain.guild.repository.GuildPermissionRepository
import com.edgerush.lootman.domain.raider.repository.RaiderEntityRepository
import com.edgerush.lootman.domain.shared.GuildId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service for managing user guild context.
 *
 * Provides functionality to:
 * - Get all guilds a user has characters in
 * - Get/set the user's active character/guild
 * - Check permissions based on guild rank
 */
@Service
@Transactional
class GuildContextService(
    private val userCharacterMappingRepository: UserCharacterMappingRepository,
    private val raiderEntityRepository: RaiderEntityRepository,
    private val guildPermissionRepository: GuildPermissionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val guildConfigurationRepository: GuildConfigurationRepository,
) {
    private val logger = LoggerFactory.getLogger(GuildContextService::class.java)

    /**
     * Gets all guild contexts for a user.
     *
     * Returns a list of guilds the user has characters in, with their
     * character info, rank, and permissions for each guild.
     */
    fun getUserGuilds(userId: UserId): List<GuildContext> {
        val mappings = userCharacterMappingRepository.findByUserId(userId)
        if (mappings.isEmpty()) {
            return emptyList()
        }

        // Get active character mapping ID for marking active context
        val preferences = userPreferencesRepository.findByUserId(userId)
        val activeMappingId = preferences?.activeCharacterMappingId

        // First pass: collect all raiders and their guild/rank pairs
        data class RaiderInfo(
            val mapping: com.edgerush.lootman.domain.auth.model.UserCharacterMapping,
            val raider: com.edgerush.datasync.entity.RaiderEntity,
            val guildId: String,
            val guildName: String,
        )

        val raiderInfos =
            mappings.mapNotNull { mapping ->
                val raider = raiderEntityRepository.findById(mapping.raiderId.value)
                if (raider == null) {
                    logger.warn("Raider not found for mapping: userId=${userId.value}, raiderId=${mapping.raiderId.value}")
                    return@mapNotNull null
                }

                val guildId = raider.guildId
                if (guildId.isNullOrBlank()) {
                    logger.debug("Raider has no guild: raiderId=${raider.id}")
                    return@mapNotNull null
                }

                val guildConfig = guildConfigurationRepository.findByGuildId(guildId)
                val guildName = guildConfig?.guildName ?: guildId

                RaiderInfo(mapping, raider, guildId, guildName)
            }

        if (raiderInfos.isEmpty()) {
            return emptyList()
        }

        // Batch fetch all permissions for all guild/rank pairs
        val guildRankPairs =
            raiderInfos
                .filter { it.raider.rank != null }
                .map { Pair(GuildId(it.guildId), it.raider.rank!!) }
                .distinct()

        val permissionsByGuildRank =
            if (guildRankPairs.isNotEmpty()) {
                guildPermissionRepository.findByGuildIdAndRankNames(guildRankPairs)
            } else {
                emptyMap()
            }

        // Build guild contexts using the pre-fetched permissions
        return raiderInfos.map { info ->
            val permissions =
                if (info.raider.rank != null) {
                    permissionsByGuildRank[Pair(info.guildId, info.raider.rank)] ?: emptyList()
                } else {
                    emptyList()
                }

            GuildContext(
                guildId = info.guildId,
                guildName = info.guildName,
                characterName = info.raider.characterName,
                characterRealm = info.raider.realm,
                characterClass = info.raider.clazz,
                characterMappingId = info.mapping.id!!.value,
                raiderId = info.raider.id!!,
                rank = info.raider.rank,
                permissions = permissions,
                isActive = info.mapping.id == activeMappingId,
            )
        }
    }

    /**
     * Gets the active guild context for a user.
     *
     * Returns the context for the user's currently selected character,
     * or the first available context if none is selected.
     */
    fun getActiveGuildContext(userId: UserId): GuildContext? {
        val allContexts = getUserGuilds(userId)
        if (allContexts.isEmpty()) {
            return null
        }

        // Return the active one, or fall back to the first
        return allContexts.find { it.isActive } ?: allContexts.first()
    }

    /**
     * Sets the active character for a user.
     *
     * @param userId The user ID
     * @param mappingId The character mapping ID to set as active
     * @return The new active guild context
     * @throws IllegalArgumentException if the mapping doesn't belong to the user
     */
    fun setActiveCharacter(
        userId: UserId,
        mappingId: UserCharacterMappingId,
    ): GuildContext {
        // Verify the mapping belongs to this user
        val mapping =
            userCharacterMappingRepository.findById(mappingId)
                ?: throw IllegalArgumentException("Character mapping not found: ${mappingId.value}")

        if (mapping.userId != userId) {
            throw IllegalArgumentException("Character mapping does not belong to user")
        }

        // Get the raider to find the guild
        val raider =
            raiderEntityRepository.findById(mapping.raiderId.value)
                ?: throw IllegalArgumentException("Character not found for mapping")

        val guildId =
            raider.guildId?.let { GuildId(it) }
                ?: throw IllegalArgumentException("Character is not associated with a guild")

        // Update preferences
        userPreferencesRepository.updateActiveCharacter(userId, mappingId, guildId)

        // Return the new active context
        return getActiveGuildContext(userId)
            ?: throw IllegalStateException("Failed to get active context after update")
    }

    /**
     * Checks if a user has a specific permission in a guild.
     *
     * Permission is determined by the character's rank in that guild
     * and the guild's permission configuration.
     */
    fun hasGuildPermission(
        userId: UserId,
        guildId: GuildId,
        permission: GuildPermissionType,
    ): Boolean {
        // Find user's character in this guild
        val mappings = userCharacterMappingRepository.findByUserId(userId)
        val raiderInGuild =
            mappings
                .mapNotNull { raiderEntityRepository.findById(it.raiderId.value) }
                .find { it.guildId == guildId.value }

        if (raiderInGuild == null) {
            logger.debug("User ${userId.value} has no character in guild ${guildId.value}")
            return false
        }

        val rank = raiderInGuild.rank
        if (rank.isNullOrBlank()) {
            logger.debug("Character has no rank in guild ${guildId.value}")
            return false
        }

        return guildPermissionRepository.hasPermission(guildId, rank, permission)
    }

    /**
     * Checks if a user is a guild officer (has SETTINGS_ACCESS permission).
     */
    fun isGuildOfficer(
        userId: UserId,
        guildId: GuildId,
    ): Boolean = hasGuildPermission(userId, guildId, GuildPermissionType.SETTINGS_ACCESS)

    /**
     * Checks if the user's active guild context has a specific permission.
     */
    fun hasActiveGuildPermission(
        userId: UserId,
        permission: GuildPermissionType,
    ): Boolean {
        val activeContext = getActiveGuildContext(userId) ?: return false
        return activeContext.hasPermission(permission)
    }
}
