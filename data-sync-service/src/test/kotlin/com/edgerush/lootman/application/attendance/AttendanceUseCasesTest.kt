package com.edgerush.lootman.application.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Unit tests for additional Attendance use cases.
 *
 * Tests verify:
 * - GetAttendanceRecordUseCase
 * - UpdateAttendanceUseCase
 * - DeleteAttendanceUseCase
 * - ListRaiderAttendanceUseCase
 * - GetGuildAttendanceSummaryUseCase
 */
class AttendanceUseCasesTest : UnitTest() {

    private lateinit var attendanceRepository: AttendanceRepository

    @BeforeEach
    fun setUp() {
        attendanceRepository = mockk(relaxed = true)
    }

    @Nested
    inner class GetAttendanceRecordUseCaseTests {
        private lateinit var useCase: GetAttendanceRecordUseCase

        @BeforeEach
        fun setUp() {
            useCase = GetAttendanceRecordUseCase(attendanceRepository)
        }

        @Test
        fun `should return attendance record when found`() {
            // Given
            val recordId = "record-123"
            val record = createAttendanceRecord()
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns record

            // When
            val result = useCase.execute(GetAttendanceRecordQuery(recordId))

            // Then
            result.isSuccess shouldBe true
            result.getOrNull()?.id shouldBe record.id
        }

        @Test
        fun `should return failure when record not found`() {
            // Given
            val recordId = "non-existent"
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns null

            // When
            val result = useCase.execute(GetAttendanceRecordQuery(recordId))

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldNotBe null
            result.exceptionOrNull()?.message shouldBe "Attendance record not found: $recordId"
        }
    }

    @Nested
    inner class UpdateAttendanceUseCaseTests {
        private lateinit var useCase: UpdateAttendanceUseCase

        @BeforeEach
        fun setUp() {
            useCase = UpdateAttendanceUseCase(attendanceRepository)
        }

        @Test
        fun `should update attendance record successfully`() {
            // Given
            val recordId = "record-456"
            val existingRecord = createAttendanceRecord(recordId, instance = "OldInstance", attendedRaids = 5)
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns existingRecord
            every { attendanceRepository.delete(AttendanceRecordId(recordId)) } returns Unit

            val savedRecordSlot = slot<AttendanceRecord>()
            every { attendanceRepository.save(capture(savedRecordSlot)) } answers { savedRecordSlot.captured }

            val command = UpdateAttendanceCommand(
                recordId = recordId,
                instance = "NewInstance",
                attendedRaids = 10
            )

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            savedRecordSlot.captured.instance shouldBe "NewInstance"
            savedRecordSlot.captured.attendedRaids shouldBe 10
            verify { attendanceRepository.delete(AttendanceRecordId(recordId)) }
            verify { attendanceRepository.save(any()) }
        }

        @Test
        fun `should return failure when record not found for update`() {
            // Given
            val recordId = "non-existent"
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns null

            val command = UpdateAttendanceCommand(
                recordId = recordId,
                instance = "NewInstance"
            )

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldBe "Attendance record not found: $recordId"
        }

        @Test
        fun `should preserve unchanged fields`() {
            // Given
            val recordId = "record-789"
            val existingRecord = createAttendanceRecord(
                recordId,
                instance = "OldInstance",
                encounter = "OldEncounter",
                attendedRaids = 5,
                totalRaids = 10
            )
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns existingRecord
            every { attendanceRepository.delete(AttendanceRecordId(recordId)) } returns Unit

            val savedRecordSlot = slot<AttendanceRecord>()
            every { attendanceRepository.save(capture(savedRecordSlot)) } answers { savedRecordSlot.captured }

            // Only update instance
            val command = UpdateAttendanceCommand(
                recordId = recordId,
                instance = "NewInstance"
            )

            // When
            useCase.execute(command)

            // Then
            savedRecordSlot.captured.instance shouldBe "NewInstance"
            savedRecordSlot.captured.encounter shouldBe "OldEncounter"
            savedRecordSlot.captured.attendedRaids shouldBe 5
            savedRecordSlot.captured.totalRaids shouldBe 10
        }

        @Test
        fun `should update encounter when provided`() {
            // Given
            val recordId = "record-enc"
            val existingRecord = createAttendanceRecord(recordId, encounter = "OldEncounter")
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns existingRecord
            every { attendanceRepository.delete(AttendanceRecordId(recordId)) } returns Unit

            val savedRecordSlot = slot<AttendanceRecord>()
            every { attendanceRepository.save(capture(savedRecordSlot)) } answers { savedRecordSlot.captured }

            val command = UpdateAttendanceCommand(
                recordId = recordId,
                encounter = "NewEncounter"
            )

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            savedRecordSlot.captured.encounter shouldBe "NewEncounter"
        }

        @Test
        fun `should update startDate when provided`() {
            // Given - existing record has startDate=2024-01-01, endDate=2024-01-07
            // Update to an earlier startDate (allowed as long as startDate <= endDate)
            val recordId = "record-start"
            val existingRecord = createAttendanceRecord(recordId)
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns existingRecord
            every { attendanceRepository.delete(AttendanceRecordId(recordId)) } returns Unit

            val savedRecordSlot = slot<AttendanceRecord>()
            every { attendanceRepository.save(capture(savedRecordSlot)) } answers { savedRecordSlot.captured }

            val newStartDate = LocalDate.of(2023, 12, 1) // Earlier start date
            val command = UpdateAttendanceCommand(
                recordId = recordId,
                startDate = newStartDate
            )

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            savedRecordSlot.captured.startDate shouldBe newStartDate
        }

        @Test
        fun `should update endDate when provided`() {
            // Given
            val recordId = "record-end"
            val existingRecord = createAttendanceRecord(recordId)
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns existingRecord
            every { attendanceRepository.delete(AttendanceRecordId(recordId)) } returns Unit

            val savedRecordSlot = slot<AttendanceRecord>()
            every { attendanceRepository.save(capture(savedRecordSlot)) } answers { savedRecordSlot.captured }

            val newEndDate = LocalDate.of(2025, 12, 31)
            val command = UpdateAttendanceCommand(
                recordId = recordId,
                endDate = newEndDate
            )

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            savedRecordSlot.captured.endDate shouldBe newEndDate
        }

        @Test
        fun `should update totalRaids when provided`() {
            // Given
            val recordId = "record-total"
            val existingRecord = createAttendanceRecord(recordId, totalRaids = 10)
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns existingRecord
            every { attendanceRepository.delete(AttendanceRecordId(recordId)) } returns Unit

            val savedRecordSlot = slot<AttendanceRecord>()
            every { attendanceRepository.save(capture(savedRecordSlot)) } answers { savedRecordSlot.captured }

            val command = UpdateAttendanceCommand(
                recordId = recordId,
                totalRaids = 20
            )

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            savedRecordSlot.captured.totalRaids shouldBe 20
        }
    }

