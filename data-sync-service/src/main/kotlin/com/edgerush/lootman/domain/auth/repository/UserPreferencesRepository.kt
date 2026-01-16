package com.edgerush.lootman.domain.auth.repository

import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserPreferences
import com.edgerush.lootman.domain.auth.model.UserPreferencesId
import com.edgerush.lootman.domain.shared.GuildId

/**
 * Repository for managing user preferences.
 */
interface UserPreferencesRepository {
    /**
     * Finds preferences by user ID.
     */
    fun findByUserId(userId: UserId): UserPreferences?

    /**
     * Finds preferences by ID.
     */
    fun findById(id: UserPreferencesId): UserPreferences?

    /**
     * Saves preferences (insert or update).
     */
    fun save(preferences: UserPreferences): UserPreferences

    /**
     * Updates the active character and guild for a user.
     * Creates preferences if they don't exist.
     */
    fun updateActiveCharacter(
        userId: UserId,
        mappingId: UserCharacterMappingId?,
        guildId: GuildId?,
    ): UserPreferences

    /**
     * Deletes preferences by user ID.
     */
    fun deleteByUserId(userId: UserId)
}
