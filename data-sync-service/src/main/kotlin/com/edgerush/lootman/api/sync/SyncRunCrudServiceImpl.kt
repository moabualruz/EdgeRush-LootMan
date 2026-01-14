package com.edgerush.lootman.api.sync

import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.lootman.api.common.PageRequest
import com.edgerush.lootman.api.common.PagedResponse
import com.edgerush.lootman.domain.sync.repository.SyncRunRepository
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

/**
 * Implementation of SyncRunCrudService.
 *
 * Provides CRUD operations for sync runs.
 */
@Service
class SyncRunCrudServiceImpl(
    private val repository: SyncRunRepository,
) : SyncRunCrudService {

    override fun findAll(pageRequest: PageRequest): PagedResponse<SyncRunResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findAll(offset, pageRequest.size)
        val total = repository.count()

        return PagedResponse(
            content = entities.map { SyncRunResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findById(id: Long): SyncRunResponse {
        val entity = repository.findById(id)
            ?: throw NoSuchElementException("Sync run not found with id: $id")
        return SyncRunResponse.from(entity)
    }

    override fun existsById(id: Long): Boolean {
        return repository.existsById(id)
    }

    override fun create(request: CreateSyncRunRequest): SyncRunResponse {
        val entity = SyncRunEntity(
            source = request.source,
            status = request.status,
            startedAt = OffsetDateTime.now(),
            completedAt = null,
            message = request.message,
        )
        val saved = repository.save(entity)
        return SyncRunResponse.from(saved)
    }

    override fun update(id: Long, request: UpdateSyncRunRequest): SyncRunResponse {
        val existing = repository.findById(id)
            ?: throw NoSuchElementException("Sync run not found with id: $id")

        val updated = existing.copy(
            status = request.status ?: existing.status,
            completedAt = request.completedAt ?: existing.completedAt,
            message = request.message ?: existing.message,
        )

        repository.save(updated)
        return SyncRunResponse.from(updated)
    }

    override fun delete(id: Long) {
        if (!repository.existsById(id)) {
            throw NoSuchElementException("Sync run not found with id: $id")
        }
        repository.delete(id)
    }

    override fun findBySource(source: String, pageRequest: PageRequest): PagedResponse<SyncRunResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findBySource(source, offset, pageRequest.size)
        val total = repository.countBySource(source)

        return PagedResponse(
            content = entities.map { SyncRunResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun findByStatus(status: String, pageRequest: PageRequest): PagedResponse<SyncRunResponse> {
        val offset = pageRequest.page.toLong() * pageRequest.size
        val entities = repository.findByStatus(status, offset, pageRequest.size)
        val total = repository.countByStatus(status)

        return PagedResponse(
            content = entities.map { SyncRunResponse.from(it) },
            page = pageRequest.page,
            size = pageRequest.size,
            totalElements = total,
        )
    }

    override fun countBySource(source: String): Long {
        return repository.countBySource(source)
    }
}
