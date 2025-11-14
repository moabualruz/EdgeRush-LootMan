package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaiderGearItemRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderGearItemRequest
import com.edgerush.datasync.api.dto.response.RaiderGearItemResponse
import com.edgerush.datasync.entity.RaiderGearItemEntity
import com.edgerush.datasync.repository.RaiderGearItemRepository
import com.edgerush.datasync.service.mapper.RaiderGearItemMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaiderGearItemCrudService(
    private val repository: RaiderGearItemRepository,
    private val mapper: RaiderGearItemMapper
,
    private val auditLogger: AuditLogger
) : CrudService<RaiderGearItemEntity, Long, CreateRaiderGearItemRequest, UpdateRaiderGearItemRequest, RaiderGearItemResponse> {

    override fun findAll(pageable: Pageable): Page<RaiderGearItemResponse> {
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

    override fun findById(id: Long): RaiderGearItemResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaiderGearItem not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateRaiderGearItemRequest, user: AuthenticatedUser): RaiderGearItemResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaiderGearItem",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateRaiderGearItemRequest, user: AuthenticatedUser): RaiderGearItemResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaiderGearItem not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaiderGearItem",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("RaiderGearItem not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaiderGearItem",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: RaiderGearItemEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
