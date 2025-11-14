package com.edgerush.datasync.domain.flps.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class RaiderMeritScoreTest {

    @Test
    fun `should create valid raider merit score within range`() {
        // Given
        val value = 0.85

        // When
        val score = RaiderMeritScore.of(value)

        // Then
        score.value shouldBe 0.85
    }

    @Test
    fun `should create zero raider merit score`() {
        // Given/When
        val score = RaiderMeritScore.zero()

        // Then
        score.value shouldBe 0.0
    }

    @Test
    fun `should create perfect raider merit score`() {
        // Given/When
        val score = RaiderMeritScore.perfect()

        // Then
        score.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when score is negative`() {
        // Given
        val invalidValue = -0.1

        // When/Then
        shouldThrow<IllegalArgumentException> {
            RaiderMeritScore.of(invalidValue)
        }
    }

    @Test
    fun `should throw exception when score exceeds 1_0`() {
        // Given
        val invalidValue = 1.5

        // When/Then
        shouldThrow<IllegalArgumentException> {
            RaiderMeritScore.of(invalidValue)
        }
    }

    @Test
    fun `should calculate from component scores with weights`() {
        // Given
        val attendance = 0.9
        val mechanical = 0.8
        val preparation = 0.7
        val weights = RmsWeights(attendance = 0.4, mechanical = 0.4, preparation = 0.2)

        // When
        val score = RaiderMeritScore.fromComponents(attendance, mechanical, preparation, weights)

        // Then
        // Expected: (0.9 * 0.4) + (0.8 * 0.4) + (0.7 * 0.2) = 0.36 + 0.32 + 0.14 = 0.82
        score.value shouldBe (0.82 plusOrMinus 0.001)
    }

    @Test
    fun `should be immutable value object`() {
        // Given
        val score1 = RaiderMeritScore.of(0.75)
        val score2 = RaiderMeritScore.of(0.75)

        // Then
        (score1 == score2) shouldBe true
        (score1.hashCode() == score2.hashCode()) shouldBe true
    }

    @Test
    fun `should compare raider merit scores correctly`() {
        // Given
        val lower = RaiderMeritScore.of(0.6)
        val higher = RaiderMeritScore.of(0.9)

        // Then
        (lower < higher) shouldBe true
        (higher > lower) shouldBe true
    }
}
