package com.edgerush.lootman.api.loot

import com.edgerush.datasync.entity.LootAwardWishDataEntity
import jakarta.validation.constraints.NotNull

data class CreateLootAwardWishDataRequest(
    @field:NotNull(message = "Loot award ID is required")
    val lootAwardId: Long,
    val specName: String? = null,
    val specIcon: String? = null,
    val value: Int? = null,
)

data class UpdateLootAwardWishDataRequest(
    val specName: String? = null,
    val specIcon: String? = null,
    val value: Int? = null,
)

data class LootAwardWishDataResponse(
    val id: Long,
    val lootAwardId: Long,
    val specName: String?,
    val specIcon: String?,
    val value: Int?,
) {
    companion object {
        fun from(e: LootAwardWishDataEntity) =
            LootAwardWishDataResponse(
                e.id!!,
                e.lootAwardId,
                e.specName,
                e.specIcon,
                e.value,
            )
    }
}

data class LootAwardWishDataExistsResponse(val exists: Boolean)

data class LootAwardWishDataCountResponse(val count: Long)
