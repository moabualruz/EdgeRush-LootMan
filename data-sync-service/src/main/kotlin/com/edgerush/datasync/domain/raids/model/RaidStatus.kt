package com.edgerush.datasync.domain.raids.model

/**
 * Enum representing the lifecycle status of a raid.
 */
enum class RaidStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;
    
    companion object {
        fun fromString(value: String?): RaidStatus? {
            return when (value?.uppercase()) {
                "SCHEDULED" -> SCHEDULED
                "IN_PROGRESS", "IN PROGRESS" -> IN_PROGRESS
                "COMPLETED" -> COMPLETED
                "CANCELLED" -> CANCELLED
                else -> null
            }
        }
    }
}
