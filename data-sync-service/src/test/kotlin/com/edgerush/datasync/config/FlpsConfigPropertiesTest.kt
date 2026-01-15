package com.edgerush.datasync.config

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

/**
 * Unit tests for FlpsConfigProperties.
 *
 * Tests default values and property structure.
 */
class FlpsConfigPropertiesTest : UnitTest() {
    @Test
    fun `should have correct default RMS weights`() {
        // Arrange & Act
        val properties = FlpsConfigProperties()

        // Assert
        properties.rms.attendance shouldBe 0.45
        properties.rms.mechanical shouldBe 0.35
        properties.rms.preparation shouldBe 0.20
    }

    @Test
    fun `RMS weights should sum to 1_0`() {
        // Arrange & Act
        val properties = FlpsConfigProperties()

        // Assert
        val total = properties.rms.attendance + properties.rms.mechanical + properties.rms.preparation
        total shouldBe 1.0
    }

    @Test
    fun `should have correct default IPI weights`() {
        // Arrange & Act
        val properties = FlpsConfigProperties()

        // Assert
        properties.ipi.upgradeValue shouldBe 0.45
        properties.ipi.tierBonus shouldBe 0.35
        properties.ipi.roleMultiplier shouldBe 0.20
    }

    @Test
    fun `IPI weights should sum to 1_0`() {
        // Arrange & Act
        val properties = FlpsConfigProperties()

        // Assert
        val total = properties.ipi.upgradeValue + properties.ipi.tierBonus + properties.ipi.roleMultiplier
        total shouldBe 1.0
    }

    @Test
    fun `should have correct default role multipliers`() {
        // Arrange & Act
        val properties = FlpsConfigProperties()

        // Assert
        properties.roleMultipliers.tank shouldBe 1.2
        properties.roleMultipliers.healer shouldBe 1.1
        properties.roleMultipliers.dps shouldBe 1.0
    }

    @Test
    fun `should have correct default thresholds`() {
        // Arrange & Act
        val properties = FlpsConfigProperties()

        // Assert
        properties.thresholds.eligibilityAttendance shouldBe 0.8
        properties.thresholds.eligibilityActivity shouldBe 0.0
        properties.thresholds.recencyDecayDays shouldBe 30
        properties.thresholds.maxAttendanceBonus shouldBe 1.0
        properties.thresholds.minMechanicalScore shouldBe 0.0
        properties.thresholds.maxPreparationScore shouldBe 1.0
    }

    @Test
    fun `should allow customizing RMS weights`() {
        // Arrange
        val properties = FlpsConfigProperties()

        // Act
        properties.rms =
            FlpsConfigProperties.RmsWeights(
                attendance = 0.50,
                mechanical = 0.30,
                preparation = 0.20,
            )

        // Assert
        properties.rms.attendance shouldBe 0.50
        properties.rms.mechanical shouldBe 0.30
        properties.rms.preparation shouldBe 0.20
    }

    @Test
    fun `should allow customizing IPI weights`() {
        // Arrange
        val properties = FlpsConfigProperties()

        // Act
        properties.ipi =
            FlpsConfigProperties.IpiWeights(
                upgradeValue = 0.50,
                tierBonus = 0.30,
                roleMultiplier = 0.20,
            )

        // Assert
        properties.ipi.upgradeValue shouldBe 0.50
        properties.ipi.tierBonus shouldBe 0.30
        properties.ipi.roleMultiplier shouldBe 0.20
    }

    @Test
    fun `should allow customizing role multipliers`() {
        // Arrange
        val properties = FlpsConfigProperties()

        // Act
        properties.roleMultipliers =
            FlpsConfigProperties.RoleMultipliers(
                tank = 1.5,
                healer = 1.3,
                dps = 1.0,
            )

        // Assert
        properties.roleMultipliers.tank shouldBe 1.5
        properties.roleMultipliers.healer shouldBe 1.3
        properties.roleMultipliers.dps shouldBe 1.0
    }

