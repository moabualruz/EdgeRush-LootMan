package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateAuditLogRequest
import com.edgerush.datasync.api.dto.request.UpdateAuditLogRequest
import com.edgerush.datasync.api.dto.response.AuditLogResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.AuditLogEntity
import com.edgerush.datasync.repository.AuditLogRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.AuditLogMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuditLogCrudService(
    private val repository: AuditLogRepository,
    private val mapper: AuditLogMapper,
    private val auditLogger: AuditLogger,
) : CrudService<AuditLogEntity, Long, CreateAuditLogRequest, UpdateAuditLogRequest, AuditLogResponse> {
    override fun findAll(pageable: Pageable): Page<AuditLogResponse> {
        val allEntities = repository.findAll().toList()
        val start = (pageable.pageNumber * pageable.pageSize).coerceAtMost(allEntities.size)
        val end = (start + pageable.pageSize).coerceAtMost(allEntities.size)
        val pageContent = allEntities.subList(start, end)
        return org.springframework.data.domain.PageImpl(
            pageContent.map(mapper::toResponse),
            pageable,
            allEntities.size.toLong(),
        )
    }

    override fun findById(id: Long): AuditLogResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("AuditLog not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateAuditLogRequest,
        user: AuthenticatedUser,
    ): AuditLogResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "AuditLog",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateAuditLogRequest,
        user: AuthenticatedUser,
    ): AuditLogResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("AuditLog not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "AuditLog",
            entityId = id,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(
        id: Long,
        user: AuthenticatedUser,
    ) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("AuditLog not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "AuditLog",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: AuditLogEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
