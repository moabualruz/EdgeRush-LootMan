package com.edgerush.datasync.domain.attendance.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class AttendanceStatsTest : UnitTest() {
    
    @Test
    fun `should create attendance stats with valid data`() {
        // Arrange & Act
        val stats = AttendanceStats.of(
            totalRaids = 10,
            attendedRaids = 8,
            totalEncounters = 50,
            selectedEncounters = 40
        )
        
        // Assert
        stats.totalRaids shouldBe 10
        stats.attendedRaids shouldBe 8
        stats.totalEncounters shouldBe 50
        stats.selectedEncounters shouldBe 40
    }
    
    @Test
    fun `should calculate attendance percentage correctly`() {
        // Arrange
        val stats = AttendanceStats.of(
            totalRaids = 10,
            attendedRaids = 8,
            totalEncounters = 50,
            selectedEncounters = 40
        )
        
        // Act
        val attendancePercentage = stats.attendancePercentage()
        val selectionPercentage = stats.selectionPercentage()
        
        // Assert
        attendancePercentage shouldBe 0.8
        selectionPercentage shouldBe 0.8
    }
    
    @Test
    fun `should return zero percentage when total raids is zero`() {
        // Arrange
        val stats = AttendanceStats.of(
            totalRaids = 0,
            attendedRaids = 0,
            totalEncounters = 0,
            selectedEncounters = 0
        )
        
        // Act
        val attendancePercentage = stats.attendancePercentage()
        val selectionPercentage = stats.selectionPercentage()
        
        // Assert
        attendancePercentage shouldBe 0.0
        selectionPercentage shouldBe 0.0
    }
    
    @Test
    fun `should return perfect attendance when all raids attended`() {
        // Arrange
        val stats = AttendanceStats.of(
            totalRaids = 10,
            attendedRaids = 10,
            totalEncounters = 50,
            selectedEncounters = 50
        )
        
        // Act
        val attendancePercentage = stats.attendancePercentage()
        val selectionPercentage = stats.selectionPercentage()
        
        // Assert
        attendancePercentage shouldBe 1.0
        selectionPercentage shouldBe 1.0
    }
    
    @Test
    fun `should throw exception when attended raids exceeds total raids`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                totalRaids = 10,
                attendedRaids = 11,
                totalEncounters = 50,
                selectedEncounters = 40
            )
        }
    }
    
    @Test
    fun `should throw exception when selected encounters exceeds total encounters`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                totalRaids = 10,
                attendedRaids = 8,
                totalEncounters = 50,
                selectedEncounters = 51
            )
        }
    }
    
    @Test
    fun `should throw exception when total raids is negative`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                totalRaids = -1,
                attendedRaids = 0,
                totalEncounters = 50,
                selectedEncounters = 40
            )
        }
    }
    
    @Test
    fun `should throw exception when attended raids is negative`() {
        // Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceStats.of(
                totalRaids = 10,
                attendedRaids = -1,
                totalEncounters = 50,
                selectedEncounters = 40
            )
        }
    }
    
    @Test
    fun `should create empty stats`() {
        // Act
        val stats = AttendanceStats.empty()
        
        // Assert
        stats.totalRaids shouldBe 0
        stats.attendedRaids shouldBe 0
        stats.totalEncounters shouldBe 0
        stats.selectedEncounters shouldBe 0
        stats.attendancePercentage() shouldBe 0.0
        stats.selectionPercentage() shouldBe 0.0
    }
    
    @Test
    fun `should combine two attendance stats`() {
        // Arrange
        val stats1 = AttendanceStats.of(
            totalRaids = 10,
            attendedRaids = 8,
            totalEncounters = 50,
            selectedEncounters = 40
        )
        val stats2 = AttendanceStats.of(
            totalRaids = 5,
            attendedRaids = 4,
            totalEncounters = 25,
            selectedEncounters = 20
        )
        
        // Act
        val combined = stats1.combine(stats2)
        
        // Assert
        combined.totalRaids shouldBe 15
        combined.attendedRaids shouldBe 12
        combined.totalEncounters shouldBe 75
        combined.selectedEncounters shouldBe 60
        combined.attendancePercentage() shouldBe 0.8
        combined.selectionPercentage() shouldBe 0.8
    }
    
    @Test
    fun `should check if attendance is perfect`() {
        // Arrange
        val perfect = AttendanceStats.of(
            totalRaids = 10,
            attendedRaids = 10,
            totalEncounters = 50,
            selectedEncounters = 50
        )
        val notPerfect = AttendanceStats.of(
            totalRaids = 10,
            attendedRaids = 8,
            totalEncounters = 50,
            selectedEncounters = 40
        )
        
        // Act & Assert
        perfect.isPerfectAttendance() shouldBe true
        notPerfect.isPerfectAttendance() shouldBe false
    }
    
    @Test
    fun `should check if attendance meets threshold`() {
        // Arrange
        val stats = AttendanceStats.of(
            totalRaids = 10,
            attendedRaids = 8,
            totalEncounters = 50,
            selectedEncounters = 40
        )
        
        // Act & Assert
        stats.meetsAttendanceThreshold(0.7) shouldBe true
        stats.meetsAttendanceThreshold(0.8) shouldBe true
        stats.meetsAttendanceThreshold(0.9) shouldBe false
    }
}
