package com.edgerush.lootman.domain.shared

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for AccountId value object.
 */
class AccountIdTest : UnitTest() {
    @Nested
    inner class CreationTests {
        @Test
        fun `should create valid account ID with positive value`() {
            // Arrange & Act
            val accountId = AccountId(42L)

            // Assert
            accountId.value shouldBe 42L
        }

        @Test
        fun `should create account ID with value of 1`() {
            // Arrange & Act
            val accountId = AccountId(1L)

            // Assert
            accountId.value shouldBe 1L
        }

        @Test
        fun `should create account ID with large positive value`() {
            // Arrange & Act
            val accountId = AccountId(Long.MAX_VALUE)

            // Assert
            accountId.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should create account ID with typical database ID`() {
            // Arrange - typical auto-increment database ID
            val dbId = 98765L

            // Act
            val accountId = AccountId(dbId)

            // Assert
            accountId.value shouldBe 98765L
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `should throw exception when value is zero`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    AccountId(0L)
                }
            exception.message shouldBe "Account ID must be positive, got 0"
        }

        @Test
        fun `should throw exception when value is negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    AccountId(-1L)
                }
            exception.message shouldBe "Account ID must be positive, got -1"
        }

        @Test
        fun `should throw exception when value is large negative`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    AccountId(-999999L)
                }
            exception.message shouldBe "Account ID must be positive, got -999999"
        }

        @Test
        fun `should throw exception when value is Long MIN_VALUE`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    AccountId(Long.MIN_VALUE)
                }
            exception.message shouldContain "Account ID must be positive, got"
            exception.message shouldContain Long.MIN_VALUE.toString()
        }

        @Test
        fun `should include actual value in error message`() {
            // Arrange
            val invalidValue = -42L

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    AccountId(invalidValue)
                }
            exception.message shouldContain "-42"
        }
    }

    @Nested
    inner class EqualityTests {
        @Test
        fun `should be equal when values are the same`() {
            // Arrange
            val accountId1 = AccountId(42L)
            val accountId2 = AccountId(42L)

            // Assert
            accountId1 shouldBe accountId2
        }

        @Test
        fun `should have same hash code when values are the same`() {
            // Arrange
            val accountId1 = AccountId(42L)
            val accountId2 = AccountId(42L)

            // Assert
            accountId1.hashCode() shouldBe accountId2.hashCode()
        }

        @Test
        fun `should not be equal when values are different`() {
            // Arrange
            val accountId1 = AccountId(42L)
            val accountId2 = AccountId(43L)

            // Assert
            accountId1 shouldNotBe accountId2
        }

        @Test
        fun `should not be equal when values differ significantly`() {
            // Arrange
            val accountId1 = AccountId(1L)
            val accountId2 = AccountId(1000000L)

            // Assert
            accountId1 shouldNotBe accountId2
        }
    }

    @Nested
    inner class DataClassTests {
        @Test
        fun `should allow destructuring`() {
            // Arrange
            val accountId = AccountId(42L)

            // Act
            val (value) = accountId

            // Assert
            value shouldBe 42L
        }

        @Test
        fun `should have meaningful toString`() {
            // Arrange
            val accountId = AccountId(42L)

            // Act
            val stringRepresentation = accountId.toString()

            // Assert
            stringRepresentation shouldBe "AccountId(value=42)"
        }

        @Test
        fun `should create copy with same value`() {
            // Arrange
            val original = AccountId(42L)

            // Act
            val copy = original.copy()

            // Assert
            copy shouldBe original
            copy.value shouldBe 42L
        }

        @Test
        fun `should create copy with different value`() {
            // Arrange
            val original = AccountId(42L)

            // Act
            val copy = original.copy(value = 100L)

            // Assert
            copy.value shouldBe 100L
            original.value shouldBe 42L
        }

        @Test
        fun `should throw exception when copy has invalid value`() {
            // Arrange
            val original = AccountId(42L)

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    original.copy(value = -1L)
                }
            exception.message shouldBe "Account ID must be positive, got -1"
        }
    }

    @Nested
    inner class BoundaryTests {
        @Test
        fun `should handle minimum valid value of 1`() {
            // Arrange & Act
            val accountId = AccountId(1L)

            // Assert
            accountId.value shouldBe 1L
        }

        @Test
        fun `should handle maximum Long value`() {
            // Arrange & Act
            val accountId = AccountId(Long.MAX_VALUE)

            // Assert
            accountId.value shouldBe Long.MAX_VALUE
        }

        @Test
        fun `should reject value at boundary of zero`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    AccountId(0L)
                }
            exception.message shouldBe "Account ID must be positive, got 0"
        }

        @Test
        fun `should reject value at boundary of -1`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    AccountId(-1L)
                }
            exception.message shouldBe "Account ID must be positive, got -1"
        }
    }

    @Nested
    inner class UsageTests {
        @Test
        fun `should work as map key`() {
            // Arrange
            val accountId = AccountId(42L)
            val map = mapOf(accountId to "PlayerAccount")

            // Act
            val result = map[AccountId(42L)]

            // Assert
            result shouldBe "PlayerAccount"
        }

        @Test
        fun `should work in set`() {
            // Arrange
            val accountId1 = AccountId(1L)
            val accountId2 = AccountId(1L)
            val accountId3 = AccountId(2L)

            // Act
            val set = setOf(accountId1, accountId2, accountId3)

            // Assert
            set.size shouldBe 2
        }

        @Test
        fun `should be comparable in sorted list`() {
            // Arrange
            val accountIds = listOf(AccountId(30L), AccountId(10L), AccountId(20L))

            // Act
            val sorted = accountIds.sortedBy { it.value }

            // Assert
            sorted[0].value shouldBe 10L
            sorted[1].value shouldBe 20L
            sorted[2].value shouldBe 30L
        }

        @Test
        fun `should filter list of account IDs`() {
            // Arrange
            val accountIds =
                listOf(
                    AccountId(1L),
                    AccountId(2L),
                    AccountId(3L),
                    AccountId(4L),
                    AccountId(5L),
                )

            // Act
            val filtered = accountIds.filter { it.value > 2L }

            // Assert
            filtered.size shouldBe 3
            filtered.map { it.value } shouldBe listOf(3L, 4L, 5L)
        }
    }

    @Nested
    inner class InteroperabilityTests {
        @Test
        fun `should be distinct from CharacterId with same numeric value`() {
            // Arrange
            val accountId = AccountId(42L)
            val characterId = CharacterId(42L)

            // Assert - they are different types, cannot be compared directly
            // This test documents that the type system prevents confusion
            accountId.value shouldBe characterId.value
            // accountId shouldNotBe characterId  // This would not compile - good!
        }

        @Test
        fun `should be distinct from RaiderId with same numeric value`() {
            // Arrange
            val accountId = AccountId(42L)
            val raiderId = RaiderId(42L)

            // Assert - they are different types, cannot be compared directly
            accountId.value shouldBe raiderId.value
        }
    }

    @Nested
    inner class AccountAggregationTests {
        @Test
        fun `should allow grouping characters by account`() {
            // Arrange
            val accountId = AccountId(1L)
            val characters =
                listOf(
                    Pair(CharacterId(1L), accountId),
                    Pair(CharacterId(2L), accountId),
                    Pair(CharacterId(3L), AccountId(2L)),
                )

            // Act
            val grouped = characters.groupBy { it.second }

            // Assert
            grouped[accountId]?.size shouldBe 2
            grouped[AccountId(2L)]?.size shouldBe 1
        }
    }
}
