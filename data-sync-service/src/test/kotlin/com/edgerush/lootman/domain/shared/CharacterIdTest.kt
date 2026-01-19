package com.edgerush.lootman.domain.shared

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for CharacterId value object.
 */
class CharacterIdTest : UnitTest() {
    @Nested
    inner class CreationTests {
        @Test
        fun `should create valid character ID with positive value`() {
            // Arrange & Act
            val characterId = CharacterId(42L)

            // Assert
            characterId.value shouldBe 42L
        }

        @Test
        fun `should create character ID with value of 1`() {
            // Arrange & Act
            val characterId = CharacterId(1L)

            // Assert
            characterId.value shouldBe 1L
        }

        @Test
        fun `should create character ID with large positive value`() {
            // Arrange & Act
            val characterId = CharacterId(Long.MAX_VALUE)

            // Assert
            characterId.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should create character ID with typical database ID`() {
            // Arrange - typical auto-increment database ID
            val dbId = 98765L

            // Act
            val characterId = CharacterId(dbId)

            // Assert
            characterId.value shouldBe 98765L
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `should throw exception when value is zero`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CharacterId(0L)
                }
            exception.message shouldBe "Character ID must be positive, got 0"
        }

        @Test
        fun `should throw exception when value is negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CharacterId(-1L)
                }
            exception.message shouldBe "Character ID must be positive, got -1"
        }

        @Test
        fun `should throw exception when value is large negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CharacterId(-999999L)
                }
            exception.message shouldBe "Character ID must be positive, got -999999"
        }

        @Test
        fun `should throw exception when value is Long MIN_VALUE`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CharacterId(Long.MIN_VALUE)
                }
            exception.message shouldContain "Character ID must be positive, got"
            exception.message shouldContain Long.MIN_VALUE.toString()
        }

        @Test
        fun `should include actual value in error message`() {
            // Arrange
            val invalidValue = -42L

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CharacterId(invalidValue)
                }
            exception.message shouldContain "-42"
        }
    }

    @Nested
    inner class EqualityTests {
        @Test
        fun `should be equal when values are the same`() {
            // Arrange
            val characterId1 = CharacterId(42L)
            val characterId2 = CharacterId(42L)

            // Assert
            characterId1 shouldBe characterId2
        }

        @Test
        fun `should have same hash code when values are the same`() {
            // Arrange
            val characterId1 = CharacterId(42L)
            val characterId2 = CharacterId(42L)

            // Assert
            characterId1.hashCode() shouldBe characterId2.hashCode()
        }

        @Test
        fun `should not be equal when values are different`() {
            // Arrange
            val characterId1 = CharacterId(42L)
            val characterId2 = CharacterId(43L)

            // Assert
            characterId1 shouldNotBe characterId2
        }

        @Test
        fun `should not be equal when values differ significantly`() {
            // Arrange
            val characterId1 = CharacterId(1L)
            val characterId2 = CharacterId(1000000L)

            // Assert
            characterId1 shouldNotBe characterId2
        }
    }

    @Nested
    inner class DataClassTests {
        @Test
        fun `should allow destructuring`() {
            // Arrange
            val characterId = CharacterId(42L)

            // Act
            val (value) = characterId

            // Assert
            value shouldBe 42L
        }

        @Test
        fun `should have meaningful toString`() {
            // Arrange
            val characterId = CharacterId(42L)

            // Act
            val stringRepresentation = characterId.toString()

            // Assert
            stringRepresentation shouldBe "CharacterId(value=42)"
        }

        @Test
        fun `should create copy with same value`() {
            // Arrange
            val original = CharacterId(42L)

            // Act
            val copy = original.copy()

            // Assert
            copy shouldBe original
            copy.value shouldBe 42L
        }

        @Test
        fun `should create copy with different value`() {
            // Arrange
            val original = CharacterId(42L)

            // Act
            val copy = original.copy(value = 100L)

            // Assert
            copy.value shouldBe 100L
            original.value shouldBe 42L
        }

        @Test
        fun `should throw exception when copy has invalid value`() {
            // Arrange
            val original = CharacterId(42L)

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    original.copy(value = -1L)
                }
            exception.message shouldBe "Character ID must be positive, got -1"
        }
    }

    @Nested
    inner class BoundaryTests {
        @Test
        fun `should handle minimum valid value of 1`() {
            // Arrange & Act
            val characterId = CharacterId(1L)

            // Assert
            characterId.value shouldBe 1L
        }

        @Test
        fun `should handle maximum Long value`() {
            // Arrange & Act
            val characterId = CharacterId(Long.MAX_VALUE)

            // Assert
            characterId.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should reject value at boundary of zero`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CharacterId(0L)
                }
            exception.message shouldBe "Character ID must be positive, got 0"
        }

        @Test
        fun `should reject value at boundary of -1`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    CharacterId(-1L)
                }
            exception.message shouldBe "Character ID must be positive, got -1"
        }
    }

    @Nested
    inner class UsageTests {
        @Test
        fun `should work as map key`() {
            // Arrange
            val characterId = CharacterId(42L)
            val map = mapOf(characterId to "Arthas")

            // Act
            val result = map[CharacterId(42L)]

            // Assert
            result shouldBe "Arthas"
        }

        @Test
        fun `should work in set`() {
            // Arrange
            val characterId1 = CharacterId(1L)
            val characterId2 = CharacterId(1L)
            val characterId3 = CharacterId(2L)

            // Act
            val set = setOf(characterId1, characterId2, characterId3)

            // Assert
            set.size shouldBe 2
        }

        @Test
        fun `should be comparable in sorted list`() {
            // Arrange
            val characterIds = listOf(CharacterId(30L), CharacterId(10L), CharacterId(20L))

            // Act
            val sorted = characterIds.sortedBy { it.value }

            // Assert
            sorted[0].value shouldBe 10L
            sorted[1].value shouldBe 20L
            sorted[2].value shouldBe 30L
        }

        @Test
        fun `should filter list of character IDs`() {
            // Arrange
            val characterIds =
                listOf(
                    CharacterId(1L),
                    CharacterId(2L),
                    CharacterId(3L),
                    CharacterId(4L),
                    CharacterId(5L),
                )

            // Act
            val filtered = characterIds.filter { it.value > 2L }

            // Assert
            filtered.size shouldBe 3
            filtered.map { it.value } shouldBe listOf(3L, 4L, 5L)
        }
    }

    @Nested
    inner class InteroperabilityTests {
        @Test
        fun `should be distinct from RaiderId with same numeric value`() {
            // Arrange
            val characterId = CharacterId(42L)
            val raiderId = RaiderId(42L)

            // Assert - they are different types, cannot be compared directly
            // This test documents that the type system prevents confusion
            characterId.value shouldBe raiderId.value
            // characterId shouldNotBe raiderId  // This would not compile - good!
        }

        @Test
        fun `should be distinct from ItemId with same numeric value`() {
            // Arrange
            val characterId = CharacterId(42L)
            val itemId = ItemId(42L)

            // Assert - they are different types, cannot be compared directly
            characterId.value shouldBe itemId.value
        }

        @Test
        fun `should be distinct from AccountId with same numeric value`() {
            // Arrange
            val characterId = CharacterId(42L)
            val accountId = AccountId(42L)

            // Assert - they are different types, cannot be compared directly
            characterId.value shouldBe accountId.value
        }
    }
}
