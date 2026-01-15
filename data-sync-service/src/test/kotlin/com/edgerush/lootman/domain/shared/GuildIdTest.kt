package com.edgerush.lootman.domain.shared

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for GuildId value object.
 */
class GuildIdTest : UnitTest() {
    @Nested
    inner class CreationTests {
        @Test
        fun `should create valid guild ID with non-blank string`() {
            // Arrange & Act
            val guildId = GuildId("guild-123")

            // Assert
            guildId.value shouldBe "guild-123"
        }

        @Test
        fun `should create guild ID with single character`() {
            // Arrange & Act
            val guildId = GuildId("a")

            // Assert
            guildId.value shouldBe "a"
        }

        @Test
        fun `should create guild ID with numbers`() {
            // Arrange & Act
            val guildId = GuildId("12345")

            // Assert
            guildId.value shouldBe "12345"
        }

        @Test
        fun `should create guild ID with special characters`() {
            // Arrange & Act
            val guildId = GuildId("guild-name_123.test")

            // Assert
            guildId.value shouldBe "guild-name_123.test"
        }

        @Test
        fun `should create guild ID with UUID format`() {
            // Arrange & Act
            val guildId = GuildId("550e8400-e29b-41d4-a716-446655440000")

            // Assert
            guildId.value shouldBe "550e8400-e29b-41d4-a716-446655440000"
        }

        @Test
        fun `should create guild ID with leading whitespace but non-blank content`() {
            // Arrange & Act
            val guildId = GuildId("  guild-123")

            // Assert
            guildId.value shouldBe "  guild-123"
        }

        @Test
        fun `should create guild ID with trailing whitespace but non-blank content`() {
            // Arrange & Act
            val guildId = GuildId("guild-123  ")

            // Assert
            guildId.value shouldBe "guild-123  "
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `should throw exception when value is empty`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    GuildId("")
                }
            exception.message shouldBe "Guild ID cannot be blank"
        }

        @Test
        fun `should throw exception when value is only whitespace`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    GuildId("   ")
                }
            exception.message shouldBe "Guild ID cannot be blank"
        }

        @Test
        fun `should throw exception when value contains only tabs`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    GuildId("\t\t\t")
                }
            exception.message shouldBe "Guild ID cannot be blank"
        }

        @Test
        fun `should throw exception when value contains only newlines`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    GuildId("\n\n")
                }
            exception.message shouldBe "Guild ID cannot be blank"
        }

        @Test
        fun `should throw exception when value contains mixed whitespace`() {
            // Arrange, Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    GuildId(" \t\n ")
                }
            exception.message shouldBe "Guild ID cannot be blank"
        }
    }

    @Nested
    inner class EqualityTests {
        @Test
        fun `should be equal when values are the same`() {
            // Arrange
            val guildId1 = GuildId("guild-123")
            val guildId2 = GuildId("guild-123")

            // Assert
            guildId1 shouldBe guildId2
        }

        @Test
        fun `should have same hash code when values are the same`() {
            // Arrange
            val guildId1 = GuildId("guild-123")
            val guildId2 = GuildId("guild-123")

            // Assert
            guildId1.hashCode() shouldBe guildId2.hashCode()
        }

        @Test
        fun `should not be equal when values are different`() {
            // Arrange
            val guildId1 = GuildId("guild-123")
            val guildId2 = GuildId("guild-456")

            // Assert
            guildId1 shouldNotBe guildId2
        }

        @Test
        fun `should be case sensitive in equality check`() {
            // Arrange
            val guildId1 = GuildId("Guild-123")
            val guildId2 = GuildId("guild-123")

            // Assert
            guildId1 shouldNotBe guildId2
        }
    }

    @Nested
    inner class DataClassTests {
        @Test
        fun `should allow destructuring`() {
            // Arrange
            val guildId = GuildId("guild-123")

            // Act
            val (value) = guildId

            // Assert
            value shouldBe "guild-123"
        }

        @Test
        fun `should have meaningful toString`() {
            // Arrange
            val guildId = GuildId("guild-123")

            // Act
            val stringRepresentation = guildId.toString()

            // Assert
            stringRepresentation shouldBe "GuildId(value=guild-123)"
        }

        @Test
        fun `should create copy with same value`() {
            // Arrange
            val original = GuildId("guild-123")

            // Act
            val copy = original.copy()

            // Assert
            copy shouldBe original
            copy.value shouldBe "guild-123"
        }

        @Test
        fun `should create copy with different value`() {
            // Arrange
            val original = GuildId("guild-123")

            // Act
            val copy = original.copy(value = "guild-456")

            // Assert
            copy.value shouldBe "guild-456"
            original.value shouldBe "guild-123"
        }
    }

    @Nested
    inner class EdgeCaseTests {
        @Test
        fun `should handle very long guild ID`() {
            // Arrange
            val longValue = "a".repeat(1000)

            // Act
            val guildId = GuildId(longValue)

            // Assert
            guildId.value.length shouldBe 1000
        }

        @Test
        fun `should handle unicode characters`() {
            // Arrange & Act
            val guildId = GuildId("guild-\u00e9\u00e0\u00fc")

            // Assert
            guildId.value shouldBe "guild-\u00e9\u00e0\u00fc"
        }

        @Test
        fun `should handle emoji in guild ID`() {
            // Arrange & Act
            val guildId = GuildId("guild-\uD83D\uDE80-test")

            // Assert
            guildId.value shouldBe "guild-\uD83D\uDE80-test"
        }
    }
}
