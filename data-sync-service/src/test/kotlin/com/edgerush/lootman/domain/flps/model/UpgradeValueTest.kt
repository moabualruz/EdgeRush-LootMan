package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for UpgradeValue value object.
 *
 * UV measures the relative power gain from an item upgrade.
 */
class UpgradeValueTest : UnitTest() {
    @Test
    fun `should create valid UV when value is between 0 and 1`() {
        // Arrange & Act
        val uv = UpgradeValue.of(0.65)

        // Assert
        uv.value shouldBe 0.65
    }

    @Test
    fun `should create UV at lower boundary`() {
        // Arrange & Act
        val uv = UpgradeValue.of(0.0)

        // Assert
        uv.value shouldBe 0.0
    }

    @Test
    fun `should create UV at upper boundary`() {
        // Arrange & Act
        val uv = UpgradeValue.of(1.0)

        // Assert
        uv.value shouldBe 1.0
    }

    @Test
    fun `should create zero UV using factory method`() {
        // Arrange & Act
        val uv = UpgradeValue.zero()

        // Assert
        uv.value shouldBe 0.0
    }

    @Test
    fun `should create max UV using factory method`() {
        // Arrange & Act
        val uv = UpgradeValue.max()

        // Assert
        uv.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                UpgradeValue.of(-0.1)
            }
        exception.message shouldBe "Upgrade Value must be between 0.0 and 1.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 1`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                UpgradeValue.of(1.5)
            }
        exception.message shouldBe "Upgrade Value must be between 0.0 and 1.0, got 1.5"
    }

    @Test
    fun `should have value equality for same value`() {
        // Arrange
        val uv1 = UpgradeValue.of(0.8)
        val uv2 = UpgradeValue.of(0.8)

        // Assert
        uv1 shouldBe uv2
    }
}
