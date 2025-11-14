package com.edgerush.lootman.application.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.AttendanceStats
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.service.AttendanceCalculationService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GetAttendanceReportUseCaseTest : UnitTest() {

    @MockK
    private lateinit var attendanceCalculationService: AttendanceCalculationService

    @InjectMockKs
    private lateinit var useCase: GetAttendanceReportUseCase

    @Test
    fun `should get overall attendance report for raider`() {
        // Arrange
        val query = GetAttendanceReportQuery(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            instance = null,
            encounter = null
        )

        val expectedStats = AttendanceStats.calculate(24, 30)

        every {
            attendanceCalculationService.calculateAttendanceStats(
                query.raiderId,
                query.guildId,
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.raiderId shouldBe query.raiderId
        report.guildId shouldBe query.guildId
        report.stats shouldBe expectedStats
        report.instance shouldBe null
        report.encounter shouldBe null

        verify(exactly = 1) {
            attendanceCalculationService.calculateAttendanceStats(
                query.raiderId,
                query.guildId,
                query.startDate,
                query.endDate
            )
        }
    }

    @Test
    fun `should get attendance report for specific instance`() {
        // Arrange
        val query = GetAttendanceReportQuery(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            instance = "Nerub-ar Palace",
            encounter = null
        )

        val expectedStats = AttendanceStats.calculate(17, 20)

        every {
            attendanceCalculationService.calculateAttendanceStatsForInstance(
                query.raiderId,
                query.guildId,
                "Nerub-ar Palace",
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.instance shouldBe "Nerub-ar Palace"
        report.stats shouldBe expectedStats

        verify(exactly = 1) {
            attendanceCalculationService.calculateAttendanceStatsForInstance(
                query.raiderId,
                query.guildId,
                "Nerub-ar Palace",
                query.startDate,
                query.endDate
            )
        }
    }

    @Test
    fun `should get attendance report for specific encounter`() {
        // Arrange
        val query = GetAttendanceReportQuery(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            instance = "Nerub-ar Palace",
            encounter = "Queen Ansurek"
        )

        val expectedStats = AttendanceStats.calculate(5, 10)

        every {
            attendanceCalculationService.calculateAttendanceStatsForEncounter(
                query.raiderId,
                query.guildId,
                "Nerub-ar Palace",
                "Queen Ansurek",
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.instance shouldBe "Nerub-ar Palace"
        report.encounter shouldBe "Queen Ansurek"
        report.stats shouldBe expectedStats

        verify(exactly = 1) {
            attendanceCalculationService.calculateAttendanceStatsForEncounter(
                query.raiderId,
                query.guildId,
                "Nerub-ar Palace",
                "Queen Ansurek",
                query.startDate,
                query.endDate
            )
        }
    }

    @Test
    fun `should return zero stats when no attendance records exist`() {
        // Arrange
        val query = GetAttendanceReportQuery(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            instance = null,
            encounter = null
        )

        every {
            attendanceCalculationService.calculateAttendanceStats(
                query.raiderId,
                query.guildId,
                query.startDate,
                query.endDate
            )
        } returns AttendanceStats.zero()

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.stats shouldBe AttendanceStats.zero()
    }

    @Test
    fun `should get guild-wide attendance report`() {
        // Arrange
        val query = GetGuildAttendanceReportQuery(
            guildId = GuildId("test-guild"),
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14)
        )

        val expectedStats = AttendanceStats.calculate(240, 300)

        every {
            attendanceCalculationService.calculateGuildAttendanceStats(
                query.guildId,
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // Act
        val result = useCase.executeGuildReport(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.guildId shouldBe query.guildId
        report.stats shouldBe expectedStats

        verify(exactly = 1) {
            attendanceCalculationService.calculateGuildAttendanceStats(
                query.guildId,
                query.startDate,
                query.endDate
            )
        }
    }

    @Test
    fun `should handle perfect attendance in report`() {
        // Arrange
        val query = GetAttendanceReportQuery(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            instance = null,
            encounter = null
        )

        val perfectStats = AttendanceStats.calculate(30, 30)

        every {
            attendanceCalculationService.calculateAttendanceStats(
                query.raiderId,
                query.guildId,
                query.startDate,
                query.endDate
            )
        } returns perfectStats

        // Act
        val result = useCase.execute(query)

        // Assert
        result.isSuccess shouldBe true
        val report = result.getOrNull()!!

        report.stats.attendancePercentage shouldBe 1.0
        report.stats.missedRaids shouldBe 0
    }
}
