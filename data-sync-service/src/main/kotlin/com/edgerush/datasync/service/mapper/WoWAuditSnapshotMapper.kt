package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateWoWAuditSnapshotRequest
import com.edgerush.datasync.api.dto.request.UpdateWoWAuditSnapshotRequest
import com.edgerush.datasync.api.dto.response.WoWAuditSnapshotResponse
import com.edgerush.datasync.entity.WoWAuditSnapshotEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class WoWAuditSnapshotMapper {
    fun toEntity(request: CreateWoWAuditSnapshotRequest): WoWAuditSnapshotEntity {
        return WoWAuditSnapshotEntity(
            id = null,
            endpoint = request.endpoint ?: "",
            rawPayload = request.rawPayload ?: "",
            syncedAt = request.syncedAt ?: OffsetDateTime.now(),
        )
    }

    fun updateEntity(
        entity: WoWAuditSnapshotEntity,
        request: UpdateWoWAuditSnapshotRequest,
    ): WoWAuditSnapshotEntity {
        return entity.copy(
            endpoint = request.endpoint ?: entity.endpoint,
            rawPayload = request.rawPayload ?: entity.rawPayload,
            syncedAt = request.syncedAt ?: entity.syncedAt,
        )
    }

    fun toResponse(entity: WoWAuditSnapshotEntity): WoWAuditSnapshotResponse {
        return WoWAuditSnapshotResponse(
            id = entity.id!!,
            endpoint = entity.endpoint,
            rawPayload = entity.rawPayload,
            syncedAt = entity.syncedAt,
        )
    }
}
