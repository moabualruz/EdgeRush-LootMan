package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderRaidProgressRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderRaidProgressRequest
import com.edgerush.datasync.api.dto.response.RaiderRaidProgressResponse
import com.edgerush.datasync.entity.RaiderRaidProgressEntity
import org.springframework.stereotype.Component

@Component
class RaiderRaidProgressMapper {

    fun toEntity(request: CreateRaiderRaidProgressRequest): RaiderRaidProgressEntity {
        return RaiderRaidProgressEntity(
            id = null,
            raiderId = 0L, // System populated
            raid = "", // System populated
            difficulty = "", // System populated
            bossesDefeated = 0 // System populated
        )
    }

    fun updateEntity(entity: RaiderRaidProgressEntity, request: UpdateRaiderRaidProgressRequest): RaiderRaidProgressEntity {
        return entity.copy(
        )
    }

    fun toResponse(entity: RaiderRaidProgressEntity): RaiderRaidProgressResponse {
        return RaiderRaidProgressResponse(
            id = entity.id!!,
        )
    }
}
