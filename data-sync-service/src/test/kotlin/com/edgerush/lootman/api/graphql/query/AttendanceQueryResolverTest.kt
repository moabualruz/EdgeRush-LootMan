package com.edgerush.lootman.api.graphql.query

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.attendance.GetGuildAttendanceSummaryQuery
import com.edgerush.lootman.application.attendance.GetGuildAttendanceSummaryUseCase
import com.edgerush.lootman.application.attendance.GuildAttendanceSummary
import com.edgerush.lootman.application.attendance.ListRaiderAttendanceQuery
import com.edgerush.lootman.application.attendance.ListRaiderAttendanceUseCase
import com.edgerush.lootman.application.attendance.RaiderAttendanceSummary
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Unit tests for AttendanceQueryResolver.
 *
 * Tests the GraphQL query resolver for attendance operations following TDD principles.
 */
class AttendanceQueryResolverTest : UnitTest() {
    @MockK
    private lateinit var listRaiderAttendanceUseCase: ListRaiderAttendanceUseCase

    @MockK
    private lateinit var getGuildAttendanceSummaryUseCase: GetGuildAttendanceSummaryUseCase

    @InjectMockKs
    private lateinit var resolver: AttendanceQueryResolver

    @Nested
    inner class RaiderAttendanceQuery {
        @Test
        fun `should return attendance records for raider`() {
            // Arrange
            val records =
                listOf(
                    createTestAttendanceRecord(raiderId = 42L, instance = "Nerub-ar Palace", attendedRaids = 8, totalRaids = 10),
                    createTestAttendanceRecord(raiderId = 42L, instance = "Vault of the Incarnates", attendedRaids = 12, totalRaids = 12),
                )
            val querySlot = slot<ListRaiderAttendanceQuery>()
            every { listRaiderAttendanceUseCase.execute(capture(querySlot)) } returns Result.success(records)

            // Act
            val result =
                resolver.raiderAttendance(
                    raiderId = "42",
                    guildId = "guild-123",
                    startDate = "2025-01-01",
                    endDate = "2025-12-31",
                )

            // Assert
            result shouldHaveSize 2
            result[0].instance shouldBe "Nerub-ar Palace"
            result[0].attendancePercentage shouldBe (0.8 plusOrMinus 0.001)
            result[1].instance shouldBe "Vault of the Incarnates"
            result[1].attendancePercentage shouldBe (1.0 plusOrMinus 0.001)
            querySlot.captured.raiderId shouldBe 42L
            querySlot.captured.guildId shouldBe "guild-123"
        }

        @Test
        fun `should return empty list when no attendance records exist`() {
            // Arrange
            every { listRaiderAttendanceUseCase.execute(any()) } returns Result.success(emptyList())

            // Act
            val result =
                resolver.raiderAttendance(
                    raiderId = "999",
                    guildId = "guild-123",
                    startDate = "2025-01-01",
                    endDate = "2025-12-31",
                )

            // Assert
            result shouldHaveSize 0
        }

        @Test
        fun `should propagate exception on error`() {
            // Arrange
            every { listRaiderAttendanceUseCase.execute(any()) } returns
                Result.failure(RuntimeException("Database connection failed"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.raiderAttendance(
                        raiderId = "42",
                        guildId = "guild-123",
                        startDate = "2025-01-01",
                        endDate = "2025-12-31",
                    )
                }
            exception.message shouldBe "Database connection failed"
        }

        @Test
        fun `should correctly convert all attendance record fields`() {
            // Arrange
            val startDate = LocalDate.of(2025, 1, 1)
            val endDate = LocalDate.of(2025, 3, 31)
            val record =
                createTestAttendanceRecord(
                    raiderId = 42L,
                    guildId = "guild-123",
                    instance = "Nerub-ar Palace",
                    encounter = "Queen Ansurek",
                    startDate = startDate,
                    endDate = endDate,
                    attendedRaids = 15,
                    totalRaids = 20,
                )
            every { listRaiderAttendanceUseCase.execute(any()) } returns Result.success(listOf(record))

            // Act
            val result =
                resolver.raiderAttendance(
                    raiderId = "42",
                    guildId = "guild-123",
                    startDate = "2025-01-01",
                    endDate = "2025-03-31",
                )

            // Assert
            result shouldHaveSize 1
            result[0].raiderId shouldBe "42"
            result[0].guildId shouldBe "guild-123"
            result[0].instance shouldBe "Nerub-ar Palace"
            result[0].encounter shouldBe "Queen Ansurek"
            result[0].startDate shouldBe startDate
            result[0].endDate shouldBe endDate
            result[0].attendedRaids shouldBe 15
            result[0].totalRaids shouldBe 20
            result[0].attendancePercentage shouldBe (0.75 plusOrMinus 0.001)
        }
    }

    @Nested
    inner class GuildAttendanceSummaryQuery {
        @Test
        fun `should return guild attendance summary`() {
            // Arrange
            val summary =
                createTestGuildSummary(
                    guildId = "guild-123",
                    uniqueRaiders = 25,
                    overallAttendancePercentage = 0.85,
                )
            val querySlot = slot<GetGuildAttendanceSummaryQuery>()
            every { getGuildAttendanceSummaryUseCase.execute(capture(querySlot)) } returns Result.success(summary)

            // Act
            val result =
                resolver.guildAttendanceSummary(
                    guildId = "guild-123",
                    startDate = "2025-01-01",
                    endDate = "2025-12-31",
                )

            // Assert
            result.guildId shouldBe "guild-123"
            result.uniqueRaiders shouldBe 25
            result.overallAttendancePercentage shouldBe (0.85 plusOrMinus 0.001)
            querySlot.captured.guildId shouldBe "guild-123"
        }

        @Test
        fun `should include raider summaries in guild summary`() {
            // Arrange
            val raiderSummaries =
                listOf(
                    RaiderAttendanceSummary(
                        raiderId = 1L,
                        totalRecords = 5,
                        totalAttendedRaids = 45,
                        totalRaids = 50,
                        averageAttendancePercentage = 0.90,
                    ),
                    RaiderAttendanceSummary(
                        raiderId = 2L,
                        totalRecords = 5,
                        totalAttendedRaids = 40,
                        totalRaids = 50,
                        averageAttendancePercentage = 0.80,
                    ),
                )
            val summary =
                GuildAttendanceSummary(
                    guildId = "guild-123",
                    startDate = LocalDate.of(2025, 1, 1),
                    endDate = LocalDate.of(2025, 12, 31),
                    totalRecords = 10,
                    uniqueRaiders = 2,
                    overallAttendancePercentage = 0.85,
                    raiderSummaries = raiderSummaries,
                )
            every { getGuildAttendanceSummaryUseCase.execute(any()) } returns Result.success(summary)

            // Act
            val result =
                resolver.guildAttendanceSummary(
                    guildId = "guild-123",
                    startDate = "2025-01-01",
                    endDate = "2025-12-31",
                )

            // Assert
            result.raiderSummaries shouldHaveSize 2
            result.raiderSummaries[0].raiderId shouldBe "1"
            result.raiderSummaries[0].averageAttendancePercentage shouldBe (0.90 plusOrMinus 0.001)
            result.raiderSummaries[1].raiderId shouldBe "2"
            result.raiderSummaries[1].averageAttendancePercentage shouldBe (0.80 plusOrMinus 0.001)
        }

        @Test
        fun `should propagate exception on error`() {
            // Arrange
            every { getGuildAttendanceSummaryUseCase.execute(any()) } returns
                Result.failure(RuntimeException("Database error"))

            // Act & Assert
            val exception =
                org.junit.jupiter.api.assertThrows<RuntimeException> {
                    resolver.guildAttendanceSummary(
                        guildId = "guild-123",
                        startDate = "2025-01-01",
                        endDate = "2025-12-31",
                    )
                }
            exception.message shouldBe "Database error"
        }

        @Test
        fun `should handle empty guild with no raiders`() {
            // Arrange
            val summary =
                GuildAttendanceSummary(
                    guildId = "empty-guild",
                    startDate = LocalDate.of(2025, 1, 1),
                    endDate = LocalDate.of(2025, 12, 31),
                    totalRecords = 0,
                    uniqueRaiders = 0,
                    overallAttendancePercentage = 0.0,
                    raiderSummaries = emptyList(),
                )
            every { getGuildAttendanceSummaryUseCase.execute(any()) } returns Result.success(summary)

            // Act
            val result =
                resolver.guildAttendanceSummary(
                    guildId = "empty-guild",
                    startDate = "2025-01-01",
                    endDate = "2025-12-31",
                )

            // Assert
            result.uniqueRaiders shouldBe 0
            result.raiderSummaries shouldHaveSize 0
            result.overallAttendancePercentage shouldBe 0.0
        }
    }

    // Helper functions

    private fun createTestAttendanceRecord(
        raiderId: Long = 1L,
        guildId: String = "test-guild",
        instance: String = "Test Instance",
        encounter: String? = null,
        startDate: LocalDate = LocalDate.of(2025, 1, 1),
        endDate: LocalDate = LocalDate.of(2025, 3, 31),
        attendedRaids: Int = 10,
        totalRaids: Int = 12,
    ): AttendanceRecord =
        AttendanceRecord.create(
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            instance = instance,
            encounter = encounter,
            startDate = startDate,
            endDate = endDate,
            attendedRaids = attendedRaids,
            totalRaids = totalRaids,
        )

    private fun createTestGuildSummary(
        guildId: String = "test-guild",
        uniqueRaiders: Int = 20,
        overallAttendancePercentage: Double = 0.80,
    ): GuildAttendanceSummary =
        GuildAttendanceSummary(
            guildId = guildId,
            startDate = LocalDate.of(2025, 1, 1),
            endDate = LocalDate.of(2025, 12, 31),
            totalRecords = 100,
            uniqueRaiders = uniqueRaiders,
            overallAttendancePercentage = overallAttendancePercentage,
            raiderSummaries = emptyList(),
        )
}
