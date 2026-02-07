package com.edgerush.lootman.domain.shared.model

import com.edgerush.lootman.domain.shared.AccountId
import com.edgerush.lootman.domain.shared.CharacterId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import java.time.Instant
import java.time.LocalDateTime

/**
 * Raider is a WoWCharacter who is a member of a guild roster.
 *
 * Raider extends [WoWCharacter] to inherit character identity properties,
 * while adding guild-specific information such as role, rank, and status.
 *
 * The key distinction:
 * - [WoWCharacter] = identity (name, realm, region, class) - persists forever
 * - [Raider] = guild membership - can be created/deleted as players join/leave
 *
 * FLPS data and attendance should be linked to [characterId] (not [id]/RaiderId)
 * so that data persists when a player leaves and rejoins a guild.
 */
data class Raider(
    /**
     * Unique identifier for this raider's guild membership.
     * This is separate from characterId - a character can have multiple
     * RaiderIds if they're in multiple guilds or rejoin a guild.
     */
    val id: RaiderId,
    /**
     * Reference to the underlying character.
     * All performance data should be linked to this, not [id].
     */
    override val characterId: CharacterId,
    /**
     * Character name (inherited from WoWCharacter).
     */
    override val name: String,
    /**
     * Realm name (inherited from WoWCharacter).
     */
    override val realm: String,
    /**
     * Region (EU, US, etc.) - inherited from WoWCharacter.
     */
    override val region: String,
    /**
     * Character class (inherited from WoWCharacter).
     */
    override val characterClass: CharacterClass,
    /**
     * Blizzard ID (inherited from WoWCharacter).
     */
    override val blizzardId: Long?,
    /**
     * Account ID for aggregation (inherited from WoWCharacter).
     */
    override val accountId: AccountId?,
    /**
     * When the character record was created (inherited from WoWCharacter).
     */
    override val createdAt: Instant,
    /**
     * When the character record was last updated (inherited from WoWCharacter).
     */
    override val updatedAt: Instant,
    // ==================== RAIDER-SPECIFIC PROPERTIES ====================
    /**
     * Guild this raider belongs to.
     */
    val guildId: GuildId,
    /**
     * Raiding role (Tank, Healer, DPS).
     */
    val role: Role,
    /**
     * Guild rank (e.g., "Officer", "Raider", "Trial").
     */
    val rank: String?,
    /**
     * Current status in the guild roster.
     */
    val status: RaiderStatus,
    /**
     * When this player joined the guild.
     */
    val joinDate: LocalDateTime?,
    /**
     * WoWAudit ID for this raider (from WoWAudit sync).
     */
    val wowauditId: Long?,
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
    // Note: The parent class already validates name, realm, and region

    /**
     * Checks if the raider is eligible for loot based on their status.
     */
    fun isEligibleForLoot(): Boolean = status == RaiderStatus.ACTIVE

    /**
     * Gets the full character identifier (name-realm).
     * @deprecated Use [displayName] from WoWCharacter instead
     */
    @Deprecated("Use displayName from WoWCharacter", replaceWith = ReplaceWith("displayName"))
    fun getFullName(): String = displayName

    /**
     * Backward compatibility alias for name.
     * Some code uses characterName instead of name.
     */
    val characterName: String
        get() = name

    companion object {
        /**
         * Create a new Raider with all fields specified.
         */
        fun create(
            id: RaiderId,
            characterId: CharacterId,
            name: String,
            realm: String,
            region: String = "eu",
            characterClass: CharacterClass,
            guildId: GuildId,
            role: Role,
            rank: String? = null,
            status: RaiderStatus = RaiderStatus.ACTIVE,
            joinDate: LocalDateTime? = null,
            wowauditId: Long? = null,
            blizzardId: Long? = null,
            accountId: AccountId? = null,
        ): Raider {
            val now = Instant.now()
            return Raider(
                id = id,
                characterId = characterId,
                name = name,
                realm = realm,
                region = region,
                characterClass = characterClass,
                blizzardId = blizzardId,
                accountId = accountId,
                createdAt = now,
                updatedAt = now,
                guildId = guildId,
                role = role,
                rank = rank,
                status = status,
                joinDate = joinDate,
                wowauditId = wowauditId,
            )
        }

        /**
         * Create a Raider from an existing WoWCharacter.
         * Use when a character becomes a guild member.
         */
        fun fromCharacter(
            character: WoWCharacter,
            raiderId: RaiderId,
            guildId: GuildId,
            role: Role,
            rank: String? = null,
            status: RaiderStatus = RaiderStatus.ACTIVE,
            joinDate: LocalDateTime? = null,
            wowauditId: Long? = null,
        ): Raider {
            return Raider(
                id = raiderId,
                characterId = character.characterId,
                name = character.name,
                realm = character.realm,
                region = character.region,
                characterClass = character.characterClass,
                blizzardId = character.blizzardId,
                accountId = character.accountId,
                createdAt = character.createdAt,
                updatedAt = Instant.now(),
                guildId = guildId,
                role = role,
                rank = rank,
                status = status,
                joinDate = joinDate,
                wowauditId = wowauditId,
            )
        }
    }
}

/**
 * Character class in World of Warcraft.
 */
enum class CharacterClass {
    DEATH_KNIGHT,
    DEMON_HUNTER,
    DRUID,
    EVOKER,
    HUNTER,
    MAGE,
    MONK,
    PALADIN,
    PRIEST,
    ROGUE,
    SHAMAN,
    WARLOCK,
    WARRIOR,
    UNKNOWN,
    ;

    companion object {
        fun fromString(value: String): CharacterClass {
            val normalized = value.uppercase().replace(" ", "_").replace("-", "_")
            return entries.firstOrNull { it.name == normalized }
                ?: entries.firstOrNull { it.name.replace("_", "").equals(normalized.replace("_", ""), ignoreCase = true) }
                ?: UNKNOWN
        }
    }
}

/**
 * Role that a character can fulfill in raids.
 */
enum class Role {
    TANK,
    HEALER,
    DPS,
    ;

    companion object {
        fun fromString(value: String): Role = entries.first { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * Status of a raider in the guild.
 */
enum class RaiderStatus {
    ACTIVE, // Currently raiding
    INACTIVE, // Not currently raiding but still in guild
    BENCHED, // On bench, available but not main roster
    TRIAL, // Trial period
    ALUMNI, // Former member
    ;

    companion object {
        fun fromString(value: String): RaiderStatus? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
