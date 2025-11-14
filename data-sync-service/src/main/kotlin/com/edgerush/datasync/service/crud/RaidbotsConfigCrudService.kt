package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaidbotsConfigRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsConfigRequest
import com.edgerush.datasync.api.dto.response.RaidbotsConfigResponse
import com.edgerush.datasync.entity.raidbots.RaidbotsConfigEntity
import com.edgerush.datasync.repository.raidbots.RaidbotsConfigRepository
import com.edgerush.datasync.service.mapper.RaidbotsConfigMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaidbotsConfigCrudService(
    private val repository: RaidbotsConfigRepository,
    private val mapper: RaidbotsConfigMapper
,
    private val auditLogger: AuditLogger
) : CrudService<RaidbotsConfigEntity, String, CreateRaidbotsConfigRequest, UpdateRaidbotsConfigRequest, RaidbotsConfigResponse> {

    override fun findAll(pageable: Pageable): Page<RaidbotsConfigResponse> {
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

    override fun findById(id: String): RaidbotsConfigResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaidbotsConfig not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateRaidbotsConfigRequest, user: AuthenticatedUser): RaidbotsConfigResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaidbotsConfig",
            entityId = saved.guildId!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: String, request: UpdateRaidbotsConfigRequest, user: AuthenticatedUser): RaidbotsConfigResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaidbotsConfig not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaidbotsConfig",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: String, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("RaidbotsConfig not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaidbotsConfig",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: RaidbotsConfigEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
