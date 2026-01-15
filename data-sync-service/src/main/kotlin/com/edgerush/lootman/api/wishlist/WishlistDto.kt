package com.edgerush.lootman.api.wishlist

import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

/**
 * Request to create or update a wishlist.
 */
data class SaveWishlistRequest(
    @field:Min(value = 1, message = "Raider ID must be positive")
    val raiderId: Long,
    @field:NotEmpty(message = "Wishlist must contain at least one item")
    @field:Valid
    val items: List<WishlistItemRequest>,
)

/**
 * Request for a single wishlist item.
 */
data class WishlistItemRequest(
    @field:Min(value = 1, message = "Item ID must be positive")
    val itemId: Long,
    @field:NotBlank(message = "Item name is required")
    @field:Size(max = 100, message = "Item name cannot exceed 100 characters")
    val itemName: String,
    @field:Min(value = 1, message = "Priority must be at least 1")
    val priority: Int,
    @field:DecimalMin(value = "0.0", message = "Upgrade percentage must be non-negative")
    @field:DecimalMax(value = "100.0", message = "Upgrade percentage cannot exceed 100%")
    val upgradePercentage: Double,
    val specName: String? = null,
)

/**
 * Response for a wishlist.
 */
data class WishlistResponse(
    val raiderId: Long,
    val items: List<WishlistItemResponse>,
    val itemCount: Int,
    val topPriorityItem: WishlistItemResponse?,
) {
    companion object {
        fun from(wishlist: Wishlist): WishlistResponse {
            val itemResponses = wishlist.items.map { WishlistItemResponse.from(it) }
            return WishlistResponse(
                raiderId = wishlist.raiderId.value,
                items = itemResponses,
                itemCount = itemResponses.size,
                topPriorityItem = wishlist.getItemsByPriority().firstOrNull()?.let { WishlistItemResponse.from(it) },
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
    val specName: String?,
) {
    companion object {
        fun from(item: WishlistItem): WishlistItemResponse =
            WishlistItemResponse(
                itemId = item.itemId.value,
                itemName = item.itemName,
                priority = item.priority,
                upgradePercentage = item.upgradePercentage,
                normalizedUpgradeValue = item.getNormalizedUpgradeValue(),
                specName = item.specName,
            )
    }
}
