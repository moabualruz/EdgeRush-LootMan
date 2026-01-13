package com.edgerush.lootman.api.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.attendance.AttendanceReport
import com.edgerush.lootman.application.attendance.GetAttendanceReportQuery
import com.edgerush.lootman.application.attendance.GetAttendanceReportUseCase
import com.edgerush.lootman.application.attendance.TrackAttendanceCommand
import com.edgerush.lootman.application.attendance.TrackAttendanceUseCase
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceStats
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.LocalDate

/**
 * Unit tests for AttendanceController.
 *
 * Tests controller methods directly without Spring context,
 * mocking use cases as dependencies.
 */
class AttendanceControllerTest : UnitTest() {
    private lateinit var trackAttendanceUseCase: TrackAttendanceUseCase
    private lateinit var getAttendanceReportUseCase: GetAttendanceReportUseCase
    private lateinit var controller: AttendanceController

    @BeforeEach
    fun setup() {
        trackAttendanceUseCase = mockk()
        getAttendanceReportUseCase = mockk()
        controller = AttendanceController(
            trackAttendanceUseCase,
            getAttendanceReportUseCase,
        )
    }

    @Test
    fun `trackAttendance should return CREATED status with attendance response`() {
        // Given
        val request = TrackAttendanceRequest(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 8,
            totalRaids = 10,
        )

        val attendanceRecord = AttendanceRecord.create(
            raiderId = RaiderId(123L),
            guildId = GuildId("guild-456"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 8,
            totalRaids = 10,
        )

        every { trackAttendanceUseCase.execute(any()) } returns Result.success(attendanceRecord)

        // When
        val response = controller.trackAttendance(request)

        // Then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body?.raiderId shouldBe 123L
        response.body?.guildId shouldBe "guild-456"
        response.body?.instance shouldBe "Nerub-ar Palace"
        response.body?.encounter shouldBe null
        response.body?.attendedRaids shouldBe 8
        response.body?.totalRaids shouldBe 10
        response.body?.attendancePercentage shouldBe 0.8

        verify(exactly = 1) { trackAttendanceUseCase.execute(any()) }
    }

    @Test
    fun `trackAttendance should pass correct command to use case`() {
        // Given
        val request = TrackAttendanceRequest(
            raiderId = 999L,
            guildId = "my-guild",
            instance = "Vault of the Incarnates",
            encounter = "Raszageth",
            startDate = LocalDate.of(2024, 2, 1),
            endDate = LocalDate.of(2024, 2, 28),
            attendedRaids = 5,
            totalRaids = 8,
        )

        val commandSlot = slot<TrackAttendanceCommand>()

        val attendanceRecord = AttendanceRecord.create(
            raiderId = RaiderId(999L),
            guildId = GuildId("my-guild"),
            instance = "Vault of the Incarnates",
            encounter = "Raszageth",
            startDate = LocalDate.of(2024, 2, 1),
            endDate = LocalDate.of(2024, 2, 28),
            attendedRaids = 5,
            totalRaids = 8,
        )

        every { trackAttendanceUseCase.execute(capture(commandSlot)) } returns Result.success(attendanceRecord)

        // When
        controller.trackAttendance(request)

        // Then
        commandSlot.captured.raiderId shouldBe 999L
        commandSlot.captured.guildId shouldBe "my-guild"
        commandSlot.captured.instance shouldBe "Vault of the Incarnates"
        commandSlot.captured.encounter shouldBe "Raszageth"
        commandSlot.captured.startDate shouldBe LocalDate.of(2024, 2, 1)
        commandSlot.captured.endDate shouldBe LocalDate.of(2024, 2, 28)
        commandSlot.captured.attendedRaids shouldBe 5
        commandSlot.captured.totalRaids shouldBe 8
    }

    @Test
    fun `trackAttendance should handle encounter-specific attendance`() {
        // Given
        val request = TrackAttendanceRequest(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = "Queen Ansurek",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 3,
            totalRaids = 4,
        )

        val attendanceRecord = AttendanceRecord.create(
            raiderId = RaiderId(123L),
            guildId = GuildId("guild-456"),
            instance = "Nerub-ar Palace",
            encounter = "Queen Ansurek",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 3,
            totalRaids = 4,
        )

        every { trackAttendanceUseCase.execute(any()) } returns Result.success(attendanceRecord)

        // When
        val response = controller.trackAttendance(request)

        // Then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body?.encounter shouldBe "Queen Ansurek"
        response.body?.attendancePercentage shouldBe 0.75
    }

    @Test
    fun `trackAttendance should throw exception when use case fails`() {
        // Given
        val request = TrackAttendanceRequest(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 31),
            endDate = LocalDate.of(2024, 1, 1),
            attendedRaids = 8,
            totalRaids = 10,
        )

        every { trackAttendanceUseCase.execute(any()) } returns Result.failure(
            IllegalArgumentException("End date cannot be before start date")
        )

        // When/Then
        try {
            controller.trackAttendance(request)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "End date cannot be before start date"
        }
    }

    @Test
    fun `getAttendanceReport should return report for overall attendance`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        val stats = AttendanceStats.calculate(attendedRaids = 8, totalRaids = 10)
        val report = AttendanceReport(
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            startDate = startDate,
            endDate = endDate,
            instance = null,
            encounter = null,
            stats = stats,
        )

