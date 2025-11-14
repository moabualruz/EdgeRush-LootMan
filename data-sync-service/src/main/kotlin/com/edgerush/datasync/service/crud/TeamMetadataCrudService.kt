package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateTeamMetadataRequest
import com.edgerush.datasync.api.dto.request.UpdateTeamMetadataRequest
import com.edgerush.datasync.api.dto.response.TeamMetadataResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.TeamMetadataEntity
import com.edgerush.datasync.repository.TeamMetadataRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.TeamMetadataMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamMetadataCrudService(
    private val repository: TeamMetadataRepository,
    private val mapper: TeamMetadataMapper,
    private val auditLogger: AuditLogger,
) : CrudService<TeamMetadataEntity, Long, CreateTeamMetadataRequest, UpdateTeamMetadataRequest, TeamMetadataResponse> {
    override fun findAll(pageable: Pageable): Page<TeamMetadataResponse> {
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

    override fun findById(id: Long): TeamMetadataResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("TeamMetadata not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateTeamMetadataRequest,
        user: AuthenticatedUser,
    ): TeamMetadataResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "TeamMetadata",
            entityId = saved.teamId!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateTeamMetadataRequest,
        user: AuthenticatedUser,
    ): TeamMetadataResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("TeamMetadata not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "TeamMetadata",
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
            throw ResourceNotFoundException("TeamMetadata not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "TeamMetadata",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: TeamMetadataEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
