package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaidRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidRequest
import com.edgerush.datasync.api.dto.response.RaidResponse
import com.edgerush.datasync.api.exception.AccessDeniedException
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.repository.LegacyRaidRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.LegacyRaidMapper
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class RaidCrudService(
    private val repository: LegacyRaidRepository,
    private val mapper: LegacyRaidMapper,
    private val auditLogger: AuditLogger,
) : CrudService<RaidEntity, Long, CreateRaidRequest, UpdateRaidRequest, RaidResponse> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun findAll(pageable: Pageable): Page<RaidResponse> {
        return repository.findAll(pageable).map { mapper.toResponse(it) }
    }

    override fun findById(id: Long): RaidResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("Raid not found with id: $id") }
        return mapper.toResponse(entity)
    }

    override fun create(
        request: CreateRaidRequest,
        user: AuthenticatedUser,
    ): RaidResponse {
        logger.info("Creating raid for date: ${request.date} by user: ${user.username}")
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)
        auditLogger.logCreate("Raid", saved.raidId, user)
        return mapper.toResponse(saved)
    }

    override fun update(
        id: Long,
        request: UpdateRaidRequest,
        user: AuthenticatedUser,
    ): RaidResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("Raid not found with id: $id") }
        validateAccess(existing, user)

        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)
        auditLogger.logUpdate("Raid", id, user)
        return mapper.toResponse(saved)
    }

    override fun delete(
        id: Long,
        user: AuthenticatedUser,
    ) {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("Raid not found with id: $id") }
        validateAccess(entity, user)

        repository.delete(entity)
        auditLogger.logDelete("Raid", id, user)
    }

    override fun validateAccess(
        entity: RaidEntity,
        user: AuthenticatedUser,
    ) {
        if (!user.isGuildAdmin()) {
            throw AccessDeniedException("Insufficient permissions to access this raid")
        }
    }

    fun findByTeamId(teamId: Long): List<RaidResponse> {
        return repository.findByTeamId(teamId).map { mapper.toResponse(it) }
    }
}
