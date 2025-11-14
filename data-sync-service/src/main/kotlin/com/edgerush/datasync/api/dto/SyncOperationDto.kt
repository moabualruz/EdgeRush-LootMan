package com.edgerush.datasync.api.dto

import com.edgerush.datasync.domain.integrations.model.SyncOperation
import java.time.Instant
import java.util.UUID

/**
 * DTO for sync operation responses
 */
data class SyncOperationDto(
    val id: UUID,
    val source: String,
    val operationType: String,
    val status: String,
    val startedAt: Instant,
    val completedAt: Instant?,
    val recordsProcessed: Int,
    val message: String?,
    val errors: List<String>
) {
    companion object {
        fun from(operation: SyncOperation): SyncOperationDto = SyncOperationDto(
            id = operation.id,
            source = operation.source.name,
            operationType = operation.operationType,
            status = operation.status.name,
            startedAt = operation.startedAt,
            completedAt = operation.completedAt,
            recordsProcessed = operation.recordsProcessed,
            message = operation.message,
            errors = operation.errors
        )
    }
}
