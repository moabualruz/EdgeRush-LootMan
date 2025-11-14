package com.edgerush.datasync.api.dto

import com.edgerush.datasync.domain.integrations.model.SyncResult
import com.edgerush.datasync.domain.integrations.model.SyncStatus
import java.time.Instant

/**
 * DTO for sync result responses
 */
data class SyncResultDto(
    val status: String,
    val recordsProcessed: Int,
    val startedAt: Instant,
    val completedAt: Instant,
    val durationMs: Long,
    val message: String?,
    val errors: List<String>
) {
    companion object {
        fun from(result: SyncResult): SyncResultDto = SyncResultDto(
            status = result.status.name,
            recordsProcessed = result.recordsProcessed,
            startedAt = result.startedAt,
            completedAt = result.completedAt,
            durationMs = result.duration,
            message = result.message,
            errors = result.errors
        )

        fun error(message: String): SyncResultDto = SyncResultDto(
            status = SyncStatus.FAILED.name,
            recordsProcessed = 0,
            startedAt = Instant.now(),
            completedAt = Instant.now(),
            durationMs = 0,
            message = message,
            errors = listOf(message)
        )
    }
}
