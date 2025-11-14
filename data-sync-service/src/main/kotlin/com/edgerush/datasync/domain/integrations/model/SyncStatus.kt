package com.edgerush.datasync.domain.integrations.model

/**
 * Status of a synchronization operation
 */
enum class SyncStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    PARTIAL_SUCCESS
}