    @Nested
    inner class DeleteAttendanceUseCaseTests {
        private lateinit var useCase: DeleteAttendanceUseCase

        @BeforeEach
        fun setUp() {
            useCase = DeleteAttendanceUseCase(attendanceRepository)
        }

        @Test
        fun `should delete attendance record successfully`() {
            // Given
            val recordId = "record-to-delete"
            val record = createAttendanceRecord(recordId)
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns record
            every { attendanceRepository.delete(AttendanceRecordId(recordId)) } returns Unit

            // When
            val result = useCase.execute(DeleteAttendanceCommand(recordId))

            // Then
            result.isSuccess shouldBe true
            verify { attendanceRepository.delete(AttendanceRecordId(recordId)) }
        }

        @Test
        fun `should return failure when record not found for delete`() {
            // Given
            val recordId = "non-existent"
            every { attendanceRepository.findById(AttendanceRecordId(recordId)) } returns null

            // When
            val result = useCase.execute(DeleteAttendanceCommand(recordId))

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldBe "Attendance record not found: $recordId"
        }
    }

    @Nested
    inner class ListRaiderAttendanceUseCaseTests {
        private lateinit var useCase: ListRaiderAttendanceUseCase

        @BeforeEach
        fun setUp() {
            useCase = ListRaiderAttendanceUseCase(attendanceRepository)
        }

        @Test
        fun `should return attendance records for raider`() {
            // Given
            val raiderId = 123L
            val guildId = "test-guild"
            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 12, 31)
            val records = listOf(
                createAttendanceRecord("record-1", raiderId = raiderId),
                createAttendanceRecord("record-2", raiderId = raiderId)
            )

            every {
                attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                    RaiderId(raiderId),
                    GuildId(guildId),
                    startDate,
                    endDate
                )
            } returns records

            // When
            val result = useCase.execute(
                ListRaiderAttendanceQuery(raiderId, guildId, startDate, endDate)
            )

