package com.edgerush.lootman.application.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.*
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

/**
 * Unit tests for TrackAttendanceUseCase.
 */
class TrackAttendanceUseCaseTest : UnitTest() {

    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var useCase: TrackAttendanceUseCase

    @BeforeEach
    fun setup() {
        attendanceRepository = mockk()
        useCase = TrackAttendanceUseCase(attendanceRepository)
    }

    @Test
    fun `should track attendance successfully when valid command provided`() {
        // Given
        val command = TrackAttendanceCommand(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 8,
            totalRaids = 10
        )

        val expectedRecord = AttendanceRecord.create(
            raiderId = RaiderId(command.raiderId),
            guildId = GuildId(command.guildId),
            instance = command.instance,
            encounter = command.encounter,
            startDate = command.startDate,
            endDate = command.endDate,
            attendedRaids = command.attendedRaids,
            totalRaids = command.totalRaids
        )

        every { attendanceRepository.save(any()) } returns expectedRecord

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        val record = result.getOrThrow()
        assertEquals(RaiderId(command.raiderId), record.raiderId)
        assertEquals(GuildId(command.guildId), record.guildId)
        assertEquals(command.instance, record.instance)
        assertEquals(command.attendedRaids, record.attendedRaids)
        assertEquals(command.totalRaids, record.totalRaids)
        assertEquals(0.8, record.attendancePercentage)

        verify(exactly = 1) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should track attendance with encounter when encounter specified`() {
        // Given
        val command = TrackAttendanceCommand(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = "Queen Ansurek",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 5,
            totalRaids = 8
        )

        val expectedRecord = AttendanceRecord.create(
            raiderId = RaiderId(command.raiderId),
            guildId = GuildId(command.guildId),
            instance = command.instance,
            encounter = command.encounter,
            startDate = command.startDate,
            endDate = command.endDate,
            attendedRaids = command.attendedRaids,
            totalRaids = command.totalRaids
        )

        every { attendanceRepository.save(any()) } returns expectedRecord

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        val record = result.getOrThrow()
        assertEquals("Queen Ansurek", record.encounter)

        verify(exactly = 1) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should return failure when attended raids exceeds total raids`() {
        // Given
        val command = TrackAttendanceCommand(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 12,
            totalRaids = 10
        )

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)

        verify(exactly = 0) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should return failure when total raids is zero`() {
        // Given
        val command = TrackAttendanceCommand(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 0,
            totalRaids = 0
        )

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)

        verify(exactly = 0) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should return failure when end date is before start date`() {
        // Given
        val command = TrackAttendanceCommand(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 31),
            endDate = LocalDate.of(2024, 1, 1),
            attendedRaids = 8,
            totalRaids = 10
        )

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)

        verify(exactly = 0) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should track perfect attendance when attended equals total`() {
        // Given
        val command = TrackAttendanceCommand(
            raiderId = 123L,
            guildId = "guild-456",
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 1, 31),
            attendedRaids = 10,
            totalRaids = 10
        )

        val expectedRecord = AttendanceRecord.create(
            raiderId = RaiderId(command.raiderId),
            guildId = GuildId(command.guildId),
            instance = command.instance,
            encounter = command.encounter,
            startDate = command.startDate,
            endDate = command.endDate,
            attendedRaids = command.attendedRaids,
            totalRaids = command.totalRaids
        )

        every { attendanceRepository.save(any()) } returns expectedRecord

        // When
        val result = useCase.execute(command)

        // Then
        assertTrue(result.isSuccess)
        val record = result.getOrThrow()
        assertEquals(1.0, record.attendancePercentage)

        verify(exactly = 1) { attendanceRepository.save(any()) }
    }
}
