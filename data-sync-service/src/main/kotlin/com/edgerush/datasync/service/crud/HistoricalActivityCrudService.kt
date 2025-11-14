package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateHistoricalActivityRequest
import com.edgerush.datasync.api.dto.request.UpdateHistoricalActivityRequest
import com.edgerush.datasync.api.dto.response.HistoricalActivityResponse
import com.edgerush.datasync.entity.HistoricalActivityEntity
import com.edgerush.datasync.repository.HistoricalActivityRepository
import com.edgerush.datasync.service.mapper.HistoricalActivityMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HistoricalActivityCrudService(
    private val repository: HistoricalActivityRepository,
    private val mapper: HistoricalActivityMapper
,
    private val auditLogger: AuditLogger
) : CrudService<HistoricalActivityEntity, Long, CreateHistoricalActivityRequest, UpdateHistoricalActivityRequest, HistoricalActivityResponse> {

    override fun findAll(pageable: Pageable): Page<HistoricalActivityResponse> {
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

    override fun findById(id: Long): HistoricalActivityResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("HistoricalActivity not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateHistoricalActivityRequest, user: AuthenticatedUser): HistoricalActivityResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "HistoricalActivity",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateHistoricalActivityRequest, user: AuthenticatedUser): HistoricalActivityResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("HistoricalActivity not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "HistoricalActivity",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("HistoricalActivity not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "HistoricalActivity",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: HistoricalActivityEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
