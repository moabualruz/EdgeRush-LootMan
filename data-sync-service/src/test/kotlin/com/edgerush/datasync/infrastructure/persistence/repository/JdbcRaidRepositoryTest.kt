package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.raids.model.*
import com.edgerush.datasync.entity.RaidEntity
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.datasync.test.base.IntegrationTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalTime

class JdbcRaidRepositoryTest : IntegrationTest() {
    
    @Autowired
    private lateinit var repository: JdbcRaidRepository
    
    @AfterEach
    fun cleanup() {
        jdbcTemplate.execute("DELETE FROM raid_signups")
        jdbcTemplate.execute("DELETE FROM raid_encounters")
        jdbcTemplate.execute("DELETE FROM raids")
    }
    
    @Test
    fun `should save and find raid by id`() {
        // Given
        val guildId = GuildId("test-guild-123")
        val raid = Raid.schedule(
            guildId = guildId,
            scheduledDate = LocalDate.of(2025, 11, 20),
            startTime = LocalTime.of(19, 0),
            endTime = LocalTime.of(22, 0),
            instance = "Nerub-ar Palace",
            difficulty = RaidDifficulty.MYTHIC,
            optional = false,
            notes = "Progression night"
        )
        
        // When
        val saved = repository.save(raid)
        val found = repository.findById(saved.id)
        
        // Then
        assertNotNull(found)
        assertEquals(saved.id, found!!.id)
        assertEquals(guildId, found.guildId)
        assertEquals(LocalDate.of(2025, 11, 20), found.scheduledDate)
        assertEquals(LocalTime.of(19, 0), found.startTime)
        assertEquals(LocalTime.of(22, 0), found.endTime)
        assertEquals("Nerub-ar Palace", found.instance)
        assertEquals(RaidDifficulty.MYTHIC, found.difficulty)
        assertEquals(false, found.optional)
        assertEquals(RaidStatus.SCHEDULED, found.status)
        assertEquals("Progression night", found.notes)
    }
    
    @Test
    fun `should return null when raid not found`() {
        // Given
        val nonExistentId = RaidId(999999L)
        
        // When
        val found = repository.findById(nonExistentId)
        
        // Then
        assertNull(found)
    }
    
    @Test
    fun `should save raid with encounters`() {
        // Given
        val guildId = GuildId("test-guild-123")
        val raid = Raid.schedule(
            guildId = guildId,
            scheduledDate = LocalDate.of(2025, 11, 20),
            instance = "Nerub-ar Palace"
        )
        val encounter1 = RaidEncounter.create(
            encounterId = 1001L,
            name = "Ulgrax the Devourer",
            enabled = true
        )
        val encounter2 = RaidEncounter.create(
            encounterId = 1002L,
            name = "The Bloodbound Horror",
            enabled = true
        )
        val raidWithEncounters = raid.addEncounter(encounter1).addEncounter(encounter2)
        
        // When
        val saved = repository.save(raidWithEncounters)
        val found = repository.findById(saved.id)
        
        // Then
        assertNotNull(found)
        assertEquals(2, found!!.getEncounters().size)
        assertEquals("Ulgrax the Devourer", found.getEncounters()[0].name)
        assertEquals("The Bloodbound Horror", found.getEncounters()[1].name)
    }
    
    @Test
    fun `should save raid with signups`() {
        // Given
        val guildId = GuildId("test-guild-123")
        val raid = Raid.schedule(
            guildId = guildId,
            scheduledDate = LocalDate.of(2025, 11, 20)
        )
        val raider1 = RaiderId(1L)
        val raider2 = RaiderId(2L)
        val raidWithSignups = raid
            .addSignup(raider1, RaidRole.TANK, "Ready to tank!")
            .addSignup(raider2, RaidRole.HEALER, "Can heal")
        
        // When
        val saved = repository.save(raidWithSignups)
        val found = repository.findById(saved.id)
        
        // Then
        assertNotNull(found)
        assertEquals(2, found!!.getSignups().size)
        assertEquals(raider1, found.getSignups()[0].raiderId)
        assertEquals(RaidRole.TANK, found.getSignups()[0].role)
        assertEquals("Ready to tank!", found.getSignups()[0].comment)
        assertEquals(raider2, found.getSignups()[1].raiderId)
        assertEquals(RaidRole.HEALER, found.getSignups()[1].role)
    }
    
