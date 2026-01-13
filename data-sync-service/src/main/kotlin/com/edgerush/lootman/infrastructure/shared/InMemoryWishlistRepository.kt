package com.edgerush.lootman.infrastructure.shared

import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of WishlistRepository.
 *
 * Uses ConcurrentHashMap for thread-safe operations.
 */
@Repository
class InMemoryWishlistRepository : WishlistRepository {
    private val storage = ConcurrentHashMap<RaiderId, Wishlist>()

    override fun findByRaiderId(raiderId: RaiderId): Wishlist? = storage[raiderId]

    override fun save(wishlist: Wishlist): Wishlist {
        storage[wishlist.raiderId] = wishlist
        return wishlist
    }

    override fun delete(raiderId: RaiderId) {
        storage.remove(raiderId)
    }
}
