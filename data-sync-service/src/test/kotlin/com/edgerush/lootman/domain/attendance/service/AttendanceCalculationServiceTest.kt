package com.edgerush.lootman.domain.attendance.service

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceStats
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AttendanceCalculationServiceTest : UnitTest() {

    @MockK
    private lateinit var attendanceRepository: AttendanceRepository

    @InjectMockKs
    private lateinit var service: AttendanceCalculationService

    @Test
    fun `should calculate attendance stats for raider with multiple records`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        val records = listOf(
            createAttendanceRecord(raiderId, guildId, 8, 10),
            createAttendanceRecord(raiderId, guildId, 9, 10),
            createAttendanceRecord(raiderId, guildId, 7, 10)
        )

        every {
            attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                raiderId,
                guildId,
                startDate,
                endDate
            )
        } returns records

        // Act
        val stats = service.calculateAttendanceStats(raiderId, guildId, startDate, endDate)

        // Assert
        stats.totalRaids shouldBe 30
        stats.attendedRaids shouldBe 24
        stats.missedRaids shouldBe 6
        stats.attendancePercentage shouldBe 0.8
    }

    @Test
    fun `should return zero stats when no attendance records exist`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        every {
            attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                raiderId,
                guildId,
                startDate,
                endDate
            )
        } returns emptyList()

        // Act
        val stats = service.calculateAttendanceStats(raiderId, guildId, startDate, endDate)

        // Assert
        stats shouldBe AttendanceStats.zero()
    }

    @Test
    fun `should calculate perfect attendance stats`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        val records = listOf(
            createAttendanceRecord(raiderId, guildId, 10, 10),
            createAttendanceRecord(raiderId, guildId, 10, 10)
        )

        every {
            attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                raiderId,
                guildId,
                startDate,
                endDate
            )
        } returns records

        // Act
        val stats = service.calculateAttendanceStats(raiderId, guildId, startDate, endDate)

        // Assert
        stats.attendancePercentage shouldBe 1.0
        stats.missedRaids shouldBe 0
    }

    @Test
    fun `should calculate attendance stats for specific instance`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val instance = "Nerub-ar Palace"
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        val records = listOf(
            createAttendanceRecord(raiderId, guildId, 8, 10, instance = instance),
            createAttendanceRecord(raiderId, guildId, 9, 10, instance = instance)
        )

        every {
            attendanceRepository.findByRaiderIdAndGuildIdAndInstanceAndDateRange(
                raiderId,
                guildId,
                instance,
                startDate,
                endDate
            )
        } returns records

        // Act
        val stats = service.calculateAttendanceStatsForInstance(
            raiderId,
            guildId,
            instance,
            startDate,
            endDate
        )

        // Assert
        stats.totalRaids shouldBe 20
        stats.attendedRaids shouldBe 17
        stats.attendancePercentage shouldBe 0.85
    }

    @Test
    fun `should calculate attendance stats for specific encounter`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val instance = "Nerub-ar Palace"
        val encounter = "Queen Ansurek"
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        val records = listOf(
            createAttendanceRecord(
                raiderId,
                guildId,
                5,
                10,
                instance = instance,
                encounter = encounter
            )
        )

        every {
            attendanceRepository.findByRaiderIdAndGuildIdAndEncounterAndDateRange(
                raiderId,
                guildId,
                instance,
                encounter,
                startDate,
                endDate
            )
        } returns records

        // Act
        val stats = service.calculateAttendanceStatsForEncounter(
            raiderId,
            guildId,
            instance,
            encounter,
            startDate,
            endDate
        )

        // Assert
        stats.totalRaids shouldBe 10
        stats.attendedRaids shouldBe 5
        stats.attendancePercentage shouldBe 0.5
    }

    @Test
    fun `should calculate overall guild attendance stats`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        val allRecords = listOf(
            createAttendanceRecord(RaiderId(1L), guildId, 8, 10),
            createAttendanceRecord(RaiderId(2L), guildId, 9, 10),
            createAttendanceRecord(RaiderId(3L), guildId, 7, 10)
        )

        every {
            attendanceRepository.findByGuildIdAndDateRange(guildId, startDate, endDate)
        } returns allRecords

        // Act
        val stats = service.calculateGuildAttendanceStats(guildId, startDate, endDate)

        // Assert
        stats.totalRaids shouldBe 30
        stats.attendedRaids shouldBe 24
        stats.attendancePercentage shouldBe 0.8
    }

    private fun createAttendanceRecord(
        raiderId: RaiderId,
        guildId: GuildId,
        attendedRaids: Int,
        totalRaids: Int,
        instance: String = "Nerub-ar Palace",
        encounter: String? = null
    ): AttendanceRecord {
        return AttendanceRecord.create(
            raiderId = raiderId,
            guildId = guildId,
            instance = instance,
            encounter = encounter,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = attendedRaids,
            totalRaids = totalRaids
        )
    }
}
