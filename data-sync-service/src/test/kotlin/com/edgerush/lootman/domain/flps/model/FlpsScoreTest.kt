package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for FlpsScore value object.
 *
 * Tests validation, immutability, and arithmetic operations.
 */
class FlpsScoreTest : UnitTest() {
    @Test
    fun `should create valid score when value is between 0 and 1`() {
        // Arrange & Act
        val score = FlpsScore.of(0.85)

        // Assert
        score.value shouldBe 0.85
    }

    @Test
    fun `should create score with zero value`() {
        // Arrange & Act
        val score = FlpsScore.zero()

        // Assert
        score.value shouldBe 0.0
    }

    @Test
    fun `should create score with max value`() {
        // Arrange & Act
        val score = FlpsScore.max()

        // Assert
        score.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                FlpsScore.of(-0.1)
            }
        exception.message shouldBe "FLPS score must be between 0.0 and 1.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 1`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                FlpsScore.of(1.5)
            }
        exception.message shouldBe "FLPS score must be between 0.0 and 1.0, got 1.5"
    }

    @Test
    fun `should support addition with coercion to valid range`() {
        // Arrange
        val score1 = FlpsScore.of(0.7)
        val score2 = FlpsScore.of(0.5)

        // Act
        val result = score1 + score2

        // Assert
        result.value shouldBe 1.0 // Coerced to max
    }

    @Test
    fun `should support addition within valid range`() {
        // Arrange
        val score1 = FlpsScore.of(0.3)
        val score2 = FlpsScore.of(0.4)

        // Act
        val result = score1 + score2

        // Assert
        result.value shouldBe 0.7
    }

    @Test
    fun `should support multiplication with coercion to valid range`() {
        // Arrange
        val score = FlpsScore.of(0.5)

        // Act
        val result = score * 2.5

        // Assert
        result.value shouldBe 1.0 // Coerced to max
    }

    @Test
    fun `should support multiplication within valid range`() {
        // Arrange
        val score = FlpsScore.of(0.5)

        // Act
        val result = score * 0.8

        // Assert
        result.value shouldBe 0.4
    }

    @Test
    fun `should support multiplication with negative multiplier coerced to zero`() {
        // Arrange
        val score = FlpsScore.of(0.5)

        // Act
        val result = score * -1.0

        // Assert
        result.value shouldBe 0.0 // Coerced to min
    }

    @Test
    fun `should be equal when values are equal`() {
        // Arrange
        val score1 = FlpsScore.of(0.85)
        val score2 = FlpsScore.of(0.85)

        // Act & Assert
        score1 shouldBe score2
    }

    @Test
    fun `should not be equal when values differ`() {
        // Arrange
        val score1 = FlpsScore.of(0.85)
        val score2 = FlpsScore.of(0.86)

        // Act & Assert
        score1 shouldNotBe score2
    }

    @Test
    fun `should have same hash code when values are equal`() {
        // Arrange
        val score1 = FlpsScore.of(0.85)
        val score2 = FlpsScore.of(0.85)

        // Act & Assert
        score1.hashCode() shouldBe score2.hashCode()
    }

    @Test
    fun `should be immutable`() {
        // Arrange
        val original = FlpsScore.of(0.5)

        // Act
        val modified = original * 2.0

        // Assert
        original.value shouldBe 0.5 // Original unchanged
        modified.value shouldBe 1.0 // New instance created
    }
}
