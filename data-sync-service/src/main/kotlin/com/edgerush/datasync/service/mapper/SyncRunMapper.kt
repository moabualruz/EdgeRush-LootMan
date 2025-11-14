package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateSyncRunRequest
import com.edgerush.datasync.api.dto.request.UpdateSyncRunRequest
import com.edgerush.datasync.api.dto.response.SyncRunResponse
import com.edgerush.datasync.entity.SyncRunEntity
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class SyncRunMapper {
    fun toEntity(request: CreateSyncRunRequest): SyncRunEntity {
        return SyncRunEntity(
            id = null,
            source = request.source ?: "",
            status = request.status ?: "",
            startedAt = request.startedAt ?: OffsetDateTime.now(),
            completedAt = request.completedAt,
            message = request.message,
        )
    }

    fun updateEntity(
        entity: SyncRunEntity,
        request: UpdateSyncRunRequest,
    ): SyncRunEntity {
        return entity.copy(
            source = request.source ?: entity.source,
            status = request.status ?: entity.status,
            startedAt = request.startedAt ?: entity.startedAt,
            completedAt = request.completedAt ?: entity.completedAt,
            message = request.message ?: entity.message,
        )
    }

    fun toResponse(entity: SyncRunEntity): SyncRunResponse {
        return SyncRunResponse(
            id = entity.id!!,
            source = entity.source,
            status = entity.status,
            startedAt = entity.startedAt,
            completedAt = entity.completedAt!!,
            message = entity.message!!,
        )
    }
}
