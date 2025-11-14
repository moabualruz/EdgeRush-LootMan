package com.edgerush.datasync.domain.integrations.service

import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import com.edgerush.datasync.domain.integrations.model.SyncOperation
import com.edgerush.datasync.domain.integrations.model.SyncResult
import com.edgerush.datasync.domain.integrations.repository.SyncOperationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Domain service for orchestrating synchronization operations
 */
@Service
class SyncOrchestrationService(
    private val syncOperationRepository: SyncOperationRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Start a new sync operation
     */
    fun startSync(source: ExternalDataSource, operationType: String): SyncOperation {
        val operation = SyncOperation.start(source, operationType)
        logger.info("Starting sync operation: ${operation.id} for $source - $operationType")
        return syncOperationRepository.save(operation)
    }

    /**
     * Complete a sync operation with the given result
     */
    fun completeSync(operation: SyncOperation, result: SyncResult): SyncOperation {
        val completed = operation.complete(result)
        logger.info(
            "Completed sync operation: ${completed.id} with status ${completed.status}, " +
                "processed ${completed.recordsProcessed} records in ${result.duration}ms"
        )
        return syncOperationRepository.save(completed)
    }

    /**
     * Mark a sync operation as failed
     */
    fun failSync(operation: SyncOperation, message: String, errors: List<String> = emptyList()): SyncOperation {
        val failed = operation.fail(message, errors)
        logger.error("Sync operation ${failed.id} failed: $message")
        return syncOperationRepository.save(failed)
    }

    /**
     * Get the latest sync operation for a source
     */
    fun getLatestSync(source: ExternalDataSource): SyncOperation? {
        return syncOperationRepository.findLatestBySource(source)
    }

    /**
     * Check if a sync is currently in progress for the given source
     */
    fun isSyncInProgress(source: ExternalDataSource): Boolean {
        val latest = getLatestSync(source)
        return latest != null && !latest.isComplete()
    }

    /**
     * Create a sync result for successful operations
     */
    fun createSuccessResult(
        recordsProcessed: Int,
        startedAt: Instant,
        message: String? = null
    ): SyncResult = SyncResult.success(
        recordsProcessed = recordsProcessed,
        startedAt = startedAt,
        message = message
    )

    /**
     * Create a sync result for failed operations
     */
    fun createFailureResult(
        startedAt: Instant,
        message: String,
        errors: List<String> = emptyList()
    ): SyncResult = SyncResult.failed(
        startedAt = startedAt,
        message = message,
        errors = errors
    )
}
