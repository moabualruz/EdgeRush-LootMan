package com.edgerush.datasync.domain.shared.model

import java.time.OffsetDateTime

/**
 * Raider aggregate root representing a guild member character.
 * 
 * This is the core domain entity for a raider, containing all essential
 * character information and guild membership details.
 */
data class Raider(
    val id: RaiderId,
    val characterName: String,
    val realm: String,
    val region: String,
    val wowauditId: Long?,
    val clazz: WowClass,
    val spec: String,
    val role: RaiderRole,
    val rank: String?,
    val status: RaiderStatus,
    val note: String?,
    val blizzardId: Long?,
    val trackingSince: OffsetDateTime?,
    val joinDate: OffsetDateTime?,
    val blizzardLastModified: OffsetDateTime?,
    val lastSync: OffsetDateTime
) {
    /**
     * Get the full character identifier (name-realm)
     */
    fun getFullName(): String = "$characterName-$realm"
    
    /**
     * Check if the raider is active in the guild
     */
    fun isActive(): Boolean = status == RaiderStatus.ACTIVE
    
    /**
     * Check if the raider has been synced recently (within last 24 hours)
     */
    fun isRecentlySynced(): Boolean {
        val oneDayAgo = OffsetDateTime.now().minusDays(1)
        return lastSync.isAfter(oneDayAgo)
    }
    
    /**
     * Update sync timestamp
     */
    fun updateSync(): Raider = copy(lastSync = OffsetDateTime.now())
    
    /**
     * Update raider status
     */
    fun updateStatus(newStatus: RaiderStatus): Raider = copy(status = newStatus)
    
    init {
        require(characterName.isNotBlank()) { "Character name cannot be blank" }
        require(realm.isNotBlank()) { "Realm cannot be blank" }
        require(region.isNotBlank()) { "Region cannot be blank" }
        require(spec.isNotBlank()) { "Spec cannot be blank" }
    }
    
    companion object {
        /**
         * Create a new raider from sync data (without ID - will be set by repository)
         */
        fun create(
            characterName: String,
            realm: String,
            region: String,
            wowauditId: Long?,
            clazz: WowClass,
            spec: String,
            role: RaiderRole,
            rank: String? = null,
            status: RaiderStatus = RaiderStatus.ACTIVE,
            note: String? = null,
            blizzardId: Long? = null,
            trackingSince: OffsetDateTime? = null,
            joinDate: OffsetDateTime? = null,
            blizzardLastModified: OffsetDateTime? = null
        ): Raider {
            return Raider(
                id = RaiderId(0L), // Temporary ID, will be replaced by repository
                characterName = characterName,
                realm = realm,
                region = region,
                wowauditId = wowauditId,
                clazz = clazz,
                spec = spec,
                role = role,
                rank = rank,
                status = status,
                note = note,
                blizzardId = blizzardId,
                trackingSince = trackingSince,
                joinDate = joinDate,
                blizzardLastModified = blizzardLastModified,
                lastSync = OffsetDateTime.now()
            )
        }
    }
}

/**
 * Raider role in the game
 */
enum class RaiderRole {
    TANK,
    HEALER,
    DPS;
    
    companion object {
        fun fromString(value: String): RaiderRole {
            return when (value.uppercase()) {
                "TANK" -> TANK
                "HEALER", "HEAL" -> HEALER
                "DPS", "DAMAGE" -> DPS
                else -> throw IllegalArgumentException("Unknown raider role: $value")
            }
        }
    }
}

/**
 * Raider status in the guild
 */
enum class RaiderStatus {
    ACTIVE,
    INACTIVE,
    BENCHED,
    TRIAL,
    ALUMNI;
    
    companion object {
        fun fromString(value: String?): RaiderStatus {
            if (value == null) return ACTIVE
            return when (value.uppercase()) {
                "ACTIVE" -> ACTIVE
                "INACTIVE" -> INACTIVE
                "BENCHED" -> BENCHED
                "TRIAL" -> TRIAL
                "ALUMNI" -> ALUMNI
                else -> ACTIVE
            }
        }
    }
}

/**
 * World of Warcraft character class
 */
enum class WowClass {
    WARRIOR,
    PALADIN,
    HUNTER,
    ROGUE,
    PRIEST,
    DEATH_KNIGHT,
    SHAMAN,
    MAGE,
    WARLOCK,
    MONK,
    DRUID,
    DEMON_HUNTER,
    EVOKER;
    
    companion object {
        fun fromString(value: String): WowClass {
            return when (value.uppercase().replace(" ", "_")) {
                "WARRIOR" -> WARRIOR
                "PALADIN" -> PALADIN
                "HUNTER" -> HUNTER
                "ROGUE" -> ROGUE
                "PRIEST" -> PRIEST
                "DEATH_KNIGHT", "DEATHKNIGHT", "DK" -> DEATH_KNIGHT
                "SHAMAN" -> SHAMAN
                "MAGE" -> MAGE
                "WARLOCK" -> WARLOCK
                "MONK" -> MONK
                "DRUID" -> DRUID
                "DEMON_HUNTER", "DEMONHUNTER", "DH" -> DEMON_HUNTER
                "EVOKER" -> EVOKER
                else -> throw IllegalArgumentException("Unknown WoW class: $value")
            }
        }
    }
}
