package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaidEncounterRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidEncounterRequest
import com.edgerush.datasync.api.dto.response.RaidEncounterResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.repository.RaidEncounterRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.LegacyRaidEncounterMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaidEncounterCrudService(
    private val repository: RaidEncounterRepository,
    private val mapper: LegacyRaidEncounterMapper,
    private val auditLogger: AuditLogger,
) : CrudService<RaidEncounterEntity, Long, CreateRaidEncounterRequest, UpdateRaidEncounterRequest, RaidEncounterResponse> {
    override fun findAll(pageable: Pageable): Page<RaidEncounterResponse> {
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

    override fun findById(id: Long): RaidEncounterResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaidEncounter not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateRaidEncounterRequest,
        user: AuthenticatedUser,
    ): RaidEncounterResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaidEncounter",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateRaidEncounterRequest,
        user: AuthenticatedUser,
    ): RaidEncounterResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaidEncounter not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaidEncounter",
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
            throw ResourceNotFoundException("RaidEncounter not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaidEncounter",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: RaidEncounterEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
