package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateLootAwardBonusIdRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardBonusIdRequest
import com.edgerush.datasync.api.dto.response.LootAwardBonusIdResponse
import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import org.springframework.stereotype.Component

@Component
class LootAwardBonusIdMapper {

    fun toEntity(request: CreateLootAwardBonusIdRequest): LootAwardBonusIdEntity {
        return LootAwardBonusIdEntity(
            id = null,
            lootAwardId = request.lootAwardId ?: 0L,
            bonusId = request.bonusId,
        )
    }

    fun updateEntity(entity: LootAwardBonusIdEntity, request: UpdateLootAwardBonusIdRequest): LootAwardBonusIdEntity {
        return entity.copy(
            lootAwardId = request.lootAwardId ?: entity.lootAwardId,
            bonusId = request.bonusId ?: entity.bonusId,
        )
    }

    fun toResponse(entity: LootAwardBonusIdEntity): LootAwardBonusIdResponse {
        return LootAwardBonusIdResponse(
            id = entity.id!!,
            lootAwardId = entity.lootAwardId,
            bonusId = entity.bonusId!!,
        )
    }
}
