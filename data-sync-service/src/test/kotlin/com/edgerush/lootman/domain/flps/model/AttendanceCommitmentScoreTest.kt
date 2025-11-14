package com.edgerush.lootman.domain.flps.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for AttendanceCommitmentScore value object.
 */
class AttendanceCommitmentScoreTest : UnitTest() {
    @Test
    fun `should create valid ACS when value is between 0 and 1`() {
        // Arrange & Act
        val acs = AttendanceCommitmentScore.of(0.9)

        // Assert
        acs.value shouldBe 0.9
    }

    @Test
    fun `should throw exception when value is negative`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                AttendanceCommitmentScore.of(-0.1)
            }
        exception.message shouldBe "Attendance Commitment Score must be between 0.0 and 1.0, got -0.1"
    }

    @Test
    fun `should throw exception when value exceeds 1`() {
        // Arrange & Act & Assert
        val exception =
            assertThrows<IllegalArgumentException> {
                AttendanceCommitmentScore.of(1.5)
            }
        exception.message shouldBe "Attendance Commitment Score must be between 0.0 and 1.0, got 1.5"
    }
}
