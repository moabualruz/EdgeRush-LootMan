package com.edgerush.datasync.domain.raids.model

/**
 * Enum representing raid roles.
 */
enum class RaidRole {
    TANK,
    HEALER,
    DPS;
    
    companion object {
        fun fromString(value: String?): RaidRole? {
            return when (value?.uppercase()) {
                "TANK" -> TANK
                "HEALER", "HEAL" -> HEALER
                "DPS", "DAMAGE" -> DPS
                else -> null
            }
        }
    }
}
