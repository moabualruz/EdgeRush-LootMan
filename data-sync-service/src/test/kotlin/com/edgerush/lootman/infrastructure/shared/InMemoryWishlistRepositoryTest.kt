package com.edgerush.lootman.infrastructure.shared

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.domain.shared.model.Wishlist
import com.edgerush.lootman.domain.shared.model.WishlistItem
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for InMemoryWishlistRepository.
 *
 * Following TDD - these tests are written before the implementation.
 */
class InMemoryWishlistRepositoryTest : UnitTest() {
    private lateinit var repository: InMemoryWishlistRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryWishlistRepository()
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save and return the wishlist`() {
            // Arrange
            val wishlist = createWishlist()

            // Act
            val saved = repository.save(wishlist)

            // Assert
            saved shouldBe wishlist
        }

        @Test
        fun `should persist wishlist to storage`() {
            // Arrange
            val wishlist = createWishlist()

            // Act
            repository.save(wishlist)
            val retrieved = repository.findByRaiderId(wishlist.raiderId)

            // Assert
            retrieved shouldBe wishlist
        }

        @Test
        fun `should replace existing wishlist for same raider`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val originalWishlist =
                createWishlist(
                    raiderId = raiderId,
                    items =
                        listOf(
                            createWishlistItem(itemId = ItemId(100L), itemName = "Original Item"),
                        ),
                )
            repository.save(originalWishlist)

            val newWishlist =
                createWishlist(
                    raiderId = raiderId,
                    items =
                        listOf(
                            createWishlistItem(itemId = ItemId(200L), itemName = "New Item 1"),
                            createWishlistItem(itemId = ItemId(201L), itemName = "New Item 2", priority = 2),
                        ),
                )

            // Act
            repository.save(newWishlist)
            val retrieved = repository.findByRaiderId(raiderId)

            // Assert
            retrieved shouldBe newWishlist
            retrieved?.items?.size shouldBe 2
            retrieved?.items?.first()?.itemName shouldBe "New Item 1"
        }

        @Test
        fun `should save multiple wishlists for different raiders`() {
            // Arrange
            val wishlist1 = createWishlist(raiderId = RaiderId(1L))
            val wishlist2 = createWishlist(raiderId = RaiderId(2L))
            val wishlist3 = createWishlist(raiderId = RaiderId(3L))

            // Act
            repository.save(wishlist1)
            repository.save(wishlist2)
            repository.save(wishlist3)

            // Assert
            repository.findByRaiderId(wishlist1.raiderId) shouldBe wishlist1
            repository.findByRaiderId(wishlist2.raiderId) shouldBe wishlist2
            repository.findByRaiderId(wishlist3.raiderId) shouldBe wishlist3
        }
    }

    @Nested
    inner class FindByRaiderIdTests {
        @Test
        fun `should return wishlist when found`() {
            // Arrange
            val wishlist = createWishlist()
            repository.save(wishlist)

            // Act
            val retrieved = repository.findByRaiderId(wishlist.raiderId)

            // Assert
            retrieved shouldNotBe null
            retrieved shouldBe wishlist
        }

        @Test
        fun `should return null when wishlist not found`() {
            // Arrange
            val nonExistentRaiderId = RaiderId(9999L)

            // Act
            val retrieved = repository.findByRaiderId(nonExistentRaiderId)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return null for raider id that was never saved`() {
            // Arrange
            val wishlist = createWishlist(raiderId = RaiderId(1L))
            val differentRaiderId = RaiderId(2L)
            repository.save(wishlist)

            // Act
            val retrieved = repository.findByRaiderId(differentRaiderId)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return correct wishlist for specific raider`() {
            // Arrange
            val wishlist1 =
                createWishlist(
                    raiderId = RaiderId(1L),
                    items =
                        listOf(
                            createWishlistItem(itemName = "Raider1 Item"),
                        ),
                )
            val wishlist2 =
                createWishlist(
                    raiderId = RaiderId(2L),
                    items =
                        listOf(
                            createWishlistItem(itemName = "Raider2 Item"),
                        ),
                )

            repository.save(wishlist1)
            repository.save(wishlist2)

            // Act
            val retrieved = repository.findByRaiderId(RaiderId(1L))

            // Assert
            retrieved?.items?.first()?.itemName shouldBe "Raider1 Item"
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete existing wishlist`() {
            // Arrange
            val wishlist = createWishlist()
            repository.save(wishlist)

            // Act
            repository.delete(wishlist.raiderId)

            // Assert
            repository.findByRaiderId(wishlist.raiderId) shouldBe null
        }

        @Test
        fun `should not throw when deleting non-existent wishlist`() {
            // Arrange
            val nonExistentRaiderId = RaiderId(9999L)

            // Act & Assert - should not throw
            repository.delete(nonExistentRaiderId)
        }

        @Test
        fun `should only delete specified wishlist and leave others intact`() {
            // Arrange
            val wishlist1 = createWishlist(raiderId = RaiderId(1L))
            val wishlist2 = createWishlist(raiderId = RaiderId(2L))
            val wishlist3 = createWishlist(raiderId = RaiderId(3L))

            repository.save(wishlist1)
            repository.save(wishlist2)
            repository.save(wishlist3)

            // Act
            repository.delete(wishlist2.raiderId)

            // Assert
            repository.findByRaiderId(wishlist1.raiderId) shouldBe wishlist1
            repository.findByRaiderId(wishlist2.raiderId) shouldBe null
            repository.findByRaiderId(wishlist3.raiderId) shouldBe wishlist3
        }
    }

