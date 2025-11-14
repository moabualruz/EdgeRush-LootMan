package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderVaultSlotRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderVaultSlotRequest
import com.edgerush.datasync.api.dto.response.RaiderVaultSlotResponse
import com.edgerush.datasync.entity.RaiderVaultSlotEntity
import org.springframework.stereotype.Component

@Component
class RaiderVaultSlotMapper {
    fun toEntity(request: CreateRaiderVaultSlotRequest): RaiderVaultSlotEntity {
        return RaiderVaultSlotEntity(
            id = null,
            raiderId = 0L, // System populated
            slot = "", // System populated - String type
            unlocked = false, // System populated
        )
    }

    fun updateEntity(
        entity: RaiderVaultSlotEntity,
        request: UpdateRaiderVaultSlotRequest,
    ): RaiderVaultSlotEntity {
        return entity.copy()
    }

    fun toResponse(entity: RaiderVaultSlotEntity): RaiderVaultSlotResponse {
        return RaiderVaultSlotResponse(
            id = entity.id!!,
        )
    }
}
