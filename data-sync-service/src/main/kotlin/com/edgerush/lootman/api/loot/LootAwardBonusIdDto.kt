package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootAwardBonusIdEntity
import jakarta.validation.constraints.NotNull

data class CreateLootAwardBonusIdRequest(
    @field:NotNull(message = "Loot award ID is required")
    val lootAwardId: Long,
    val bonusId: String? = null,
)

data class UpdateLootAwardBonusIdRequest(
    val bonusId: String? = null,
)

data class LootAwardBonusIdResponse(
    val id: Long,
    val lootAwardId: Long,
    val bonusId: String?,
) {
    companion object {
        fun from(e: LootAwardBonusIdEntity) =
            LootAwardBonusIdResponse(
                e.id!!,
                e.lootAwardId,
                e.bonusId,
            )
    }
}

data class LootAwardBonusIdExistsResponse(val exists: Boolean)

data class LootAwardBonusIdCountResponse(val count: Long)
