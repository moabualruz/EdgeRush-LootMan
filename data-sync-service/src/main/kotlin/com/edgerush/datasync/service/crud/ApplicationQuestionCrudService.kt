package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateApplicationQuestionRequest
import com.edgerush.datasync.api.dto.request.UpdateApplicationQuestionRequest
import com.edgerush.datasync.api.dto.response.ApplicationQuestionResponse
import com.edgerush.datasync.entity.ApplicationQuestionEntity
import com.edgerush.datasync.repository.ApplicationQuestionRepository
import com.edgerush.datasync.service.mapper.ApplicationQuestionMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplicationQuestionCrudService(
    private val repository: ApplicationQuestionRepository,
    private val mapper: ApplicationQuestionMapper
,
    private val auditLogger: AuditLogger
) : CrudService<ApplicationQuestionEntity, Long, CreateApplicationQuestionRequest, UpdateApplicationQuestionRequest, ApplicationQuestionResponse> {

    override fun findAll(pageable: Pageable): Page<ApplicationQuestionResponse> {
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

    override fun findById(id: Long): ApplicationQuestionResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("ApplicationQuestion not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateApplicationQuestionRequest, user: AuthenticatedUser): ApplicationQuestionResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "ApplicationQuestion",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateApplicationQuestionRequest, user: AuthenticatedUser): ApplicationQuestionResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("ApplicationQuestion not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "ApplicationQuestion",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("ApplicationQuestion not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "ApplicationQuestion",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: ApplicationQuestionEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
