package com.edgerush.datasync.domain.integrations.model

import java.time.Instant
import java.util.UUID

/**
 * Represents a synchronization operation with an external data source
 */
data class SyncOperation(
    val id: UUID,
    val source: ExternalDataSource,
    val operationType: String,
    val status: SyncStatus,
    val startedAt: Instant,
    val completedAt: Instant? = null,
    val recordsProcessed: Int = 0,
    val message: String? = null,
    val errors: List<String> = emptyList()
) {
    fun complete(result: SyncResult): SyncOperation = copy(
        status = result.status,
        completedAt = result.completedAt,
        recordsProcessed = result.recordsProcessed,
        message = result.message,
        errors = result.errors
    )

    fun fail(message: String, errors: List<String> = emptyList()): SyncOperation = copy(
        status = SyncStatus.FAILED,
        completedAt = Instant.now(),
        message = message,
        errors = errors
    )

    fun isComplete(): Boolean = completedAt != null

    fun isSuccess(): Boolean = status == SyncStatus.SUCCESS

    companion object {
        fun start(
            source: ExternalDataSource,
            operationType: String
        ): SyncOperation = SyncOperation(
            id = UUID.randomUUID(),
            source = source,
            operationType = operationType,
            status = SyncStatus.IN_PROGRESS,
            startedAt = Instant.now()
        )
    }
}
