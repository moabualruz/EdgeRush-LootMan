package com.edgerush.datasync.domain.shared.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

/**
 * Unit tests for Raider domain model.
 */
class RaiderTest : UnitTest() {
    
    @Test
    fun `should create valid raider with all required fields`() {
        // Given
        val characterName = "Testchar"
        val realm = "TestRealm"
        val region = "US"
        val clazz = WowClass.WARRIOR
        val spec = "Arms"
        val role = RaiderRole.DPS
        
        // When
        val raider = Raider.create(
            characterName = characterName,
            realm = realm,
            region = region,
            wowauditId = 12345L,
            clazz = clazz,
            spec = spec,
            role = role
        )
        
        // Then
        raider.characterName shouldBe characterName
        raider.realm shouldBe realm
        raider.region shouldBe region
        raider.clazz shouldBe clazz
        raider.spec shouldBe spec
        raider.role shouldBe role
        raider.status shouldBe RaiderStatus.ACTIVE
    }
    
    @Test
    fun `should throw exception when character name is blank`() {
        // When/Then
        shouldThrow<IllegalArgumentException> {
            Raider.create(
                characterName = "",
                realm = "TestRealm",
                region = "US",
                wowauditId = null,
                clazz = WowClass.WARRIOR,
                spec = "Arms",
                role = RaiderRole.DPS
            )
        }
    }
    
    @Test
    fun `should return full character name`() {
        // Given
        val raider = createTestRaider(characterName = "Testchar", realm = "TestRealm")
        
        // When
        val fullName = raider.getFullName()
        
        // Then
        fullName shouldBe "Testchar-TestRealm"
    }
    
    @Test
    fun `should return true when raider is active`() {
        // Given
        val raider = createTestRaider(status = RaiderStatus.ACTIVE)
        
        // When/Then
        raider.isActive() shouldBe true
    }
    
    @Test
    fun `should return false when raider is inactive`() {
        // Given
        val raider = createTestRaider(status = RaiderStatus.INACTIVE)
        
        // When/Then
        raider.isActive() shouldBe false
    }
    
    @Test
    fun `should return true when raider was synced recently`() {
        // Given
        val raider = createTestRaider(lastSync = OffsetDateTime.now().minusHours(12))
        
        // When/Then
        raider.isRecentlySynced() shouldBe true
    }
    
    @Test
    fun `should return false when raider was not synced recently`() {
        // Given
        val raider = createTestRaider(lastSync = OffsetDateTime.now().minusDays(2))
        
        // When/Then
        raider.isRecentlySynced() shouldBe false
    }
    
    @Test
    fun `should update sync timestamp`() {
        // Given
        val oldSync = OffsetDateTime.now().minusDays(1)
        val raider = createTestRaider(lastSync = oldSync)
        
        // When
        val updated = raider.updateSync()
        
        // Then
        updated.lastSync.isAfter(oldSync) shouldBe true
    }
    
    @Test
    fun `should update raider status`() {
        // Given
        val raider = createTestRaider(status = RaiderStatus.ACTIVE)
        
        // When
        val updated = raider.updateStatus(RaiderStatus.BENCHED)
        
        // Then
        updated.status shouldBe RaiderStatus.BENCHED
    }
    
    private fun createTestRaider(
        characterName: String = "Testchar",
        realm: String = "TestRealm",
        status: RaiderStatus = RaiderStatus.ACTIVE,
        lastSync: OffsetDateTime = OffsetDateTime.now()
    ): Raider {
        return Raider(
            id = RaiderId(1L),
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
            lastSync = lastSync
        )
    }
}
