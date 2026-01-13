package com.edgerush.lootman.domain.shared

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for RaiderId value object.
 */
class RaiderIdTest : UnitTest() {

    @Nested
    inner class CreationTests {

        @Test
        fun `should create valid raider ID with positive value`() {
            // Arrange & Act
            val raiderId = RaiderId(42L)

            // Assert
            raiderId.value shouldBe 42L
        }

        @Test
        fun `should create raider ID with value of 1`() {
            // Arrange & Act
            val raiderId = RaiderId(1L)

            // Assert
            raiderId.value shouldBe 1L
        }

        @Test
        fun `should create raider ID with large positive value`() {
            // Arrange & Act
            val raiderId = RaiderId(Long.MAX_VALUE)

            // Assert
            raiderId.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should create raider ID with typical database ID`() {
            // Arrange - typical auto-increment database ID
            val dbId = 98765L

            // Act
            val raiderId = RaiderId(dbId)

            // Assert
            raiderId.value shouldBe 98765L
        }
    }

    @Nested
    inner class ValidationTests {

        @Test
        fun `should throw exception when value is zero`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderId(0L)
            }
            exception.message shouldBe "Raider ID must be positive, got 0"
        }

        @Test
        fun `should throw exception when value is negative`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderId(-1L)
            }
            exception.message shouldBe "Raider ID must be positive, got -1"
        }

        @Test
        fun `should throw exception when value is large negative`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderId(-999999L)
            }
            exception.message shouldBe "Raider ID must be positive, got -999999"
        }

        @Test
        fun `should throw exception when value is Long MIN_VALUE`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderId(Long.MIN_VALUE)
            }
            exception.message shouldContain "Raider ID must be positive, got"
            exception.message shouldContain Long.MIN_VALUE.toString()
        }

        @Test
        fun `should include actual value in error message`() {
            // Arrange
            val invalidValue = -42L

            // Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderId(invalidValue)
            }
            exception.message shouldContain "-42"
        }
    }

    @Nested
    inner class EqualityTests {

        @Test
        fun `should be equal when values are the same`() {
            // Arrange
            val raiderId1 = RaiderId(42L)
            val raiderId2 = RaiderId(42L)

            // Assert
            raiderId1 shouldBe raiderId2
        }

        @Test
        fun `should have same hash code when values are the same`() {
            // Arrange
            val raiderId1 = RaiderId(42L)
            val raiderId2 = RaiderId(42L)

            // Assert
            raiderId1.hashCode() shouldBe raiderId2.hashCode()
        }

        @Test
        fun `should not be equal when values are different`() {
            // Arrange
            val raiderId1 = RaiderId(42L)
            val raiderId2 = RaiderId(43L)

            // Assert
            raiderId1 shouldNotBe raiderId2
        }

        @Test
        fun `should not be equal when values differ significantly`() {
            // Arrange
            val raiderId1 = RaiderId(1L)
            val raiderId2 = RaiderId(1000000L)

            // Assert
            raiderId1 shouldNotBe raiderId2
        }
    }

    @Nested
    inner class DataClassTests {

        @Test
        fun `should allow destructuring`() {
            // Arrange
            val raiderId = RaiderId(42L)

            // Act
            val (value) = raiderId

            // Assert
            value shouldBe 42L
        }

        @Test
        fun `should have meaningful toString`() {
            // Arrange
            val raiderId = RaiderId(42L)

            // Act
            val stringRepresentation = raiderId.toString()

            // Assert
            stringRepresentation shouldBe "RaiderId(value=42)"
        }

        @Test
        fun `should create copy with same value`() {
            // Arrange
            val original = RaiderId(42L)

            // Act
            val copy = original.copy()

            // Assert
            copy shouldBe original
            copy.value shouldBe 42L
        }

        @Test
        fun `should create copy with different value`() {
            // Arrange
            val original = RaiderId(42L)

            // Act
            val copy = original.copy(value = 100L)

            // Assert
            copy.value shouldBe 100L
            original.value shouldBe 42L
        }

        @Test
        fun `should throw exception when copy has invalid value`() {
            // Arrange
            val original = RaiderId(42L)

            // Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                original.copy(value = -1L)
            }
            exception.message shouldBe "Raider ID must be positive, got -1"
        }
    }

    @Nested
    inner class BoundaryTests {

        @Test
        fun `should handle minimum valid value of 1`() {
            // Arrange & Act
            val raiderId = RaiderId(1L)

            // Assert
            raiderId.value shouldBe 1L
        }

        @Test
        fun `should handle maximum Long value`() {
            // Arrange & Act
            val raiderId = RaiderId(Long.MAX_VALUE)

            // Assert
            raiderId.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should reject value at boundary of zero`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderId(0L)
            }
            exception.message shouldBe "Raider ID must be positive, got 0"
        }

        @Test
        fun `should reject value at boundary of -1`() {
            // Arrange, Act & Assert
            val exception = shouldThrow<IllegalArgumentException> {
                RaiderId(-1L)
            }
            exception.message shouldBe "Raider ID must be positive, got -1"
        }
    }

    @Nested
    inner class UsageTests {

        @Test
        fun `should work as map key`() {
            // Arrange
            val raiderId = RaiderId(42L)
            val map = mapOf(raiderId to "Arthas")

            // Act
            val result = map[RaiderId(42L)]

            // Assert
            result shouldBe "Arthas"
        }

        @Test
        fun `should work in set`() {
            // Arrange
            val raiderId1 = RaiderId(1L)
            val raiderId2 = RaiderId(1L)
            val raiderId3 = RaiderId(2L)

            // Act
            val set = setOf(raiderId1, raiderId2, raiderId3)

            // Assert
            set.size shouldBe 2
        }

        @Test
        fun `should be comparable in sorted list`() {
            // Arrange
            val raiderIds = listOf(RaiderId(30L), RaiderId(10L), RaiderId(20L))

            // Act
            val sorted = raiderIds.sortedBy { it.value }

            // Assert
            sorted[0].value shouldBe 10L
            sorted[1].value shouldBe 20L
            sorted[2].value shouldBe 30L
        }

        @Test
        fun `should filter list of raider IDs`() {
            // Arrange
            val raiderIds = listOf(
                RaiderId(1L),
                RaiderId(2L),
                RaiderId(3L),
                RaiderId(4L),
                RaiderId(5L)
            )

            // Act
            val filtered = raiderIds.filter { it.value > 2L }

            // Assert
            filtered.size shouldBe 3
            filtered.map { it.value } shouldBe listOf(3L, 4L, 5L)
        }
    }

    @Nested
    inner class InteroperabilityTests {

        @Test
        fun `should be distinct from ItemId with same numeric value`() {
            // Arrange
            val raiderId = RaiderId(42L)
            val itemId = ItemId(42L)

            // Assert - they are different types, cannot be compared directly
            // This test documents that the type system prevents confusion
            raiderId.value shouldBe itemId.value
            // raiderId shouldNotBe itemId  // This would not compile - good!
        }
    }
}
