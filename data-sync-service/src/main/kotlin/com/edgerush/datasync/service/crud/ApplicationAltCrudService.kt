package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateApplicationAltRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationAltRequest
import com.edgerush.datasync.api.dto.response.ApplicationAltResponse
import com.edgerush.datasync.entity.ApplicationAltEntity
import com.edgerush.datasync.repository.ApplicationAltRepository
import com.edgerush.datasync.service.mapper.ApplicationAltMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicationAltCrudService(
    private val repository: ApplicationAltRepository,
    private val mapper: ApplicationAltMapper
,
    private val auditLogger: AuditLogger
) : CrudService<ApplicationAltEntity, Long, CreateApplicationAltRequest, UpdateApplicationAltRequest, ApplicationAltResponse> {

    override fun findAll(pageable: Pageable): Page<ApplicationAltResponse> {
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

    override fun findById(id: Long): ApplicationAltResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("ApplicationAlt not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateApplicationAltRequest, user: AuthenticatedUser): ApplicationAltResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "ApplicationAlt",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateApplicationAltRequest, user: AuthenticatedUser): ApplicationAltResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("ApplicationAlt not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "ApplicationAlt",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("ApplicationAlt not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "ApplicationAlt",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: ApplicationAltEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