    @Nested
    inner class ConcurrencyTests {
        @Test
        fun `should handle concurrent saves without data loss`() {
            // Arrange
            val wishlists =
                (1..100).map { index ->
                    createWishlist(raiderId = RaiderId(index.toLong()))
                }

            // Act - simulate concurrent saves
            wishlists.parallelStream().forEach { wishlist ->
                repository.save(wishlist)
            }

            // Assert - all wishlists should be saved
            wishlists.forEach { wishlist ->
                repository.findByRaiderId(wishlist.raiderId) shouldBe wishlist
            }
        }
    }

    @Nested
    inner class WishlistItemsTests {
        @Test
        fun `should correctly store and retrieve wishlist with multiple items`() {
            // Arrange
            val items =
                listOf(
                    createWishlistItem(itemId = ItemId(100L), itemName = "Item 1", priority = 1),
                    createWishlistItem(itemId = ItemId(200L), itemName = "Item 2", priority = 2),
                    createWishlistItem(itemId = ItemId(300L), itemName = "Item 3", priority = 3),
                )
            val wishlist = createWishlist(items = items)

            // Act
            repository.save(wishlist)
            val retrieved = repository.findByRaiderId(wishlist.raiderId)

            // Assert
            retrieved?.items?.size shouldBe 3
            retrieved?.items?.map { it.itemName } shouldBe listOf("Item 1", "Item 2", "Item 3")
        }

        @Test
        fun `should correctly store empty wishlist`() {
            // Arrange
            val wishlist = createWishlist(items = emptyList())

            // Act
            repository.save(wishlist)
            val retrieved = repository.findByRaiderId(wishlist.raiderId)

            // Assert
            retrieved shouldNotBe null
            retrieved?.items?.size shouldBe 0
        }

        @Test
        fun `should preserve upgrade percentage values`() {
            // Arrange
            val items =
                listOf(
                    createWishlistItem(upgradePercentage = 5.5),
                    createWishlistItem(itemId = ItemId(200L), upgradePercentage = 15.75, priority = 2),
                )
            val wishlist = createWishlist(items = items)

            // Act
            repository.save(wishlist)
            val retrieved = repository.findByRaiderId(wishlist.raiderId)

            // Assert
            retrieved?.items?.first()?.upgradePercentage shouldBe 5.5
            retrieved?.items?.get(1)?.upgradePercentage shouldBe 15.75
        }
    }

    private fun createWishlist(
        raiderId: RaiderId = RaiderId(1L),
        items: List<WishlistItem> = listOf(createWishlistItem()),
    ): Wishlist =
        Wishlist(
            raiderId = raiderId,
            items = items,
        )

    private fun createWishlistItem(
        itemId: ItemId = ItemId(12345L),
        itemName: String = "Test Item",
        priority: Int = 1,
        upgradePercentage: Double = 10.0,
        specName: String? = null,
    ): WishlistItem =
        WishlistItem(
            itemId = itemId,
            itemName = itemName,
            priority = priority,
            upgradePercentage = upgradePercentage,
            specName = specName,
        )
}
