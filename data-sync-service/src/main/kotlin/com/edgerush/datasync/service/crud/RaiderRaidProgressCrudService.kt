package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaiderRaidProgressRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderRaidProgressRequest
import com.edgerush.datasync.api.dto.response.RaiderRaidProgressResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import com.edgerush.datasync.repository.RaiderRaidProgressRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.RaiderRaidProgressMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaiderRaidProgressCrudService(
    private val repository: RaiderRaidProgressRepository,
    private val mapper: RaiderRaidProgressMapper,
    private val auditLogger: AuditLogger,
) : CrudService<RaiderRaidProgressEntity, Long, CreateRaiderRaidProgressRequest, UpdateRaiderRaidProgressRequest, RaiderRaidProgressResponse> {
    override fun findAll(pageable: Pageable): Page<RaiderRaidProgressResponse> {
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

    override fun findById(id: Long): RaiderRaidProgressResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaiderRaidProgress not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateRaiderRaidProgressRequest,
        user: AuthenticatedUser,
    ): RaiderRaidProgressResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaiderRaidProgress",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateRaiderRaidProgressRequest,
        user: AuthenticatedUser,
    ): RaiderRaidProgressResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaiderRaidProgress not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaiderRaidProgress",
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
            throw ResourceNotFoundException("RaiderRaidProgress not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaiderRaidProgress",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: RaiderRaidProgressEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
