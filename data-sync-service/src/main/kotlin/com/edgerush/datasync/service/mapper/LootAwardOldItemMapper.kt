package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateLootAwardOldItemRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardOldItemRequest
import com.edgerush.datasync.api.dto.response.LootAwardOldItemResponse
import com.edgerush.datasync.entity.LootAwardOldItemEntity
import org.springframework.stereotype.Component

@Component
class LootAwardOldItemMapper {

    fun toEntity(request: CreateLootAwardOldItemRequest): LootAwardOldItemEntity {
        return LootAwardOldItemEntity(
            id = null,
            lootAwardId = request.lootAwardId ?: 0L,
            itemId = request.itemId,
            bonusId = request.bonusId,
        )
    }

    fun updateEntity(entity: LootAwardOldItemEntity, request: UpdateLootAwardOldItemRequest): LootAwardOldItemEntity {
        return entity.copy(
            lootAwardId = request.lootAwardId ?: entity.lootAwardId,
            itemId = request.itemId ?: entity.itemId,
            bonusId = request.bonusId ?: entity.bonusId,
        )
    }

    fun toResponse(entity: LootAwardOldItemEntity): LootAwardOldItemResponse {
        return LootAwardOldItemResponse(
            id = entity.id!!,
            lootAwardId = entity.lootAwardId,
            itemId = entity.itemId!!,
            bonusId = entity.bonusId!!,
        )
    }
}
