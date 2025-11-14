package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaidbotsResultRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsResultRequest
import com.edgerush.datasync.api.dto.response.RaidbotsResultResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.raidbots.RaidbotsResultEntity
import com.edgerush.datasync.repository.raidbots.RaidbotsResultRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.RaidbotsResultMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaidbotsResultCrudService(
    private val repository: RaidbotsResultRepository,
    private val mapper: RaidbotsResultMapper,
    private val auditLogger: AuditLogger,
) : CrudService<RaidbotsResultEntity, Long, CreateRaidbotsResultRequest, UpdateRaidbotsResultRequest, RaidbotsResultResponse> {
    override fun findAll(pageable: Pageable): Page<RaidbotsResultResponse> {
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

    override fun findById(id: Long): RaidbotsResultResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaidbotsResult not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateRaidbotsResultRequest,
        user: AuthenticatedUser,
    ): RaidbotsResultResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaidbotsResult",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateRaidbotsResultRequest,
        user: AuthenticatedUser,
    ): RaidbotsResultResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaidbotsResult not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaidbotsResult",
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
            throw ResourceNotFoundException("RaidbotsResult not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaidbotsResult",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: RaidbotsResultEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
