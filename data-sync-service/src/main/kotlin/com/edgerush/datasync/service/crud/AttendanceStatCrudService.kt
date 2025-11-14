package com.edgerush.datasync.service.crud

import com.edgerush.datasync.api.dto.request.CreateAttendanceStatRequest
import com.edgerush.datasync.api.dto.request.UpdateAttendanceStatRequest
import com.edgerush.datasync.api.dto.response.AttendanceStatResponse
import com.edgerush.datasync.entity.AttendanceStatEntity
import com.edgerush.datasync.repository.AttendanceStatRepository
import com.edgerush.datasync.service.mapper.AttendanceStatMapper
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.api.exception.ResourceNotFoundException
import com.edgerush.datasync.service.AuditLogger
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AttendanceStatCrudService(
    private val repository: AttendanceStatRepository,
    private val mapper: AttendanceStatMapper
,
    private val auditLogger: AuditLogger
) : CrudService<AttendanceStatEntity, Long, CreateAttendanceStatRequest, UpdateAttendanceStatRequest, AttendanceStatResponse> {

    override fun findAll(pageable: Pageable): Page<AttendanceStatResponse> {
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

    override fun findById(id: Long): AttendanceStatResponse {
        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("AttendanceStat not found with id: $id") }
        return mapper.toResponse(entity)
    }

    @Transactional
    override fun create(request: CreateAttendanceStatRequest, user: AuthenticatedUser): AttendanceStatResponse {
        val entity = mapper.toEntity(request)
        val saved = repository.save(entity)

        auditLogger.logCreate(
            entityType = "AttendanceStat",
            entityId = saved.id!!,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun update(id: Long, request: UpdateAttendanceStatRequest, user: AuthenticatedUser): AttendanceStatResponse {
        val existing = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("AttendanceStat not found with id: $id") }
        val updated = mapper.updateEntity(existing, request)
        val saved = repository.save(updated)

        auditLogger.logUpdate(
            entityType = "AttendanceStat",
            entityId = id,
            user = user
        )

        return mapper.toResponse(saved)
    }

    @Transactional
    override fun delete(id: Long, user: AuthenticatedUser) {
        if (!repository.existsById(id)) {
            throw ResourceNotFoundException("AttendanceStat not found with id: $id")
        }

        repository.deleteById(id)

        auditLogger.logDelete(
            entityType = "AttendanceStat",
            entityId = id,
            user = user
        )
    }

    override fun validateAccess(entity: AttendanceStatEntity, user: AuthenticatedUser) {
        // Add guild-based access control if needed
    }
}
