package com.edgerush.lootman.api.guild

import com.edgerush.datasync.entity.GuildConfigurationEntity
import com.edgerush.datasync.security.AuthenticatedUser
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.guild.GuildContextService
import com.edgerush.lootman.application.guild.GuildRosterSyncResult
import com.edgerush.lootman.application.guild.GuildRosterSyncService
import com.edgerush.lootman.application.guild.WarcraftLogsRosterSyncService
import com.edgerush.lootman.application.guild.WarcraftLogsSyncResult
import com.edgerush.lootman.application.guild.WoWAuditAttendanceSyncService
import com.edgerush.lootman.application.guild.WoWAuditHistoricalDataSyncService
import com.edgerush.lootman.application.guild.WoWAuditLootHistorySyncService
import com.edgerush.lootman.application.guild.WoWAuditRosterSyncService
import com.edgerush.lootman.application.guild.WoWAuditSyncResult
import com.edgerush.lootman.application.guild.WoWAuditWishlistSyncService
import com.edgerush.lootman.domain.guild.repository.GuildConfigurationRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

class GuildSyncControllerTest : UnitTest() {
    private lateinit var guildConfigurationRepository: GuildConfigurationRepository
    private lateinit var guildRosterSyncService: GuildRosterSyncService
    private lateinit var wowAuditRosterSyncService: WoWAuditRosterSyncService
    private lateinit var wowAuditAttendanceSyncService: WoWAuditAttendanceSyncService
    private lateinit var wowAuditLootHistorySyncService: WoWAuditLootHistorySyncService
    private lateinit var wowAuditWishlistSyncService: WoWAuditWishlistSyncService
    private lateinit var wowAuditHistoricalDataSyncService: WoWAuditHistoricalDataSyncService
    private lateinit var warcraftLogsRosterSyncService: WarcraftLogsRosterSyncService
    private lateinit var guildContextService: GuildContextService
    private lateinit var controller: GuildSyncController

    private val user = AuthenticatedUser("1", "test-user", roles = listOf("ADMIN"), guildIds = listOf("guild-1"))

    @BeforeEach
    fun setup() {
        guildConfigurationRepository = mockk()
        guildRosterSyncService = mockk()
        wowAuditRosterSyncService = mockk()
        wowAuditAttendanceSyncService = mockk()
        wowAuditLootHistorySyncService = mockk()
        wowAuditWishlistSyncService = mockk()
        wowAuditHistoricalDataSyncService = mockk()
        warcraftLogsRosterSyncService = mockk()
        guildContextService = mockk()
        
        controller = GuildSyncController(
            guildConfigurationRepository,
            guildRosterSyncService,
            wowAuditRosterSyncService,
            wowAuditAttendanceSyncService,
            wowAuditLootHistorySyncService,
            wowAuditWishlistSyncService,
            wowAuditHistoricalDataSyncService,
            warcraftLogsRosterSyncService,
            guildContextService
        )
    }

    @Nested
    inner class UpdateSyncConfigTests {
        @Test
        fun `should update sync config with provided values`() {
            // Given
            val guildId = "guild-1"
            val existing = createGuildConfiguration(guildId = guildId)
            val request = UpdateGuildSyncConfigRequest(
                wowauditGuildUri = "eu/twisting-nether/dod",
                bnetRealmSlug = "twisting-nether",
                bnetGuildNameSlug = "dod",
                bnetRegion = "eu",
                syncEnabled = true,
                bnetSyncEnabled = true
            )

            val slot = slot<GuildConfigurationEntity>()
            every { guildConfigurationRepository.findByGuildId(guildId) } returns existing
            every { guildConfigurationRepository.save(capture(slot)) } answers { slot.captured }

            // When
            val result = controller.updateSyncConfig(guildId, request, user)

            // Then
            result.wowauditGuildUri shouldBe "eu/twisting-nether/dod"
            result.bnetRealmSlug shouldBe "twisting-nether"
            result.bnetGuildNameSlug shouldBe "dod"
            result.bnetRegion shouldBe "eu"
            result.syncEnabled shouldBe true
            result.bnetSyncEnabled shouldBe true

            verify { guildConfigurationRepository.save(any()) }
        }
        
        @Test
        fun `should perform partial update`() {
            // Given
            val guildId = "guild-1"
            val existing = createGuildConfiguration(
                guildId = guildId,
                bnetRegion = "us",
                bnetRealmSlug = "existing-realm"
            )
            val request = UpdateGuildSyncConfigRequest(
                bnetRegion = "eu"
            )

            val slot = slot<GuildConfigurationEntity>()
            every { guildConfigurationRepository.findByGuildId(guildId) } returns existing
            every { guildConfigurationRepository.save(capture(slot)) } answers { slot.captured }

            // When
            val result = controller.updateSyncConfig(guildId, request, user)

            // Then
            result.bnetRegion shouldBe "eu"
            result.bnetRealmSlug shouldBe "existing-realm" // Should match existing
        }
    }
    
