package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootAwardOldItemEntity
import jakarta.validation.constraints.NotNull

data class CreateLootAwardOldItemRequest(
    @field:NotNull(message = "Loot award ID is required")
    val lootAwardId: Long,
    val itemId: Long? = null,
    val bonusId: String? = null,
)

data class UpdateLootAwardOldItemRequest(
    val itemId: Long? = null,
    val bonusId: String? = null,
)

data class LootAwardOldItemResponse(
    val id: Long,
    val lootAwardId: Long,
    val itemId: Long?,
    val bonusId: String?,
) {
    companion object {
        fun from(e: LootAwardOldItemEntity) = LootAwardOldItemResponse(
            e.id!!, e.lootAwardId, e.itemId, e.bonusId
        )
    }
}

data class LootAwardOldItemExistsResponse(val exists: Boolean)
data class LootAwardOldItemCountResponse(val count: Long)
