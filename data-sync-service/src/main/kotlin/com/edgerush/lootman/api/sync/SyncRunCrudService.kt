package com.edgerush.lootman.api.sync

import com.edgerush.lootman.api.common.CrudService
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse

/**
 * CRUD service interface for SyncRun entity operations.
 *
 * Extends the generic CrudService with sync-run-specific query methods.
 */
interface SyncRunCrudService : CrudService<Long, CreateSyncRunRequest, UpdateSyncRunRequest, SyncRunResponse> {

    /**
     * Find sync runs by source with pagination.
     *
     * @param source The sync source (e.g., WoWAudit, WarcraftLogs)
     * @param pageRequest Pagination parameters
     * @return Paginated list of sync runs for the source
     */
    fun findBySource(source: String, pageRequest: PageRequest): PagedResponse<SyncRunResponse>

    /**
     * Find sync runs by status with pagination.
     *
     * @param status The sync status (e.g., RUNNING, COMPLETED, FAILED)
     * @param pageRequest Pagination parameters
     * @return Paginated list of sync runs with the status
     */
    fun findByStatus(status: String, pageRequest: PageRequest): PagedResponse<SyncRunResponse>

    /**
     * Count sync runs for a source.
     *
     * @param source The sync source
     * @return The count of sync runs for the source
     */
    fun countBySource(source: String): Long
}
