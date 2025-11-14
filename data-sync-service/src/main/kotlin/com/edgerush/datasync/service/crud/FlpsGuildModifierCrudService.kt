package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateFlpsGuildModifierRequest
import com.edgerush.datasync.api.dto.request.UpdateFlpsGuildModifierRequest
import com.edgerush.datasync.api.dto.response.FlpsGuildModifierResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.FlpsGuildModifierEntity
import com.edgerush.datasync.repository.FlpsGuildModifierRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.FlpsGuildModifierMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FlpsGuildModifierCrudService(
    private val repository: FlpsGuildModifierRepository,
    private val mapper: FlpsGuildModifierMapper,
    private val auditLogger: AuditLogger,
) : CrudService<FlpsGuildModifierEntity, Long, CreateFlpsGuildModifierRequest, UpdateFlpsGuildModifierRequest, FlpsGuildModifierResponse> {
    override fun findAll(pageable: Pageable): Page<FlpsGuildModifierResponse> {
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

    override fun findById(id: Long): FlpsGuildModifierResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("FlpsGuildModifier not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateFlpsGuildModifierRequest,
        user: AuthenticatedUser,
    ): FlpsGuildModifierResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "FlpsGuildModifier",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateFlpsGuildModifierRequest,
        user: AuthenticatedUser,
    ): FlpsGuildModifierResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("FlpsGuildModifier not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "FlpsGuildModifier",
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
            throw ResourceNotFoundException("FlpsGuildModifier not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "FlpsGuildModifier",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: FlpsGuildModifierEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
