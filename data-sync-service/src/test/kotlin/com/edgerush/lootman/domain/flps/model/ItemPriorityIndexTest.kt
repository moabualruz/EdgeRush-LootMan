package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for ItemPriorityIndex value object.
 *
 * IPI = (UV × upgrade_weight) + (TB × tier_weight) + (RM × role_weight)
 */
class ItemPriorityIndexTest : UnitTest() {
    @Test
    fun `should create valid IPI when value is between 0 and 1`() {
        // Arrange & Act
        val ipi = ItemPriorityIndex.of(0.75)

        // Assert
        ipi.value shouldBe 0.75
    }

    @Test
    fun `should create IPI from component scores with default weights`() {
        // Arrange
        val uv = UpgradeValue.of(0.8)
        val tb = TierBonus.of(1.1)
        val rm = RoleMultiplier.of(1.0)

        // Act
        val ipi = ItemPriorityIndex.fromComponents(uv, tb, rm)

        // Assert
        // Default weights: upgrade=0.45, tier=0.35, role=0.20
        // (0.8 * 0.45) + (1.1 * 0.35) + (1.0 * 0.20) = 0.36 + 0.385 + 0.20 = 0.945
        ipi.value shouldBe 0.945
    }

    @Test
    fun `should create IPI from component scores with custom weights`() {
        // Arrange
        val uv = UpgradeValue.of(0.8)
        val tb = TierBonus.of(1.1)
        val rm = RoleMultiplier.of(1.0)

        // Act
        val ipi =
            ItemPriorityIndex.fromComponents(
                uv,
                tb,
                rm,
                upgradeWeight = 0.5,
                tierWeight = 0.3,
                roleWeight = 0.2,
            )

        // Assert
        // (0.8 * 0.5) + (1.1 * 0.3) + (1.0 * 0.2) = 0.4 + 0.33 + 0.2 = 0.93
        ipi.value shouldBe 0.93
    }

    @Test
    fun `should coerce IPI to max when components exceed 1`() {
        // Arrange
        val uv = UpgradeValue.of(1.0)
        val tb = TierBonus.of(1.2)
        val rm = RoleMultiplier.of(1.0)

        // Act
        val ipi = ItemPriorityIndex.fromComponents(uv, tb, rm)

        // Assert
        // Result would be > 1.0, should be coerced to 1.0
        ipi.value shouldBe 1.0
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                ItemPriorityIndex.of(-0.1)
            }
        exception.message shouldBe "Item Priority Index must be between 0.0 and 1.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 1`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                ItemPriorityIndex.of(1.5)
            }
        exception.message shouldBe "Item Priority Index must be between 0.0 and 1.0, got 1.5"
    }
}
