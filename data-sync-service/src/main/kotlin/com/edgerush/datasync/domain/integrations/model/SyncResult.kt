package com.edgerush.datasync.domain.integrations.model

import java.time.Instant

/**
 * Result of a synchronization operation
 */
data class SyncResult(
    val status: SyncStatus,
    val recordsProcessed: Int,
    val startedAt: Instant,
    val completedAt: Instant,
    val message: String? = null,
    val errors: List<String> = emptyList()
) {
    val duration: Long
        get() = completedAt.toEpochMilli() - startedAt.toEpochMilli()

    fun isSuccess(): Boolean = status == SyncStatus.SUCCESS

    fun isFailed(): Boolean = status == SyncStatus.FAILED

    companion object {
        fun success(
            recordsProcessed: Int,
            startedAt: Instant,
            completedAt: Instant = Instant.now(),
            message: String? = null
        ): SyncResult = SyncResult(
            status = SyncStatus.SUCCESS,
            recordsProcessed = recordsProcessed,
            startedAt = startedAt,
            completedAt = completedAt,
            message = message
        )

        fun failed(
            startedAt: Instant,
            completedAt: Instant = Instant.now(),
            message: String,
            errors: List<String> = emptyList()
        ): SyncResult = SyncResult(
            status = SyncStatus.FAILED,
            recordsProcessed = 0,
            startedAt = startedAt,
            completedAt = completedAt,
            message = message,
            errors = errors
        )

        fun partialSuccess(
            recordsProcessed: Int,
            startedAt: Instant,
            completedAt: Instant = Instant.now(),
            message: String,
            errors: List<String>
        ): SyncResult = SyncResult(
            status = SyncStatus.PARTIAL_SUCCESS,
            recordsProcessed = recordsProcessed,
            startedAt = startedAt,
            completedAt = completedAt,
            message = message,
            errors = errors
        )
    }
}
