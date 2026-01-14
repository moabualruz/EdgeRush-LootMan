package com.edgerush.lootman.api.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.attendance.AttendanceReport
import com.edgerush.lootman.application.attendance.GuildAttendanceSummary
import com.edgerush.lootman.application.attendance.RaiderAttendanceSummary
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceStats
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

/**
 * Unit tests for attendance DTOs.
 *
 * Tests DTO construction, mapping from domain models, and data integrity.
 */
class AttendanceDtoTest : UnitTest() {

    @Nested
    inner class TrackAttendanceRequestTest {

        @Test
        fun `should create request with all fields`() {
            val request = TrackAttendanceRequest(
                raiderId = 123L,
                guildId = "guild-456",
                instance = "Nerub-ar Palace",
                encounter = "Queen Ansurek",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10
            )

            request.raiderId shouldBe 123L
            request.guildId shouldBe "guild-456"
            request.instance shouldBe "Nerub-ar Palace"
            request.encounter shouldBe "Queen Ansurek"
            request.startDate shouldBe LocalDate.of(2024, 11, 1)
            request.endDate shouldBe LocalDate.of(2024, 11, 14)
            request.attendedRaids shouldBe 8
            request.totalRaids shouldBe 10
        }

        @Test
        fun `should create request with null encounter`() {
            val request = TrackAttendanceRequest(
                raiderId = 123L,
                guildId = "guild-456",
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10
            )

            request.encounter shouldBe null
        }

        @Test
        fun `should support equality`() {
            val request1 = TrackAttendanceRequest(
                raiderId = 123L,
                guildId = "guild-456",
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10
            )

            val request2 = TrackAttendanceRequest(
                raiderId = 123L,
                guildId = "guild-456",
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10
            )

            request1 shouldBe request2
        }
    }

