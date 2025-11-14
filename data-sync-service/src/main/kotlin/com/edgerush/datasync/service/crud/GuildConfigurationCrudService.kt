package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateGuildConfigurationRequest
import com.edgerush.datasync.api.dto.request.UpdateGuildConfigurationRequest
import com.edgerush.datasync.api.dto.response.GuildConfigurationResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.datasync.repository.GuildConfigurationRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.GuildConfigurationMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GuildConfigurationCrudService(
    private val repository: GuildConfigurationRepository,
    private val mapper: GuildConfigurationMapper,
    private val auditLogger: AuditLogger,
) : CrudService<GuildConfigurationEntity, Long, CreateGuildConfigurationRequest, UpdateGuildConfigurationRequest, GuildConfigurationResponse> {
    override fun findAll(pageable: Pageable): Page<GuildConfigurationResponse> {
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

    override fun findById(id: Long): GuildConfigurationResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("GuildConfiguration not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateGuildConfigurationRequest,
        user: AuthenticatedUser,
    ): GuildConfigurationResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "GuildConfiguration",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateGuildConfigurationRequest,
        user: AuthenticatedUser,
    ): GuildConfigurationResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("GuildConfiguration not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "GuildConfiguration",
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
            throw ResourceNotFoundException("GuildConfiguration not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "GuildConfiguration",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: GuildConfigurationEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
