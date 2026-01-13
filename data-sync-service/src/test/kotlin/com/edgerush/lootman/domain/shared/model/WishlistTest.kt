package com.edgerush.lootman.domain.shared.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for Wishlist and WishlistItem.
 */
class WishlistTest : UnitTest() {

    // region Test Fixtures

    private fun createWishlistItem(
        itemId: Long = 207160L,
        itemName: String = "Crown of the Firelands",
        priority: Int = 1,
        upgradePercentage: Double = 15.5,
        specName: String? = "Protection"
    ) = WishlistItem(
        itemId = ItemId(itemId),
        itemName = itemName,
        priority = priority,
        upgradePercentage = upgradePercentage,
        specName = specName
    )

    private fun createWishlist(
        raiderId: RaiderId = RaiderId(1L),
        items: List<WishlistItem> = emptyList()
    ) = Wishlist(
        raiderId = raiderId,
        items = items
    )

    // endregion

    @Nested
    inner class WishlistItemCreationTests {

        @Test
        fun `should create valid wishlist item with all fields`() {
            // Arrange & Act
            val item = createWishlistItem(
                itemId = 207160L,
                itemName = "Helm of Domination",
                priority = 1,
                upgradePercentage = 18.7,
                specName = "Frost"
            )

            // Assert
            item.itemId shouldBe ItemId(207160L)
            item.itemName shouldBe "Helm of Domination"
            item.priority shouldBe 1
            item.upgradePercentage shouldBeExactly 18.7
            item.specName shouldBe "Frost"
        }

        @Test
        fun `should create wishlist item with null specName`() {
            // Arrange & Act
            val item = createWishlistItem(specName = null)

            // Assert
            item.specName.shouldBeNull()
        }

        @Test
        fun `should throw exception when priority is zero`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                createWishlistItem(priority = 0)
            }
            exception.message shouldBe "Priority must be positive"
        }

        @Test
        fun `should throw exception when priority is negative`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                createWishlistItem(priority = -1)
            }
            exception.message shouldBe "Priority must be positive"
        }

        @Test
        fun `should throw exception when upgrade percentage is negative`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                createWishlistItem(upgradePercentage = -0.1)
            }
            exception.message shouldBe "Upgrade percentage cannot be negative"
        }

        @Test
        fun `should allow zero upgrade percentage`() {
            // Arrange & Act
            val item = createWishlistItem(upgradePercentage = 0.0)

            // Assert
            item.upgradePercentage shouldBeExactly 0.0
        }

        @Test
        fun `should allow high upgrade percentages`() {
            // Arrange & Act
            val item = createWishlistItem(upgradePercentage = 150.0)

            // Assert
            item.upgradePercentage shouldBeExactly 150.0
        }

        @Test
        fun `should allow priority of 1`() {
            // Arrange & Act
            val item = createWishlistItem(priority = 1)

            // Assert
            item.priority shouldBe 1
        }

        @Test
        fun `should be immutable data class`() {
            // Arrange
            val item1 = createWishlistItem(itemId = 100L, priority = 1)
            val item2 = createWishlistItem(itemId = 100L, priority = 1)

            // Assert
            item1 shouldBe item2
            item1.hashCode() shouldBe item2.hashCode()
        }
    }

    @Nested
    inner class GetNormalizedUpgradeValueTests {

        @Test
        fun `should return normalized value for typical upgrade percentage`() {
            // Arrange
            val item = createWishlistItem(upgradePercentage = 15.0)

            // Act
            val normalized = item.getNormalizedUpgradeValue()

            // Assert
            normalized shouldBeExactly 0.15
        }

        @Test
        fun `should return zero for zero upgrade percentage`() {
            // Arrange
            val item = createWishlistItem(upgradePercentage = 0.0)

            // Act
            val normalized = item.getNormalizedUpgradeValue()

            // Assert
            normalized shouldBeExactly 0.0
        }

        @Test
        fun `should return 1_0 for 100 percent upgrade`() {
            // Arrange
            val item = createWishlistItem(upgradePercentage = 100.0)

            // Act
            val normalized = item.getNormalizedUpgradeValue()

            // Assert
            normalized shouldBeExactly 1.0
        }

        @Test
        fun `should coerce to 1_0 when upgrade percentage exceeds 100`() {
            // Arrange
            val item = createWishlistItem(upgradePercentage = 150.0)

            // Act
            val normalized = item.getNormalizedUpgradeValue()

            // Assert
            normalized shouldBeExactly 1.0
        }

        @Test
        fun `should handle small fractional upgrades`() {
            // Arrange
            val item = createWishlistItem(upgradePercentage = 0.5)

            // Act
            val normalized = item.getNormalizedUpgradeValue()

            // Assert
            normalized shouldBeExactly 0.005
        }

        @Test
        fun `should handle 50 percent upgrade`() {
            // Arrange
            val item = createWishlistItem(upgradePercentage = 50.0)

            // Act
            val normalized = item.getNormalizedUpgradeValue()

            // Assert
            normalized shouldBeExactly 0.5
        }
    }

    @Nested
    inner class WishlistCreationTests {

        @Test
        fun `should create valid wishlist with items`() {
            // Arrange
            val items = listOf(
                createWishlistItem(itemId = 100L, priority = 1),
                createWishlistItem(itemId = 200L, priority = 2)
            )

            // Act
            val wishlist = createWishlist(
                raiderId = RaiderId(42L),
                items = items
            )

            // Assert
            wishlist.raiderId shouldBe RaiderId(42L)
            wishlist.items.size shouldBe 2
        }

        @Test
        fun `should create empty wishlist`() {
            // Arrange & Act
            val wishlist = createWishlist(items = emptyList())

            // Assert
            wishlist.items.shouldBeEmpty()
        }
    }

    @Nested
    inner class FindItemTests {

        @Test
        fun `should find item by item ID when present`() {
            // Arrange
            val targetItem = createWishlistItem(itemId = 200L, itemName = "Target Item")
            val wishlist = createWishlist(
                items = listOf(
                    createWishlistItem(itemId = 100L, itemName = "First Item"),
                    targetItem,
                    createWishlistItem(itemId = 300L, itemName = "Third Item")
                )
            )

            // Act
            val found = wishlist.findItem(ItemId(200L))

            // Assert
            found shouldBe targetItem
        }

        @Test
        fun `should return null when item ID not found`() {
            // Arrange
            val wishlist = createWishlist(
                items = listOf(
                    createWishlistItem(itemId = 100L),
                    createWishlistItem(itemId = 200L)
                )
            )

            // Act
            val found = wishlist.findItem(ItemId(999L))

            // Assert
            found.shouldBeNull()
        }

        @Test
        fun `should return null when wishlist is empty`() {
            // Arrange
            val wishlist = createWishlist(items = emptyList())

            // Act
            val found = wishlist.findItem(ItemId(100L))

            // Assert
            found.shouldBeNull()
        }

        @Test
        fun `should find first item when multiple items have same ID`() {
            // Arrange
            val firstItem = createWishlistItem(itemId = 100L, itemName = "First", specName = "Frost")
            val secondItem = createWishlistItem(itemId = 100L, itemName = "Second", specName = "Fire")
            val wishlist = createWishlist(items = listOf(firstItem, secondItem))

            // Act
            val found = wishlist.findItem(ItemId(100L))

            // Assert
            found shouldBe firstItem
        }
    }

    @Nested
    inner class GetUpgradePercentageTests {

        @Test
        fun `should return upgrade percentage for existing item`() {
            // Arrange
            val wishlist = createWishlist(
                items = listOf(
                    createWishlistItem(itemId = 100L, upgradePercentage = 12.5),
                    createWishlistItem(itemId = 200L, upgradePercentage = 25.0)
                )
            )

            // Act
            val percentage = wishlist.getUpgradePercentage(ItemId(200L))

            // Assert
            percentage shouldBe 25.0
        }

        @Test
        fun `should return null for non-existing item`() {
            // Arrange
            val wishlist = createWishlist(
                items = listOf(
                    createWishlistItem(itemId = 100L, upgradePercentage = 12.5)
                )
            )

            // Act
            val percentage = wishlist.getUpgradePercentage(ItemId(999L))

            // Assert
            percentage.shouldBeNull()
        }

        @Test
        fun `should return null for empty wishlist`() {
            // Arrange
            val wishlist = createWishlist(items = emptyList())

            // Act
            val percentage = wishlist.getUpgradePercentage(ItemId(100L))

            // Assert
            percentage.shouldBeNull()
        }
    }

    @Nested
    inner class ContainsItemTests {

        @Test
        fun `should return true when item exists`() {
            // Arrange
            val wishlist = createWishlist(
                items = listOf(
                    createWishlistItem(itemId = 100L),
                    createWishlistItem(itemId = 200L),
                    createWishlistItem(itemId = 300L)
                )
            )

            // Act & Assert
            wishlist.containsItem(ItemId(200L)) shouldBe true
        }

        @Test
        fun `should return false when item does not exist`() {
            // Arrange
            val wishlist = createWishlist(
                items = listOf(
                    createWishlistItem(itemId = 100L),
                    createWishlistItem(itemId = 200L)
                )
            )

            // Act & Assert
            wishlist.containsItem(ItemId(999L)) shouldBe false
        }

        @Test
        fun `should return false for empty wishlist`() {
            // Arrange
            val wishlist = createWishlist(items = emptyList())

            // Act & Assert
            wishlist.containsItem(ItemId(100L)) shouldBe false
        }
    }

    @Nested
    inner class GetItemsByPriorityTests {

        @Test
        fun `should return items sorted by priority descending`() {
            // Arrange
            val lowPriority = createWishlistItem(itemId = 100L, itemName = "Low", priority = 1)
            val medPriority = createWishlistItem(itemId = 200L, itemName = "Medium", priority = 5)
            val highPriority = createWishlistItem(itemId = 300L, itemName = "High", priority = 10)
            val wishlist = createWishlist(items = listOf(lowPriority, highPriority, medPriority))

            // Act
            val sorted = wishlist.getItemsByPriority()

            // Assert
            sorted.map { it.itemName } shouldContainExactly listOf("High", "Medium", "Low")
        }

        @Test
        fun `should return empty list for empty wishlist`() {
            // Arrange
            val wishlist = createWishlist(items = emptyList())

            // Act
            val sorted = wishlist.getItemsByPriority()

            // Assert
            sorted.shouldBeEmpty()
        }

        @Test
        fun `should handle single item`() {
            // Arrange
            val item = createWishlistItem(itemId = 100L, priority = 5)
            val wishlist = createWishlist(items = listOf(item))

            // Act
            val sorted = wishlist.getItemsByPriority()

            // Assert
            sorted shouldContainExactly listOf(item)
        }

        @Test
        fun `should maintain stable order for equal priorities`() {
            // Arrange
            val first = createWishlistItem(itemId = 100L, itemName = "First", priority = 5)
            val second = createWishlistItem(itemId = 200L, itemName = "Second", priority = 5)
            val third = createWishlistItem(itemId = 300L, itemName = "Third", priority = 5)
            val wishlist = createWishlist(items = listOf(first, second, third))

            // Act
            val sorted = wishlist.getItemsByPriority()

            // Assert - stable sort should maintain original order for equal priorities
            sorted.map { it.itemName } shouldContainExactly listOf("First", "Second", "Third")
        }

        @Test
        fun `should not modify original items list`() {
            // Arrange
            val items = listOf(
                createWishlistItem(itemId = 100L, priority = 1),
                createWishlistItem(itemId = 200L, priority = 10),
                createWishlistItem(itemId = 300L, priority = 5)
            )
            val wishlist = createWishlist(items = items)

            // Act
            wishlist.getItemsByPriority()

            // Assert - original order unchanged
            wishlist.items[0].priority shouldBe 1
            wishlist.items[1].priority shouldBe 10
            wishlist.items[2].priority shouldBe 5
        }
    }

    @Nested
    inner class WishlistImmutabilityTests {

        @Test
        fun `should be immutable data class`() {
            // Arrange
            val items = listOf(createWishlistItem(itemId = 100L))
            val wishlist1 = createWishlist(raiderId = RaiderId(1L), items = items)
            val wishlist2 = createWishlist(raiderId = RaiderId(1L), items = items)

            // Assert
            wishlist1 shouldBe wishlist2
            wishlist1.hashCode() shouldBe wishlist2.hashCode()
        }

        @Test
        fun `should allow creating modified copy`() {
            // Arrange
            val originalItems = listOf(createWishlistItem(itemId = 100L))
            val original = createWishlist(raiderId = RaiderId(1L), items = originalItems)

            // Act
            val newItems = listOf(
                createWishlistItem(itemId = 100L),
                createWishlistItem(itemId = 200L)
            )
            val modified = original.copy(items = newItems)

            // Assert
            original.items.size shouldBe 1
            modified.items.size shouldBe 2
        }
    }

    @Nested
    inner class WishlistEdgeCaseTests {

        @Test
        fun `should handle large wishlist with many items`() {
            // Arrange
            val items = (1L..100L).map { id ->
                createWishlistItem(
                    itemId = id,
                    itemName = "Item $id",
                    priority = id.toInt(),
                    upgradePercentage = id.toDouble()
                )
            }
            val wishlist = createWishlist(items = items)

            // Act & Assert
            wishlist.items.size shouldBe 100
            wishlist.containsItem(ItemId(50L)) shouldBe true
            wishlist.containsItem(ItemId(101L)) shouldBe false
            wishlist.getItemsByPriority().first().priority shouldBe 100
            wishlist.getItemsByPriority().last().priority shouldBe 1
        }

        @Test
        fun `should handle items with very small upgrade percentages`() {
            // Arrange
            val item = createWishlistItem(upgradePercentage = 0.001)

            // Act
            val normalized = item.getNormalizedUpgradeValue()

            // Assert
            normalized shouldBeExactly 0.00001
        }

        @Test
        fun `should handle maximum integer priority`() {
            // Arrange & Act
            val item = createWishlistItem(priority = Int.MAX_VALUE)

            // Assert
            item.priority shouldBe Int.MAX_VALUE
        }
    }
}
