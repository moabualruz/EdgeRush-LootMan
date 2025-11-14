package com.edgerush.lootman.infrastructure.attendance

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class InMemoryAttendanceRepositoryTest : UnitTest() {

    private lateinit var repository: InMemoryAttendanceRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryAttendanceRepository()
    }

    @Test
    fun `should save and retrieve attendance record by id`() {
        // Arrange
        val record = createAttendanceRecord()

        // Act
        val saved = repository.save(record)
        val retrieved = repository.findById(saved.id)

        // Assert
        retrieved shouldNotBe null
        retrieved shouldBe saved
    }

    @Test
    fun `should return null when record not found by id`() {
        // Arrange
        val record = createAttendanceRecord()

        // Act
        val retrieved = repository.findById(record.id)

        // Assert
        retrieved shouldBe null
    }

    @Test
    fun `should find records by raider and guild within date range`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        val record1 = createAttendanceRecord(
            raiderId = raiderId,
            guildId = guildId,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 7)
        )
        val record2 = createAttendanceRecord(
            raiderId = raiderId,
            guildId = guildId,
            startDate = LocalDate.of(2024, 11, 8),
            endDate = LocalDate.of(2024, 11, 14)
        )
        val record3 = createAttendanceRecord(
            raiderId = RaiderId(2L),
            guildId = guildId,
            startDate = LocalDate.of(2024, 11, 1),
            endDate = LocalDate.of(2024, 11, 14)
        )

        repository.save(record1)
        repository.save(record2)
        repository.save(record3)

        // Act
        val results = repository.findByRaiderIdAndGuildIdAndDateRange(
            raiderId,
            guildId,
            startDate,
            endDate
        )

        // Assert
        results shouldHaveSize 2
        results shouldContain record1
        results shouldContain record2
    }

    @Test
    fun `should find records by instance`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val instance = "Nerub-ar Palace"

        val record1 = createAttendanceRecord(
            raiderId = raiderId,
            guildId = guildId,
            instance = instance
        )
        val record2 = createAttendanceRecord(
            raiderId = raiderId,
            guildId = guildId,
            instance = "Amirdrassil"
        )

        repository.save(record1)
        repository.save(record2)

        // Act
        val results = repository.findByRaiderIdAndGuildIdAndInstanceAndDateRange(
            raiderId,
            guildId,
            instance,
            LocalDate.of(2024, 11, 1),
            LocalDate.of(2024, 11, 14)
        )

        // Assert
        results shouldHaveSize 1
        results shouldContain record1
    }

    @Test
    fun `should find records by encounter`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")
        val instance = "Nerub-ar Palace"
        val encounter = "Queen Ansurek"

        val record1 = createAttendanceRecord(
            raiderId = raiderId,
            guildId = guildId,
            instance = instance,
            encounter = encounter
        )
        val record2 = createAttendanceRecord(
            raiderId = raiderId,
            guildId = guildId,
            instance = instance,
            encounter = "Sikran"
        )

        repository.save(record1)
        repository.save(record2)

        // Act
        val results = repository.findByRaiderIdAndGuildIdAndEncounterAndDateRange(
            raiderId,
            guildId,
            instance,
            encounter,
            LocalDate.of(2024, 11, 1),
            LocalDate.of(2024, 11, 14)
        )

        // Assert
        results shouldHaveSize 1
        results shouldContain record1
    }

    @Test
    fun `should find all records for guild within date range`() {
        // Arrange
        val guildId = GuildId("test-guild")
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)

        val record1 = createAttendanceRecord(
            raiderId = RaiderId(1L),
            guildId = guildId
        )
        val record2 = createAttendanceRecord(
            raiderId = RaiderId(2L),
            guildId = guildId
        )
        val record3 = createAttendanceRecord(
            raiderId = RaiderId(3L),
            guildId = GuildId("other-guild")
        )

        repository.save(record1)
        repository.save(record2)
        repository.save(record3)

        // Act
        val results = repository.findByGuildIdAndDateRange(guildId, startDate, endDate)

        // Assert
        results shouldHaveSize 2
        results shouldContain record1
        results shouldContain record2
    }

    @Test
    fun `should delete attendance record`() {
        // Arrange
        val record = createAttendanceRecord()
        repository.save(record)

        // Act
        repository.delete(record.id)
        val retrieved = repository.findById(record.id)

        // Assert
        retrieved shouldBe null
    }

    @Test
    fun `should return empty list when no records match criteria`() {
        // Arrange
        val raiderId = RaiderId(1L)
        val guildId = GuildId("test-guild")

        // Act
        val results = repository.findByRaiderIdAndGuildIdAndDateRange(
            raiderId,
            guildId,
            LocalDate.of(2024, 11, 1),
            LocalDate.of(2024, 11, 14)
        )

        // Assert
        results.shouldBeEmpty()
    }

    @Test
    fun `should update existing record when saving with same id`() {
        // Arrange
        val record = createAttendanceRecord()
        repository.save(record)

        // Act - Save again (simulating update)
        val updated = repository.save(record)
        val retrieved = repository.findById(record.id)

        // Assert
        retrieved shouldBe updated
    }

    private fun createAttendanceRecord(
        raiderId: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("test-guild"),
        instance: String = "Nerub-ar Palace",
        encounter: String? = null,
        startDate: LocalDate = LocalDate.of(2024, 11, 1),
        endDate: LocalDate = LocalDate.of(2024, 11, 14)
    ): AttendanceRecord {
        return AttendanceRecord.create(
            raiderId = raiderId,
            guildId = guildId,
            instance = instance,
            encounter = encounter,
            startDate = startDate,
            endDate = endDate,
            attendedRaids = 8,
            totalRaids = 10
        )
    }
}
