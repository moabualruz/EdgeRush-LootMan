package com.edgerush.lootman.application.wishlist

import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import org.springframework.stereotype.Service

/**
 * Use case for retrieving a raider's wishlist.
 */
@Service
class GetWishlistUseCase(
    private val wishlistRepository: WishlistRepository
) {
    fun execute(query: GetWishlistQuery): Result<Wishlist> = runCatching {
        wishlistRepository.findByRaiderId(RaiderId(query.raiderId))
            ?: throw NoSuchElementException("Wishlist not found for raider: ${query.raiderId}")
    }
}

/**
 * Use case for creating or updating a wishlist.
 */
@Service
class SaveWishlistUseCase(
    private val wishlistRepository: WishlistRepository
) {
    fun execute(command: SaveWishlistCommand): Result<Wishlist> = runCatching {
        val wishlist = Wishlist(
            raiderId = RaiderId(command.raiderId),
            items = command.items.map { item ->
                WishlistItem(
                    itemId = ItemId(item.itemId),
                    itemName = item.itemName,
                    priority = item.priority,
                    upgradePercentage = item.upgradePercentage,
                    specName = item.specName
                )
            }
        )
        wishlistRepository.save(wishlist)
    }
}

/**
 * Use case for deleting a raider's wishlist.
 */
@Service
class DeleteWishlistUseCase(
    private val wishlistRepository: WishlistRepository
) {
    fun execute(command: DeleteWishlistCommand): Result<Unit> = runCatching {
        val raiderId = RaiderId(command.raiderId)
        wishlistRepository.findByRaiderId(raiderId)
            ?: throw NoSuchElementException("Wishlist not found for raider: ${command.raiderId}")
        wishlistRepository.delete(raiderId)
    }
}

// Query and Command classes

data class GetWishlistQuery(
    val raiderId: Long
)

data class SaveWishlistCommand(
    val raiderId: Long,
    val items: List<WishlistItemCommand>
)

data class WishlistItemCommand(
    val itemId: Long,
    val itemName: String,
    val priority: Int,
    val upgradePercentage: Double,
    val specName: String? = null
)

data class DeleteWishlistCommand(
    val raiderId: Long
)
