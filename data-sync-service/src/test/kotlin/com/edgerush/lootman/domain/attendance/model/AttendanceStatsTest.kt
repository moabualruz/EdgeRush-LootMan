package com.edgerush.lootman.domain.attendance.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AttendanceStatsTest : UnitTest() {

    @Test
    fun `should create valid attendance stats with all fields`() {
        // Arrange & Act
        val stats = AttendanceStats.of(
            attendancePercentage = 0.85,
            totalRaids = 20,
            attendedRaids = 17,
            missedRaids = 3
        )

        // Assert
        stats.attendancePercentage shouldBe 0.85
        stats.totalRaids shouldBe 20
        stats.attendedRaids shouldBe 17
        stats.missedRaids shouldBe 3
    }

    @Test
    fun `should calculate attendance percentage correctly`() {
        // Arrange & Act
        val stats = AttendanceStats.calculate(
            attendedRaids = 15,
            totalRaids = 20
        )

        // Assert
        stats.attendancePercentage shouldBe 0.75
        stats.attendedRaids shouldBe 15
        stats.totalRaids shouldBe 20
        stats.missedRaids shouldBe 5
    }

    @Test
    fun `should handle perfect attendance`() {
        // Arrange & Act
        val stats = AttendanceStats.calculate(
            attendedRaids = 20,
            totalRaids = 20
        )

        // Assert
        stats.attendancePercentage shouldBe 1.0
        stats.missedRaids shouldBe 0
    }

    @Test
    fun `should handle zero attendance`() {
        // Arrange & Act
        val stats = AttendanceStats.calculate(
            attendedRaids = 0,
            totalRaids = 20
        )

        // Assert
        stats.attendancePercentage shouldBe 0.0
        stats.missedRaids shouldBe 20
    }

    @Test
    fun `should create zero stats`() {
        // Arrange & Act
        val stats = AttendanceStats.zero()

        // Assert
        stats.attendancePercentage shouldBe 0.0
        stats.totalRaids shouldBe 0
        stats.attendedRaids shouldBe 0
        stats.missedRaids shouldBe 0
    }

    @Test
    fun `should throw exception when attendance percentage is negative`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                attendancePercentage = -0.1,
                totalRaids = 20,
                attendedRaids = 17,
                missedRaids = 3
            )
        }
    }

    @Test
    fun `should throw exception when attendance percentage exceeds 1_0`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                attendancePercentage = 1.1,
                totalRaids = 20,
                attendedRaids = 17,
                missedRaids = 3
            )
        }
    }

    @Test
    fun `should throw exception when total raids is negative`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                attendancePercentage = 0.85,
                totalRaids = -1,
                attendedRaids = 17,
                missedRaids = 3
            )
        }
    }

    @Test
    fun `should throw exception when attended raids is negative`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                attendancePercentage = 0.85,
                totalRaids = 20,
                attendedRaids = -1,
                missedRaids = 3
            )
        }
    }

    @Test
    fun `should throw exception when missed raids is negative`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                attendancePercentage = 0.85,
                totalRaids = 20,
                attendedRaids = 17,
                missedRaids = -1
            )
        }
    }

    @Test
    fun `should throw exception when attended raids exceeds total raids`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.calculate(
                attendedRaids = 21,
                totalRaids = 20
            )
        }
    }

    @Test
    fun `should throw exception when total raids is zero but attended raids is not`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.calculate(
                attendedRaids = 1,
                totalRaids = 0
            )
        }
    }

    @Test
    fun `should throw exception when attended plus missed does not equal total`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                attendancePercentage = 0.85,
                totalRaids = 20,
                attendedRaids = 15,
                missedRaids = 3  // Should be 5
            )
        }
    }

    @Test
    fun `should be immutable value object`() {
        // Arrange
        val stats1 = AttendanceStats.calculate(15, 20)
        val stats2 = AttendanceStats.calculate(15, 20)

        // Assert - Same values should be equal
        stats1 shouldBe stats2
    }
}
