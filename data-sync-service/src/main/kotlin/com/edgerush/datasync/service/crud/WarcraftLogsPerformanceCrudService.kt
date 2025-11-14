package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsPerformanceRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsPerformanceRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsPerformanceResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsPerformanceEntity
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsPerformanceRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.WarcraftLogsPerformanceMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WarcraftLogsPerformanceCrudService(
    private val repository: WarcraftLogsPerformanceRepository,
    private val mapper: WarcraftLogsPerformanceMapper,
    private val auditLogger: AuditLogger,
) : CrudService<WarcraftLogsPerformanceEntity, Long, CreateWarcraftLogsPerformanceRequest, UpdateWarcraftLogsPerformanceRequest, WarcraftLogsPerformanceResponse> {
    override fun findAll(pageable: Pageable): Page<WarcraftLogsPerformanceResponse> {
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

    override fun findById(id: Long): WarcraftLogsPerformanceResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("WarcraftLogsPerformance not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateWarcraftLogsPerformanceRequest,
        user: AuthenticatedUser,
    ): WarcraftLogsPerformanceResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "WarcraftLogsPerformance",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateWarcraftLogsPerformanceRequest,
        user: AuthenticatedUser,
    ): WarcraftLogsPerformanceResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("WarcraftLogsPerformance not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "WarcraftLogsPerformance",
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
            throw ResourceNotFoundException("WarcraftLogsPerformance not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "WarcraftLogsPerformance",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: WarcraftLogsPerformanceEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
