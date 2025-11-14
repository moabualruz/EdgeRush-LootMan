package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateAuditLogRequest
import com.edgerush.datasync.api.dto.request.UpdateAuditLogRequest
import com.edgerush.datasync.api.dto.response.AuditLogResponse
import com.edgerush.datasync.entity.AuditLogEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AuditLogMapper {
    fun toEntity(request: CreateAuditLogRequest): AuditLogEntity {
        return AuditLogEntity(
            id = null,
            timestamp = request.timestamp ?: LocalDateTime.now(),
            operation = request.operation ?: "",
            entityType = request.entityType ?: "",
            entityId = request.entityId ?: "",
            userId = request.userId ?: "",
            username = request.username ?: "",
            isAdminMode = request.isAdminMode ?: false,
            requestId = request.requestId,
        )
    }

    fun updateEntity(
        entity: AuditLogEntity,
        request: UpdateAuditLogRequest,
    ): AuditLogEntity {
        return entity.copy(
            timestamp = request.timestamp ?: entity.timestamp,
            operation = request.operation ?: entity.operation,
            entityType = request.entityType ?: entity.entityType,
            entityId = request.entityId ?: entity.entityId,
            userId = request.userId ?: entity.userId,
            username = request.username ?: entity.username,
            isAdminMode = request.isAdminMode ?: entity.isAdminMode,
            requestId = request.requestId ?: entity.requestId,
        )
    }

    fun toResponse(entity: AuditLogEntity): AuditLogResponse {
        return AuditLogResponse(
            id = entity.id!!,
            timestamp = entity.timestamp,
            operation = entity.operation,
            entityType = entity.entityType,
            entityId = entity.entityId,
            userId = entity.userId,
            username = entity.username,
            isAdminMode = entity.isAdminMode,
            requestId = entity.requestId!!,
        )
    }
}
