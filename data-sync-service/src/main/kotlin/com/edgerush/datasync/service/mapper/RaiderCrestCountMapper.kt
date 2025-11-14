package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderCrestCountRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderCrestCountRequest
import com.edgerush.datasync.api.dto.response.RaiderCrestCountResponse
import com.edgerush.datasync.entity.RaiderCrestCountEntity
import org.springframework.stereotype.Component

@Component
class RaiderCrestCountMapper {

    fun toEntity(request: CreateRaiderCrestCountRequest): RaiderCrestCountEntity {
        return RaiderCrestCountEntity(
            id = null,
            raiderId = 0L, // System populated
            crestType = "", // System populated
            crestCount = 0 // System populated
        )
    }

    fun updateEntity(entity: RaiderCrestCountEntity, request: UpdateRaiderCrestCountRequest): RaiderCrestCountEntity {
        return entity.copy(
        )
    }

    fun toResponse(entity: RaiderCrestCountEntity): RaiderCrestCountResponse {
        return RaiderCrestCountResponse(
            id = entity.id!!,
        )
    }
}
