package com.edgerush.lootman.domain.shared

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for ItemId value object.
 */
class ItemIdTest : UnitTest() {

    @Nested
    inner class CreationTests {

        @Test
        fun `should create valid item ID with positive value`() {
            // Arrange & Act
            val itemId = ItemId(12345L)

            // Assert
            itemId.value shouldBe 12345L
        }

        @Test
        fun `should create item ID with value of 1`() {
            // Arrange & Act
            val itemId = ItemId(1L)

            // Assert
            itemId.value shouldBe 1L
        }

        @Test
        fun `should create item ID with large positive value`() {
            // Arrange & Act
            val itemId = ItemId(Long.MAX_VALUE)

            // Assert
            itemId.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should create item ID with typical WoW item ID`() {
            // Arrange - typical WoW item IDs are in the hundreds of thousands
            val wowItemId = 207160L

            // Act
            val itemId = ItemId(wowItemId)

            // Assert
            itemId.value shouldBe 207160L
        }
    }

    @Nested
    inner class ValidationTests {

        @Test
        fun `should throw exception when value is zero`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                ItemId(0L)
            }
            exception.message shouldBe "Item ID must be positive"
        }

        @Test
        fun `should throw exception when value is negative`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                ItemId(-1L)
            }
            exception.message shouldBe "Item ID must be positive"
        }

        @Test
        fun `should throw exception when value is large negative`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                ItemId(-999999L)
            }
            exception.message shouldBe "Item ID must be positive"
        }

        @Test
        fun `should throw exception when value is Long MIN_VALUE`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                ItemId(Long.MIN_VALUE)
            }
            exception.message shouldBe "Item ID must be positive"
        }
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `should be equal when values are the same`() {
            // Arrange
            val itemId1 = ItemId(12345L)
            val itemId2 = ItemId(12345L)

            // Assert
            itemId1 shouldBe itemId2
        }

        @Test
        fun `should have same hash code when values are the same`() {
            // Arrange
            val itemId1 = ItemId(12345L)
            val itemId2 = ItemId(12345L)

            // Assert
            itemId1.hashCode() shouldBe itemId2.hashCode()
        }

        @Test
        fun `should not be equal when values are different`() {
            // Arrange
            val itemId1 = ItemId(12345L)
            val itemId2 = ItemId(67890L)

            // Assert
            itemId1 shouldNotBe itemId2
        }

        @Test
        fun `should not be equal when values differ by one`() {
            // Arrange
            val itemId1 = ItemId(100L)
            val itemId2 = ItemId(101L)

            // Assert
            itemId1 shouldNotBe itemId2
        }
    }

    @Nested
    inner class DataClassTests {

        @Test
        fun `should allow destructuring`() {
            // Arrange
            val itemId = ItemId(12345L)

            // Act
            val (value) = itemId

            // Assert
            value shouldBe 12345L
        }

        @Test
        fun `should have meaningful toString`() {
            // Arrange
            val itemId = ItemId(207160L)

            // Act
            val stringRepresentation = itemId.toString()

            // Assert
            stringRepresentation shouldBe "ItemId(value=207160)"
        }

        @Test
        fun `should create copy with same value`() {
            // Arrange
            val original = ItemId(12345L)

            // Act
            val copy = original.copy()

            // Assert
            copy shouldBe original
            copy.value shouldBe 12345L
        }

        @Test
        fun `should create copy with different value`() {
            // Arrange
            val original = ItemId(12345L)

            // Act
            val copy = original.copy(value = 67890L)

            // Assert
            copy.value shouldBe 67890L
            original.value shouldBe 12345L
        }

        @Test
        fun `should throw exception when copy has invalid value`() {
            // Arrange
            val original = ItemId(12345L)

            // Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                original.copy(value = 0L)
            }
            exception.message shouldBe "Item ID must be positive"
        }
    }

    @Nested
    inner class BoundaryTests {

        @Test
        fun `should handle minimum valid value of 1`() {
            // Arrange & Act
            val itemId = ItemId(1L)

            // Assert
            itemId.value shouldBe 1L
        }

        @Test
        fun `should handle maximum Long value`() {
            // Arrange & Act
            val itemId = ItemId(Long.MAX_VALUE)

            // Assert
            itemId.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should handle value just above zero`() {
            // Arrange & Act
            val itemId = ItemId(1L)

            // Assert
            itemId.value shouldBe 1L
        }

        @Test
        fun `should reject value just below minimum`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                ItemId(0L)
            }
            exception.message shouldBe "Item ID must be positive"
        }
    }

    @Nested
    inner class UsageTests {

        @Test
        fun `should work as map key`() {
            // Arrange
            val itemId = ItemId(12345L)
            val map = mapOf(itemId to "Test Item")

            // Act
            val result = map[ItemId(12345L)]

            // Assert
            result shouldBe "Test Item"
        }

        @Test
        fun `should work in set`() {
            // Arrange
            val itemId1 = ItemId(100L)
            val itemId2 = ItemId(100L)
            val itemId3 = ItemId(200L)

            // Act
            val set = setOf(itemId1, itemId2, itemId3)

            // Assert
            set.size shouldBe 2
        }

        @Test
        fun `should be comparable in sorted list`() {
            // Arrange
            val itemIds = listOf(ItemId(300L), ItemId(100L), ItemId(200L))

            // Act
            val sorted = itemIds.sortedBy { it.value }

            // Assert
            sorted[0].value shouldBe 100L
            sorted[1].value shouldBe 200L
            sorted[2].value shouldBe 300L
        }
    }
}
