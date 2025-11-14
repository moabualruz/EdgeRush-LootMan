package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderTrackItemRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderTrackItemRequest
import com.edgerush.datasync.api.dto.response.RaiderTrackItemResponse
import com.edgerush.datasync.entity.RaiderTrackItemEntity
import org.springframework.stereotype.Component

@Component
class RaiderTrackItemMapper {
    fun toEntity(request: CreateRaiderTrackItemRequest): RaiderTrackItemEntity {
        return RaiderTrackItemEntity(
            id = null,
            raiderId = 0L, // System populated
            tier = "", // System populated
            itemCount = 0, // System populated
        )
    }

    fun updateEntity(
        entity: RaiderTrackItemEntity,
        request: UpdateRaiderTrackItemRequest,
    ): RaiderTrackItemEntity {
        return entity.copy()
    }

    fun toResponse(entity: RaiderTrackItemEntity): RaiderTrackItemResponse {
        return RaiderTrackItemResponse(
            id = entity.id!!,
        )
    }
}
