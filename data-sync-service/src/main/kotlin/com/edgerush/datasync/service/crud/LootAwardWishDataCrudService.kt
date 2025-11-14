package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateLootAwardWishDataRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardWishDataRequest
import com.edgerush.datasync.api.dto.response.LootAwardWishDataResponse
import com.edgerush.datasync.entity.LootAwardWishDataEntity
import com.edgerush.datasync.repository.LootAwardWishDataRepository
import com.edgerush.datasync.service.mapper.LootAwardWishDataMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LootAwardWishDataCrudService(
    private val repository: LootAwardWishDataRepository,
    private val mapper: LootAwardWishDataMapper
,
    private val auditLogger: AuditLogger
) : CrudService<LootAwardWishDataEntity, Long, CreateLootAwardWishDataRequest, UpdateLootAwardWishDataRequest, LootAwardWishDataResponse> {

    override fun findAll(pageable: Pageable): Page<LootAwardWishDataResponse> {
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

    override fun findById(id: Long): LootAwardWishDataResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootAwardWishData not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateLootAwardWishDataRequest, user: AuthenticatedUser): LootAwardWishDataResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "LootAwardWishData",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateLootAwardWishDataRequest, user: AuthenticatedUser): LootAwardWishDataResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("LootAwardWishData not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "LootAwardWishData",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("LootAwardWishData not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "LootAwardWishData",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: LootAwardWishDataEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
