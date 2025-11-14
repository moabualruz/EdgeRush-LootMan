package com.edgerush.datasync.model

enum class Role {
    DPS,
    Tank,
    Healer,
    ;

    companion object {
        fun fromWoWAuditRole(wowAuditRole: String?): Role {
            return when (wowAuditRole?.lowercase()) {
                "tank" -> Tank
                "heal" -> Healer
                "ranged", "melee" -> DPS
                else -> DPS // Default fallback
            }
        }
    }
}
