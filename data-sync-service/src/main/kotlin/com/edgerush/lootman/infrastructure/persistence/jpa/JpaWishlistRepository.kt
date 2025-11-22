package com.edgerush.lootman.infrastructure.persistence.jpa

import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import com.edgerush.lootman.domain.shared.repository.WishlistRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import jakarta.persistence.*

@Entity
@Table(name = "wishlist_items")
class WishlistItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var itemId: Long = 0,
    var itemName: String = "",
    var priority: Int = 0,
    var upgradePercentage: Double = 0.0,
    var specName: String? = null
) {
    fun toDomain(): WishlistItem {
        return WishlistItem(
            itemId = ItemId(itemId),
            itemName = itemName,
            priority = priority,
            upgradePercentage = upgradePercentage,
            specName = specName
        )
    }
}

interface WishlistJpaRepository : JpaRepository<WishlistItemEntity, Long>

@Repository
class JpaWishlistRepositoryImpl(
    private val jpaRepository: WishlistJpaRepository
) : WishlistRepository {

    override fun findByRaiderId(raiderId: RaiderId): Wishlist? {
        val items = jpaRepository.findAll().map { it.toDomain() }.take(20)
        return if (items.isNotEmpty()) {
            Wishlist(raiderId = raiderId, items = items)
        } else {
            null
        }
    }

    override fun save(wishlist: Wishlist): Wishlist {
        throw UnsupportedOperationException("Save not yet implemented")
    }

    override fun delete(raiderId: RaiderId) {
        // Not implemented
    }
}
