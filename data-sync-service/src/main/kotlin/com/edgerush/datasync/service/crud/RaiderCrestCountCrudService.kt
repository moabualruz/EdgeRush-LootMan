package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaiderCrestCountRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderCrestCountRequest
import com.edgerush.datasync.api.dto.response.RaiderCrestCountResponse
import com.edgerush.datasync.entity.RaiderCrestCountEntity
import com.edgerush.datasync.repository.RaiderCrestCountRepository
import com.edgerush.datasync.service.mapper.RaiderCrestCountMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaiderCrestCountCrudService(
    private val repository: RaiderCrestCountRepository,
    private val mapper: RaiderCrestCountMapper
,
    private val auditLogger: AuditLogger
) : CrudService<RaiderCrestCountEntity, Long, CreateRaiderCrestCountRequest, UpdateRaiderCrestCountRequest, RaiderCrestCountResponse> {

    override fun findAll(pageable: Pageable): Page<RaiderCrestCountResponse> {
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

    override fun findById(id: Long): RaiderCrestCountResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaiderCrestCount not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateRaiderCrestCountRequest, user: AuthenticatedUser): RaiderCrestCountResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaiderCrestCount",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateRaiderCrestCountRequest, user: AuthenticatedUser): RaiderCrestCountResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaiderCrestCount not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaiderCrestCount",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("RaiderCrestCount not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaiderCrestCount",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: RaiderCrestCountEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
