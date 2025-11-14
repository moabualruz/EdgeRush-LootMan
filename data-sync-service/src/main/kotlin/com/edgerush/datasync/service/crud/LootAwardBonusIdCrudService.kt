package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateLootAwardBonusIdRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardBonusIdRequest
import com.edgerush.datasync.api.dto.response.LootAwardBonusIdResponse
import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import com.edgerush.datasync.repository.LootAwardBonusIdRepository
import com.edgerush.datasync.service.mapper.LootAwardBonusIdMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LootAwardBonusIdCrudService(
    private val repository: LootAwardBonusIdRepository,
    private val mapper: LootAwardBonusIdMapper
,
    private val auditLogger: AuditLogger
) : CrudService<LootAwardBonusIdEntity, Long, CreateLootAwardBonusIdRequest, UpdateLootAwardBonusIdRequest, LootAwardBonusIdResponse> {

    override fun findAll(pageable: Pageable): Page<LootAwardBonusIdResponse> {
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

    override fun findById(id: Long): LootAwardBonusIdResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootAwardBonusId not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateLootAwardBonusIdRequest, user: AuthenticatedUser): LootAwardBonusIdResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "LootAwardBonusId",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateLootAwardBonusIdRequest, user: AuthenticatedUser): LootAwardBonusIdResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootAwardBonusId not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "LootAwardBonusId",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("LootAwardBonusId not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "LootAwardBonusId",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: LootAwardBonusIdEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
