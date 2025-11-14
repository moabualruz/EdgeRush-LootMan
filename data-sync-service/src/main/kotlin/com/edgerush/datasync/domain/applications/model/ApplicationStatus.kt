package com.edgerush.datasync.domain.applications.model

/**
 * Enum representing the status of a guild application
 */
enum class ApplicationStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    WITHDRAWN;

    companion object {
        fun fromString(value: String?): ApplicationStatus {
            return value?.let { 
                entries.find { it.name.equals(value, ignoreCase = true) } 
            } ?: PENDING
        }
    }
}
