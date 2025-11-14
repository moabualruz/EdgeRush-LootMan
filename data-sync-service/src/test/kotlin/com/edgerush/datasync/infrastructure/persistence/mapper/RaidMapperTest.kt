package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.raids.model.*
import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.datasync.test.base.UnitTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

class RaidMapperTest : UnitTest() {
    
    private val mapper = RaidMapper()
    
    @Test
    fun `should map domain Raid to RaidEntity`() {
        // Given
        val raid = Raid.schedule(
            guildId = GuildId("test-guild-123"),
            scheduledDate = LocalDate.of(2025, 11, 20),
            startTime = LocalTime.of(19, 0),
            endTime = LocalTime.of(22, 0),
            instance = "Nerub-ar Palace",
            difficulty = RaidDifficulty.MYTHIC,
            optional = false,
            notes = "Progression night"
        )
        
        // When
        val entity = mapper.toEntity(raid)
        
        // Then
        assertEquals(raid.id.value, entity.raidId)
        assertEquals(raid.scheduledDate, entity.date)
        assertEquals(raid.startTime, entity.startTime)
        assertEquals(raid.endTime, entity.endTime)
        assertEquals(raid.instance, entity.instance)
        assertEquals("MYTHIC", entity.difficulty)
        assertEquals(raid.optional, entity.optional)
        assertEquals("SCHEDULED", entity.status)
        assertEquals(raid.notes, entity.notes)
        assertNotNull(entity.syncedAt)
    }
    
    @Test
    fun `should map RaidEntity to domain Raid`() {
        // Given
        val entity = RaidEntity(
            raidId = 12345L,
            date = LocalDate.of(2025, 11, 20),
            startTime = LocalTime.of(19, 0),
            endTime = LocalTime.of(22, 0),
            instance = "Nerub-ar Palace",
            difficulty = "MYTHIC",
            optional = false,
            status = "SCHEDULED",
            presentSize = null,
            totalSize = null,
            notes = "Progression night",
            selectionsImage = null,
            teamId = null,
            seasonId = null,
            periodId = null,
            createdAt = null,
            updatedAt = null,
            syncedAt = OffsetDateTime.now()
        )
        
        // When
        val raid = mapper.toDomain(entity, "test-guild-123", emptyList(), emptyList())
        
        // Then
        assertEquals(RaidId(12345L), raid.id)
        assertEquals(GuildId("test-guild-123"), raid.guildId)
        assertEquals(LocalDate.of(2025, 11, 20), raid.scheduledDate)
        assertEquals(LocalTime.of(19, 0), raid.startTime)
        assertEquals(LocalTime.of(22, 0), raid.endTime)
        assertEquals("Nerub-ar Palace", raid.instance)
        assertEquals(RaidDifficulty.MYTHIC, raid.difficulty)
        assertEquals(false, raid.optional)
        assertEquals(RaidStatus.SCHEDULED, raid.status)
        assertEquals("Progression night", raid.notes)
    }
    
    @Test
    fun `should map Raid with encounters and signups`() {
        // Given
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20)
        )
        val encounter = RaidEncounter.create(1001L, "Boss 1", enabled = true)
        val raidWithData = raid
            .addEncounter(encounter)
            .addSignup(RaiderId(1L), RaidRole.TANK, "Ready!")
        
        // When
        val entity = mapper.toEntity(raidWithData)
        val encounterEntities = mapper.encounterMapper.toEntities(raidWithData.getEncounters(), entity.raidId)
        val signupEntities = mapper.signupMapper.toEntities(raidWithData.getSignups(), entity.raidId)
        
        // Then
        assertEquals(1, encounterEntities.size)
        assertEquals(1, signupEntities.size)
        assertEquals("Boss 1", encounterEntities[0].name)
        assertEquals(1L, signupEntities[0].characterId)
    }
    
    @Test
    fun `should handle null optional fields`() {
        // Given
        val raid = Raid.schedule(
            guildId = GuildId("test-guild"),
            scheduledDate = LocalDate.of(2025, 11, 20),
            startTime = null,
            endTime = null,
            instance = null,
            difficulty = null,
            notes = null
        )
        
        // When
        val entity = mapper.toEntity(raid)
        val domainRaid = mapper.toDomain(entity, "test-guild", emptyList(), emptyList())
        
        // Then
        assertEquals(raid.id, domainRaid.id)
        assertEquals(raid.guildId, domainRaid.guildId)
        assertEquals(raid.scheduledDate, domainRaid.scheduledDate)
        assertEquals(null, domainRaid.startTime)
        assertEquals(null, domainRaid.endTime)
        assertEquals(null, domainRaid.instance)
        assertEquals(null, domainRaid.difficulty)
        assertEquals(null, domainRaid.notes)
    }
}
