package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.raids.model.RaidRole
import com.edgerush.datasync.domain.raids.model.RaidSignup
import com.edgerush.datasync.domain.raids.model.RaiderId
import com.edgerush.datasync.entity.RaidSignupEntity
import com.edgerush.datasync.test.base.UnitTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class RaidSignupMapperTest : UnitTest() {
    
    private val mapper = RaidSignupMapper()
    
    @Test
    fun `should map domain RaidSignup to RaidSignupEntity`() {
        // Given
        val signup = RaidSignup.create(
            raiderId = RaiderId(123L),
            role = RaidRole.TANK,
            status = RaidSignup.SignupStatus.CONFIRMED,
            comment = "Ready to tank!"
        )
        val raidId = 12345L
        
        // When
        val entity = mapper.toEntity(signup, raidId)
        
        // Then
        assertEquals(raidId, entity.raidId)
        assertEquals(123L, entity.characterId)
        assertEquals("TANK", entity.characterRole)
        assertEquals("CONFIRMED", entity.status)
        assertEquals("Ready to tank!", entity.comment)
        assertEquals(false, entity.selected)
    }
    
    @Test
    fun `should map RaidSignupEntity to domain RaidSignup`() {
        // Given
        val entity = RaidSignupEntity(
            id = 1L,
            raidId = 12345L,
            characterId = 123L,
            characterName = "TestPlayer",
            characterRealm = "TestRealm",
            characterRegion = "US",
            characterClass = "Warrior",
            characterRole = "TANK",
            characterGuest = false,
            status = "CONFIRMED",
            comment = "Ready to tank!",
            selected = false
        )
        
        // When
        val signup = mapper.toDomain(entity)
        
        // Then
        assertEquals(1L, signup.id)
        assertEquals(RaiderId(123L), signup.raiderId)
        assertEquals(RaidRole.TANK, signup.role)
        assertEquals(RaidSignup.SignupStatus.CONFIRMED, signup.status)
        assertEquals("Ready to tank!", signup.comment)
        assertEquals(false, signup.selected)
    }
    
    @Test
    fun `should handle all signup statuses`() {
        // Given
        val statuses = listOf(
            RaidSignup.SignupStatus.CONFIRMED,
            RaidSignup.SignupStatus.TENTATIVE,
            RaidSignup.SignupStatus.DECLINED,
            RaidSignup.SignupStatus.LATE
        )
        
        statuses.forEach { status ->
            // When
            val signup = RaidSignup.create(RaiderId(1L), RaidRole.DPS, status)
            val entity = mapper.toEntity(signup, 12345L)
            val domainSignup = mapper.toDomain(entity)
            
            // Then
            assertEquals(status, domainSignup.status)
        }
    }
    
    @Test
    fun `should handle all raid roles`() {
        // Given
        val roles = listOf(
            RaidRole.TANK,
            RaidRole.HEALER,
            RaidRole.DPS
        )
        
        roles.forEach { role ->
            // When
            val signup = RaidSignup.create(RaiderId(1L), role)
            val entity = mapper.toEntity(signup, 12345L)
            val domainSignup = mapper.toDomain(entity)
            
            // Then
            assertEquals(role, domainSignup.role)
        }
    }
    
    @Test
    fun `should handle null comment`() {
        // Given
        val signup = RaidSignup.create(
            raiderId = RaiderId(123L),
            role = RaidRole.HEALER,
            comment = null
        )
        
        // When
        val entity = mapper.toEntity(signup, 12345L)
        val domainSignup = mapper.toDomain(entity)
        
        // Then
        assertNull(domainSignup.comment)
    }
    
    @Test
    fun `should handle selected signup`() {
        // Given
        val signup = RaidSignup.create(RaiderId(1L), RaidRole.TANK).select()
        
        // When
        val entity = mapper.toEntity(signup, 12345L)
        val domainSignup = mapper.toDomain(entity)
        
        // Then
        assertEquals(true, domainSignup.selected)
    }
    
    @Test
    fun `should map list of signups`() {
        // Given
        val signups = listOf(
            RaidSignup.create(RaiderId(1L), RaidRole.TANK),
            RaidSignup.create(RaiderId(2L), RaidRole.HEALER),
            RaidSignup.create(RaiderId(3L), RaidRole.DPS)
        )
        val raidId = 12345L
        
        // When
        val entities = mapper.toEntities(signups, raidId)
        
        // Then
        assertEquals(3, entities.size)
        assertEquals("TANK", entities[0].characterRole)
        assertEquals("HEALER", entities[1].characterRole)
        assertEquals("DPS", entities[2].characterRole)
        entities.forEach { assertEquals(raidId, it.raidId) }
    }
}
