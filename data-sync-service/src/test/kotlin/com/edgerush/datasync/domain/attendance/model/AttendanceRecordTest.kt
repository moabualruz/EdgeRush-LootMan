package com.edgerush.datasync.domain.attendance.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AttendanceRecordTest : UnitTest() {
    
    @Test
    fun `should create attendance record with valid data`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val raidDate = LocalDate.now()
        
        // Act
        val record = AttendanceRecord.create(
            raiderId = raiderId,
            guildId = guildId,
            raidDate = raidDate,
            wasPresent = true,
            wasSelected = true,
            instance = "Vault of the Incarnates",
            difficulty = "Heroic"
        )
        
        // Assert
        record.raiderId shouldBe raiderId
        record.guildId shouldBe guildId
        record.raidDate shouldBe raidDate
        record.wasPresent shouldBe true
        record.wasSelected shouldBe true
        record.instance shouldBe "Vault of the Incarnates"
        record.difficulty shouldBe "Heroic"
    }
    
    @Test
    fun `should create attendance record with minimal data`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val raidDate = LocalDate.now()
        
        // Act
        val record = AttendanceRecord.create(
            raiderId = raiderId,
            guildId = guildId,
            raidDate = raidDate,
            wasPresent = true,
            wasSelected = false
        )
        
        // Assert
        record.raiderId shouldBe raiderId
        record.guildId shouldBe guildId
        record.raidDate shouldBe raidDate
        record.wasPresent shouldBe true
        record.wasSelected shouldBe false
        record.instance shouldBe null
        record.difficulty shouldBe null
    }
    
    @Test
    fun `should mark raider as absent`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val raidDate = LocalDate.now()
        
        // Act
        val record = AttendanceRecord.create(
            raiderId = raiderId,
            guildId = guildId,
            raidDate = raidDate,
            wasPresent = false,
            wasSelected = false
        )
        
        // Assert
        record.wasPresent shouldBe false
        record.wasSelected shouldBe false
    }
    
    @Test
    fun `should reconstitute attendance record from persistence`() {
        // Arrange
        val id = AttendanceRecordId(100L)
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val raidDate = LocalDate.now()
        
        // Act
        val record = AttendanceRecord.reconstitute(
            id = id,
            raiderId = raiderId,
            guildId = guildId,
            raidDate = raidDate,
            wasPresent = true,
            wasSelected = true,
            instance = "Vault of the Incarnates",
            difficulty = "Heroic"
        )
        
        // Assert
        record.id shouldBe id
        record.raiderId shouldBe raiderId
        record.guildId shouldBe guildId
        record.raidDate shouldBe raidDate
        record.wasPresent shouldBe true
        record.wasSelected shouldBe true
    }
    
    @Test
    fun `should check if raider was present and selected`() {
        // Arrange
        val presentAndSelected = AttendanceRecord.create(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            raidDate = LocalDate.now(),
            wasPresent = true,
            wasSelected = true
        )
        
        val presentNotSelected = AttendanceRecord.create(
            raiderId = RaiderId(2L),
            guildId = GuildId("test-guild"),
            raidDate = LocalDate.now(),
            wasPresent = true,
            wasSelected = false
        )
        
        val absent = AttendanceRecord.create(
            raiderId = RaiderId(3L),
            guildId = GuildId("test-guild"),
            raidDate = LocalDate.now(),
            wasPresent = false,
            wasSelected = false
        )
        
        // Act & Assert
        presentAndSelected.isPresent() shouldBe true
        presentAndSelected.isSelected() shouldBe true
        presentAndSelected.isAbsent() shouldBe false
        
        presentNotSelected.isPresent() shouldBe true
        presentNotSelected.isSelected() shouldBe false
        presentNotSelected.isAbsent() shouldBe false
        
        absent.isPresent() shouldBe false
        absent.isSelected() shouldBe false
        absent.isAbsent() shouldBe true
    }
}
