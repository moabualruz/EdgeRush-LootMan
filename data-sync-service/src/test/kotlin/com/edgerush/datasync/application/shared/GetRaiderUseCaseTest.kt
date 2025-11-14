package com.edgerush.datasync.application.shared

import com.edgerush.datasync.domain.shared.model.*
import com.edgerush.datasync.domain.shared.repository.RaiderRepository
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

/**
 * Unit tests for GetRaiderUseCase.
 */
class GetRaiderUseCaseTest : UnitTest() {
    
    private lateinit var raiderRepository: RaiderRepository
    private lateinit var useCase: GetRaiderUseCase
    
    @BeforeEach
    fun setup() {
        raiderRepository = mockk()
        useCase = GetRaiderUseCase(raiderRepository)
    }
    
    @Test
    fun `should get raider by ID when raider exists`() {
        // Given
        val raiderId = RaiderId(1L)
        val raider = createTestRaider(id = raiderId)
        
        every { raiderRepository.findById(raiderId) } returns raider
        
        // When
        val result = useCase.execute(raiderId)
        
        // Then
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe raider
        verify(exactly = 1) { raiderRepository.findById(raiderId) }
    }
    
    @Test
    fun `should throw exception when raider not found by ID`() {
        // Given
        val raiderId = RaiderId(999L)
        
        every { raiderRepository.findById(raiderId) } returns null
        
        // When
        val result = useCase.execute(raiderId)
        
        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is RaiderNotFoundException) shouldBe true
    }
    
    @Test
    fun `should get raider by character name and realm`() {
        // Given
        val characterName = "Testchar"
        val realm = "TestRealm"
        val raider = createTestRaider(characterName = characterName, realm = realm)
        
        every { raiderRepository.findByCharacterNameAndRealm(characterName, realm) } returns raider
        
        // When
        val result = useCase.execute(characterName, realm)
        
        // Then
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe raider
        verify(exactly = 1) { raiderRepository.findByCharacterNameAndRealm(characterName, realm) }
    }
    
    @Test
    fun `should get all raiders`() {
        // Given
        val raiders = listOf(
            createTestRaider(id = RaiderId(1L)),
            createTestRaider(id = RaiderId(2L))
        )
        
        every { raiderRepository.findAll() } returns raiders
        
        // When
        val result = useCase.executeAll()
        
        // Then
        result.isSuccess shouldBe true
        result.getOrNull()?.size shouldBe 2
        verify(exactly = 1) { raiderRepository.findAll() }
    }
    
    @Test
    fun `should get all active raiders`() {
        // Given
        val raiders = listOf(
            createTestRaider(id = RaiderId(1L), status = RaiderStatus.ACTIVE),
            createTestRaider(id = RaiderId(2L), status = RaiderStatus.ACTIVE)
        )
        
        every { raiderRepository.findAllActive() } returns raiders
        
        // When
        val result = useCase.executeAllActive()
        
        // Then
        result.isSuccess shouldBe true
        result.getOrNull()?.size shouldBe 2
        verify(exactly = 1) { raiderRepository.findAllActive() }
    }
    
    private fun createTestRaider(
        id: RaiderId = RaiderId(1L),
        characterName: String = "Testchar",
        realm: String = "TestRealm",
        status: RaiderStatus = RaiderStatus.ACTIVE
    ): Raider {
        return Raider(
            id = id,
            characterName = characterName,
            realm = realm,
            region = "US",
            wowauditId = 12345L,
            clazz = WowClass.WARRIOR,
            spec = "Arms",
            role = RaiderRole.DPS,
            rank = "Member",
            status = status,
            note = null,
            blizzardId = null,
            trackingSince = null,
            joinDate = null,
            blizzardLastModified = null,
            lastSync = OffsetDateTime.now()
        )
    }
}
