package com.edgerush.lootman.api.graphql.scalar

import graphql.language.StringValue
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime

/**
 * Unit tests for custom GraphQL scalars.
 *
 * Tests serialization and deserialization of custom date/time types.
 */
class CustomScalarsTest {
    @Nested
    inner class InstantScalarCoercingTests {
        private val coercing = InstantScalarCoercing()

        @Nested
        inner class Serialize {
            @Test
            fun `should serialize Instant to ISO string`() {
                // Arrange
                val instant = Instant.parse("2026-01-14T10:30:00Z")

                // Act
                val result = coercing.serialize(instant)

                // Assert
                result shouldBe "2026-01-14T10:30:00Z"
            }

            @Test
            fun `should throw exception for non-Instant types`() {
                // Act & Assert
                shouldThrow<CoercingSerializeException> {
                    coercing.serialize("not an instant")
                }
            }
        }

        @Nested
        inner class ParseValue {
            @Test
            fun `should parse ISO string to Instant`() {
                // Arrange
                val input = "2026-01-14T10:30:00Z"

                // Act
                val result = coercing.parseValue(input)

                // Assert
                result shouldBe Instant.parse("2026-01-14T10:30:00Z")
            }

            @Test
            fun `should throw exception for invalid string`() {
                // Act & Assert
                shouldThrow<CoercingParseValueException> {
                    coercing.parseValue("invalid-date")
                }
            }

            @Test
            fun `should throw exception for non-String types`() {
                // Act & Assert
                shouldThrow<CoercingParseValueException> {
                    coercing.parseValue(12345)
                }
            }
        }

        @Nested
        inner class ParseLiteral {
            @Test
            fun `should parse StringValue to Instant`() {
                // Arrange
                val input = StringValue("2026-01-14T10:30:00Z")

                // Act
                val result = coercing.parseLiteral(input)

                // Assert
                result shouldBe Instant.parse("2026-01-14T10:30:00Z")
            }

            @Test
            fun `should throw exception for non-StringValue types`() {
                // Act & Assert
                shouldThrow<CoercingParseLiteralException> {
                    coercing.parseLiteral(graphql.language.IntValue.newIntValue().value(java.math.BigInteger.ONE).build())
                }
            }
        }
    }

    @Nested
    inner class LocalDateTimeScalarCoercingTests {
        private val coercing = LocalDateTimeScalarCoercing()

        @Nested
        inner class Serialize {
            @Test
            fun `should serialize LocalDateTime to ISO string`() {
                // Arrange - use non-zero seconds to ensure consistent ISO format
                val dateTime = LocalDateTime.of(2026, 1, 14, 10, 30, 45)

                // Act
                val result = coercing.serialize(dateTime)

                // Assert
                result shouldBe "2026-01-14T10:30:45"
            }

            @Test
            fun `should throw exception for non-LocalDateTime types`() {
                // Act & Assert
                shouldThrow<CoercingSerializeException> {
                    coercing.serialize("not a datetime")
                }
            }
        }

        @Nested
        inner class ParseValue {
            @Test
            fun `should parse ISO string to LocalDateTime`() {
                // Arrange
                val input = "2026-01-14T10:30:00"

                // Act
                val result = coercing.parseValue(input)

                // Assert
                result shouldBe LocalDateTime.of(2026, 1, 14, 10, 30, 0)
            }

            @Test
            fun `should throw exception for invalid string`() {
                // Act & Assert
                shouldThrow<CoercingParseValueException> {
                    coercing.parseValue("invalid-date")
                }
            }
        }

        @Nested
        inner class ParseLiteral {
            @Test
            fun `should parse StringValue to LocalDateTime`() {
                // Arrange
                val input = StringValue("2026-01-14T10:30:00")

                // Act
                val result = coercing.parseLiteral(input)

                // Assert
                result shouldBe LocalDateTime.of(2026, 1, 14, 10, 30, 0)
            }
        }
    }
}
