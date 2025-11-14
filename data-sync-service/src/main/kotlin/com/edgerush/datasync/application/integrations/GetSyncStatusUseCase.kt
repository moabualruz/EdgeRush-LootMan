package com.edgerush.datasync.application.integrations

import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import com.edgerush.datasync.domain.integrations.model.SyncOperation
import com.edgerush.datasync.domain.integrations.repository.SyncOperationRepository
import org.springframework.stereotype.Service

/**
 * Use case for retrieving sync operation status
 */
@Service
class GetSyncStatusUseCase(
    private val syncOperationRepository: SyncOperationRepository
) {
    /**
     * Get the latest sync operation for a specific source
     */
    fun getLatestForSource(source: ExternalDataSource): SyncOperation? {
        return syncOperationRepository.findLatestBySource(source)
    }

    /**
     * Get recent sync operations for a specific source
     */
    fun getRecentForSource(source: ExternalDataSource, limit: Int = 10): List<SyncOperation> {
        return syncOperationRepository.findBySource(source, limit)
    }

    /**
     * Get all recent sync operations across all sources
     */
    fun getAllRecent(limit: Int = 50): List<SyncOperation> {
        return syncOperationRepository.findAll(limit)
    }

    /**
     * Check if a sync is currently in progress for the given source
     */
    fun isSyncInProgress(source: ExternalDataSource): Boolean {
        val latest = getLatestForSource(source)
        return latest != null && !latest.isComplete()
    }
}