    @Nested
    inner class TrackAttendanceResponseTest {

        @Test
        fun `should create from attendance record`() {
            val record = AttendanceRecord.create(
                raiderId = RaiderId(123L),
                guildId = GuildId("guild-456"),
                instance = "Nerub-ar Palace",
                encounter = "Queen Ansurek",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10
            )

            val response = TrackAttendanceResponse.from(record)

            response.recordId shouldBe record.id.value
            response.raiderId shouldBe 123L
            response.guildId shouldBe "guild-456"
            response.instance shouldBe "Nerub-ar Palace"
            response.encounter shouldBe "Queen Ansurek"
            response.startDate shouldBe LocalDate.of(2024, 11, 1)
            response.endDate shouldBe LocalDate.of(2024, 11, 14)
            response.attendedRaids shouldBe 8
            response.totalRaids shouldBe 10
            response.attendancePercentage shouldBe 0.8
            response.recordedAt shouldNotBe null
        }

        @Test
        fun `should create from record without encounter`() {
            val record = AttendanceRecord.create(
                raiderId = RaiderId(123L),
                guildId = GuildId("guild-456"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 10,
                totalRaids = 10
            )

            val response = TrackAttendanceResponse.from(record)

            response.encounter shouldBe null
            response.attendancePercentage shouldBe 1.0
        }

        @Test
        fun `should handle zero attendance`() {
            val record = AttendanceRecord.create(
                raiderId = RaiderId(123L),
                guildId = GuildId("guild-456"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 0,
                totalRaids = 10
            )

            val response = TrackAttendanceResponse.from(record)

            response.attendancePercentage shouldBe 0.0
        }
    }

    @Nested
    inner class UpdateAttendanceRequestTest {

        @Test
        fun `should create request with all fields`() {
            val request = UpdateAttendanceRequest(
                instance = "Updated Instance",
                encounter = "Updated Encounter",
                startDate = LocalDate.of(2024, 12, 1),
                endDate = LocalDate.of(2024, 12, 14),
                attendedRaids = 9,
                totalRaids = 10
            )

            request.instance shouldBe "Updated Instance"
            request.encounter shouldBe "Updated Encounter"
            request.startDate shouldBe LocalDate.of(2024, 12, 1)
            request.endDate shouldBe LocalDate.of(2024, 12, 14)
            request.attendedRaids shouldBe 9
            request.totalRaids shouldBe 10
        }

        @Test
        fun `should create request with null fields`() {
            val request = UpdateAttendanceRequest()

            request.instance shouldBe null
            request.encounter shouldBe null
            request.startDate shouldBe null
            request.endDate shouldBe null
            request.attendedRaids shouldBe null
            request.totalRaids shouldBe null
        }

        @Test
        fun `should create request with partial fields`() {
            val request = UpdateAttendanceRequest(
                instance = "Updated Instance",
                attendedRaids = 8
            )

            request.instance shouldBe "Updated Instance"
            request.attendedRaids shouldBe 8
            request.encounter shouldBe null
            request.startDate shouldBe null
            request.endDate shouldBe null
            request.totalRaids shouldBe null
        }

        @Test
        fun `should support equality`() {
            val request1 = UpdateAttendanceRequest(
                instance = "Instance",
                attendedRaids = 5
            )
            val request2 = UpdateAttendanceRequest(
                instance = "Instance",
                attendedRaids = 5
            )

            request1 shouldBe request2
        }

        @Test
        fun `should support copy`() {
            val request = UpdateAttendanceRequest(
                instance = "Instance",
                attendedRaids = 5
            )

            val copied = request.copy(totalRaids = 10)

            copied.instance shouldBe "Instance"
            copied.attendedRaids shouldBe 5
            copied.totalRaids shouldBe 10
        }
    }

    @Nested
    inner class AttendanceReportResponseTest {

        @Test
        fun `should create from attendance report`() {
            val stats = AttendanceStats.calculate(attendedRaids = 8, totalRaids = 10)
            val report = AttendanceReport(
                raiderId = RaiderId(123L),
                guildId = GuildId("guild-456"),
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                instance = "Nerub-ar Palace",
                encounter = "Queen Ansurek",
                stats = stats
            )

            val response = AttendanceReportResponse.from(report)

            response.raiderId shouldBe 123L
            response.guildId shouldBe "guild-456"
            response.startDate shouldBe LocalDate.of(2024, 11, 1)
            response.endDate shouldBe LocalDate.of(2024, 11, 14)
            response.instance shouldBe "Nerub-ar Palace"
            response.encounter shouldBe "Queen Ansurek"
            response.stats.attendancePercentage shouldBe 0.8
            response.stats.totalRaids shouldBe 10
            response.stats.attendedRaids shouldBe 8
            response.stats.missedRaids shouldBe 2
        }

        @Test
        fun `should create from report without instance or encounter`() {
            val stats = AttendanceStats.calculate(attendedRaids = 10, totalRaids = 10)
            val report = AttendanceReport(
                raiderId = RaiderId(123L),
                guildId = GuildId("guild-456"),
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                instance = null,
                encounter = null,
                stats = stats
            )

            val response = AttendanceReportResponse.from(report)

            response.instance shouldBe null
            response.encounter shouldBe null
            response.stats.attendancePercentage shouldBe 1.0
        }
    }

    @Nested
    inner class AttendanceStatsDtoTest {

        @Test
        fun `should create from attendance stats`() {
            val stats = AttendanceStats.calculate(attendedRaids = 7, totalRaids = 10)
            val dto = AttendanceStatsDto.from(stats)

            dto.attendancePercentage shouldBe 0.7
            dto.totalRaids shouldBe 10
            dto.attendedRaids shouldBe 7
            dto.missedRaids shouldBe 3
        }

        @Test
        fun `should handle perfect attendance`() {
            val stats = AttendanceStats.calculate(attendedRaids = 10, totalRaids = 10)
            val dto = AttendanceStatsDto.from(stats)

            dto.attendancePercentage shouldBe 1.0
            dto.missedRaids shouldBe 0
        }

        @Test
        fun `should handle zero attendance`() {
            val stats = AttendanceStats.calculate(attendedRaids = 0, totalRaids = 10)
            val dto = AttendanceStatsDto.from(stats)

            dto.attendancePercentage shouldBe 0.0
            dto.missedRaids shouldBe 10
        }
    }

    @Nested
    inner class RaiderAttendanceHistoryResponseTest {

        @Test
        fun `should create response with records`() {
            val records = listOf(
                TrackAttendanceResponse(
                    recordId = "record-1",
                    raiderId = 123L,
                    guildId = "guild-456",
                    instance = "Instance 1",
                    encounter = null,
                    startDate = LocalDate.of(2024, 11, 1),
                    endDate = LocalDate.of(2024, 11, 7),
                    attendedRaids = 8,
                    totalRaids = 10,
                    attendancePercentage = 0.8,
                    recordedAt = Instant.now()
                ),
                TrackAttendanceResponse(
                    recordId = "record-2",
                    raiderId = 123L,
                    guildId = "guild-456",
                    instance = "Instance 2",
                    encounter = null,
                    startDate = LocalDate.of(2024, 11, 8),
                    endDate = LocalDate.of(2024, 11, 14),
                    attendedRaids = 9,
                    totalRaids = 10,
                    attendancePercentage = 0.9,
                    recordedAt = Instant.now()
                )
            )

            val response = RaiderAttendanceHistoryResponse(
                raiderId = 123L,
                guildId = "guild-456",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                records = records,
                totalRecords = 2
            )

            response.raiderId shouldBe 123L
            response.guildId shouldBe "guild-456"
            response.startDate shouldBe LocalDate.of(2024, 11, 1)
            response.endDate shouldBe LocalDate.of(2024, 11, 14)
            response.records.size shouldBe 2
            response.totalRecords shouldBe 2
        }

        @Test
        fun `should create empty response`() {
            val response = RaiderAttendanceHistoryResponse(
                raiderId = 123L,
                guildId = "guild-456",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                records = emptyList(),
                totalRecords = 0
            )

            response.records shouldBe emptyList()
            response.totalRecords shouldBe 0
        }

        @Test
        fun `should support equality`() {
            val response1 = RaiderAttendanceHistoryResponse(
                raiderId = 123L,
                guildId = "guild-456",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                records = emptyList(),
                totalRecords = 0
            )
            val response2 = RaiderAttendanceHistoryResponse(
                raiderId = 123L,
                guildId = "guild-456",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                records = emptyList(),
                totalRecords = 0
            )

            response1 shouldBe response2
        }
    }

    @Nested
    inner class GuildAttendanceSummaryResponseTest {

        @Test
        fun `should create from guild attendance summary`() {
            val raiderSummaries = listOf(
                RaiderAttendanceSummary(
                    raiderId = 123L,
                    totalRecords = 5,
                    totalAttendedRaids = 40,
                    totalRaids = 50,
                    averageAttendancePercentage = 0.8
                ),
                RaiderAttendanceSummary(
                    raiderId = 456L,
                    totalRecords = 5,
                    totalAttendedRaids = 45,
                    totalRaids = 50,
                    averageAttendancePercentage = 0.9
                )
            )

            val summary = GuildAttendanceSummary(
                guildId = "guild-789",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 30),
                totalRecords = 10,
                uniqueRaiders = 2,
                overallAttendancePercentage = 0.85,
                raiderSummaries = raiderSummaries
            )

            val response = GuildAttendanceSummaryResponse.from(summary)

            response.guildId shouldBe "guild-789"
            response.startDate shouldBe LocalDate.of(2024, 11, 1)
            response.endDate shouldBe LocalDate.of(2024, 11, 30)
            response.totalRecords shouldBe 10
            response.uniqueRaiders shouldBe 2
            response.overallAttendancePercentage shouldBe 0.85
            response.raiderSummaries.size shouldBe 2
        }

        @Test
        fun `should create empty response`() {
            val summary = GuildAttendanceSummary(
                guildId = "guild-789",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 30),
                totalRecords = 0,
                uniqueRaiders = 0,
                overallAttendancePercentage = 0.0,
                raiderSummaries = emptyList()
            )

            val response = GuildAttendanceSummaryResponse.from(summary)

            response.totalRecords shouldBe 0
            response.uniqueRaiders shouldBe 0
            response.overallAttendancePercentage shouldBe 0.0
            response.raiderSummaries shouldBe emptyList()
        }

        @Test
        fun `should support data class operations`() {
            val response = GuildAttendanceSummaryResponse(
                guildId = "guild-789",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 30),
                totalRecords = 10,
                uniqueRaiders = 2,
                overallAttendancePercentage = 0.85,
                raiderSummaries = emptyList()
            )

            response.guildId shouldBe "guild-789"
            response.totalRecords shouldBe 10
            response.uniqueRaiders shouldBe 2
        }
    }

    @Nested
    inner class RaiderAttendanceSummaryResponseTest {

        @Test
        fun `should create from raider attendance summary`() {
            val summary = RaiderAttendanceSummary(
                raiderId = 123L,
                totalRecords = 5,
                totalAttendedRaids = 40,
                totalRaids = 50,
                averageAttendancePercentage = 0.8
            )

            val response = RaiderAttendanceSummaryResponse.from(summary)

            response.raiderId shouldBe 123L
            response.totalRecords shouldBe 5
            response.totalAttendedRaids shouldBe 40
            response.totalRaids shouldBe 50
            response.averageAttendancePercentage shouldBe 0.8
        }

        @Test
        fun `should handle perfect attendance`() {
            val summary = RaiderAttendanceSummary(
                raiderId = 123L,
                totalRecords = 10,
                totalAttendedRaids = 100,
                totalRaids = 100,
                averageAttendancePercentage = 1.0
            )

            val response = RaiderAttendanceSummaryResponse.from(summary)

            response.averageAttendancePercentage shouldBe 1.0
            response.totalAttendedRaids shouldBe response.totalRaids
        }

        @Test
        fun `should handle zero attendance`() {
            val summary = RaiderAttendanceSummary(
                raiderId = 123L,
                totalRecords = 5,
                totalAttendedRaids = 0,
                totalRaids = 50,
                averageAttendancePercentage = 0.0
            )

            val response = RaiderAttendanceSummaryResponse.from(summary)

            response.totalAttendedRaids shouldBe 0
            response.averageAttendancePercentage shouldBe 0.0
        }

        @Test
        fun `should support equality`() {
            val response1 = RaiderAttendanceSummaryResponse(
                raiderId = 123L,
                totalRecords = 5,
                totalAttendedRaids = 40,
                totalRaids = 50,
                averageAttendancePercentage = 0.8
            )
            val response2 = RaiderAttendanceSummaryResponse(
                raiderId = 123L,
                totalRecords = 5,
                totalAttendedRaids = 40,
                totalRaids = 50,
                averageAttendancePercentage = 0.8
            )

            response1 shouldBe response2
        }
    }
}
