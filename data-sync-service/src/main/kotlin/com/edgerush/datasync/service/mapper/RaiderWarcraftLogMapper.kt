package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderWarcraftLogRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderWarcraftLogRequest
import com.edgerush.datasync.api.dto.response.RaiderWarcraftLogResponse
import com.edgerush.datasync.entity.RaiderWarcraftLogEntity
import org.springframework.stereotype.Component

@Component
class RaiderWarcraftLogMapper {

    fun toEntity(request: CreateRaiderWarcraftLogRequest): RaiderWarcraftLogEntity {
        return RaiderWarcraftLogEntity(
            id = null,
            raiderId = 0L, // System populated
            difficulty = "", // System populated
            score = null // System populated - nullable Int
        )
    }

    fun updateEntity(entity: RaiderWarcraftLogEntity, request: UpdateRaiderWarcraftLogRequest): RaiderWarcraftLogEntity {
        return entity.copy(
        )
    }

    fun toResponse(entity: RaiderWarcraftLogEntity): RaiderWarcraftLogResponse {
        return RaiderWarcraftLogResponse(
            id = entity.id!!,
        )
    }
}
