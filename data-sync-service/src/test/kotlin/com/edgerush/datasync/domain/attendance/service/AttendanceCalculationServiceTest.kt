package com.edgerush.datasync.domain.attendance.service

import com.edgerush.datasync.domain.attendance.model.*
import com.edgerush.datasync.domain.attendance.repository.AttendanceRecordRepository
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AttendanceCalculationServiceTest : UnitTest() {
    
    @MockK
    private lateinit var attendanceRecordRepository: AttendanceRecordRepository
    
    @InjectMockKs
    private lateinit var service: AttendanceCalculationService
    
    @Test
    fun `should calculate attendance stats for raider with all raids attended`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.now().minusDays(30)
        val endDate = LocalDate.now()
        
        val records = listOf(
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(7), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(14), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(21), present = true, selected = true)
        )
        
        every { 
            attendanceRecordRepository.findByRaiderAndGuildInDateRange(raiderId, guildId, startDate, endDate) 
        } returns records
        
        // Act
        val stats = service.calculateAttendanceStats(raiderId, guildId, startDate, endDate)
        
        // Assert
        stats.totalRaids shouldBe 3
        stats.attendedRaids shouldBe 3
        stats.totalEncounters shouldBe 3
        stats.selectedEncounters shouldBe 3
        stats.attendancePercentage() shouldBe 1.0
        stats.selectionPercentage() shouldBe 1.0
    }
    
    @Test
    fun `should calculate attendance stats for raider with some absences`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.now().minusDays(30)
        val endDate = LocalDate.now()
        
        val records = listOf(
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(7), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(14), present = false, selected = false),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(21), present = true, selected = false)
        )
        
        every { 
            attendanceRecordRepository.findByRaiderAndGuildInDateRange(raiderId, guildId, startDate, endDate) 
        } returns records
        
        // Act
        val stats = service.calculateAttendanceStats(raiderId, guildId, startDate, endDate)
        
        // Assert
        stats.totalRaids shouldBe 3
        stats.attendedRaids shouldBe 2
        stats.totalEncounters shouldBe 3
        stats.selectedEncounters shouldBe 1
        stats.attendancePercentage() shouldBe (2.0 / 3.0)
        stats.selectionPercentage() shouldBe (1.0 / 3.0)
    }
    
    @Test
    fun `should return empty stats when no attendance records found`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.now().minusDays(30)
        val endDate = LocalDate.now()
        
        every { 
            attendanceRecordRepository.findByRaiderAndGuildInDateRange(raiderId, guildId, startDate, endDate) 
        } returns emptyList()
        
        // Act
        val stats = service.calculateAttendanceStats(raiderId, guildId, startDate, endDate)
        
        // Assert
        stats.totalRaids shouldBe 0
        stats.attendedRaids shouldBe 0
        stats.totalEncounters shouldBe 0
        stats.selectedEncounters shouldBe 0
        stats.attendancePercentage() shouldBe 0.0
        stats.selectionPercentage() shouldBe 0.0
    }
    
    @Test
    fun `should calculate attendance score based on percentage`() {
        // Arrange
        val stats = AttendanceStats.of(
            totalRaids = 10,
            attendedRaids = 8,
            totalEncounters = 50,
            selectedEncounters = 40
        )
        
        // Act
        val score = service.calculateAttendanceScore(stats)
        
        // Assert
        score shouldBe 0.8
    }
    
    @Test
    fun `should return zero score for empty stats`() {
        // Arrange
        val stats = AttendanceStats.empty()
        
        // Act
        val score = service.calculateAttendanceScore(stats)
        
        // Assert
        score shouldBe 0.0
    }
    
    @Test
    fun `should calculate recent attendance for last N raids`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val lastNRaids = 5
        
        val records = listOf(
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(1), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(2), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(3), present = false, selected = false),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(4), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(5), present = true, selected = false)
        )
        
        every { 
            attendanceRecordRepository.findRecentByRaiderAndGuild(raiderId, guildId, lastNRaids) 
        } returns records
        
        // Act
        val stats = service.calculateRecentAttendance(raiderId, guildId, lastNRaids)
        
        // Assert
        stats.totalRaids shouldBe 5
        stats.attendedRaids shouldBe 4
        stats.totalEncounters shouldBe 5
        stats.selectedEncounters shouldBe 3
        stats.attendancePercentage() shouldBe 0.8
    }
    
    @Test
    fun `should check if raider meets attendance threshold`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.now().minusDays(30)
        val endDate = LocalDate.now()
        val threshold = 0.75
        
        val records = listOf(
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(7), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(14), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(21), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(28), present = false, selected = false)
        )
        
        every { 
            attendanceRecordRepository.findByRaiderAndGuildInDateRange(raiderId, guildId, startDate, endDate) 
        } returns records
        
        // Act
        val meetsThreshold = service.meetsAttendanceThreshold(raiderId, guildId, startDate, endDate, threshold)
        
        // Assert
        meetsThreshold shouldBe true
    }
    
    @Test
    fun `should return false when raider does not meet attendance threshold`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.now().minusDays(30)
        val endDate = LocalDate.now()
        val threshold = 0.9
        
        val records = listOf(
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(7), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(14), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(21), present = true, selected = true),
            createAttendanceRecord(raiderId, guildId, LocalDate.now().minusDays(28), present = false, selected = false)
        )
        
        every { 
            attendanceRecordRepository.findByRaiderAndGuildInDateRange(raiderId, guildId, startDate, endDate) 
        } returns records
        
        // Act
        val meetsThreshold = service.meetsAttendanceThreshold(raiderId, guildId, startDate, endDate, threshold)
        
        // Assert
        meetsThreshold shouldBe false
    }
    
    private fun createAttendanceRecord(
        raiderId: RaiderId,
        guildId: GuildId,
        raidDate: LocalDate,
        present: Boolean,
        selected: Boolean
    ): AttendanceRecord {
        return AttendanceRecord.create(
            raiderId = raiderId,
            guildId = guildId,
            raidDate = raidDate,
            wasPresent = present,
            wasSelected = selected
        )
    }
}
