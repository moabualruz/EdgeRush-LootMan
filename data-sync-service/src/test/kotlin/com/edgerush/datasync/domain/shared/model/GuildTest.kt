package com.edgerush.datasync.domain.shared.model

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

/**
 * Unit tests for Guild domain model.
 */
class GuildTest : UnitTest() {
    
    @Test
    fun `should create valid guild with required fields`() {
        // Given
        val guildId = "test-guild"
        val name = "Test Guild"
        
        // When
        val guild = Guild.create(
            guildId = guildId,
            name = name
        )
        
        // Then
        guild.id.value shouldBe guildId
        guild.name shouldBe name
        guild.isActive shouldBe true
        guild.syncEnabled shouldBe true
        guild.benchmarkMode shouldBe BenchmarkMode.THEORETICAL
    }
    
    @Test
    fun `should throw exception when guild ID is blank`() {
        // When/Then
        shouldThrow<IllegalArgumentException> {
            Guild.create(
                guildId = "",
                name = "Test Guild"
            )
        }
    }
    
    @Test
    fun `should throw exception when guild name is blank`() {
        // When/Then
        shouldThrow<IllegalArgumentException> {
            Guild.create(
                guildId = "test-guild",
                name = ""
            )
        }
    }
    
    @Test
    fun `should return true when WoWAudit config is present`() {
        // Given
        val guild = createTestGuild(
            wowauditApiKey = "encrypted-key",
            wowauditGuildUri = "guild-uri"
        )
        
        // When/Then
        guild.hasWoWAuditConfig() shouldBe true
    }
    
    @Test
    fun `should return false when WoWAudit config is missing`() {
        // Given
        val guild = createTestGuild(
            wowauditApiKey = null,
            wowauditGuildUri = null
        )
        
        // When/Then
        guild.hasWoWAuditConfig() shouldBe false
    }
    
    @Test
    fun `should return true when guild can sync`() {
        // Given
        val guild = createTestGuild(
            syncEnabled = true,
            wowauditApiKey = "encrypted-key",
            wowauditGuildUri = "guild-uri"
        )
        
        // When/Then
        guild.canSync() shouldBe true
    }
    
    @Test
    fun `should return false when sync is disabled`() {
        // Given
        val guild = createTestGuild(
            syncEnabled = false,
            wowauditApiKey = "encrypted-key",
            wowauditGuildUri = "guild-uri"
        )
        
        // When/Then
        guild.canSync() shouldBe false
    }
    
    @Test
    fun `should update sync status`() {
        // Given
        val guild = createTestGuild()
        
        // When
        val updated = guild.updateSyncStatus(SyncStatus.SUCCESS)
        
        // Then
        updated.lastSyncStatus shouldBe SyncStatus.SUCCESS
        updated.lastSyncError shouldBe null
        updated.lastSyncAt shouldBe updated.lastSyncAt
    }
    
    @Test
    fun `should update sync status with error`() {
        // Given
        val guild = createTestGuild()
        val error = "Sync failed"
        
        // When
        val updated = guild.updateSyncStatus(SyncStatus.FAILED, error)
        
        // Then
        updated.lastSyncStatus shouldBe SyncStatus.FAILED
        updated.lastSyncError shouldBe error
    }
    
    @Test
    fun `should update benchmark configuration`() {
        // Given
        val guild = createTestGuild()
        
        // When
        val updated = guild.updateBenchmark(
            mode = BenchmarkMode.CUSTOM,
            customRms = 0.95,
            customIpi = 0.90
        )
        
        // Then
        updated.benchmarkMode shouldBe BenchmarkMode.CUSTOM
        updated.customBenchmarkRms shouldBe 0.95
        updated.customBenchmarkIpi shouldBe 0.90
    }
    
    @Test
    fun `should throw exception when custom benchmark mode without values`() {
        // Given
        val guild = createTestGuild()
        
        // When/Then
        shouldThrow<IllegalArgumentException> {
            guild.updateBenchmark(
                mode = BenchmarkMode.CUSTOM,
                customRms = null,
                customIpi = null
            )
        }
    }
    
    @Test
    fun `should update WoWAudit configuration`() {
        // Given
        val guild = createTestGuild()
        val newApiKey = "new-encrypted-key"
        val newUri = "new-guild-uri"
        
        // When
        val updated = guild.updateWoWAuditConfig(newApiKey, newUri)
        
        // Then
        updated.wowauditApiKeyEncrypted shouldBe newApiKey
        updated.wowauditGuildUri shouldBe newUri
    }
    
    @Test
    fun `should enable sync`() {
        // Given
        val guild = createTestGuild(syncEnabled = false)
        
        // When
        val updated = guild.setSyncEnabled(true)
        
        // Then
        updated.syncEnabled shouldBe true
    }
    
    @Test
    fun `should disable sync`() {
        // Given
        val guild = createTestGuild(syncEnabled = true)
        
        // When
        val updated = guild.setSyncEnabled(false)
        
        // Then
        updated.syncEnabled shouldBe false
    }
    
    @Test
    fun `should deactivate guild`() {
        // Given
        val guild = createTestGuild()
        
        // When
        val updated = guild.deactivate()
        
        // Then
        updated.isActive shouldBe false
    }
    
    private fun createTestGuild(
        guildId: String = "test-guild",
        syncEnabled: Boolean = true,
        wowauditApiKey: String? = "encrypted-key",
        wowauditGuildUri: String? = "guild-uri"
    ): Guild {
        val now = OffsetDateTime.now()
        return Guild(
            id = GuildId(guildId),
            name = "Test Guild",
            description = "Test description",
            wowauditApiKeyEncrypted = wowauditApiKey,
            wowauditGuildUri = wowauditGuildUri,
            wowauditBaseUrl = "https://wowaudit.com",
            syncEnabled = syncEnabled,
            syncCronExpression = "0 0 4 * * *",
            syncRunOnStartup = false,
            lastSyncAt = null,
            lastSyncStatus = null,
            lastSyncError = null,
            timezone = "UTC",
            isActive = true,
            createdAt = now,
            updatedAt = now,
            benchmarkMode = BenchmarkMode.THEORETICAL,
            customBenchmarkRms = null,
            customBenchmarkIpi = null,
            benchmarkUpdatedAt = null
        )
    }
}
