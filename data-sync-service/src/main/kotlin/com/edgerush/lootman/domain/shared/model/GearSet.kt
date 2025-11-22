package com.edgerush.lootman.domain.shared.model

import com.edgerush.lootman.domain.shared.ItemId

/**
 * Represents a character's complete set of equipped gear.
 *
 * Used for calculating tier bonuses and item level for FLPS calculations.
 */
data class GearSet(
    val items: Map<EquipmentSlot, GearItem>,
    val gearSetType: GearSetType
) {
    /**
     * Calculates the average item level across all equipped items.
     */
    fun getAverageItemLevel(): Double {
        if (items.isEmpty()) return 0.0
        return items.values.map { it.itemLevel }.average()
    }

    /**
     * Counts how many tier set pieces are equipped.
     */
    fun getTierPieceCount(): Int {
        return items.values.count { it.isTierPiece }
    }

    /**
     * Checks if the character has the specified tier bonus.
     *
     * @param pieces Number of pieces required (2 or 4)
     * @return true if the character has at least that many tier pieces equipped
     */
    fun hasTierBonus(pieces: Int): Boolean {
        require(pieces in listOf(2, 4)) { "Tier bonus can only be 2 or 4 pieces" }
        return getTierPieceCount() >= pieces
    }

    /**
     * Gets the item in a specific equipment slot, if any.
     */
    fun getItem(slot: EquipmentSlot): GearItem? = items[slot]
}

/**
 * Represents a single equipped item.
 */
data class GearItem(
    val itemId: ItemId,
    val name: String,
    val itemLevel: Int,
    val quality: ItemQuality,
    val slot: EquipmentSlot,
    val isTierPiece: Boolean = false,
    val enchant: String? = null,
    val sockets: Int = 0
) {
    init {
        require(itemLevel > 0) { "Item level must be positive" }
        require(sockets >= 0) { "Sockets cannot be negative" }
    }
}

/**
 * Equipment slots where items can be equipped.
 */
enum class EquipmentSlot {
    HEAD,
    NECK,
    SHOULDER,
    BACK,
    CHEST,
    WRIST,
    HANDS,
    WAIST,
    LEGS,
    FEET,
    FINGER_1,
    FINGER_2,
    TRINKET_1,
    TRINKET_2,
    MAIN_HAND,
    OFF_HAND;

    companion object {
        fun fromString(value: String): EquipmentSlot? =
            entries.firstOrNull { it.name.replace("_", "").equals(value.replace(" ", ""), ignoreCase = true) }
    }
}

/**
 * Item quality (rarity) levels.
 */
enum class ItemQuality {
    POOR,
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY,
    ARTIFACT;

    companion object {
        fun fromInt(value: Int): ItemQuality? = entries.getOrNull(value)
    }
}

/**
 * Type of gear set (best possible vs currently equipped).
 */
enum class GearSetType {
    EQUIPPED,  // Currently equipped gear
    BEST       // Best in bags
}
