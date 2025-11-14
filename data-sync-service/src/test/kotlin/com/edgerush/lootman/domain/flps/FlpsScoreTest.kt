package com.edgerush.lootman.domain.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for FlpsScore value object.
 *
 * Tests the FLPS score validation and arithmetic operations.
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
    fun `should create score with value 0`() {
        // Arrange & Act
        val score = FlpsScore.of(0.0)

        // Assert
        score.value shouldBe 0.0
    }

    @Test
    fun `should create score with value 1`() {
        // Arrange & Act
        val score = FlpsScore.of(1.0)

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
    fun `should provide zero factory method`() {
        // Arrange & Act
        val score = FlpsScore.zero()

        // Assert
        score.value shouldBe 0.0
    }

    @Test
    fun `should provide max factory method`() {
        // Arrange & Act
        val score = FlpsScore.max()

        // Assert
        score.value shouldBe 1.0
    }

    @Test
    fun `should add two scores correctly`() {
        // Arrange
        val score1 = FlpsScore.of(0.5)
        val score2 = FlpsScore.of(0.3)

        // Act
        val result = score1 + score2

        // Assert
        result.value shouldBe 0.8
    }

    @Test
    fun `should clamp addition result to 1_0`() {
        // Arrange
        val score1 = FlpsScore.of(0.7)
        val score2 = FlpsScore.of(0.5)

        // Act
        val result = score1 + score2

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `should multiply score by factor correctly`() {
        // Arrange
        val score = FlpsScore.of(0.8)

        // Act
        val result = score * 0.5

        // Assert
        result.value shouldBe 0.4
    }

    @Test
    fun `should clamp multiplication result to 1_0`() {
        // Arrange
        val score = FlpsScore.of(0.8)

        // Act
        val result = score * 2.0

        // Assert
        result.value shouldBe 1.0
    }

    @Test
    fun `should clamp multiplication result to 0_0`() {
        // Arrange
        val score = FlpsScore.of(0.5)

        // Act
        val result = score * -1.0

        // Assert
        result.value shouldBe 0.0
    }

    @Test
    fun `should support equality comparison`() {
        // Arrange
        val score1 = FlpsScore.of(0.85)
        val score2 = FlpsScore.of(0.85)
        val score3 = FlpsScore.of(0.90)

        // Act & Assert
        (score1 == score2) shouldBe true
        (score1 == score3) shouldBe false
    }
}
