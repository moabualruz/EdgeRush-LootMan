package com.edgerush.lootman.api.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.attendance.AttendanceReport
import com.edgerush.lootman.application.attendance.DeleteAttendanceUseCase
import com.edgerush.lootman.application.attendance.GetAttendanceRecordUseCase
import com.edgerush.lootman.application.attendance.GetAttendanceReportQuery
import com.edgerush.lootman.application.attendance.GetAttendanceReportUseCase
import com.edgerush.lootman.application.attendance.GetGuildAttendanceSummaryUseCase
import com.edgerush.lootman.application.attendance.ListRaiderAttendanceUseCase
import com.edgerush.lootman.application.attendance.TrackAttendanceCommand
import com.edgerush.lootman.application.attendance.TrackAttendanceUseCase
import com.edgerush.lootman.application.attendance.UpdateAttendanceUseCase
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
    private lateinit var getAttendanceRecordUseCase: GetAttendanceRecordUseCase
    private lateinit var updateAttendanceUseCase: UpdateAttendanceUseCase
    private lateinit var deleteAttendanceUseCase: DeleteAttendanceUseCase
    private lateinit var listRaiderAttendanceUseCase: ListRaiderAttendanceUseCase
    private lateinit var getGuildAttendanceSummaryUseCase: GetGuildAttendanceSummaryUseCase
    private lateinit var controller: AttendanceController

    @BeforeEach
    fun setup() {
        trackAttendanceUseCase = mockk()
        getAttendanceReportUseCase = mockk()
        getAttendanceRecordUseCase = mockk()
        updateAttendanceUseCase = mockk()
        deleteAttendanceUseCase = mockk()
        listRaiderAttendanceUseCase = mockk()
        getGuildAttendanceSummaryUseCase = mockk()
        controller = AttendanceController(
            trackAttendanceUseCase,
            getAttendanceReportUseCase,
            getAttendanceRecordUseCase,
            updateAttendanceUseCase,
            deleteAttendanceUseCase,
            listRaiderAttendanceUseCase,
            getGuildAttendanceSummaryUseCase,
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

    @Test
    fun `getAttendanceRecord should return record when found`() {
        // Given
        val recordId = "record-123"
        val record = AttendanceRecord.create(
            raiderId = RaiderId(123L),
            guildId = GuildId("guild-456"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 8,
            totalRaids = 10,
        )

        every { getAttendanceRecordUseCase.execute(any()) } returns Result.success(record)

        // When
        val response = controller.getAttendanceRecord(recordId)

        // Then
        response.raiderId shouldBe 123L
        response.guildId shouldBe "guild-456"
        response.instance shouldBe "Nerub-ar Palace"
        response.attendedRaids shouldBe 8
        response.totalRaids shouldBe 10

        verify(exactly = 1) { getAttendanceRecordUseCase.execute(any()) }
    }

    @Test
    fun `getAttendanceRecord should throw exception when not found`() {
        // Given
        val recordId = "non-existent-record"

        every { getAttendanceRecordUseCase.execute(any()) } returns Result.failure(
            NoSuchElementException("Attendance record not found: $recordId")
        )

        // When/Then
        try {
            controller.getAttendanceRecord(recordId)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: NoSuchElementException) {
            e.message shouldBe "Attendance record not found: $recordId"
        }
    }

    @Test
    fun `updateAttendanceRecord should return updated record`() {
        // Given
        val recordId = "record-123"
        val request = UpdateAttendanceRequest(
            instance = "Updated Instance",
            attendedRaids = 9,
            totalRaids = 10,
        )

        val updatedRecord = AttendanceRecord.create(
            raiderId = RaiderId(123L),
            guildId = GuildId("guild-456"),
            instance = "Updated Instance",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 9,
            totalRaids = 10,
        )

        every { updateAttendanceUseCase.execute(any()) } returns Result.success(updatedRecord)

        // When
        val response = controller.updateAttendanceRecord(recordId, request)

        // Then
        response.instance shouldBe "Updated Instance"
        response.attendedRaids shouldBe 9
        response.attendancePercentage shouldBe 0.9

        verify(exactly = 1) { updateAttendanceUseCase.execute(any()) }
    }

    @Test
    fun `updateAttendanceRecord should throw exception when not found`() {
        // Given
        val recordId = "non-existent-record"
        val request = UpdateAttendanceRequest(instance = "Updated Instance")

        every { updateAttendanceUseCase.execute(any()) } returns Result.failure(
            NoSuchElementException("Attendance record not found: $recordId")
        )

        // When/Then
        try {
            controller.updateAttendanceRecord(recordId, request)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: NoSuchElementException) {
            e.message shouldBe "Attendance record not found: $recordId"
        }
    }

    @Test
    fun `deleteAttendanceRecord should return NO_CONTENT when successful`() {
        // Given
        val recordId = "record-123"

        every { deleteAttendanceUseCase.execute(any()) } returns Result.success(Unit)

        // When
        val response = controller.deleteAttendanceRecord(recordId)

        // Then
        response.statusCode shouldBe HttpStatus.NO_CONTENT

        verify(exactly = 1) { deleteAttendanceUseCase.execute(any()) }
    }

    @Test
    fun `deleteAttendanceRecord should throw exception when not found`() {
        // Given
        val recordId = "non-existent-record"

        every { deleteAttendanceUseCase.execute(any()) } returns Result.failure(
            NoSuchElementException("Attendance record not found: $recordId")
        )

        // When/Then
        try {
            controller.deleteAttendanceRecord(recordId)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: NoSuchElementException) {
            e.message shouldBe "Attendance record not found: $recordId"
        }
    }

    @Test
    fun `getRaiderAttendanceHistory should return history with records`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        val records = listOf(
            AttendanceRecord.create(
                raiderId = RaiderId(raiderId),
                guildId = GuildId(guildId),
                instance = "Instance 1",
                encounter = null,
                startDate = LocalDate.of(2024, 1, 1),
                endDate = LocalDate.of(2024, 1, 15),
                attendedRaids = 8,
                totalRaids = 10,
            ),
            AttendanceRecord.create(
                raiderId = RaiderId(raiderId),
                guildId = GuildId(guildId),
                instance = "Instance 2",
                encounter = null,
                startDate = LocalDate.of(2024, 1, 16),
                endDate = LocalDate.of(2024, 1, 31),
                attendedRaids = 9,
                totalRaids = 10,
            ),
        )

        every { listRaiderAttendanceUseCase.execute(any()) } returns Result.success(records)

        // When
        val response = controller.getRaiderAttendanceHistory(raiderId, guildId, startDate, endDate)

        // Then
        response.raiderId shouldBe raiderId
        response.guildId shouldBe guildId
        response.startDate shouldBe startDate
        response.endDate shouldBe endDate
        response.totalRecords shouldBe 2
        response.records.size shouldBe 2

        verify(exactly = 1) { listRaiderAttendanceUseCase.execute(any()) }
    }

    @Test
    fun `getRaiderAttendanceHistory should return empty history`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        every { listRaiderAttendanceUseCase.execute(any()) } returns Result.success(emptyList())

        // When
        val response = controller.getRaiderAttendanceHistory(raiderId, guildId, startDate, endDate)

        // Then
        response.totalRecords shouldBe 0
        response.records shouldBe emptyList()
    }

    @Test
    fun `getRaiderAttendanceHistory should throw exception when use case fails`() {
        // Given
        val raiderId = 123L
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        every { listRaiderAttendanceUseCase.execute(any()) } returns Result.failure(
            IllegalArgumentException("Invalid date range")
        )

        // When/Then
        try {
            controller.getRaiderAttendanceHistory(raiderId, guildId, startDate, endDate)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "Invalid date range"
        }
    }

    @Test
    fun `getGuildAttendanceSummary should return summary with raider summaries`() {
        // Given
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        val summary = com.edgerush.lootman.application.attendance.GuildAttendanceSummary(
            guildId = guildId,
            startDate = startDate,
            endDate = endDate,
            totalRecords = 10,
            uniqueRaiders = 5,
            overallAttendancePercentage = 0.85,
            raiderSummaries = listOf(
                com.edgerush.lootman.application.attendance.RaiderAttendanceSummary(
                    raiderId = 123L,
                    totalRecords = 2,
                    totalAttendedRaids = 18,
                    totalRaids = 20,
                    averageAttendancePercentage = 0.9,
                ),
                com.edgerush.lootman.application.attendance.RaiderAttendanceSummary(
                    raiderId = 456L,
                    totalRecords = 2,
                    totalAttendedRaids = 16,
                    totalRaids = 20,
                    averageAttendancePercentage = 0.8,
                ),
            ),
        )

        every { getGuildAttendanceSummaryUseCase.execute(any()) } returns Result.success(summary)

        // When
        val response = controller.getGuildAttendanceSummary(guildId, startDate, endDate)

        // Then
        response.guildId shouldBe guildId
        response.startDate shouldBe startDate
        response.endDate shouldBe endDate
        response.totalRecords shouldBe 10
        response.uniqueRaiders shouldBe 5
        response.overallAttendancePercentage shouldBe 0.85
        response.raiderSummaries.size shouldBe 2

        verify(exactly = 1) { getGuildAttendanceSummaryUseCase.execute(any()) }
    }

    @Test
    fun `getGuildAttendanceSummary should return empty summary`() {
        // Given
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        val summary = com.edgerush.lootman.application.attendance.GuildAttendanceSummary(
            guildId = guildId,
            startDate = startDate,
            endDate = endDate,
            totalRecords = 0,
            uniqueRaiders = 0,
            overallAttendancePercentage = 0.0,
            raiderSummaries = emptyList(),
        )

        every { getGuildAttendanceSummaryUseCase.execute(any()) } returns Result.success(summary)

        // When
        val response = controller.getGuildAttendanceSummary(guildId, startDate, endDate)

        // Then
        response.totalRecords shouldBe 0
        response.uniqueRaiders shouldBe 0
        response.overallAttendancePercentage shouldBe 0.0
        response.raiderSummaries shouldBe emptyList()
    }

    @Test
    fun `getGuildAttendanceSummary should throw exception when use case fails`() {
        // Given
        val guildId = "guild-456"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)

        every { getGuildAttendanceSummaryUseCase.execute(any()) } returns Result.failure(
            IllegalArgumentException("Invalid date range")
        )

        // When/Then
        try {
            controller.getGuildAttendanceSummary(guildId, startDate, endDate)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: IllegalArgumentException) {
            e.message shouldBe "Invalid date range"
        }
    }
}
