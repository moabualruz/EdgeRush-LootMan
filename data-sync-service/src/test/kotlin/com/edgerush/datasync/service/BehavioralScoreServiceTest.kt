package com.edgerush.datasync.service

import com.edgerush.datasync.entity.BehavioralActionEntity
import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.datasync.repository.BehavioralActionRepository
import com.edgerush.datasync.repository.LootBanRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDateTime

class BehavioralScoreServiceTest {

    private lateinit var behavioralActionRepository: BehavioralActionRepository
    private lateinit var lootBanRepository: LootBanRepository
    private lateinit var behavioralScoreService: BehavioralScoreService

    @BeforeEach
    fun setUp() {
        behavioralActionRepository = mockk()
        lootBanRepository = mockk()
        behavioralScoreService = BehavioralScoreService(behavioralActionRepository, lootBanRepository)
    }

    @Test
    fun `calculateBehavioralScore should return 1_0 for perfect behavior`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        every { 
            behavioralActionRepository.findActiveActionsForCharacter(
                guildId, 
                characterName, 
                any()
            )
        } returns emptyList()

        // When
        val result = behavioralScoreService.calculateBehavioralScore(guildId, characterName)

        // Then
        assertEquals(1.0, result)
    }

    @Test
    fun `calculateBehavioralScore should apply deductions correctly`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        val deduction = BehavioralActionEntity(
            id = 1,
            guildId = guildId,
            characterName = characterName,
            actionType = "DEDUCTION",
            deductionAmount = 0.2,
            reason = "Late to raid",
            appliedBy = "GuildLeader",
            appliedAt = LocalDateTime.now().minusDays(1),
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        
        every { 
            behavioralActionRepository.findActiveActionsForCharacter(guildId, characterName, any())
        } returns listOf(deduction)

        // When
        val result = behavioralScoreService.calculateBehavioralScore(guildId, characterName)

        // Then
        assertEquals(0.8, result, 0.001) // 1.0 - 0.2 = 0.8
    }

    @Test
    fun `calculateBehavioralScore should apply multiple deductions correctly`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        val deduction1 = BehavioralActionEntity(
            id = 1,
            guildId = guildId,
            characterName = characterName,
            actionType = "DEDUCTION",
            deductionAmount = 0.2,
            reason = "Late to raid",
            appliedBy = "GuildLeader",
            appliedAt = LocalDateTime.now().minusDays(2),
            expiresAt = LocalDateTime.now().plusDays(6)
        )
        val deduction2 = BehavioralActionEntity(
            id = 2,
            guildId = guildId,
            characterName = characterName,
            actionType = "DEDUCTION",
            deductionAmount = 0.1,
            reason = "Unprepared for raid",
            appliedBy = "GuildLeader",
            appliedAt = LocalDateTime.now().minusDays(1),
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        
        every { 
            behavioralActionRepository.findActiveActionsForCharacter(guildId, characterName, any())
        } returns listOf(deduction1, deduction2)

        // When
        val result = behavioralScoreService.calculateBehavioralScore(guildId, characterName)

        // Then
        assertEquals(0.7, result, 0.001) // 1.0 - 0.2 - 0.1 = 0.7
    }

    @Test
    fun `calculateBehavioralScore should apply restorations correctly`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        val deduction = BehavioralActionEntity(
            id = 1,
            guildId = guildId,
            characterName = characterName,
            actionType = "DEDUCTION",
            deductionAmount = 0.3,
            reason = "Late to raid",
            appliedBy = "GuildLeader",
            appliedAt = LocalDateTime.now().minusDays(2),
            expiresAt = LocalDateTime.now().plusDays(6)
        )
        val restoration = BehavioralActionEntity(
            id = 2,
            guildId = guildId,
            characterName = characterName,
            actionType = "RESTORATION",
            deductionAmount = 0.1,
            reason = "Good behavior improvement",
            appliedBy = "GuildLeader",
            appliedAt = LocalDateTime.now().minusDays(1),
            expiresAt = null
        )
        
        every { 
            behavioralActionRepository.findActiveActionsForCharacter(guildId, characterName, any())
        } returns listOf(deduction, restoration)

        // When
        val result = behavioralScoreService.calculateBehavioralScore(guildId, characterName)

        // Then
        assertEquals(0.8, result, 0.001) // 1.0 - 0.3 + 0.1 = 0.8
    }

    @Test
    fun `calculateBehavioralScore should not go below 0_0`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        val largeDeduction = BehavioralActionEntity(
            id = 1,
            guildId = guildId,
            characterName = characterName,
            actionType = "DEDUCTION",
            deductionAmount = 1.5, // More than possible
            reason = "Major violation",
            appliedBy = "GuildLeader",
            appliedAt = LocalDateTime.now().minusDays(1),
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        
        every { 
            behavioralActionRepository.findActiveActionsForCharacter(guildId, characterName, any())
        } returns listOf(largeDeduction)

        // When
        val result = behavioralScoreService.calculateBehavioralScore(guildId, characterName)

        // Then
        assertEquals(0.0, result)
    }

    @Test
    fun `calculateBehavioralScore should not go above 1_0`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        val largeRestoration = BehavioralActionEntity(
            id = 1,
            guildId = guildId,
            characterName = characterName,
            actionType = "RESTORATION",
            deductionAmount = 0.5, // Would put us above 1.0
            reason = "Exceptional behavior",
            appliedBy = "GuildLeader",
            appliedAt = LocalDateTime.now().minusDays(1),
            expiresAt = null
        )
        
        every { 
            behavioralActionRepository.findActiveActionsForCharacter(guildId, characterName, any())
        } returns listOf(largeRestoration)

        // When
        val result = behavioralScoreService.calculateBehavioralScore(guildId, characterName)

        // Then
        assertEquals(1.0, result)
    }

    @Test
    fun `isCharacterBannedFromLoot should return false when no ban exists`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        every { 
            lootBanRepository.findActiveBanForCharacter(guildId, characterName, any())
        } returns null

        // When
        val result = behavioralScoreService.isCharacterBannedFromLoot(guildId, characterName)

        // Then
        assertFalse(result.isBanned)
        assertNull(result.reason)
    }

    @Test
    fun `isCharacterBannedFromLoot should return true when active ban exists`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        val ban = LootBanEntity(
            id = 1,
            guildId = guildId,
            characterName = characterName,
            reason = "DKP violation",
            bannedBy = "GuildLeader",
            bannedAt = LocalDateTime.now().minusDays(1),
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        
        every { 
            lootBanRepository.findActiveBanForCharacter(guildId, characterName, any())
        } returns ban

        // When
        val result = behavioralScoreService.isCharacterBannedFromLoot(guildId, characterName)

        // Then
        assertTrue(result.isBanned)
        assertEquals("DKP violation", result.reason)
        assertEquals("GuildLeader", result.bannedBy)
        assertNotNull(result.bannedAt)
        assertNotNull(result.expiresAt)
    }

    @Test
    fun `getBehavioralBreakdown should return comprehensive information`() {
        // Given
        val guildId = "testguild"
        val characterName = "TestCharacter"
        val deduction = BehavioralActionEntity(
            id = 1,
            guildId = guildId,
            characterName = characterName,
            actionType = "DEDUCTION",
            deductionAmount = 0.2,
            reason = "Late to raid",
            appliedBy = "GuildLeader",
            appliedAt = LocalDateTime.now().minusDays(1),
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        
        every { 
            behavioralActionRepository.findActiveActionsForCharacter(guildId, characterName, any())
        } returns listOf(deduction)
        every { 
            lootBanRepository.findActiveBanForCharacter(guildId, characterName, any())
        } returns null

        // When
        val result = behavioralScoreService.getBehavioralBreakdown(guildId, characterName)

        // Then
        assertEquals(0.8, result.behavioralScore, 0.001)
        assertFalse(result.lootBanInfo.isBanned)
        assertEquals(1, result.activeActions.size)
        assertEquals("DEDUCTION", result.activeActions[0].actionType)
        assertEquals(0.2, result.activeActions[0].deductionAmount)
        assertEquals("Late to raid", result.activeActions[0].reason)
    }
}