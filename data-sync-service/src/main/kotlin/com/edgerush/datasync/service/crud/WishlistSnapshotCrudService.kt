package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateWishlistSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdateWishlistSnapshotRequest
import com.edgerush.datasync.api.dto.response.WishlistSnapshotResponse
import com.edgerush.datasync.entity.WishlistSnapshotEntity
import com.edgerush.datasync.repository.WishlistSnapshotRepository
import com.edgerush.datasync.service.mapper.WishlistSnapshotMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WishlistSnapshotCrudService(
    private val repository: WishlistSnapshotRepository,
    private val mapper: WishlistSnapshotMapper
,
    private val auditLogger: AuditLogger
) : CrudService<WishlistSnapshotEntity, Long, CreateWishlistSnapshotRequest, UpdateWishlistSnapshotRequest, WishlistSnapshotResponse> {

    override fun findAll(pageable: Pageable): Page<WishlistSnapshotResponse> {
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

    override fun findById(id: Long): WishlistSnapshotResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("WishlistSnapshot not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateWishlistSnapshotRequest, user: AuthenticatedUser): WishlistSnapshotResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "WishlistSnapshot",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateWishlistSnapshotRequest, user: AuthenticatedUser): WishlistSnapshotResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("WishlistSnapshot not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "WishlistSnapshot",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("WishlistSnapshot not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "WishlistSnapshot",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: WishlistSnapshotEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
