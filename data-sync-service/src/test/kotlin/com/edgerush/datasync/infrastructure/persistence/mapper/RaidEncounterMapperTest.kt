package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.raids.model.RaidEncounter
import com.edgerush.datasync.entity.RaidEncounterEntity
import com.edgerush.datasync.test.base.UnitTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class RaidEncounterMapperTest : UnitTest() {
    
    private val mapper = RaidEncounterMapper()
    
    @Test
    fun `should map domain RaidEncounter to RaidEncounterEntity`() {
        // Given
        val encounter = RaidEncounter.create(
            encounterId = 1001L,
            name = "Ulgrax the Devourer",
            enabled = true,
            extra = false,
            notes = "First boss"
        )
        val raidId = 12345L
        
        // When
        val entity = mapper.toEntity(encounter, raidId)
        
        // Then
        assertEquals(raidId, entity.raidId)
        assertEquals(1001L, entity.encounterId)
        assertEquals("Ulgrax the Devourer", entity.name)
        assertEquals(true, entity.enabled)
        assertEquals(false, entity.extra)
        assertEquals("First boss", entity.notes)
    }
    
    @Test
    fun `should map RaidEncounterEntity to domain RaidEncounter`() {
        // Given
        val entity = RaidEncounterEntity(
            id = 1L,
            raidId = 12345L,
            encounterId = 1001L,
            name = "Ulgrax the Devourer",
            enabled = true,
            extra = false,
            notes = "First boss"
        )
        
        // When
        val encounter = mapper.toDomain(entity)
        
        // Then
        assertEquals(1L, encounter.id)
        assertEquals(1001L, encounter.encounterId)
        assertEquals("Ulgrax the Devourer", encounter.name)
        assertEquals(true, encounter.enabled)
        assertEquals(false, encounter.extra)
        assertEquals("First boss", encounter.notes)
    }
    
    @Test
    fun `should handle null optional fields`() {
        // Given
        val encounter = RaidEncounter.create(
            encounterId = null,
            name = "Custom Boss",
            enabled = true,
            extra = false,
            notes = null
        )
        
        // When
        val entity = mapper.toEntity(encounter, 12345L)
        val domainEncounter = mapper.toDomain(entity)
        
        // Then
        assertNull(domainEncounter.encounterId)
        assertEquals("Custom Boss", domainEncounter.name)
        assertNull(domainEncounter.notes)
    }
    
    @Test
    fun `should map list of encounters`() {
        // Given
        val encounters = listOf(
            RaidEncounter.create(1001L, "Boss 1"),
            RaidEncounter.create(1002L, "Boss 2"),
            RaidEncounter.create(1003L, "Boss 3")
        )
        val raidId = 12345L
        
        // When
        val entities = mapper.toEntities(encounters, raidId)
        
        // Then
        assertEquals(3, entities.size)
        assertEquals("Boss 1", entities[0].name)
        assertEquals("Boss 2", entities[1].name)
        assertEquals("Boss 3", entities[2].name)
        entities.forEach { assertEquals(raidId, it.raidId) }
    }
}
