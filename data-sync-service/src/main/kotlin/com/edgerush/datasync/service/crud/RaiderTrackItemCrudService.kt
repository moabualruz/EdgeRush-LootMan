package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaiderTrackItemRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderTrackItemRequest
import com.edgerush.datasync.api.dto.response.RaiderTrackItemResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.RaiderTrackItemEntity
import com.edgerush.datasync.repository.RaiderTrackItemRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.RaiderTrackItemMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaiderTrackItemCrudService(
    private val repository: RaiderTrackItemRepository,
    private val mapper: RaiderTrackItemMapper,
    private val auditLogger: AuditLogger,
) : CrudService<RaiderTrackItemEntity, Long, CreateRaiderTrackItemRequest, UpdateRaiderTrackItemRequest, RaiderTrackItemResponse> {
    override fun findAll(pageable: Pageable): Page<RaiderTrackItemResponse> {
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

    override fun findById(id: Long): RaiderTrackItemResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaiderTrackItem not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateRaiderTrackItemRequest,
        user: AuthenticatedUser,
    ): RaiderTrackItemResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaiderTrackItem",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateRaiderTrackItemRequest,
        user: AuthenticatedUser,
    ): RaiderTrackItemResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaiderTrackItem not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaiderTrackItem",
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
            throw ResourceNotFoundException("RaiderTrackItem not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaiderTrackItem",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: RaiderTrackItemEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
