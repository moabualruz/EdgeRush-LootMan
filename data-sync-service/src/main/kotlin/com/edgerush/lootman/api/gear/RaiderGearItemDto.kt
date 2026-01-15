package com.edgerush.lootman.api.gear

import com.edgerush.datasync.entity.RaiderGearItemEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateRaiderGearItemRequest(
    @field:NotNull(message = "Raider ID is required")
    val raiderId: Long,
    @field:NotBlank(message = "Gear set is required")
    val gearSet: String,
    @field:NotBlank(message = "Slot is required")
    val slot: String,
    val itemId: Long? = null,
    val itemLevel: Int? = null,
    val quality: Int? = null,
    val enchant: String? = null,
    val enchantQuality: Int? = null,
    val upgradeLevel: Int? = null,
    val sockets: Int? = null,
    val name: String? = null,
)

data class UpdateRaiderGearItemRequest(
    val itemId: Long? = null,
    val itemLevel: Int? = null,
    val quality: Int? = null,
    val enchant: String? = null,
    val enchantQuality: Int? = null,
    val upgradeLevel: Int? = null,
    val sockets: Int? = null,
    val name: String? = null,
)

data class RaiderGearItemResponse(
    val id: Long,
    val raiderId: Long,
    val gearSet: String,
    val slot: String,
    val itemId: Long?,
    val itemLevel: Int?,
    val quality: Int?,
    val enchant: String?,
    val enchantQuality: Int?,
    val upgradeLevel: Int?,
    val sockets: Int?,
    val name: String?,
) {
    companion object {
        fun from(e: RaiderGearItemEntity) =
            RaiderGearItemResponse(
                e.id!!, e.raiderId, e.gearSet, e.slot, e.itemId, e.itemLevel,
                e.quality, e.enchant, e.enchantQuality, e.upgradeLevel, e.sockets, e.name,
            )
    }
}

data class RaiderGearItemExistsResponse(val exists: Boolean)

data class RaiderGearItemCountResponse(val count: Long)