    @Test
    fun `should allow customizing thresholds`() {
        // Arrange
        val properties = FlpsConfigProperties()

        // Act
        properties.thresholds =
            FlpsConfigProperties.Thresholds(
                eligibilityAttendance = 0.9,
                eligibilityActivity = 0.5,
                recencyDecayDays = 14,
                maxAttendanceBonus = 0.8,
                minMechanicalScore = 0.2,
                maxPreparationScore = 0.9,
            )

        // Assert
        properties.thresholds.eligibilityAttendance shouldBe 0.9
        properties.thresholds.eligibilityActivity shouldBe 0.5
        properties.thresholds.recencyDecayDays shouldBe 14
        properties.thresholds.maxAttendanceBonus shouldBe 0.8
        properties.thresholds.minMechanicalScore shouldBe 0.2
        properties.thresholds.maxPreparationScore shouldBe 0.9
    }

    @Test
    fun `RmsWeights data class should support copy`() {
        // Arrange
        val original = FlpsConfigProperties.RmsWeights()

        // Act
        val copied = original.copy(attendance = 0.60)

        // Assert
        copied.attendance shouldBe 0.60
        copied.mechanical shouldBe 0.35
        copied.preparation shouldBe 0.20
    }

    @Test
    fun `IpiWeights data class should support copy`() {
        // Arrange
        val original = FlpsConfigProperties.IpiWeights()

        // Act
        val copied = original.copy(upgradeValue = 0.60)

        // Assert
        copied.upgradeValue shouldBe 0.60
        copied.tierBonus shouldBe 0.35
        copied.roleMultiplier shouldBe 0.20
    }

    @Test
    fun `RoleMultipliers data class should support copy`() {
        // Arrange
        val original = FlpsConfigProperties.RoleMultipliers()

        // Act
        val copied = original.copy(tank = 1.5)

        // Assert
        copied.tank shouldBe 1.5
        copied.healer shouldBe 1.1
        copied.dps shouldBe 1.0
    }

    @Test
    fun `Thresholds data class should support copy`() {
        // Arrange
        val original = FlpsConfigProperties.Thresholds()

        // Act
        val copied = original.copy(recencyDecayDays = 7)

        // Assert
        copied.recencyDecayDays shouldBe 7
        copied.eligibilityAttendance shouldBe 0.8
    }

    @Test
    fun `RmsWeights data class should support equals and hashCode`() {
        // Arrange
        val weights1 = FlpsConfigProperties.RmsWeights(0.45, 0.35, 0.20)
        val weights2 = FlpsConfigProperties.RmsWeights(0.45, 0.35, 0.20)
        val weights3 = FlpsConfigProperties.RmsWeights(0.50, 0.30, 0.20)

        // Assert
        (weights1 == weights2) shouldBe true
        (weights1 == weights3) shouldBe false
        weights1.hashCode() shouldBe weights2.hashCode()
    }

    @Test
    fun `RmsWeights data class should support toString`() {
        // Arrange
        val weights = FlpsConfigProperties.RmsWeights()

        // Act
        val string = weights.toString()

        // Assert
        string shouldContain "attendance=0.45"
        string shouldContain "mechanical=0.35"
        string shouldContain "preparation=0.2"
    }

    @Test
    fun `IpiWeights data class should support equals and hashCode`() {
        // Arrange
        val weights1 = FlpsConfigProperties.IpiWeights(0.45, 0.35, 0.20)
        val weights2 = FlpsConfigProperties.IpiWeights(0.45, 0.35, 0.20)
        val weights3 = FlpsConfigProperties.IpiWeights(0.50, 0.30, 0.20)

        // Assert
        (weights1 == weights2) shouldBe true
        (weights1 == weights3) shouldBe false
        weights1.hashCode() shouldBe weights2.hashCode()
    }

    @Test
    fun `IpiWeights data class should support toString`() {
        // Arrange
        val weights = FlpsConfigProperties.IpiWeights()

        // Act
        val string = weights.toString()

        // Assert
        string shouldContain "upgradeValue=0.45"
        string shouldContain "tierBonus=0.35"
        string shouldContain "roleMultiplier=0.2"
    }

