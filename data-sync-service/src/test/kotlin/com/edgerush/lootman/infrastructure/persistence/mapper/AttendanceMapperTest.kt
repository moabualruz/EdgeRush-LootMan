package com.edgerush.lootman.infrastructure.persistence.mapper

import com.edgerush.datasync.entity.AttendanceStatEntity
import com.edgerush.lootman.domain.attendance.model.AttendanceRecord
import com.edgerush.lootman.domain.attendance.model.AttendanceRecordId
import com.edgerush.lootman.domain.attendance.model.GuildId
import com.edgerush.lootman.domain.attendance.model.RaiderId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * Unit tests for AttendanceMapper.
 *
 * Tests the mapping between AttendanceStatEntity (database) and AttendanceRecord (domain).
 */
class AttendanceMapperTest {
    private val mapper = AttendanceMapper()

    @Test
    fun `should map entity to domain with all fields present`() {
        // Arrange
        val entity =
            AttendanceStatEntity(
                id = 1L,
                instance = "Nerub-ar Palace",
                encounter = "Ulgrax the Devourer",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                characterId = 12345L,
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
        val guildId = GuildId("test-guild")

        // Act
        val domain = mapper.toDomain(entity, guildId)

        // Then
        domain.raiderId.value shouldBe 12345L
        domain.guildId shouldBe guildId
        domain.startDate shouldBe LocalDate.of(2024, 11, 1)
        domain.endDate shouldBe LocalDate.of(2024, 11, 14)
        domain.instance shouldBe "Nerub-ar Palace"
        domain.encounter shouldBe "Ulgrax the Devourer"
        domain.attendedRaids shouldBe 8
        domain.totalRaids shouldBe 10
        domain.attendancePercentage shouldBe 0.80
    }

    @Test
    fun `should map entity to domain with minimal fields`() {
        // Arrange
        val entity =
            AttendanceStatEntity(
                id = 2L,
                instance = "Unknown Instance",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 10),
                endDate = LocalDate.of(2024, 11, 10),
                characterId = 67890L,
                characterName = "MinimalRaider",
                characterRealm = null,
                characterClass = null,
                characterRole = null,
                characterRegion = null,
                attendedAmountOfRaids = 0,
                totalAmountOfRaids = 5,
                attendedPercentage = 0.0,
                selectedAmountOfEncounters = 0,
                totalAmountOfEncounters = 10,
                selectedPercentage = 0.0,
                teamId = null,
                seasonId = null,
                periodId = null,
                syncedAt = OffsetDateTime.now(),
            )
        val guildId = GuildId("test-guild")

        // Act
        val domain = mapper.toDomain(entity, guildId)

        // Then
        domain.raiderId.value shouldBe 67890L
        domain.guildId shouldBe guildId
        domain.startDate shouldBe LocalDate.of(2024, 11, 10)
        domain.endDate shouldBe LocalDate.of(2024, 11, 10)
        domain.instance shouldBe "Unknown Instance"
        domain.attendedRaids shouldBe 0
        domain.totalRaids shouldBe 5
        domain.attendancePercentage shouldBe 0.0
    }

    @Test
    fun `should map domain to entity with all fields`() {
        // Arrange
        val domain =
            AttendanceRecord.create(
                raiderId = RaiderId(45678L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = "Ulgrax",
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 15),
                attendedRaids = 8,
                totalRaids = 10,
            )

        // Act
        val entity = mapper.toEntity(domain)

        // Then
        entity.characterId shouldBe 45678L
        entity.characterName shouldBe "" // Not tracked in domain
        entity.startDate shouldBe LocalDate.of(2024, 11, 1)
        entity.endDate shouldBe LocalDate.of(2024, 11, 15)
        entity.instance shouldBe "Nerub-ar Palace"
        entity.encounter shouldBe "Ulgrax"
        entity.attendedAmountOfRaids shouldBe 8
        entity.totalAmountOfRaids shouldBe 10
        entity.attendedPercentage shouldBe 0.8
        entity.syncedAt shouldNotBe null
    }

    @Test
    fun `should map domain to entity with zero attendance`() {
        // Arrange
        val domain =
            AttendanceRecord.create(
                raiderId = RaiderId(99999L),
                guildId = GuildId("test-guild"),
                instance = "Nerub-ar Palace",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 16),
                endDate = LocalDate.of(2024, 11, 16),
                attendedRaids = 0,
                totalRaids = 5,
            )

        // Act
        val entity = mapper.toEntity(domain)

        // Then
        entity.characterId shouldBe 99999L
        entity.attendedAmountOfRaids shouldBe 0
        entity.totalAmountOfRaids shouldBe 5
        entity.attendedPercentage shouldBe 0.0
    }

    @Test
    fun `should throw exception when characterId is null`() {
        // Arrange
        val entity =
            AttendanceStatEntity(
                id = 3L,
                instance = "Test Instance",
                encounter = null,
                startDate = LocalDate.of(2024, 11, 1),
                endDate = LocalDate.of(2024, 11, 14),
                characterId = null,
                characterName = "UnknownRaider",
                characterRealm = "TestRealm",
                characterClass = "Mage",
                characterRole = "DPS",
                characterRegion = "US",
                attendedAmountOfRaids = 5,
                totalAmountOfRaids = 10,
                attendedPercentage = 0.50,
                selectedAmountOfEncounters = 10,
                totalAmountOfEncounters = 20,
                selectedPercentage = 0.50,
                teamId = 100L,
                seasonId = 200L,
                periodId = 300L,
                syncedAt = OffsetDateTime.now(),
            )
        val guildId = GuildId("test-guild")

        // Act & Assert
        val exception =
            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                mapper.toDomain(entity, guildId)
            }
        exception.message shouldBe "Entity must have a character ID"
    }
}
