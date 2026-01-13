package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for TierBonus value object.
 *
 * TB is a multiplier based on tier set completion status.
 */
class TierBonusTest : UnitTest() {
    @Test
    fun `should create valid TB when value is between 0 and 2`() {
        // Arrange & Act
        val tb = TierBonus.of(1.1)

        // Assert
        tb.value shouldBe 1.1
    }

    @Test
    fun `should create TB at lower boundary`() {
        // Arrange & Act
        val tb = TierBonus.of(0.0)

        // Assert
        tb.value shouldBe 0.0
    }

    @Test
    fun `should create TB at upper boundary`() {
        // Arrange & Act
        val tb = TierBonus.of(2.0)

        // Assert
        tb.value shouldBe 2.0
    }

    @Test
    fun `should create no bonus TB using factory method`() {
        // Arrange & Act
        val tb = TierBonus.none()

        // Assert
        tb.value shouldBe 1.0
    }

    @Test
    fun `should create max TB using factory method`() {
        // Arrange & Act
        val tb = TierBonus.max()

        // Assert
        tb.value shouldBe 1.2
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                TierBonus.of(-0.1)
            }
        exception.message shouldBe "Tier Bonus must be between 0.0 and 2.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 2`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                TierBonus.of(2.5)
            }
        exception.message shouldBe "Tier Bonus must be between 0.0 and 2.0, got 2.5"
    }

    @Test
    fun `should have value equality for same value`() {
        // Arrange
        val tb1 = TierBonus.of(1.15)
        val tb2 = TierBonus.of(1.15)

        // Assert
        tb1 shouldBe tb2
    }

    @Test
    fun `max bonus should be greater than no bonus`() {
        // Arrange & Act
        val maxBonus = TierBonus.max()
        val noBonus = TierBonus.none()

        // Assert
        (maxBonus.value > noBonus.value) shouldBe true
    }

    @Test
    fun `no bonus should be neutral multiplier of 1`() {
        // Arrange & Act
        val noBonus = TierBonus.none()

        // Assert - neutral multiplier doesn't change the calculation
        noBonus.value shouldBe 1.0
    }
}