    @Test
    fun `RoleMultipliers data class should support equals and hashCode`() {
        // Arrange
        val multipliers1 = FlpsConfigProperties.RoleMultipliers(1.2, 1.1, 1.0)
        val multipliers2 = FlpsConfigProperties.RoleMultipliers(1.2, 1.1, 1.0)
        val multipliers3 = FlpsConfigProperties.RoleMultipliers(1.5, 1.3, 1.0)

        // Assert
        (multipliers1 == multipliers2) shouldBe true
        (multipliers1 == multipliers3) shouldBe false
        multipliers1.hashCode() shouldBe multipliers2.hashCode()
    }

    @Test
    fun `RoleMultipliers data class should support toString`() {
        // Arrange
        val multipliers = FlpsConfigProperties.RoleMultipliers()

        // Act
        val string = multipliers.toString()

        // Assert
        string shouldContain "tank=1.2"
        string shouldContain "healer=1.1"
        string shouldContain "dps=1.0"
    }

    @Test
    fun `Thresholds data class should support equals and hashCode`() {
        // Arrange
        val thresholds1 = FlpsConfigProperties.Thresholds(0.8, 0.0, 30, 1.0, 0.0, 1.0)
        val thresholds2 = FlpsConfigProperties.Thresholds(0.8, 0.0, 30, 1.0, 0.0, 1.0)
        val thresholds3 = FlpsConfigProperties.Thresholds(0.9, 0.5, 14, 0.8, 0.2, 0.9)

        // Assert
        (thresholds1 == thresholds2) shouldBe true
        (thresholds1 == thresholds3) shouldBe false
        thresholds1.hashCode() shouldBe thresholds2.hashCode()
    }

    @Test
    fun `Thresholds data class should support toString`() {
        // Arrange
        val thresholds = FlpsConfigProperties.Thresholds()

        // Act
        val string = thresholds.toString()

        // Assert
        string shouldContain "eligibilityAttendance=0.8"
        string shouldContain "recencyDecayDays=30"
    }

    // Tests for setters (used by Spring Boot configuration binding)
    @Test
    fun `Thresholds should support property setters`() {
        // Arrange
        val thresholds = FlpsConfigProperties.Thresholds()

        // Act
        thresholds.eligibilityAttendance = 0.9
        thresholds.eligibilityActivity = 0.5
        thresholds.recencyDecayDays = 14
        thresholds.maxAttendanceBonus = 0.8
        thresholds.minMechanicalScore = 0.2
        thresholds.maxPreparationScore = 0.9

        // Assert
        thresholds.eligibilityAttendance shouldBe 0.9
        thresholds.eligibilityActivity shouldBe 0.5
        thresholds.recencyDecayDays shouldBe 14
        thresholds.maxAttendanceBonus shouldBe 0.8
        thresholds.minMechanicalScore shouldBe 0.2
        thresholds.maxPreparationScore shouldBe 0.9
    }

    @Test
    fun `RmsWeights should support property setters`() {
        // Arrange
        val weights = FlpsConfigProperties.RmsWeights()

        // Act
        weights.attendance = 0.5
        weights.mechanical = 0.3
        weights.preparation = 0.2

        // Assert
        weights.attendance shouldBe 0.5
        weights.mechanical shouldBe 0.3
        weights.preparation shouldBe 0.2
    }

    @Test
    fun `IpiWeights should support property setters`() {
        // Arrange
        val weights = FlpsConfigProperties.IpiWeights()

        // Act
        weights.upgradeValue = 0.5
        weights.tierBonus = 0.3
        weights.roleMultiplier = 0.2

        // Assert
        weights.upgradeValue shouldBe 0.5
        weights.tierBonus shouldBe 0.3
        weights.roleMultiplier shouldBe 0.2
    }

    @Test
    fun `RoleMultipliers should support property setters`() {
        // Arrange
        val multipliers = FlpsConfigProperties.RoleMultipliers()

        // Act
        multipliers.tank = 1.5
        multipliers.healer = 1.3
        multipliers.dps = 1.0

        // Assert
        multipliers.tank shouldBe 1.5
        multipliers.healer shouldBe 1.3
        multipliers.dps shouldBe 1.0
    }
}
