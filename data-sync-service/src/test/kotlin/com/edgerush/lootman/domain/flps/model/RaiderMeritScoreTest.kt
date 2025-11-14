package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for RaiderMeritScore value object.
 *
 * RMS = (ACS × attendance_weight) + (MAS × mechanical_weight) + (EPS × preparation_weight)
 */
class RaiderMeritScoreTest : UnitTest() {
    @Test
    fun `should create valid RMS when value is between 0 and 1`() {
        // Arrange & Act
        val rms = RaiderMeritScore.of(0.85)

        // Assert
        rms.value shouldBe 0.85
    }

    @Test
    fun `should create RMS from component scores with default weights`() {
        // Arrange
        val acs = AttendanceCommitmentScore.of(0.9)
        val mas = MechanicalAdherenceScore.of(0.8)
        val eps = ExternalPreparationScore.of(0.7)

        // Act
        val rms = RaiderMeritScore.fromComponents(acs, mas, eps)

        // Assert
        // Default weights: attendance=0.4, mechanical=0.4, preparation=0.2
        // (0.9 * 0.4) + (0.8 * 0.4) + (0.7 * 0.2) = 0.36 + 0.32 + 0.14 = 0.82
        rms.value shouldBe 0.82
    }

    @Test
    fun `should create RMS from component scores with custom weights`() {
        // Arrange
        val acs = AttendanceCommitmentScore.of(0.9)
        val mas = MechanicalAdherenceScore.of(0.8)
        val eps = ExternalPreparationScore.of(0.7)

        // Act
        val rms =
            RaiderMeritScore.fromComponents(
                acs,
                mas,
                eps,
                attendanceWeight = 0.5,
                mechanicalWeight = 0.3,
                preparationWeight = 0.2,
            )

        // Assert
        // (0.9 * 0.5) + (0.8 * 0.3) + (0.7 * 0.2) = 0.45 + 0.24 + 0.14 = 0.83
        rms.value shouldBe 0.83
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
    fun `should create zero RMS`() {
        // Arrange & Act
        val rms = RaiderMeritScore.zero()

        // Assert
        rms.value shouldBe 0.0
    }

    @Test
    fun `should create max RMS`() {
        // Arrange & Act
        val rms = RaiderMeritScore.max()

        // Assert
        rms.value shouldBe 1.0
    }
}
