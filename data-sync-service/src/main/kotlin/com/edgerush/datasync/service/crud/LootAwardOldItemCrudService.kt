package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateLootAwardOldItemRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardOldItemRequest
import com.edgerush.datasync.api.dto.response.LootAwardOldItemResponse
import com.edgerush.datasync.entity.LootAwardOldItemEntity
import com.edgerush.datasync.repository.LootAwardOldItemRepository
import com.edgerush.datasync.service.mapper.LootAwardOldItemMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LootAwardOldItemCrudService(
    private val repository: LootAwardOldItemRepository,
    private val mapper: LootAwardOldItemMapper
,
    private val auditLogger: AuditLogger
) : CrudService<LootAwardOldItemEntity, Long, CreateLootAwardOldItemRequest, UpdateLootAwardOldItemRequest, LootAwardOldItemResponse> {

    override fun findAll(pageable: Pageable): Page<LootAwardOldItemResponse> {
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

    override fun findById(id: Long): LootAwardOldItemResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootAwardOldItem not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateLootAwardOldItemRequest, user: AuthenticatedUser): LootAwardOldItemResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "LootAwardOldItem",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateLootAwardOldItemRequest, user: AuthenticatedUser): LootAwardOldItemResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootAwardOldItem not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "LootAwardOldItem",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("LootAwardOldItem not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "LootAwardOldItem",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: LootAwardOldItemEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
