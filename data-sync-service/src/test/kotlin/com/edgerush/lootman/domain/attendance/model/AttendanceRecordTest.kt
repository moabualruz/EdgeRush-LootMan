package com.edgerush.lootman.domain.attendance.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AttendanceRecordTest : UnitTest() {
    @Test
    fun `should create valid attendance record with all required fields`() {
        // Arrange & Act
        val record =
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = "Queen Ansurek",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10,
            )

        // Assert
        record shouldNotBe null
        record.raiderId shouldBe RaiderId(1L)
        record.guildId shouldBe GuildId("test-guild")
        record.instance shouldBe "Nerub-ar Palace"
        record.encounter shouldBe "Queen Ansurek"
        record.attendedRaids shouldBe 8
        record.totalRaids shouldBe 10
        record.attendancePercentage shouldBe 0.8
    }

    @Test
    fun `should calculate attendance percentage correctly`() {
        // Arrange & Act
        val record =
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 7,
                totalRaids = 10,
            )

        // Assert
        record.attendancePercentage shouldBe 0.7
    }

    @Test
    fun `should handle perfect attendance`() {
        // Arrange & Act
        val record =
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 10,
                totalRaids = 10,
            )

        // Assert
        record.attendancePercentage shouldBe 1.0
    }

    @Test
    fun `should handle zero attendance`() {
        // Arrange & Act
        val record =
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 0,
                totalRaids = 10,
            )

        // Assert
        record.attendancePercentage shouldBe 0.0
    }

    @Test
    fun `should throw exception when attended raids exceeds total raids`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 11,
                totalRaids = 10,
            )
        }
    }

    @Test
    fun `should throw exception when attended raids is negative`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = -1,
                totalRaids = 10,
            )
        }
    }

    @Test
    fun `should throw exception when total raids is zero`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 0,
                totalRaids = 0,
            )
        }
    }

    @Test
    fun `should throw exception when total raids is negative`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 0,
                totalRaids = -1,
            )
        }
    }

    @Test
    fun `should throw exception when end date is before start date`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 14),
                endDate = LocalDate.of(2024, 11, 1),
                attendedRaids = 8,
                totalRaids = 10,
            )
        }
    }

    @Test
    fun `should allow null encounter for overall instance attendance`() {
        // Arrange & Act
        val record =
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10,
            )

        // Assert
        record.encounter shouldBe null
    }

    @Test
    fun `should have unique identity based on id`() {
        // Arrange
        val record1 =
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10,
            )

        val record2 =
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10,
            )

        // Assert - Different instances should have different IDs
        record1.id shouldNotBe record2.id
    }

    @Test
    fun `should throw exception when instance name is blank`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10,
            )
        }.message shouldBe "Instance name cannot be blank"
    }

    @Test
    fun `should throw exception when instance name is whitespace only`() {
        // Arrange, Act & Assert
        shouldThrow<IllegalArgumentException> {
            AttendanceRecord.create(
                raiderId = RaiderId(1L),
                guildId = GuildId("test-guild"),
                instance = "   ",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10,
            )
        }.message shouldBe "Instance name cannot be blank"
    }

    @Test
    fun `should allow same start and end date`() {
        // Arrange & Act
        val record = AttendanceRecord.create(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 1),
            attendedRaids = 1,
            totalRaids = 1,
        )

        // Assert
        record.startDate shouldBe record.endDate
        record.attendancePercentage shouldBe 1.0
    }

    @Test
    fun `should generate valid id on create`() {
        // Arrange & Act
        val record = AttendanceRecord.create(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 8,
            totalRaids = 10,
        )

        // Assert
        record.id.value.isNotBlank() shouldBe true
        record.id.value.length shouldBe 36 // UUID format
    }

    @Test
    fun `should set recordedAt on create`() {
        // Arrange & Act
        val beforeCreate = java.time.Instant.now()
        val record = AttendanceRecord.create(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 8,
            totalRaids = 10,
        )
        val afterCreate = java.time.Instant.now()

        // Assert - recordedAt should be between before and after create times
        (record.recordedAt >= beforeCreate) shouldBe true
        (record.recordedAt <= afterCreate) shouldBe true
    }
}
