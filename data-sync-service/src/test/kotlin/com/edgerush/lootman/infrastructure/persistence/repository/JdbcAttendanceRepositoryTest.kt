package com.edgerush.lootman.infrastructure.persistence.repository

import com.edgerush.datasync.entity.AttendanceStatEntity
import com.edgerush.datasync.repository.AttendanceStatRepository
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import com.edgerush.lootman.infrastructure.persistence.mapper.AttendanceMapper
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.Optional

/**
 * Unit tests for JdbcAttendanceRepository.
 *
 * Tests the repository implementation that bridges domain and infrastructure layers.
 */
class JdbcAttendanceRepositoryTest {
    private lateinit var springRepository: AttendanceStatRepository
    private lateinit var mapper: AttendanceMapper
    private lateinit var repository: JdbcAttendanceRepository

    @BeforeEach
    fun setup() {
        springRepository = mockk()
        mapper = AttendanceMapper()
        repository = JdbcAttendanceRepository(springRepository, mapper)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should find attendance record by id`() {
        // Arrange
        val entity =
            createTestEntity(
                id = 1L,
                characterId = 12345L,
                endDate = LocalDate.of(2024, 11, 14),
            )
        every { springRepository.findById(1L) } returns Optional.of(entity)

        // Act
        val result = repository.findById(AttendanceRecordId("1"))

        // Then
        result shouldNotBe null
        result!!.raiderId.value shouldBe 12345L
        result.attendedRaids shouldBe 8
        result.totalRaids shouldBe 10
        verify(exactly = 1) { springRepository.findById(1L) }
    }

    @Test
    fun `should return null when attendance record not found by id`() {
        // Arrange
        every { springRepository.findById(999L) } returns Optional.empty()

        // Act
        val result = repository.findById(AttendanceRecordId("999"))

        // Then
        result shouldBe null
        verify(exactly = 1) { springRepository.findById(999L) }
    }

    @Test
    fun `should return null when id is not a valid long`() {
        // Act
        val result = repository.findById(AttendanceRecordId("invalid-id"))

        // Then
        result shouldBe null
        verify(exactly = 0) { springRepository.findById(any()) }
    }

    @Test
    fun `should find attendance records by raider and guild in date range`() {
        // Arrange
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)
        val entities =
            listOf(
                createTestEntity(id = 1L, characterId = 12345L, endDate = LocalDate.of(2024, 11, 5)),
                createTestEntity(id = 2L, characterId = 12345L, endDate = LocalDate.of(2024, 11, 10)),
            )
        every { springRepository.findByCharacterId(12345L) } returns entities

        // Act
        val result =
            repository.findByRaiderIdAndGuildIdAndDateRange(
                raiderId = RaiderId(12345L),
                guildId = GuildId("test-guild"),
                startDate = startDate,
                endDate = endDate,
            )

        // Then
        result shouldHaveSize 2
        result[0].raiderId.value shouldBe 12345L
        result[1].raiderId.value shouldBe 12345L
        verify(exactly = 1) { springRepository.findByCharacterId(12345L) }
    }

    @Test
    fun `should filter attendance records by date range`() {
        // Arrange
        val startDate = LocalDate.of(2024, 11, 5)
        val endDate = LocalDate.of(2024, 11, 10)
        val entities =
            listOf(
                createTestEntity(id = 1L, characterId = 12345L, endDate = LocalDate.of(2024, 11, 1)), // Before range
                createTestEntity(id = 2L, characterId = 12345L, endDate = LocalDate.of(2024, 11, 7)), // In range
                createTestEntity(id = 3L, characterId = 12345L, endDate = LocalDate.of(2024, 11, 15)), // After range
            )
        every { springRepository.findByCharacterId(12345L) } returns entities

        // Act
        val result =
            repository.findByRaiderIdAndGuildIdAndDateRange(
                raiderId = RaiderId(12345L),
                guildId = GuildId("test-guild"),
                startDate = startDate,
                endDate = endDate,
            )

        // Then
        result shouldHaveSize 1
        result[0].endDate shouldBe LocalDate.of(2024, 11, 7)
    }

    @Test
    fun `should find attendance records by raider, guild, instance and date range`() {
        // Arrange
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)
        val entities =
            listOf(
                createTestEntity(id = 1L, characterId = 12345L, instance = "Nerub-ar Palace", endDate = LocalDate.of(2024, 11, 5)),
                createTestEntity(id = 2L, characterId = 12345L, instance = "Nerub-ar Palace", endDate = LocalDate.of(2024, 11, 10)),
                createTestEntity(id = 3L, characterId = 12345L, instance = "Other Instance", endDate = LocalDate.of(2024, 11, 12)),
            )
        every { springRepository.findByCharacterId(12345L) } returns entities

        // Act
        val result =
            repository.findByRaiderIdAndGuildIdAndInstanceAndDateRange(
                raiderId = RaiderId(12345L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                startDate = startDate,
                endDate = endDate,
            )

        // Then
        result shouldHaveSize 2
        result.all { it.instance == "Nerub-ar Palace" } shouldBe true
    }

    @Test
    fun `should find attendance records by raider, guild, encounter and date range`() {
        // Arrange
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)
        val entities =
            listOf(
                createTestEntity(
                    id = 1L,
                    characterId = 12345L,
                    instance = "Nerub-ar Palace",
                    encounter = "Ulgrax",
                    endDate = LocalDate.of(2024, 11, 5),
                ),
                createTestEntity(
                    id = 2L,
                    characterId = 12345L,
                    instance = "Nerub-ar Palace",
                    encounter = "Ulgrax",
                    endDate = LocalDate.of(2024, 11, 10),
                ),
                createTestEntity(
                    id = 3L,
                    characterId = 12345L,
                    instance = "Nerub-ar Palace",
                    encounter = "Bloodbound Horror",
                    endDate = LocalDate.of(2024, 11, 12),
                ),
            )
        every { springRepository.findByCharacterId(12345L) } returns entities

        // Act
        val result =
            repository.findByRaiderIdAndGuildIdAndEncounterAndDateRange(
                raiderId = RaiderId(12345L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = "Ulgrax",
                startDate = startDate,
                endDate = endDate,
            )

        // Then
        result shouldHaveSize 2
        result.all { it.instance == "Nerub-ar Palace" } shouldBe true
    }

    @Test
    fun `should find attendance records by guild and date range`() {
        // Arrange
        val startDate = LocalDate.of(2024, 11, 1)
        val endDate = LocalDate.of(2024, 11, 14)
        val entities =
            listOf(
                createTestEntity(id = 1L, characterId = 12345L, endDate = LocalDate.of(2024, 11, 5)),
                createTestEntity(id = 2L, characterId = 67890L, endDate = LocalDate.of(2024, 11, 10)),
            )
        every { springRepository.findAll() } returns entities

        // Act
        val result =
            repository.findByGuildIdAndDateRange(
                guildId = GuildId("test-guild"),
                startDate = startDate,
                endDate = endDate,
            )

        // Then
        result shouldHaveSize 2
        verify(exactly = 1) { springRepository.findAll() }
    }

    @Test
    fun `should return empty list when no records in date range`() {
        // Arrange
        val startDate = LocalDate.of(2024, 12, 1)
        val endDate = LocalDate.of(2024, 12, 14)
        val entities =
            listOf(
                createTestEntity(id = 1L, characterId = 12345L, endDate = LocalDate.of(2024, 11, 5)),
            )
        every { springRepository.findByCharacterId(12345L) } returns entities

        // Act
        val result =
            repository.findByRaiderIdAndGuildIdAndDateRange(
                raiderId = RaiderId(12345L),
                guildId = GuildId("test-guild"),
                startDate = startDate,
                endDate = endDate,
            )

        // Then
        result.shouldBeEmpty()
    }

    @Test
    fun `should save attendance record`() {
        // Arrange
        val domain =
            AttendanceRecord.create(
                raiderId = RaiderId(12345L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = "Ulgrax",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                attendedRaids = 8,
                totalRaids = 10,
            )
        val entity = mapper.toEntity(domain)
        val savedEntity = entity.copy(id = 100L)
        every { springRepository.save(any()) } returns savedEntity

        // Act
        val result = repository.save(domain)

        // Then
        result.raiderId shouldBe domain.raiderId
        result.guildId shouldBe domain.guildId
        result.attendedRaids shouldBe 8
        result.totalRaids shouldBe 10
        verify(exactly = 1) { springRepository.save(any()) }
    }

    @Test
    fun `should delete attendance record by id`() {
        // Arrange
        every { springRepository.deleteById(1L) } returns Unit

        // Act
        repository.delete(AttendanceRecordId("1"))

        // Then
        verify(exactly = 1) { springRepository.deleteById(1L) }
    }

    @Test
    fun `should not call delete when id is invalid`() {
        // Act
        repository.delete(AttendanceRecordId("invalid-id"))

        // Then
        verify(exactly = 0) { springRepository.deleteById(any()) }
    }

    private fun createTestEntity(
        id: Long,
        characterId: Long,
        instance: String? = "Test Instance",
        encounter: String? = null,
        endDate: LocalDate,
    ): AttendanceStatEntity =
        AttendanceStatEntity(
            id = id,
            instance = instance,
            encounter = encounter,
            startDate = endDate.minusDays(7),
            endDate = endDate,
            characterId = characterId,
            characterName = "TestRaider",
            characterRealm = "Area-52",
            characterClass = "Warrior",
            characterRole = "Tank",
            characterRegion = "US",
            attendedAmountOfRaids = 8,
            totalAmountOfRaids = 10,
            attendedPercentage = 0.80,
            selectedAmountOfEncounters = 15,
            totalAmountOfEncounters = 20,
            selectedPercentage = 0.75,
            teamId = 100L,
            seasonId = 200L,
            periodId = 300L,
            syncedAt = OffsetDateTime.now(),
        )
}
