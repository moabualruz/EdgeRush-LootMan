package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateBehavioralActionRequest
import com.edgerush.datasync.api.dto.request.UpdateBehavioralActionRequest
import com.edgerush.datasync.api.dto.response.BehavioralActionResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.BehavioralActionEntity
import com.edgerush.datasync.repository.BehavioralActionRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.BehavioralActionMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BehavioralActionCrudService(
    private val repository: BehavioralActionRepository,
    private val mapper: BehavioralActionMapper,
    private val auditLogger: AuditLogger,
) : CrudService<BehavioralActionEntity, Long, CreateBehavioralActionRequest, UpdateBehavioralActionRequest, BehavioralActionResponse> {
    override fun findAll(pageable: Pageable): Page<BehavioralActionResponse> {
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

    override fun findById(id: Long): BehavioralActionResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("BehavioralAction not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateBehavioralActionRequest,
        user: AuthenticatedUser,
    ): BehavioralActionResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "BehavioralAction",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateBehavioralActionRequest,
        user: AuthenticatedUser,
    ): BehavioralActionResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("BehavioralAction not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "BehavioralAction",
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
            throw ResourceNotFoundException("BehavioralAction not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "BehavioralAction",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: BehavioralActionEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
