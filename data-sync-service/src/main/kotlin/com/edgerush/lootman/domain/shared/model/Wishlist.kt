package com.edgerush.lootman.domain.shared.model

import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId

/**
 * Wishlist aggregate representing a raider's desired items.
 *
 * Contains the items a raider wants and their relative priority/upgrade value.
 */
data class Wishlist(
    val raiderId: RaiderId,
    val items: List<WishlistItem>
) {
    /**
     * Finds a wishlist item by item ID.
     */
    fun findItem(itemId: ItemId): WishlistItem? =
        items.firstOrNull { it.itemId == itemId }

    /**
     * Gets the upgrade percentage for a specific item, or null if not on wishlist.
     */
    fun getUpgradePercentage(itemId: ItemId): Double? =
        findItem(itemId)?.upgradePercentage

    /**
     * Checks if an item is on the wishlist.
     */
    fun containsItem(itemId: ItemId): Boolean =
        items.any { it.itemId == itemId }

    /**
     * Gets items sorted by priority (highest first).
     */
    fun getItemsByPriority(): List<WishlistItem> =
        items.sortedByDescending { it.priority }
}

/**
 * Represents a single item on a wishlist with its priority and upgrade value.
 */
data class WishlistItem(
    val itemId: ItemId,
    val itemName: String,
    val priority: Int,
    val upgradePercentage: Double,  // From WoWAudit simulation data
    val specName: String? = null
) {
    init {
        require(priority > 0) { "Priority must be positive" }
        require(upgradePercentage >= 0.0) { "Upgrade percentage cannot be negative" }
    }

    /**
     * Gets the normalized upgrade value (0.0 to 1.0) for FLPS calculations.
     */
    fun getNormalizedUpgradeValue(): Double = (upgradePercentage / 100.0).coerceIn(0.0, 1.0)
}
