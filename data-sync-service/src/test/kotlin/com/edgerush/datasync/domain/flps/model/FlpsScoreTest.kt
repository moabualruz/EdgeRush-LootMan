package com.edgerush.datasync.domain.flps.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FlpsScoreTest {

    @Test
    fun `should create valid FLPS score within range`() {
        // Given
        val value = 0.75

        // When
        val score = FlpsScore.of(value)

        // Then
        score.value shouldBe 0.75
    }

    @Test
    fun `should create zero FLPS score`() {
        // Given/When
        val score = FlpsScore.zero()

        // Then
        score.value shouldBe 0.0
    }

    @Test
    fun `should create max FLPS score`() {
        // Given/When
        val score = FlpsScore.max()

        // Then
        score.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when score is negative`() {
        // Given
        val invalidValue = -0.1

        // When/Then
        shouldThrow<IllegalArgumentException> {
            FlpsScore.of(invalidValue)
        }
    }

    @Test
    fun `should throw exception when score exceeds 1_0`() {
        // Given
        val invalidValue = 1.1

        // When/Then
        shouldThrow<IllegalArgumentException> {
            FlpsScore.of(invalidValue)
        }
    }

    @Test
    fun `should add two FLPS scores correctly`() {
        // Given
        val score1 = FlpsScore.of(0.3)
        val score2 = FlpsScore.of(0.4)

        // When
        val result = score1 + score2

        // Then
        result.value shouldBe 0.7
    }

    @Test
    fun `should cap addition at 1_0`() {
        // Given
        val score1 = FlpsScore.of(0.7)
        val score2 = FlpsScore.of(0.5)

        // When
        val result = score1 + score2

        // Then
        result.value shouldBe 1.0
    }

    @Test
    fun `should multiply FLPS score by factor`() {
        // Given
        val score = FlpsScore.of(0.8)
        val multiplier = 0.5

        // When
        val result = score * multiplier

        // Then
        result.value shouldBe 0.4
    }

    @Test
    fun `should cap multiplication at 1_0`() {
        // Given
        val score = FlpsScore.of(0.8)
        val multiplier = 2.0

        // When
        val result = score * multiplier

        // Then
        result.value shouldBe 1.0
    }

    @Test
    fun `should floor multiplication at 0_0`() {
        // Given
        val score = FlpsScore.of(0.5)
        val multiplier = -1.0

        // When
        val result = score * multiplier

        // Then
        result.value shouldBe 0.0
    }

    @Test
    fun `should compare FLPS scores correctly`() {
        // Given
        val lower = FlpsScore.of(0.5)
        val higher = FlpsScore.of(0.8)

        // Then
        (lower < higher) shouldBe true
        (higher > lower) shouldBe true
        (lower == FlpsScore.of(0.5)) shouldBe true
    }

    @Test
    fun `should be immutable value object`() {
        // Given
        val score1 = FlpsScore.of(0.5)
        val score2 = FlpsScore.of(0.5)

        // Then
        (score1 == score2) shouldBe true
        (score1.hashCode() == score2.hashCode()) shouldBe true
    }
}
