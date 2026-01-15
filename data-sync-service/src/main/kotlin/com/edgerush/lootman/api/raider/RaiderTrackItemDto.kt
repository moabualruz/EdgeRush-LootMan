package com.edgerush.lootman.api.raider

import com.edgerush.datasync.entity.RaiderTrackItemEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRaiderTrackItemRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,
    @field:NotBlank(message = "Tier is required")
    val tier: String,
    val itemCount: Int? = null,
)

data class UpdateRaiderTrackItemRequest(
    val itemCount: Int? = null,
)

data class RaiderTrackItemResponse(
    val id: Long,
    val raiderId: Long,
    val tier: String,
    val itemCount: Int?,
) {
    companion object {
        fun from(e: RaiderTrackItemEntity) =
            RaiderTrackItemResponse(
                e.id!!,
                e.raiderId,
                e.tier,
                e.itemCount,
            )
    }
}

data class RaiderTrackItemExistsResponse(val exists: Boolean)

data class RaiderTrackItemCountResponse(val count: Long)
