package com.edgerush.lootman.api.gear

import com.edgerush.lootman.domain.shared.model.GearItem
import com.edgerush.lootman.domain.shared.model.GearSet

/**
 * Request to save gear for a raider.
 */
data class SaveGearRequest(
    val gearSetType: String,
    val items: List<GearItemRequest>
)

/**
 * Request for a single gear item.
 */
data class GearItemRequest(
    val itemId: Long,
    val name: String,
    val itemLevel: Int,
    val quality: String,
    val slot: String,
    val isTierPiece: Boolean = false,
    val enchant: String? = null,
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
