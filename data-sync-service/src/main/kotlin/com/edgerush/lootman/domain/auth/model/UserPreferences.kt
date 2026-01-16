package com.edgerush.lootman.domain.auth.model

import com.edgerush.lootman.domain.shared.GuildId
import java.time.Instant

/**
 * User preferences for multi-guild support.
 *
 * Stores the user's currently selected character and last accessed guild
 * to provide continuity across sessions.
 */
data class UserPreferences(
    val id: UserPreferencesId? = null,
    val userId: UserId,
    val activeCharacterMappingId: UserCharacterMappingId?,
    val lastGuildId: GuildId?,
    val updatedAt: Instant = Instant.now(),
) {
    /**
     * Updates the active character selection.
     */
    fun withActiveCharacter(
        mappingId: UserCharacterMappingId,
        guildId: GuildId,
    ): UserPreferences =
        copy(
            activeCharacterMappingId = mappingId,
            lastGuildId = guildId,
            updatedAt = Instant.now(),
        )

    /**
     * Clears the active character selection (e.g., when character is unlinked).
     */
    fun clearActiveCharacter(): UserPreferences =
        copy(
            activeCharacterMappingId = null,
            updatedAt = Instant.now(),
        )

    companion object {
        /**
         * Creates a new user preferences entry with no active character.
         */
        fun create(userId: UserId): UserPreferences =
            UserPreferences(
                userId = userId,
                activeCharacterMappingId = null,
                lastGuildId = null,
            )

        /**
         * Creates a new user preferences entry with an initial active character.
         */
        fun create(
            userId: UserId,
            activeCharacterMappingId: UserCharacterMappingId,
            guildId: GuildId,
        ): UserPreferences =
            UserPreferences(
                userId = userId,
                activeCharacterMappingId = activeCharacterMappingId,
                lastGuildId = guildId,
            )
    }
}

/**
 * Value object for UserPreferences ID.
 */
data class UserPreferencesId(val value: Long) {
    init {
        require(value > 0) { "UserPreferencesId must be positive, got $value" }
    }
}