    @Test
    fun `should update existing raid`() {
        // Given
        val guildId = GuildId("test-guild-123")
        val raid = Raid.schedule(
            guildId = guildId,
            scheduledDate = LocalDate.of(2025, 11, 20),
            notes = "Original notes"
        )
        val saved = repository.save(raid)
        
        // When
        val started = saved.start()
        val updated = repository.save(started)
        val found = repository.findById(updated.id)
        
        // Then
        assertNotNull(found)
        assertEquals(RaidStatus.IN_PROGRESS, found!!.status)
    }
    
    @Test
    fun `should find raids by guild id`() {
        // Given
        val guildId1 = GuildId("guild-1")
        val guildId2 = GuildId("guild-2")
        
        val raid1 = Raid.schedule(guildId1, LocalDate.of(2025, 11, 20))
        val raid2 = Raid.schedule(guildId1, LocalDate.of(2025, 11, 21))
        val raid3 = Raid.schedule(guildId2, LocalDate.of(2025, 11, 20))
        
        repository.save(raid1)
        repository.save(raid2)
        repository.save(raid3)
        
        // When
        val guild1Raids = repository.findByGuildId(guildId1)
        val guild2Raids = repository.findByGuildId(guildId2)
        
        // Then
        assertEquals(2, guild1Raids.size)
        assertEquals(1, guild2Raids.size)
        assertTrue(guild1Raids.all { it.guildId == guildId1 })
        assertTrue(guild2Raids.all { it.guildId == guildId2 })
    }
    
    @Test
    fun `should find raids by guild id and date`() {
        // Given
        val guildId = GuildId("test-guild")
        val date1 = LocalDate.of(2025, 11, 20)
        val date2 = LocalDate.of(2025, 11, 21)
        
        val raid1 = Raid.schedule(guildId, date1)
        val raid2 = Raid.schedule(guildId, date1)
        val raid3 = Raid.schedule(guildId, date2)
        
        repository.save(raid1)
        repository.save(raid2)
        repository.save(raid3)
        
        // When
        val date1Raids = repository.findByGuildIdAndDate(guildId, date1)
        val date2Raids = repository.findByGuildIdAndDate(guildId, date2)
        
        // Then
        assertEquals(2, date1Raids.size)
        assertEquals(1, date2Raids.size)
        assertTrue(date1Raids.all { it.scheduledDate == date1 })
        assertTrue(date2Raids.all { it.scheduledDate == date2 })
    }
    
    @Test
    fun `should delete raid and cascade to encounters and signups`() {
        // Given
        val guildId = GuildId("test-guild")
        val raid = Raid.schedule(guildId, LocalDate.of(2025, 11, 20))
        val encounter = RaidEncounter.create(1001L, "Boss 1")
        val raidWithData = raid
            .addEncounter(encounter)
            .addSignup(RaiderId(1L), RaidRole.TANK)
        
        val saved = repository.save(raidWithData)
        
        // When
        repository.delete(saved.id)
        val found = repository.findById(saved.id)
        
        // Then
        assertNull(found)
        
        // Verify cascading deletes
        val encounterCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM raid_encounters WHERE raid_id = ?",
            Int::class.java,
            saved.id.value
        )
        val signupCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM raid_signups WHERE raid_id = ?",
            Int::class.java,
            saved.id.value
        )
        
        assertEquals(0, encounterCount)
        assertEquals(0, signupCount)
    }
}
