package com.edgerush.datasync.domain.flps.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class RecencyDecayFactorTest {

    @Test
    fun `should create valid recency decay factor within range`() {
        // Given
        val value = 0.85

        // When
        val factor = RecencyDecayFactor.of(value)

        // Then
        factor.value shouldBe 0.85
    }

    @Test
    fun `should create no penalty recency decay factor`() {
        // Given/When
        val factor = RecencyDecayFactor.noPenalty()

        // Then
        factor.value shouldBe 1.0
    }

    @Test
    fun `should create maximum penalty recency decay factor`() {
        // Given/When
        val factor = RecencyDecayFactor.maxPenalty()

        // Then
        factor.value shouldBe 0.0
    }

    @Test
    fun `should throw exception when factor is negative`() {
        // Given
        val invalidValue = -0.1

        // When/Then
        shouldThrow<IllegalArgumentException> {
            RecencyDecayFactor.of(invalidValue)
        }
    }

    @Test
    fun `should throw exception when factor exceeds 1_0`() {
        // Given
        val invalidValue = 1.1

        // When/Then
        shouldThrow<IllegalArgumentException> {
            RecencyDecayFactor.of(invalidValue)
        }
    }

    @Test
    fun `should calculate from weeks since last award with no loot history`() {
        // Given
        val weeksSinceLastAward = null
        val basePenalty = 0.8

        // When
        val factor = RecencyDecayFactor.fromWeeksSince(weeksSinceLastAward, basePenalty)

        // Then
        factor.value shouldBe 1.0 // No penalty if no loot history
    }

    @Test
    fun `should calculate from weeks since last award with recent loot`() {
        // Given
        val weeksSinceLastAward = 0
        val basePenalty = 0.8

        // When
        val factor = RecencyDecayFactor.fromWeeksSince(weeksSinceLastAward, basePenalty)

        // Then
        factor.value shouldBe 0.8 // Base penalty applied
    }

    @Test
    fun `should calculate from weeks since last award with decay over time`() {
        // Given
        val weeksSinceLastAward = 2
        val basePenalty = 0.8
        val recoveryRate = 0.1

        // When
        val factor = RecencyDecayFactor.fromWeeksSince(weeksSinceLastAward, basePenalty, recoveryRate)

        // Then
        // Expected: 0.8 + (0.1 * 2) = 1.0 (capped)
        factor.value shouldBe 1.0
    }

    @Test
    fun `should calculate from weeks since last award with partial recovery`() {
        // Given
        val weeksSinceLastAward = 1
        val basePenalty = 0.7
        val recoveryRate = 0.1

        // When
        val factor = RecencyDecayFactor.fromWeeksSince(weeksSinceLastAward, basePenalty, recoveryRate)

        // Then
        // Expected: 0.7 + (0.1 * 1) = 0.8
        factor.value shouldBe (0.8 plusOrMinus 0.001)
    }

    @Test
    fun `should cap recovery at 1_0`() {
        // Given
        val weeksSinceLastAward = 10
        val basePenalty = 0.5
        val recoveryRate = 0.1

        // When
        val factor = RecencyDecayFactor.fromWeeksSince(weeksSinceLastAward, basePenalty, recoveryRate)

        // Then
        factor.value shouldBe 1.0
    }

    @Test
    fun `should be immutable value object`() {
        // Given
        val factor1 = RecencyDecayFactor.of(0.9)
        val factor2 = RecencyDecayFactor.of(0.9)

        // Then
        (factor1 == factor2) shouldBe true
        (factor1.hashCode() == factor2.hashCode()) shouldBe true
    }

    @Test
    fun `should compare recency decay factors correctly`() {
        // Given
        val lower = RecencyDecayFactor.of(0.7)
        val higher = RecencyDecayFactor.of(0.95)

        // Then
        (lower < higher) shouldBe true
        (higher > lower) shouldBe true
    }
}
