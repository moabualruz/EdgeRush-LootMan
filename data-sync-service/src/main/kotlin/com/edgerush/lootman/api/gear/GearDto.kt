package com.edgerush.lootman.api.gear

import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * Request to save gear for a raider.
 */
data class SaveGearRequest(
    @field:NotBlank(message = "Gear set type is required")
    val gearSetType: String,

    @field:NotEmpty(message = "Gear set must contain at least one item")
    @field:Valid
    val items: List<GearItemRequest>
)

/**
 * Request for a single gear item.
 */
data class GearItemRequest(
    @field:Min(value = 1, message = "Item ID must be positive")
    val itemId: Long,

    @field:NotBlank(message = "Item name is required")
    @field:Size(max = 100, message = "Item name cannot exceed 100 characters")
    val name: String,

    @field:Min(value = 1, message = "Item level must be at least 1")
    @field:Max(value = 1000, message = "Item level cannot exceed 1000")
    val itemLevel: Int,

    @field:NotBlank(message = "Quality is required")
    val quality: String,

    @field:NotBlank(message = "Slot is required")
    val slot: String,

    val isTierPiece: Boolean = false,
    val enchant: String? = null,

    @field:Min(value = 0, message = "Sockets must be non-negative")
    @field:Max(value = 3, message = "Sockets cannot exceed 3")
    val sockets: Int = 0
)

/**
 * Response for a gear set.
 */
data class GearSetResponse(
    val gearSetType: String,
    val items: List<GearItemResponse>,
    val averageItemLevel: Double,
    val tierPieceCount: Int,
    val has2PieceBonus: Boolean,
    val has4PieceBonus: Boolean,
    val totalSlots: Int
) {
    companion object {
        fun from(gearSet: GearSet): GearSetResponse {
            val itemResponses = gearSet.items.map { (slot, item) ->
                GearItemResponse.from(item)
            }
            return GearSetResponse(
                gearSetType = gearSet.gearSetType.name,
                items = itemResponses,
                averageItemLevel = gearSet.getAverageItemLevel(),
                tierPieceCount = gearSet.getTierPieceCount(),
                has2PieceBonus = gearSet.hasTierBonus(2),
                has4PieceBonus = gearSet.hasTierBonus(4),
                totalSlots = gearSet.items.size
            )
        }
    }
}

/**
 * Response for a single gear item.
 */
data class GearItemResponse(
    val itemId: Long,
    val name: String,
    val itemLevel: Int,
    val quality: String,
    val slot: String,
    val isTierPiece: Boolean,
    val enchant: String?,
    val sockets: Int
) {
    companion object {
        fun from(item: GearItem): GearItemResponse = GearItemResponse(
            itemId = item.itemId.value,
            name = item.name,
            itemLevel = item.itemLevel,
            quality = item.quality.name,
            slot = item.slot.name,
            isTierPiece = item.isTierPiece,
            enchant = item.enchant,
            sockets = item.sockets
        )
    }
}
