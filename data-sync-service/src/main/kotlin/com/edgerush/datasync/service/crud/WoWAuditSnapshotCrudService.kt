package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateWoWAuditSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdateWoWAuditSnapshotRequest
import com.edgerush.datasync.api.dto.response.WoWAuditSnapshotResponse
import com.edgerush.datasync.entity.WoWAuditSnapshotEntity
import com.edgerush.datasync.repository.WoWAuditSnapshotRepository
import com.edgerush.datasync.service.mapper.WoWAuditSnapshotMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WoWAuditSnapshotCrudService(
    private val repository: WoWAuditSnapshotRepository,
    private val mapper: WoWAuditSnapshotMapper
,
    private val auditLogger: AuditLogger
) : CrudService<WoWAuditSnapshotEntity, Long, CreateWoWAuditSnapshotRequest, UpdateWoWAuditSnapshotRequest, WoWAuditSnapshotResponse> {

    override fun findAll(pageable: Pageable): Page<WoWAuditSnapshotResponse> {
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

    override fun findById(id: Long): WoWAuditSnapshotResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("WoWAuditSnapshot not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateWoWAuditSnapshotRequest, user: AuthenticatedUser): WoWAuditSnapshotResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "WoWAuditSnapshot",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateWoWAuditSnapshotRequest, user: AuthenticatedUser): WoWAuditSnapshotResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("WoWAuditSnapshot not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "WoWAuditSnapshot",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("WoWAuditSnapshot not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "WoWAuditSnapshot",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: WoWAuditSnapshotEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
