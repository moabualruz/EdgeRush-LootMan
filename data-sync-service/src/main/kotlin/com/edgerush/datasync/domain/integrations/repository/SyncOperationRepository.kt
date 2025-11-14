package com.edgerush.datasync.domain.integrations.repository

import com.edgerush.datasync.domain.integrations.model.ExternalDataSource
import com.edgerush.datasync.domain.integrations.model.SyncOperation
import java.util.UUID

/**
 * Repository for managing sync operations
 */
interface SyncOperationRepository {
    fun save(operation: SyncOperation): SyncOperation
    fun findById(id: UUID): SyncOperation?
    fun findBySource(source: ExternalDataSource, limit: Int = 100): List<SyncOperation>
    fun findLatestBySource(source: ExternalDataSource): SyncOperation?
    fun findAll(limit: Int = 100): List<SyncOperation>
}
