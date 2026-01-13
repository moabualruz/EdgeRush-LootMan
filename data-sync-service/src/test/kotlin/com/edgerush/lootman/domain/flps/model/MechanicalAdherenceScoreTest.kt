package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for MechanicalAdherenceScore value object.
 *
 * MAS measures a raider's mechanical skill through death rates and avoidable damage.
 */
class MechanicalAdherenceScoreTest : UnitTest() {
    @Test
    fun `should create valid MAS when value is between 0 and 1`() {
        // Arrange & Act
        val mas = MechanicalAdherenceScore.of(0.85)

        // Assert
        mas.value shouldBe 0.85
    }

    @Test
    fun `should create MAS at lower boundary`() {
        // Arrange & Act
        val mas = MechanicalAdherenceScore.of(0.0)

        // Assert
        mas.value shouldBe 0.0
    }

    @Test
    fun `should create MAS at upper boundary`() {
        // Arrange & Act
        val mas = MechanicalAdherenceScore.of(1.0)

        // Assert
        mas.value shouldBe 1.0
    }

    @Test
    fun `should create zero MAS using factory method`() {
        // Arrange & Act
        val mas = MechanicalAdherenceScore.zero()

        // Assert
        mas.value shouldBe 0.0
    }

    @Test
    fun `should create max MAS using factory method`() {
        // Arrange & Act
        val mas = MechanicalAdherenceScore.max()

        // Assert
        mas.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                MechanicalAdherenceScore.of(-0.1)
            }
        exception.message shouldBe "Mechanical Adherence Score must be between 0.0 and 1.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 1`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                MechanicalAdherenceScore.of(1.5)
            }
        exception.message shouldBe "Mechanical Adherence Score must be between 0.0 and 1.0, got 1.5"
    }

    @Test
    fun `should have value equality for same value`() {
        // Arrange
        val mas1 = MechanicalAdherenceScore.of(0.6)
        val mas2 = MechanicalAdherenceScore.of(0.6)

        // Assert
        mas1 shouldBe mas2
    }
}