    @Nested
    inner class TriggerBnetSyncTests {
        @Test
        fun `should trigger bnet sync when enabled and configured`() {
            // Given
            val guildId = "guild-1"
            val config = createGuildConfiguration(
                guildId = guildId,
                bnetSyncEnabled = true,
                bnetRealmSlug = "realm",
                bnetGuildNameSlug = "guild",
                bnetRegion = "eu"
            )
            
            every { guildConfigurationRepository.findByGuildId(guildId) } returns config
            every { guildConfigurationRepository.save(any()) } returns config
            every { guildRosterSyncService.syncGuildRoster(any(), any(), any(), any()) } returns 
                GuildRosterSyncResult(created = 5, updated = 5, skipped = 0)

            // When
            val response = controller.triggerBnetSync(guildId, user)

            // Then
            response.body?.success shouldBe true
            verify { guildRosterSyncService.syncGuildRoster("realm", "guild", "eu", guildId) }
        }
    }
    
    @Nested
    inner class TriggerWowauditSyncTests {
        @Test
        fun `should trigger wowaudit sync when enabled and configured`() {
            // Given
            val guildId = "guild-1"
            val config = createGuildConfiguration(
                guildId = guildId,
                syncEnabled = true,
                wowauditGuildUri = "uri"
            )
            
            every { guildConfigurationRepository.findByGuildId(guildId) } returns config
            every { guildConfigurationRepository.save(any()) } returns config
            every { wowAuditRosterSyncService.syncRoster(guildId) } returns Mono.just(
                WoWAuditSyncResult(created = 2, updated = 3, skipped = 0, error = null)
            )

            // When
            val response = controller.triggerWowauditSync(guildId, user).block()

            // Then
            response?.body?.success shouldBe true
            verify { wowAuditRosterSyncService.syncRoster(guildId) }
        }
    }

    @Nested
    inner class TriggerWarcraftLogsSyncTests {
        @Test
        fun `should trigger warcraft logs sync when configured`() {
            // Given
            val guildId = "guild-1"
            // Assuming config is present (controller assumes implicitly enabled if accessed or similar)
            // Ideally we check if WarcraftLogs specific config exists, but current controller implementation 
            // doesn't block it based on a specific flag other than permission check.
            
            // We need returns for findByGuildId only if the controller checks it.
            // GuildSyncController.triggerWarcraftLogsSync doesn't fetch config in the provided code snippet (Step 15),
            // it only calls requireSettingsAccess and then the service.
            // Wait, I should check the controller code again to be sure.
            // Line 620: requireSettingsAccess(user, guildId)
            // Line 625: return warcraftLogsRosterSyncService.syncRoster(guildId)...
            // So it doesn't need config repository mock for this specific method (unlike others).
            // But let's mock it just in case `requireSettingsAccess` or future changes need it.
            // Actually `requireSettingsAccess` currently is mocked/bypassed or needs guildContextService.
            
            every { warcraftLogsRosterSyncService.syncRoster(guildId) } returns Mono.just(
                WarcraftLogsSyncResult(created = 0, updated = 3, skipped = 0, error = null)
            )

            // When
            val response = controller.triggerWarcraftLogsSync(guildId, user).block()

            // Then
            response?.body?.success shouldBe true
            verify { warcraftLogsRosterSyncService.syncRoster(guildId) }
        }
    }

    private fun createGuildConfiguration(
        id: Long = 1L,
        guildId: String = "test-guild",
        guildName: String = "Test Guild",
        wowauditGuildUri: String? = null,
        bnetRealmSlug: String? = null,
        bnetGuildNameSlug: String? = null,
        bnetRegion: String = "eu",
        syncEnabled: Boolean = false,
        bnetSyncEnabled: Boolean = false,
    ): GuildConfigurationEntity =
        GuildConfigurationEntity(
            id = id,
            guildId = guildId,
            guildName = guildName,
            guildDescription = null,
            wowauditApiKeyEncrypted = null,
            wowauditGuildUri = wowauditGuildUri,
            syncEnabled = syncEnabled,
            lastSyncAt = null,
            lastSyncStatus = null,
            lastSyncError = null,
            customBenchmarkRms = null,
            customBenchmarkIpi = null,
            benchmarkUpdatedAt = null,
            bnetRealmSlug = bnetRealmSlug,
            bnetGuildNameSlug = bnetGuildNameSlug,
            bnetRegion = bnetRegion,
            bnetSyncEnabled = bnetSyncEnabled,
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now()
        )
}