        every { getAttendanceReportUseCase.execute(any()) } returns Result.success(report)

        // When
        val response = controller.getAttendanceReport(
            raiderId = raiderId,
            guildId = guildId,
            startDate = startDate,
            endDate = endDate,
            instance = null,
            encounter = null,
        )

        // Then
        response.raiderId shouldBe raiderId
        response.guildId shouldBe guildId
        response.startDate shouldBe startDate
        response.endDate shouldBe endDate
        response.instance shouldBe null
        response.encounter shouldBe null
        response.stats.attendancePercentage shouldBe 0.8
        response.stats.totalRaids shouldBe 10
        response.stats.attendedRaids shouldBe 8
        response.stats.missedRaids shouldBe 2

        verify(exactly = 1) { getAttendanceReportUseCase.execute(any()) }
    }

    @Test
    fun `getAttendanceReport should pass correct query for instance-specific report`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val instance = "Nerub-ar Palace"

        val querySlot = slot<GetAttendanceReportQuery>()

        val stats = AttendanceStats.calculate(attendedRaids = 6, totalRaids = 8)
        val report = AttendanceReport(
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            startDate = startDate,
            endDate = endDate,
            instance = instance,
            encounter = null,
            stats = stats,
        )

        every { getAttendanceReportUseCase.execute(capture(querySlot)) } returns Result.success(report)

        // When
        val response = controller.getAttendanceReport(
            raiderId = raiderId,
            guildId = guildId,
            startDate = startDate,
            endDate = endDate,
            instance = instance,
            encounter = null,
        )

        // Then
        querySlot.captured.raiderId shouldBe raiderId
        querySlot.captured.guildId shouldBe guildId
        querySlot.captured.startDate shouldBe startDate
        querySlot.captured.endDate shouldBe endDate
        querySlot.captured.instance shouldBe instance
        querySlot.captured.encounter shouldBe null

        response.instance shouldBe instance
    }

    @Test
    fun `getAttendanceReport should pass correct query for encounter-specific report`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val instance = "Nerub-ar Palace"
        val encounter = "Queen Ansurek"

        val querySlot = slot<GetAttendanceReportQuery>()

        val stats = AttendanceStats.calculate(attendedRaids = 3, totalRaids = 4)
        val report = AttendanceReport(
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            startDate = startDate,
            endDate = endDate,
            instance = instance,
            encounter = encounter,
            stats = stats,
        )

        every { getAttendanceReportUseCase.execute(capture(querySlot)) } returns Result.success(report)

        // When
        val response = controller.getAttendanceReport(
            raiderId = raiderId,
            guildId = guildId,
            startDate = startDate,
            endDate = endDate,
            instance = instance,
            encounter = encounter,
        )

        // Then
        querySlot.captured.raiderId shouldBe raiderId
        querySlot.captured.guildId shouldBe guildId
        querySlot.captured.instance shouldBe instance
        querySlot.captured.encounter shouldBe encounter

        response.instance shouldBe instance
        response.encounter shouldBe encounter
        response.stats.attendancePercentage shouldBe 0.75
    }

    @Test
    fun `getAttendanceReport should return perfect attendance stats`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        val stats = AttendanceStats.calculate(attendedRaids = 10, totalRaids = 10)
        val report = AttendanceReport(
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            startDate = startDate,
            endDate = endDate,
            instance = null,
            encounter = null,
            stats = stats,
        )

        every { getAttendanceReportUseCase.execute(any()) } returns Result.success(report)

        // When
        val response = controller.getAttendanceReport(
            raiderId = raiderId,
            guildId = guildId,
            startDate = startDate,
            endDate = endDate,
            instance = null,
            encounter = null,
        )

        // Then
        response.stats.attendancePercentage shouldBe 1.0
        response.stats.totalRaids shouldBe 10
        response.stats.attendedRaids shouldBe 10
        response.stats.missedRaids shouldBe 0
    }

    @Test
    fun `getAttendanceReport should return zero attendance stats`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        val stats = AttendanceStats.calculate(attendedRaids = 0, totalRaids = 10)
        val report = AttendanceReport(
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            startDate = startDate,
            endDate = endDate,
            instance = null,
            encounter = null,
            stats = stats,
        )

        every { getAttendanceReportUseCase.execute(any()) } returns Result.success(report)

        // When
        val response = controller.getAttendanceReport(
            raiderId = raiderId,
            guildId = guildId,
            startDate = startDate,
            endDate = endDate,
            instance = null,
            encounter = null,
        )

        // Then
        response.stats.attendancePercentage shouldBe 0.0
        response.stats.totalRaids shouldBe 10
        response.stats.attendedRaids shouldBe 0
        response.stats.missedRaids shouldBe 10
    }

    @Test
    fun `getAttendanceReport should throw exception when use case fails`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        every { getAttendanceReportUseCase.execute(any()) } returns Result.failure(
            IllegalArgumentException("Cannot query encounter attendance without specifying an instance")
        )

        // When/Then
        try {
            controller.getAttendanceReport(
                raiderId = raiderId,
                guildId = guildId,
                startDate = startDate,
                endDate = endDate,
                instance = null,
                encounter = "SomeEncounter",
            )
            throw AssertionError("Expected exception was not thrown")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "Cannot query encounter attendance without specifying an instance"
        }
    }
}
