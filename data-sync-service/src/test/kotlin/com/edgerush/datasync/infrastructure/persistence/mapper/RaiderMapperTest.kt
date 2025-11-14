package com.edgerush.datasync.infrastructure.persistence.mapper

import com.edgerush.datasync.domain.shared.model.*
import com.edgerush.datasync.entity.RaiderEntity
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

/**
 * Unit tests for RaiderMapper.
 */
class RaiderMapperTest : UnitTest() {
    
    private lateinit var mapper: RaiderMapper
    
    @BeforeEach
    fun setup() {
        mapper = RaiderMapper()
    }
    
    @Test
    fun `should map domain model to entity`() {
        // Given
        val now = OffsetDateTime.now()
        val raider = Raider(
            id = RaiderId(1L),
            characterName = "Testchar",
            realm = "TestRealm",
            region = "US",
            wowauditId = 12345L,
            clazz = WowClass.WARRIOR,
            spec = "Arms",
            role = RaiderRole.DPS,
            rank = "Member",
            status = RaiderStatus.ACTIVE,
            note = "Test note",
            blizzardId = 67890L,
            trackingSince = now,
            joinDate = now,
            blizzardLastModified = now,
            lastSync = now
        )
        
        // When
        val entity = mapper.toEntity(raider)
        
        // Then
        entity.id shouldBe 1L
        entity.characterName shouldBe "Testchar"
        entity.realm shouldBe "TestRealm"
        entity.region shouldBe "US"
        entity.wowauditId shouldBe 12345L
        entity.clazz shouldBe "WARRIOR"
        entity.spec shouldBe "Arms"
        entity.role shouldBe "DPS"
        entity.rank shouldBe "Member"
        entity.status shouldBe "ACTIVE"
        entity.note shouldBe "Test note"
        entity.blizzardId shouldBe 67890L
    }
    
    @Test
    fun `should map entity to domain model`() {
        // Given
        val now = OffsetDateTime.now()
        val entity = RaiderEntity(
            id = 1L,
            characterName = "Testchar",
            realm = "TestRealm",
            region = "US",
            wowauditId = 12345L,
            clazz = "WARRIOR",
            spec = "Arms",
            role = "DPS",
            rank = "Member",
            status = "ACTIVE",
            note = "Test note",
            blizzardId = 67890L,
            trackingSince = now,
            joinDate = now,
            blizzardLastModified = now,
            lastSync = now
        )
        
        // When
        val raider = mapper.toDomain(entity)
        
        // Then
        raider.id.value shouldBe 1L
        raider.characterName shouldBe "Testchar"
        raider.realm shouldBe "TestRealm"
        raider.region shouldBe "US"
        raider.wowauditId shouldBe 12345L
        raider.clazz shouldBe WowClass.WARRIOR
        raider.spec shouldBe "Arms"
        raider.role shouldBe RaiderRole.DPS
        raider.rank shouldBe "Member"
        raider.status shouldBe RaiderStatus.ACTIVE
        raider.note shouldBe "Test note"
        raider.blizzardId shouldBe 67890L
    }
    
    @Test
    fun `should handle null optional fields`() {
        // Given
        val now = OffsetDateTime.now()
        val entity = RaiderEntity(
            id = 1L,
            characterName = "Testchar",
            realm = "TestRealm",
            region = "US",
            wowauditId = null,
            clazz = "WARRIOR",
            spec = "Arms",
            role = "DPS",
            rank = null,
            status = null,
            note = null,
            blizzardId = null,
            trackingSince = null,
            joinDate = null,
            blizzardLastModified = null,
            lastSync = now
        )
        
        // When
        val raider = mapper.toDomain(entity)
        
        // Then
        raider.wowauditId shouldBe null
        raider.rank shouldBe null
        raider.status shouldBe RaiderStatus.ACTIVE // Default
        raider.note shouldBe null
        raider.blizzardId shouldBe null
    }
}
