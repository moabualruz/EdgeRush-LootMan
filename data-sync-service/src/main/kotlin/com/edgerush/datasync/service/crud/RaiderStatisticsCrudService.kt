package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateRaiderStatisticsRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderStatisticsRequest
import com.edgerush.datasync.api.dto.response.RaiderStatisticsResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.RaiderStatisticsEntity
import com.edgerush.datasync.repository.RaiderStatisticsRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.RaiderStatisticsMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RaiderStatisticsCrudService(
    private val repository: RaiderStatisticsRepository,
    private val mapper: RaiderStatisticsMapper,
    private val auditLogger: AuditLogger,
) : CrudService<RaiderStatisticsEntity, Long, CreateRaiderStatisticsRequest, UpdateRaiderStatisticsRequest, RaiderStatisticsResponse> {
    override fun findAll(pageable: Pageable): Page<RaiderStatisticsResponse> {
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

    override fun findById(id: Long): RaiderStatisticsResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaiderStatistics not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreateRaiderStatisticsRequest,
        user: AuthenticatedUser,
    ): RaiderStatisticsResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "RaiderStatistics",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdateRaiderStatisticsRequest,
        user: AuthenticatedUser,
    ): RaiderStatisticsResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("RaiderStatistics not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "RaiderStatistics",
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
            throw ResourceNotFoundException("RaiderStatistics not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "RaiderStatistics",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: RaiderStatisticsEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
