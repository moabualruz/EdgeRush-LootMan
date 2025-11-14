package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsFightRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsFightRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsFightResponse
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsFightEntity
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsFightRepository
import com.edgerush.datasync.service.mapper.WarcraftLogsFightMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WarcraftLogsFightCrudService(
    private val repository: WarcraftLogsFightRepository,
    private val mapper: WarcraftLogsFightMapper
,
    private val auditLogger: AuditLogger
) : CrudService<WarcraftLogsFightEntity, Long, CreateWarcraftLogsFightRequest, UpdateWarcraftLogsFightRequest, WarcraftLogsFightResponse> {

    override fun findAll(pageable: Pageable): Page<WarcraftLogsFightResponse> {
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

    override fun findById(id: Long): WarcraftLogsFightResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("WarcraftLogsFight not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateWarcraftLogsFightRequest, user: AuthenticatedUser): WarcraftLogsFightResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "WarcraftLogsFight",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateWarcraftLogsFightRequest, user: AuthenticatedUser): WarcraftLogsFightResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("WarcraftLogsFight not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "WarcraftLogsFight",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("WarcraftLogsFight not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "WarcraftLogsFight",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: WarcraftLogsFightEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
