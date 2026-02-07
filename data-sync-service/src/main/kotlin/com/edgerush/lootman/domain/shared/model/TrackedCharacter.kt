package com.edgerush.lootman.domain.shared.model

import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.shared.AccountId
import com.edgerush.lootman.domain.shared.CharacterId
import java.time.Instant

/**
 * A TrackedCharacter is a personal WoW character tracked via Battle.net OAuth.
 *
 * Unlike [Raider], a TrackedCharacter is not necessarily part of a guild roster.
 * It represents a character owned by a user that may or may not be in a tracked guild.
 *
 * TrackedCharacter extends [WoWCharacter] to share identity properties while adding
 * user-specific tracking information.
 */
data class TrackedCharacter(
    override val characterId: CharacterId,
    override val name: String,
    override val realm: String,
    override val region: String,
    override val characterClass: CharacterClass,
    override val blizzardId: Long?,
    override val accountId: AccountId?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    /**
     * The user who owns this character (via Battle.net OAuth).
     */
    val userId: UserId,
    /**
     * Character level in WoW.
     */
    val level: Int,
    /**
     * Character race (Human, Orc, etc.).
     */
    val race: String,
    /**
     * Character faction (Alliance, Horde).
     */
    val faction: String,
    /**
     * Character specialization ID (from Blizzard API).
     */
    val specId: Int?,
    /**
     * Current guild name (if any).
     */
    val guildName: String?,
    /**
     * Current guild realm (if different from character realm).
     */
    val guildRealm: String?,
    /**
     * Guild ID if the character is in a tracked guild.
     */
    val guildId: String?,
    /**
     * When this character was last synced from Battle.net.
     */
    val lastSyncedAt: Instant,
) : WoWCharacter(
        characterId = characterId,
        name = name,
        realm = realm,
        region = region,
        characterClass = characterClass,
        blizzardId = blizzardId,
        accountId = accountId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    ) {
    companion object {
        /**
         * Create a new TrackedCharacter from Battle.net API data.
         */
        fun create(
            characterId: CharacterId,
            userId: UserId,
            name: String,
            realm: String,
            region: String,
            characterClass: CharacterClass,
            level: Int,
            race: String,
            faction: String,
            blizzardId: Long?,
            accountId: AccountId? = null,
            specId: Int? = null,
            guildName: String? = null,
            guildRealm: String? = null,
            guildId: String? = null,
        ): TrackedCharacter {
            val now = Instant.now()
            return TrackedCharacter(
                characterId = characterId,
                name = name,
                realm = realm,
                region = region,
                characterClass = characterClass,
                blizzardId = blizzardId,
                accountId = accountId,
                createdAt = now,
                updatedAt = now,
                userId = userId,
                level = level,
                race = race,
                faction = faction,
                specId = specId,
                guildName = guildName,
                guildRealm = guildRealm,
                guildId = guildId,
                lastSyncedAt = now,
            )
        }
    }

    /**
     * Whether this character is in any guild.
     */
    fun isInGuild(): Boolean = guildName != null

    /**
     * Whether this character is in a tracked guild.
     */
    fun isInTrackedGuild(): Boolean = guildId != null
}
