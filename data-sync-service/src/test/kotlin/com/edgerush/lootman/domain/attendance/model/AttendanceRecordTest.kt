package com.edgerush.lootman.domain.attendance.model

import com.edgerush.datasync.test.base.UnitTest
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
}
