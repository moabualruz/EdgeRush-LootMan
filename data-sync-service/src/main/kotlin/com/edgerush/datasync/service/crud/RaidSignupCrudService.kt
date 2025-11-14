package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaidSignupRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidSignupRequest
import com.edgerush.datasync.api.dto.response.RaidSignupResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.datasync.repository.RaidSignupRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.RaidSignupMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaidSignupCrudService(
    private val repository: RaidSignupRepository,
    private val mapper: RaidSignupMapper,
    private val auditLogger: AuditLogger,
) : CrudService<RaidSignupEntity, Long, CreateRaidSignupRequest, UpdateRaidSignupRequest, RaidSignupResponse> {
    override fun findAll(pageable: Pageable): Page<RaidSignupResponse> {
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

    override fun findById(id: Long): RaidSignupResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaidSignup not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateRaidSignupRequest,
        user: AuthenticatedUser,
    ): RaidSignupResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaidSignup",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateRaidSignupRequest,
        user: AuthenticatedUser,
    ): RaidSignupResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaidSignup not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaidSignup",
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
            throw ResourceNotFoundException("RaidSignup not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaidSignup",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: RaidSignupEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