            // Then
            result.isSuccess shouldBe true
            result.getOrNull()?.size shouldBe 2
        }

        @Test
        fun `should return empty list when no records found`() {
            // Given
            val raiderId = 999L
            val guildId = "test-guild"
            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 12, 31)

            every {
                attendanceRepository.findByRaiderIdAndGuildIdAndDateRange(
                    RaiderId(raiderId),
                    GuildId(guildId),
                    startDate,
                    endDate
                )
            } returns emptyList()

            // When
            val result = useCase.execute(
                ListRaiderAttendanceQuery(raiderId, guildId, startDate, endDate)
            )

            // Then
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe emptyList()
        }
    }

    @Nested
    inner class GetGuildAttendanceSummaryUseCaseTests {
        private lateinit var useCase: GetGuildAttendanceSummaryUseCase

        @BeforeEach
        fun setUp() {
            useCase = GetGuildAttendanceSummaryUseCase(attendanceRepository)
        }

        @Test
        fun `should return guild attendance summary`() {
            // Given
            val guildId = "test-guild"
            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 12, 31)
            val records = listOf(
                createAttendanceRecord("record-1", raiderId = 1L, attendedRaids = 8, totalRaids = 10),
                createAttendanceRecord("record-2", raiderId = 1L, attendedRaids = 9, totalRaids = 10),
                createAttendanceRecord("record-3", raiderId = 2L, attendedRaids = 5, totalRaids = 10)
            )

            every {
                attendanceRepository.findByGuildIdAndDateRange(GuildId(guildId), startDate, endDate)
            } returns records

            // When
            val result = useCase.execute(
                GetGuildAttendanceSummaryQuery(guildId, startDate, endDate)
            )

            // Then
            result.isSuccess shouldBe true
            val summary = result.getOrNull()!!
            summary.guildId shouldBe guildId
            summary.totalRecords shouldBe 3
            summary.uniqueRaiders shouldBe 2
            // Total attended: 8 + 9 + 5 = 22, Total raids: 30, Percentage: 22/30 = 0.733...
            summary.overallAttendancePercentage shouldBe (22.0 / 30.0)
        }

        @Test
        fun `should return empty summary when no records`() {
            // Given
            val guildId = "empty-guild"
            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 12, 31)

            every {
                attendanceRepository.findByGuildIdAndDateRange(GuildId(guildId), startDate, endDate)
            } returns emptyList()

            // When
            val result = useCase.execute(
                GetGuildAttendanceSummaryQuery(guildId, startDate, endDate)
            )

            // Then
            result.isSuccess shouldBe true
            val summary = result.getOrNull()!!
            summary.totalRecords shouldBe 0
            summary.uniqueRaiders shouldBe 0
            summary.overallAttendancePercentage shouldBe 0.0
            summary.raiderSummaries shouldBe emptyList()
        }

        @Test
        fun `should calculate individual raider summaries`() {
            // Given
            val guildId = "test-guild"
            val startDate = LocalDate.of(2024, 1, 1)
            val endDate = LocalDate.of(2024, 12, 31)
            val records = listOf(
                createAttendanceRecord("record-1", raiderId = 1L, attendedRaids = 10, totalRaids = 10),
                createAttendanceRecord("record-2", raiderId = 2L, attendedRaids = 5, totalRaids = 10)
            )

            every {
                attendanceRepository.findByGuildIdAndDateRange(GuildId(guildId), startDate, endDate)
            } returns records

            // When
            val result = useCase.execute(
                GetGuildAttendanceSummaryQuery(guildId, startDate, endDate)
            )

            // Then
            result.isSuccess shouldBe true
            val summary = result.getOrNull()!!
            summary.raiderSummaries.size shouldBe 2

            val raider1Summary = summary.raiderSummaries.find { it.raiderId == 1L }!!
            raider1Summary.totalAttendedRaids shouldBe 10
            raider1Summary.totalRaids shouldBe 10
            raider1Summary.averageAttendancePercentage shouldBe 1.0

            val raider2Summary = summary.raiderSummaries.find { it.raiderId == 2L }!!
            raider2Summary.totalAttendedRaids shouldBe 5
            raider2Summary.totalRaids shouldBe 10
            raider2Summary.averageAttendancePercentage shouldBe 0.5
        }

        // Note: The branch `if (totalRaids > 0) ... else 0.0` at line 105 is unreachable
        // because AttendanceRecord.create() requires totalRaids > 0 (domain constraint).
        // This is dead code that cannot be covered by tests with valid domain objects.
    }

    // Helper method
    private fun createAttendanceRecord(
        id: String = "record-1",
        raiderId: Long = 1L,
        guildId: String = "test-guild",
        instance: String = "Nerub-ar Palace",
        encounter: String = "Ulgrax",
        attendedRaids: Int = 10,
        totalRaids: Int = 10
    ): AttendanceRecord = AttendanceRecord.create(
        raiderId = RaiderId(raiderId),
        guildId = GuildId(guildId),
        instance = instance,
        encounter = encounter,
        startDate = LocalDate.of(2024, 1, 1),
        endDate = LocalDate.of(2024, 1, 7),
        attendedRaids = attendedRaids,
        totalRaids = totalRaids
    )
}
