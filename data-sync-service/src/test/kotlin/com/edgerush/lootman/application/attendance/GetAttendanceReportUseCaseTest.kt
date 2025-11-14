package com.edgerush.lootman.application.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.*
import com.edgerush.lootman.domain.attendance.service.AttendanceCalculationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Unit tests for GetAttendanceReportUseCase.
 */
class GetAttendanceReportUseCaseTest : UnitTest() {

    private lateinit var attendanceCalculationService: AttendanceCalculationService
    private lateinit var useCase: GetAttendanceReportUseCase

    @BeforeEach
    fun setup() {
        attendanceCalculationService = mockk()
        useCase = GetAttendanceReportUseCase(attendanceCalculationService)
    }

    @Test
    fun `should get overall attendance report when no instance specified`() {
        // Given
        val query = GetAttendanceReportQuery(
            raiderId = 123L,
            guildId = "guild-456",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            instance = null,
            encounter = null
        )

        val expectedStats = AttendanceStats.calculate(8, 10)

        every {
            attendanceCalculationService.calculateAttendanceStats(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals(RaiderId(query.raiderId), report.raiderId)
        assertEquals(GuildId(query.guildId), report.guildId)
        assertEquals(query.startDate, report.startDate)
        assertEquals(query.endDate, report.endDate)
        assertEquals(null, report.instance)
        assertEquals(null, report.encounter)
        assertEquals(expectedStats, report.stats)

        verify(exactly = 1) {
            attendanceCalculationService.calculateAttendanceStats(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.startDate,
                query.endDate
            )
        }
    }

    @Test
    fun `should get instance-specific attendance report when instance specified`() {
        // Given
        val query = GetAttendanceReportQuery(
            raiderId = 123L,
            guildId = "guild-456",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            instance = "Nerub-ar Palace",
            encounter = null
        )

        val expectedStats = AttendanceStats.calculate(6, 8)

        every {
            attendanceCalculationService.calculateAttendanceStatsForInstance(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.instance!!,
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals("Nerub-ar Palace", report.instance)
        assertEquals(expectedStats, report.stats)

        verify(exactly = 1) {
            attendanceCalculationService.calculateAttendanceStatsForInstance(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.instance!!,
                query.startDate,
                query.endDate
            )
        }
    }

    @Test
    fun `should get encounter-specific attendance report when encounter specified`() {
        // Given
        val query = GetAttendanceReportQuery(
            raiderId = 123L,
            guildId = "guild-456",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            instance = "Nerub-ar Palace",
            encounter = "Queen Ansurek"
        )

        val expectedStats = AttendanceStats.calculate(4, 5)

        every {
            attendanceCalculationService.calculateAttendanceStatsForEncounter(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.instance!!,
                query.encounter!!,
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals("Nerub-ar Palace", report.instance)
        assertEquals("Queen Ansurek", report.encounter)
        assertEquals(expectedStats, report.stats)

        verify(exactly = 1) {
            attendanceCalculationService.calculateAttendanceStatsForEncounter(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.instance!!,
                query.encounter!!,
                query.startDate,
                query.endDate
            )
        }
    }

    @Test
    fun `should return zero stats when no attendance data exists`() {
        // Given
        val query = GetAttendanceReportQuery(
            raiderId = 999L,
            guildId = "guild-456",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            instance = null,
            encounter = null
        )

        val expectedStats = AttendanceStats.zero()

        every {
            attendanceCalculationService.calculateAttendanceStats(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals(0, report.stats.attendedRaids)
        assertEquals(0, report.stats.totalRaids)
        assertEquals(0.0, report.stats.attendancePercentage)

        verify(exactly = 1) {
            attendanceCalculationService.calculateAttendanceStats(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.startDate,
                query.endDate
            )
        }
    }

    @Test
    fun `should return failure when encounter specified without instance`() {
        // Given
        val query = GetAttendanceReportQuery(
            raiderId = 123L,
            guildId = "guild-456",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            instance = null,
            encounter = "Queen Ansurek"
        )

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("instance") == true)

        verify(exactly = 0) {
            attendanceCalculationService.calculateAttendanceStats(any(), any(), any(), any())
        }
    }

    @Test
    fun `should handle perfect attendance in report`() {
        // Given
        val query = GetAttendanceReportQuery(
            raiderId = 123L,
            guildId = "guild-456",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            instance = null,
            encounter = null
        )

        val expectedStats = AttendanceStats.calculate(10, 10)

        every {
            attendanceCalculationService.calculateAttendanceStats(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals(1.0, report.stats.attendancePercentage)
        assertEquals(10, report.stats.attendedRaids)
        assertEquals(10, report.stats.totalRaids)
    }

    @Test
    fun `should aggregate data correctly for date range`() {
        // Given
        val query = GetAttendanceReportQuery(
            raiderId = 123L,
            guildId = "guild-456",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 3, 31),
            instance = null,
            encounter = null
        )

        val expectedStats = AttendanceStats.calculate(24, 30)

        every {
            attendanceCalculationService.calculateAttendanceStats(
                RaiderId(query.raiderId),
                GuildId(query.guildId),
                query.startDate,
                query.endDate
            )
        } returns expectedStats

        // When
        val result = useCase.execute(query)

        // Then
        assertTrue(result.isSuccess)
        val report = result.getOrThrow()
        assertEquals(24, report.stats.attendedRaids)
        assertEquals(30, report.stats.totalRaids)
        assertEquals(0.8, report.stats.attendancePercentage)
    }
}
