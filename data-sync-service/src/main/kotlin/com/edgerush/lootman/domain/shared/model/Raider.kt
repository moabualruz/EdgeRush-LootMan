package com.edgerush.lootman.domain.shared.model

import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import java.time.LocalDateTime

/**
 * Raider aggregate root representing a guild member.
 *
 * A raider is a player character who is part of the guild's roster.
 * This aggregate contains core identity and status information.
 */
data class Raider(
    val id: RaiderId,
    val guildId: GuildId,
    val characterName: String,
    val realm: String,
    val characterClass: CharacterClass,
    val role: Role,
    val rank: String?,
    val status: RaiderStatus,
    val joinDate: LocalDateTime?,
    val wowauditId: Long?,
) {
    init {
        require(characterName.isNotBlank()) { "Character name cannot be blank" }
        require(realm.isNotBlank()) { "Realm cannot be blank" }
    }

    /**
     * Checks if the raider is eligible for loot based on their status.
     */
    fun isEligibleForLoot(): Boolean = status == RaiderStatus.ACTIVE

    /**
     * Gets the full character identifier (name-realm).
     */
    fun getFullName(): String = "$characterName-$realm"
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
