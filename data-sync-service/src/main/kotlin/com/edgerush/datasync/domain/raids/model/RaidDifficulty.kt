package com.edgerush.datasync.domain.raids.model

/**
 * Enum representing raid difficulty levels.
 */
enum class RaidDifficulty {
    MYTHIC,
    HEROIC,
    NORMAL,
    LFR;
    
    companion object {
        fun fromString(value: String?): RaidDifficulty? {
            return when (value?.uppercase()) {
                "MYTHIC" -> MYTHIC
                "HEROIC" -> HEROIC
                "NORMAL" -> NORMAL
                "LFR" -> LFR
                else -> null
            }
        }
    }
}
