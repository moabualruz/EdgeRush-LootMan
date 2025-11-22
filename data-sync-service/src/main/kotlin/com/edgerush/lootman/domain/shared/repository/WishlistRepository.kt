package com.edgerush.lootman.domain.shared.repository

import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist

/**
 * Repository interface for Wishlist aggregate.
 */
interface WishlistRepository {
    /**
     * Finds a wishlist for a specific raider.
     *
     * @param raiderId The raider's unique identifier
     * @return The wishlist if found, null otherwise
     */
    fun findByRaiderId(raiderId: RaiderId): Wishlist?

    /**
     * Saves a wishlist.
     *
     * @param wishlist The wishlist to save
     * @return The saved wishlist
     */
    fun save(wishlist: Wishlist): Wishlist

    /**
     * Deletes a wishlist.
     *
     * @param raiderId The raider ID whose wishlist to delete
     */
    fun delete(raiderId: RaiderId)
}
