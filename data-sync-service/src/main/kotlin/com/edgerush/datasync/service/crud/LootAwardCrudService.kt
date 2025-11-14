package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateLootAwardRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardRequest
import com.edgerush.datasync.api.dto.response.LootAwardResponse
import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.datasync.repository.LootAwardRepository
import com.edgerush.datasync.service.mapper.LootAwardMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LootAwardCrudService(
    private val repository: LootAwardRepository,
    private val mapper: LootAwardMapper
,
    private val auditLogger: AuditLogger
) : CrudService<LootAwardEntity, Long, CreateLootAwardRequest, UpdateLootAwardRequest, LootAwardResponse> {

    override fun findAll(pageable: Pageable): Page<LootAwardResponse> {
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

    override fun findById(id: Long): LootAwardResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootAward not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateLootAwardRequest, user: AuthenticatedUser): LootAwardResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "LootAward",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateLootAwardRequest, user: AuthenticatedUser): LootAwardResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootAward not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "LootAward",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("LootAward not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "LootAward",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: LootAwardEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
