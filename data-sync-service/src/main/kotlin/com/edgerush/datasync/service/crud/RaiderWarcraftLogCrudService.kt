package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaiderWarcraftLogRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderWarcraftLogRequest
import com.edgerush.datasync.api.dto.response.RaiderWarcraftLogResponse
import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import com.edgerush.datasync.repository.RaiderWarcraftLogRepository
import com.edgerush.datasync.service.mapper.RaiderWarcraftLogMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaiderWarcraftLogCrudService(
    private val repository: RaiderWarcraftLogRepository,
    private val mapper: RaiderWarcraftLogMapper
,
    private val auditLogger: AuditLogger
) : CrudService<RaiderWarcraftLogEntity, Long, CreateRaiderWarcraftLogRequest, UpdateRaiderWarcraftLogRequest, RaiderWarcraftLogResponse> {

    override fun findAll(pageable: Pageable): Page<RaiderWarcraftLogResponse> {
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

    override fun findById(id: Long): RaiderWarcraftLogResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaiderWarcraftLog not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateRaiderWarcraftLogRequest, user: AuthenticatedUser): RaiderWarcraftLogResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaiderWarcraftLog",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateRaiderWarcraftLogRequest, user: AuthenticatedUser): RaiderWarcraftLogResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaiderWarcraftLog not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaiderWarcraftLog",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("RaiderWarcraftLog not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaiderWarcraftLog",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: RaiderWarcraftLogEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
