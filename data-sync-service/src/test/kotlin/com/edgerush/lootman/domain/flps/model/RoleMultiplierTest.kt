package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for RoleMultiplier value object.
 *
 * RM adjusts priority based on role (DPS, Tank, Healer).
 */
class RoleMultiplierTest : UnitTest() {
    @Test
    fun `should create valid RM when value is between 0 and 2`() {
        // Arrange & Act
        val rm = RoleMultiplier.of(1.5)

        // Assert
        rm.value shouldBe 1.5
    }

    @Test
    fun `should create RM at lower boundary`() {
        // Arrange & Act
        val rm = RoleMultiplier.of(0.0)

        // Assert
        rm.value shouldBe 0.0
    }

    @Test
    fun `should create RM at upper boundary`() {
        // Arrange & Act
        val rm = RoleMultiplier.of(2.0)

        // Assert
        rm.value shouldBe 2.0
    }

    @Test
    fun `should create DPS multiplier using factory method`() {
        // Arrange & Act
        val rm = RoleMultiplier.dps()

        // Assert
        rm.value shouldBe 1.0
    }

    @Test
    fun `should create Tank multiplier using factory method`() {
        // Arrange & Act
        val rm = RoleMultiplier.tank()

        // Assert
        rm.value shouldBe 0.8
    }

    @Test
    fun `should create Healer multiplier using factory method`() {
        // Arrange & Act
        val rm = RoleMultiplier.healer()

        // Assert
        rm.value shouldBe 0.7
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                RoleMultiplier.of(-0.1)
            }
        exception.message shouldBe "Role Multiplier must be between 0.0 and 2.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 2`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                RoleMultiplier.of(2.5)
            }
        exception.message shouldBe "Role Multiplier must be between 0.0 and 2.0, got 2.5"
    }

    @Test
    fun `should have value equality for same value`() {
        // Arrange
        val rm1 = RoleMultiplier.of(0.9)
        val rm2 = RoleMultiplier.of(0.9)

        // Assert
        rm1 shouldBe rm2
    }

    @Test
    fun `DPS should have higher priority than Tank`() {
        // Arrange & Act
        val dps = RoleMultiplier.dps()
        val tank = RoleMultiplier.tank()

        // Assert
        (dps.value > tank.value) shouldBe true
    }

    @Test
    fun `Tank should have higher priority than Healer`() {
        // Arrange & Act
        val tank = RoleMultiplier.tank()
        val healer = RoleMultiplier.healer()

        // Assert
        (tank.value > healer.value) shouldBe true
    }
}
