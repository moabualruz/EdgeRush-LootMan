package com.edgerush.lootman.application.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.domain.attendance.repository.AttendanceRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TrackAttendanceUseCaseTest : UnitTest() {

    @MockK
    private lateinit var attendanceRepository: AttendanceRepository

    @InjectMockKs
    private lateinit var useCase: TrackAttendanceUseCase

    @Test
    fun `should track attendance successfully with valid command`() {
        // Arrange
        val command = TrackAttendanceCommand(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = "Queen Ansurek",
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 8,
            totalRaids = 10
        )

        val recordSlot = slot<AttendanceRecord>()
        every { attendanceRepository.save(capture(recordSlot)) } answers { recordSlot.captured }

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val attendanceResult = result.getOrNull()!!

        attendanceResult.raiderId shouldBe command.raiderId
        attendanceResult.guildId shouldBe command.guildId
        attendanceResult.instance shouldBe command.instance
        attendanceResult.encounter shouldBe command.encounter
        attendanceResult.attendedRaids shouldBe 8
        attendanceResult.totalRaids shouldBe 10
        attendanceResult.attendancePercentage shouldBe 0.8

        verify(exactly = 1) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should track attendance for overall instance when encounter is null`() {
        // Arrange
        val command = TrackAttendanceCommand(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 9,
            totalRaids = 10
        )

        val recordSlot = slot<AttendanceRecord>()
        every { attendanceRepository.save(capture(recordSlot)) } answers { recordSlot.captured }

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val attendanceResult = result.getOrNull()!!

        attendanceResult.encounter shouldBe null
        attendanceResult.attendancePercentage shouldBe 0.9

        verify(exactly = 1) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should track perfect attendance`() {
        // Arrange
        val command = TrackAttendanceCommand(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 10,
            totalRaids = 10
        )

        val recordSlot = slot<AttendanceRecord>()
        every { attendanceRepository.save(capture(recordSlot)) } answers { recordSlot.captured }

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isSuccess shouldBe true
        val attendanceResult = result.getOrNull()!!

        attendanceResult.attendancePercentage shouldBe 1.0
    }

    @Test
    fun `should fail when attended raids exceeds total raids`() {
        // Arrange
        val command = TrackAttendanceCommand(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 11,
            totalRaids = 10
        )

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isFailure shouldBe true
        verify(exactly = 0) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should fail when end date is before start date`() {
        // Arrange
        val command = TrackAttendanceCommand(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 14),
            endDate = LocalDate.of(2024, 11, 1),
            attendedRaids = 8,
            totalRaids = 10
        )

        // Act
        val result = useCase.execute(command)

        // Assert
        result.isFailure shouldBe true
        verify(exactly = 0) { attendanceRepository.save(any()) }
    }

    @Test
    fun `should generate unique record ID for each tracking`() {
        // Arrange
        val command = TrackAttendanceCommand(
            raiderId = RaiderId(1L),
            guildId = GuildId("test-guild"),
            instance = "Nerub-ar Palace",
            encounter = null,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14),
            attendedRaids = 8,
            totalRaids = 10
        )

        val recordSlot = slot<AttendanceRecord>()
        every { attendanceRepository.save(capture(recordSlot)) } answers { recordSlot.captured }

        // Act
        val result1 = useCase.execute(command)
        val result2 = useCase.execute(command)

        // Assert
        result1.isSuccess shouldBe true
        result2.isSuccess shouldBe true

        val record1 = result1.getOrNull()!!
        val record2 = result2.getOrNull()!!

        record1.recordId shouldNotBe record2.recordId
    }
}
