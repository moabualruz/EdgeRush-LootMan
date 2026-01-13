package com.edgerush.lootman.api.wishlist

import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import java.time.LocalDateTime

/**
 * Request to create or update a wishlist.
 */
data class SaveWishlistRequest(
    val raiderId: Long,
    val items: List<WishlistItemRequest>
)

/**
 * Request for a single wishlist item.
 */
data class WishlistItemRequest(
    val itemId: Long,
    val itemName: String,
    val priority: Int,
    val upgradePercentage: Double,
    val specName: String? = null
)

/**
 * Response for a wishlist.
 */
data class WishlistResponse(
    val raiderId: Long,
    val items: List<WishlistItemResponse>,
    val itemCount: Int,
    val topPriorityItem: WishlistItemResponse?
) {
    companion object {
        fun from(wishlist: Wishlist): WishlistResponse {
            val itemResponses = wishlist.items.map { WishlistItemResponse.from(it) }
            return WishlistResponse(
                raiderId = wishlist.raiderId.value,
                items = itemResponses,
                itemCount = itemResponses.size,
                topPriorityItem = wishlist.getItemsByPriority().firstOrNull()?.let { WishlistItemResponse.from(it) }
            )
        }
    }
}

/**
 * Response for a single wishlist item.
 */
data class WishlistItemResponse(
    val itemId: Long,
    val itemName: String,
    val priority: Int,
    val upgradePercentage: Double,
    val normalizedUpgradeValue: Double,
    val specName: String?
) {
    companion object {
        fun from(item: WishlistItem): WishlistItemResponse = WishlistItemResponse(
            itemId = item.itemId.value,
            itemName = item.itemName,
            priority = item.priority,
            upgradePercentage = item.upgradePercentage,
            normalizedUpgradeValue = item.getNormalizedUpgradeValue(),
            specName = item.specName
        )
    }
}
