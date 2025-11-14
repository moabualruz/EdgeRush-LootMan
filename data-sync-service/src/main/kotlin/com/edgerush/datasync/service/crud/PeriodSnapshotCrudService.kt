package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreatePeriodSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdatePeriodSnapshotRequest
import com.edgerush.datasync.api.dto.response.PeriodSnapshotResponse
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.entity.PeriodSnapshotEntity
import com.edgerush.datasync.repository.PeriodSnapshotRepository
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.service.AuditLogger
import com.edgerush.datasync.service.mapper.PeriodSnapshotMapper
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PeriodSnapshotCrudService(
    private val repository: PeriodSnapshotRepository,
    private val mapper: PeriodSnapshotMapper,
    private val auditLogger: AuditLogger,
) : CrudService<PeriodSnapshotEntity, Long, CreatePeriodSnapshotRequest, UpdatePeriodSnapshotRequest, PeriodSnapshotResponse> {
    override fun findAll(pageable: Pageable): Page<PeriodSnapshotResponse> {
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

    override fun findById(id: Long): PeriodSnapshotResponse {
        val entity =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("PeriodSnapshot not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(
        request: CreatePeriodSnapshotRequest,
        user: AuthenticatedUser,
    ): PeriodSnapshotResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "PeriodSnapshot",
            entityId = saved.id!!,
            user = user,
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(
        id: Long,
        request: UpdatePeriodSnapshotRequest,
        user: AuthenticatedUser,
    ): PeriodSnapshotResponse {
        val existing =
            repository.findById(id)
                .orElseThrow { ResourceNotFoundException("PeriodSnapshot not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "PeriodSnapshot",
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
            throw ResourceNotFoundException("PeriodSnapshot not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "PeriodSnapshot",
            entityId = id,
            user = user,
        )
    }

    override fun validateAccess(
        entity: PeriodSnapshotEntity,
        user: AuthenticatedUser,
    ) {
        // Add guild-based access control if needed
    }
}
