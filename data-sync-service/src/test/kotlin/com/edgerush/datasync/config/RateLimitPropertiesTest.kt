package com.edgerush.datasync.config

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

/**
 * Unit tests for RateLimitProperties.
 *
 * Tests default values and configuration options.
 */
class RateLimitPropertiesTest : UnitTest() {
    @Test
    fun `should have correct default values`() {
        // Arrange & Act
        val properties = RateLimitProperties()

        // Assert
        properties.enabled shouldBe true
        properties.readRequestsPerSecond shouldBe 100.0
        properties.writeRequestsPerSecond shouldBe 20.0
    }

    @Test
    fun `should allow disabling rate limiting`() {
        // Arrange & Act
        val properties = RateLimitProperties(enabled = false)

        // Assert
        properties.enabled shouldBe false
    }

    @Test
    fun `should allow custom read rate limit`() {
        // Arrange & Act
        val properties = RateLimitProperties(readRequestsPerSecond = 50.0)

        // Assert
        properties.readRequestsPerSecond shouldBe 50.0
    }

    @Test
    fun `should allow custom write rate limit`() {
        // Arrange & Act
        val properties = RateLimitProperties(writeRequestsPerSecond = 10.0)

        // Assert
        properties.writeRequestsPerSecond shouldBe 10.0
    }

    @Test
    fun `should allow both custom rate limits`() {
        // Arrange & Act
        val properties =
            RateLimitProperties(
                readRequestsPerSecond = 200.0,
                writeRequestsPerSecond = 50.0,
            )

        // Assert
        properties.readRequestsPerSecond shouldBe 200.0
        properties.writeRequestsPerSecond shouldBe 50.0
    }

    @Test
    fun `should support copy with modifications`() {
        // Arrange
        val original = RateLimitProperties()

        // Act
        val copied = original.copy(enabled = false)

        // Assert
        copied.enabled shouldBe false
        copied.readRequestsPerSecond shouldBe 100.0
        copied.writeRequestsPerSecond shouldBe 20.0
    }

    @Test
    fun `should support equality comparison`() {
        // Arrange
        val properties1 =
            RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 100.0,
                writeRequestsPerSecond = 20.0,
            )
        val properties2 =
            RateLimitProperties(
                enabled = true,
                readRequestsPerSecond = 100.0,
                writeRequestsPerSecond = 20.0,
            )

        // Assert
        properties1 shouldBe properties2
    }

    @Test
    fun `should allow very low rate limits`() {
        // Arrange & Act
        val properties =
            RateLimitProperties(
                readRequestsPerSecond = 1.0,
                writeRequestsPerSecond = 0.5,
            )

        // Assert
        properties.readRequestsPerSecond shouldBe 1.0
        properties.writeRequestsPerSecond shouldBe 0.5
    }

    @Test
    fun `should allow very high rate limits`() {
        // Arrange & Act
        val properties =
            RateLimitProperties(
                readRequestsPerSecond = 10000.0,
                writeRequestsPerSecond = 1000.0,
            )

        // Assert
        properties.readRequestsPerSecond shouldBe 10000.0
        properties.writeRequestsPerSecond shouldBe 1000.0
    }

    @Test
    fun `write rate limit should be lower than read rate by default`() {
        // Arrange & Act
        val properties = RateLimitProperties()

        // Assert - write operations are more expensive, so the default write limit is lower
        (properties.writeRequestsPerSecond < properties.readRequestsPerSecond) shouldBe true
    }

    @Test
    fun `should have consistent hashCode for equal objects`() {
        // Arrange
        val properties1 = RateLimitProperties(true, 100.0, 20.0)
        val properties2 = RateLimitProperties(true, 100.0, 20.0)

        // Assert
        properties1.hashCode() shouldBe properties2.hashCode()
    }

    @Test
    fun `should produce readable toString`() {
        // Arrange
        val properties = RateLimitProperties()

        // Act
        val string = properties.toString()

        // Assert
        string shouldContain "enabled=true"
        string shouldContain "readRequestsPerSecond=100.0"
        string shouldContain "writeRequestsPerSecond=20.0"
    }

    @Test
    fun `should not be equal when different values`() {
        // Arrange
        val properties1 = RateLimitProperties(true, 100.0, 20.0)
        val properties2 = RateLimitProperties(false, 50.0, 10.0)

        // Assert
        (properties1 == properties2) shouldBe false
    }

    // Tests for setters (used by Spring Boot configuration binding)
    @Test
    fun `should support property setters`() {
        // Arrange
        val properties = RateLimitProperties()

        // Act
        properties.enabled = false
        properties.readRequestsPerSecond = 50.0
        properties.writeRequestsPerSecond = 10.0

        // Assert
        properties.enabled shouldBe false
        properties.readRequestsPerSecond shouldBe 50.0
        properties.writeRequestsPerSecond shouldBe 10.0
    }

    @Test
    fun `should allow setting enabled to true after false`() {
        // Arrange
        val properties = RateLimitProperties(enabled = false)

        // Act
        properties.enabled = true

        // Assert
        properties.enabled shouldBe true
    }

    @Test
    fun `should allow setting readRequestsPerSecond via setter`() {
        // Arrange
        val properties = RateLimitProperties()

        // Act
        properties.readRequestsPerSecond = 200.0

        // Assert
        properties.readRequestsPerSecond shouldBe 200.0
    }

    @Test
    fun `should allow setting writeRequestsPerSecond via setter`() {
        // Arrange
        val properties = RateLimitProperties()

        // Act
        properties.writeRequestsPerSecond = 5.0

        // Assert
        properties.writeRequestsPerSecond shouldBe 5.0
    }
}
