package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for ExternalPreparationScore value object.
 *
 * EPS measures a raider's preparation through vault slots, crest usage, and heroic clears.
 */
class ExternalPreparationScoreTest : UnitTest() {
    @Test
    fun `should create valid EPS when value is between 0 and 1`() {
        // Arrange & Act
        val eps = ExternalPreparationScore.of(0.75)

        // Assert
        eps.value shouldBe 0.75
    }

    @Test
    fun `should create EPS at lower boundary`() {
        // Arrange & Act
        val eps = ExternalPreparationScore.of(0.0)

        // Assert
        eps.value shouldBe 0.0
    }

    @Test
    fun `should create EPS at upper boundary`() {
        // Arrange & Act
        val eps = ExternalPreparationScore.of(1.0)

        // Assert
        eps.value shouldBe 1.0
    }

    @Test
    fun `should create zero EPS using factory method`() {
        // Arrange & Act
        val eps = ExternalPreparationScore.zero()

        // Assert
        eps.value shouldBe 0.0
    }

    @Test
    fun `should create max EPS using factory method`() {
        // Arrange & Act
        val eps = ExternalPreparationScore.max()

        // Assert
        eps.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                ExternalPreparationScore.of(-0.1)
            }
        exception.message shouldBe "External Preparation Score must be between 0.0 and 1.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 1`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                ExternalPreparationScore.of(1.5)
            }
        exception.message shouldBe "External Preparation Score must be between 0.0 and 1.0, got 1.5"
    }

    @Test
    fun `should have value equality for same value`() {
        // Arrange
        val eps1 = ExternalPreparationScore.of(0.5)
        val eps2 = ExternalPreparationScore.of(0.5)

        // Assert
        eps1 shouldBe eps2
    }
}
