package com.edgerush.lootman.domain.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.RaiderMeritScore
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for RaiderMeritScore value object.
 */
class RaiderMeritScoreTest : UnitTest() {
    @Test
    fun `should create valid score when value is between 0 and 1`() {
        // Arrange & Act
        val score = RaiderMeritScore.of(0.85)

        // Assert
        score.value shouldBe 0.85
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                RaiderMeritScore.of(-0.1)
            }
        exception.message shouldBe "Raider Merit Score must be between 0.0 and 1.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 1`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                RaiderMeritScore.of(1.5)
            }
        exception.message shouldBe "Raider Merit Score must be between 0.0 and 1.0, got 1.5"
    }

    @Test
    fun `should provide zero factory method`() {
        // Arrange & Act
        val score = RaiderMeritScore.zero()

        // Assert
        score.value shouldBe 0.0
    }

    @Test
    fun `should provide max factory method`() {
        // Arrange & Act
        val score = RaiderMeritScore.max()

        // Assert
        score.value shouldBe 1.0
    }
}
