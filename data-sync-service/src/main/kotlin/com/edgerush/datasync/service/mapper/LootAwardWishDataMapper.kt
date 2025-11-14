package com.edgerush.datasync.service.mapper

import com.edgerush.datasync.api.dto.request.CreateLootAwardWishDataRequest
import com.edgerush.datasync.api.dto.request.UpdateLootAwardWishDataRequest
import com.edgerush.datasync.api.dto.response.LootAwardWishDataResponse
import com.edgerush.datasync.entity.LootAwardWishDataEntity
import org.springframework.stereotype.Component

@Component
class LootAwardWishDataMapper {
    fun toEntity(request: CreateLootAwardWishDataRequest): LootAwardWishDataEntity {
        return LootAwardWishDataEntity(
            id = null,
            lootAwardId = request.lootAwardId ?: 0L,
            specName = request.specName,
            specIcon = request.specIcon,
            value = request.value,
        )
    }

    fun updateEntity(
        entity: LootAwardWishDataEntity,
        request: UpdateLootAwardWishDataRequest,
    ): LootAwardWishDataEntity {
        return entity.copy(
            lootAwardId = request.lootAwardId ?: entity.lootAwardId,
            specName = request.specName ?: entity.specName,
            specIcon = request.specIcon ?: entity.specIcon,
            value = request.value ?: entity.value,
        )
    }

    fun toResponse(entity: LootAwardWishDataEntity): LootAwardWishDataResponse {
        return LootAwardWishDataResponse(
            id = entity.id!!,
            lootAwardId = entity.lootAwardId,
            specName = entity.specName!!,
            specIcon = entity.specIcon!!,
            value = entity.value!!,
        )
    }
}
