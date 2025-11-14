package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderRenownRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderRenownRequest
import com.edgerush.datasync.api.dto.response.RaiderRenownResponse
import com.edgerush.datasync.entity.RaiderRenownEntity
import org.springframework.stereotype.Component

@Component
class RaiderRenownMapper {

    fun toEntity(request: CreateRaiderRenownRequest): RaiderRenownEntity {
        return RaiderRenownEntity(
            id = null,
            raiderId = 0L, // System populated
            faction = "", // System populated
            level = 0 // System populated
        )
    }

    fun updateEntity(entity: RaiderRenownEntity, request: UpdateRaiderRenownRequest): RaiderRenownEntity {
        return entity.copy(
        )
    }

    fun toResponse(entity: RaiderRenownEntity): RaiderRenownResponse {
        return RaiderRenownResponse(
            id = entity.id!!,
        )
    }
}
