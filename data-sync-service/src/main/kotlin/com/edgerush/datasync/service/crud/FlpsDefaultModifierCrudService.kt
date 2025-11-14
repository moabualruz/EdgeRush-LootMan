package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateFlpsDefaultModifierRequest
import com.edgerush.datasync.api.dto.request.UpdateFlpsDefaultModifierRequest
import com.edgerush.datasync.api.dto.response.FlpsDefaultModifierResponse
import com.edgerush.datasync.entity.FlpsDefaultModifierEntity
import com.edgerush.datasync.repository.FlpsDefaultModifierRepository
import com.edgerush.datasync.service.mapper.FlpsDefaultModifierMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FlpsDefaultModifierCrudService(
    private val repository: FlpsDefaultModifierRepository,
    private val mapper: FlpsDefaultModifierMapper
,
    private val auditLogger: AuditLogger
) : CrudService<FlpsDefaultModifierEntity, Long, CreateFlpsDefaultModifierRequest, UpdateFlpsDefaultModifierRequest, FlpsDefaultModifierResponse> {

    override fun findAll(pageable: Pageable): Page<FlpsDefaultModifierResponse> {
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

    override fun findById(id: Long): FlpsDefaultModifierResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("FlpsDefaultModifier not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateFlpsDefaultModifierRequest, user: AuthenticatedUser): FlpsDefaultModifierResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "FlpsDefaultModifier",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateFlpsDefaultModifierRequest, user: AuthenticatedUser): FlpsDefaultModifierResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("FlpsDefaultModifier not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "FlpsDefaultModifier",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("FlpsDefaultModifier not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "FlpsDefaultModifier",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: FlpsDefaultModifierEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
