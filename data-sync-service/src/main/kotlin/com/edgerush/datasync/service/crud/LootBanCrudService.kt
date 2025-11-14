package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateLootBanRequest
import com.edgerush.datasync.api.dto.request.UpdateLootBanRequest
import com.edgerush.datasync.api.dto.response.LootBanResponse
import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.datasync.repository.LootBanRepository
import com.edgerush.datasync.service.mapper.LootBanMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LootBanCrudService(
    private val repository: LootBanRepository,
    private val mapper: LootBanMapper
,
    private val auditLogger: AuditLogger
) : CrudService<LootBanEntity, Long, CreateLootBanRequest, UpdateLootBanRequest, LootBanResponse> {

    override fun findAll(pageable: Pageable): Page<LootBanResponse> {
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

    override fun findById(id: Long): LootBanResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootBan not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateLootBanRequest, user: AuthenticatedUser): LootBanResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "LootBan",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateLootBanRequest, user: AuthenticatedUser): LootBanResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootBan not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "LootBan",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("LootBan not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "LootBan",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: LootBanEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
