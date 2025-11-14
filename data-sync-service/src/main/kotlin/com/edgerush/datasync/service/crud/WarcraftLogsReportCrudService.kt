package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateWarcraftLogsReportRequest
import com.edgerush.datasync.api.dto.request.UpdateWarcraftLogsReportRequest
import com.edgerush.datasync.api.dto.response.WarcraftLogsReportResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.warcraftlogs.WarcraftLogsReportEntity
import com.edgerush.datasync.repository.warcraftlogs.WarcraftLogsReportRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.WarcraftLogsReportMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WarcraftLogsReportCrudService(
    private val repository: WarcraftLogsReportRepository,
    private val mapper: WarcraftLogsReportMapper,
    private val auditLogger: AuditLogger,
) : CrudService<WarcraftLogsReportEntity, Long, CreateWarcraftLogsReportRequest, UpdateWarcraftLogsReportRequest, WarcraftLogsReportResponse> {
    override fun findAll(pageable: Pageable): Page<WarcraftLogsReportResponse> {
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

    override fun findById(id: Long): WarcraftLogsReportResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("WarcraftLogsReport not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateWarcraftLogsReportRequest,
        user: AuthenticatedUser,
    ): WarcraftLogsReportResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "WarcraftLogsReport",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateWarcraftLogsReportRequest,
        user: AuthenticatedUser,
    ): WarcraftLogsReportResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("WarcraftLogsReport not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "WarcraftLogsReport",
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
            throw ResourceNotFoundException("WarcraftLogsReport not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "WarcraftLogsReport",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: WarcraftLogsReportEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
