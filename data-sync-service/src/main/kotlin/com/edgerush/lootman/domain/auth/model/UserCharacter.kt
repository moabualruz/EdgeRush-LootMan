package com.edgerush.lootman.domain.auth.model

import java.time.Instant

/**
 * Represents a personal WoW character owned by a User.
 * This is separate from the Guild Roster (Raider) and is used for personal simulations.
 */
data class UserCharacter(
    val id: Long? = null,
    val userId: UserId,
    val name: String,
    val realm: String,
    val className: String,  // Class name from Blizzard API (e.g., "Death Knight", "Mage")
    val classId: Int? = null,  // Reference to wow_classes table (dynamic, from Blizzard API)
    val specId: Int? = null,   // Reference to wow_specializations table (dynamic, from Blizzard API)
    val level: Int,
    val race: String,
    val faction: String,
    val blizzardId: Long?,
    val lastSyncedAt: Instant = Instant.now()
) {
    fun identifier(): String = "$name-$realm"
}
