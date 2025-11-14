package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateSyncRunRequest
import com.edgerush.datasync.api.dto.request.UpdateSyncRunRequest
import com.edgerush.datasync.api.dto.response.SyncRunResponse
import com.edgerush.datasync.entity.SyncRunEntity
import com.edgerush.datasync.repository.SyncRunRepository
import com.edgerush.datasync.service.mapper.SyncRunMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SyncRunCrudService(
    private val repository: SyncRunRepository,
    private val mapper: SyncRunMapper
,
    private val auditLogger: AuditLogger
) : CrudService<SyncRunEntity, Long, CreateSyncRunRequest, UpdateSyncRunRequest, SyncRunResponse> {

    override fun findAll(pageable: Pageable): Page<SyncRunResponse> {
        val allEntities = repository.findAll().toList()
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(allEntities.size)
        val end = (start + pageable.pageSize).coerceAtMost(allEntities.size)
        val pageContent = allEntities.subList(start, end)
        return org.springframework.data.domain.PageImpl(
            pageContent.map(mapper::toResponse),
            pageable,
            allEntities.size.toLong()
        )
    }

    override fun findById(id: Long): SyncRunResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("SyncRun not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateSyncRunRequest, user: AuthenticatedUser): SyncRunResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "SyncRun",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateSyncRunRequest, user: AuthenticatedUser): SyncRunResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("SyncRun not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "SyncRun",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("SyncRun not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "SyncRun",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: SyncRunEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
