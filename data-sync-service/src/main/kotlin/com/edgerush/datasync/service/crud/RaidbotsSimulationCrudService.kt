package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaidbotsSimulationRequest
import com.edgerush.datasync.api.dto.request.UpdateRaidbotsSimulationRequest
import com.edgerush.datasync.api.dto.response.RaidbotsSimulationResponse
import com.edgerush.datasync.entity.raidbots.RaidbotsSimulationEntity
import com.edgerush.datasync.repository.raidbots.RaidbotsSimulationRepository
import com.edgerush.datasync.service.mapper.RaidbotsSimulationMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaidbotsSimulationCrudService(
    private val repository: RaidbotsSimulationRepository,
    private val mapper: RaidbotsSimulationMapper
,
    private val auditLogger: AuditLogger
) : CrudService<RaidbotsSimulationEntity, Long, CreateRaidbotsSimulationRequest, UpdateRaidbotsSimulationRequest, RaidbotsSimulationResponse> {

    override fun findAll(pageable: Pageable): Page<RaidbotsSimulationResponse> {
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

    override fun findById(id: Long): RaidbotsSimulationResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaidbotsSimulation not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateRaidbotsSimulationRequest, user: AuthenticatedUser): RaidbotsSimulationResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaidbotsSimulation",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateRaidbotsSimulationRequest, user: AuthenticatedUser): RaidbotsSimulationResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("RaidbotsSimulation not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaidbotsSimulation",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("RaidbotsSimulation not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaidbotsSimulation",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: RaidbotsSimulationEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
