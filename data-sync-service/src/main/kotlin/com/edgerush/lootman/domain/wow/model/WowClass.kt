package com.edgerush.lootman.domain.wow.model

import java.time.Instant

/**
 * World of Warcraft playable class, synced from Blizzard API.
 */
data class WowClass(
    val id: Int,
    val name: String,
    val slug: String,
    val mediaUrl: String? = null,
    val powerType: String? = null,
    val syncedAt: Instant = Instant.now(),
)

/**
 * World of Warcraft specialization, synced from Blizzard API.
 */
data class WowSpecialization(
    val id: Int,
    val classId: Int,
    val name: String,
    val slug: String,
    val role: WowRole,
    val mediaUrl: String? = null,
    val syncedAt: Instant = Instant.now(),
)

/**
 * Role that a specialization fulfills.
 */
enum class WowRole {
    TANK,
    HEALER,
    DPS,
    ;

    companion object {
        fun fromString(value: String): WowRole {
            return when (value.uppercase()) {
                "TANK" -> TANK
                "HEALER" -> HEALER
                "DPS", "DAMAGE" -> DPS
                else -> DPS // Default fallback
            }
        }
    }
}
