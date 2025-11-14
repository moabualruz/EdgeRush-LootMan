package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateRaiderGearItemRequest
import com.edgerush.datasync.api.dto.request.UpdateRaiderGearItemRequest
import com.edgerush.datasync.api.dto.response.RaiderGearItemResponse
import com.edgerush.datasync.entity.RaiderGearItemEntity
import org.springframework.stereotype.Component

@Component
class RaiderGearItemMapper {

    fun toEntity(request: CreateRaiderGearItemRequest): RaiderGearItemEntity {
        return RaiderGearItemEntity(
            id = null,
            raiderId = 0L, // System populated
            gearSet = "", // System populated
            slot = "", // System populated
            itemId = null, // System populated
            itemLevel = null, // System populated
            quality = null, // System populated
            enchant = null, // System populated
            enchantQuality = null, // System populated
            upgradeLevel = null, // System populated
            sockets = null, // System populated
            name = null // System populated
        )
    }

    fun updateEntity(entity: RaiderGearItemEntity, request: UpdateRaiderGearItemRequest): RaiderGearItemEntity {
        return entity.copy(
        )
    }

    fun toResponse(entity: RaiderGearItemEntity): RaiderGearItemResponse {
        return RaiderGearItemResponse(
            id = entity.id!!,
        )
    }
}
