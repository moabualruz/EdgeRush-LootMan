package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaiderRenownRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderRenownRequest
import com.edgerush.datasync.api.dto.response.RaiderRenownResponse
import com.edgerush.datasync.entity.RaiderRenownEntity
import com.edgerush.datasync.repository.RaiderRenownRepository
import com.edgerush.datasync.service.mapper.RaiderRenownMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaiderRenownCrudService(
    private val repository: RaiderRenownRepository,
    private val mapper: RaiderRenownMapper
,
    private val auditLogger: AuditLogger
) : CrudService<RaiderRenownEntity, Long, CreateRaiderRenownRequest, UpdateRaiderRenownRequest, RaiderRenownResponse> {

    override fun findAll(pageable: Pageable): Page<RaiderRenownResponse> {
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

    override fun findById(id: Long): RaiderRenownResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaiderRenown not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateRaiderRenownRequest, user: AuthenticatedUser): RaiderRenownResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaiderRenown",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateRaiderRenownRequest, user: AuthenticatedUser): RaiderRenownResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaiderRenown not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaiderRenown",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("RaiderRenown not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaiderRenown",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: RaiderRenownEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
