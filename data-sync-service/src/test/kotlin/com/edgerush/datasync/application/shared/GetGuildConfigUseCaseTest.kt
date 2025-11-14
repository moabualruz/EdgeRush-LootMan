package com.edgerush.datasync.application.shared

import com.edgerush.datasync.domain.shared.model.BenchmarkMode
import com.edgerush.datasync.domain.shared.model.Guild
import com.edgerush.datasync.domain.shared.model.GuildId
import com.edgerush.datasync.domain.shared.repository.GuildRepository
import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

/**
 * Unit tests for GetGuildConfigUseCase.
 */
class GetGuildConfigUseCaseTest : UnitTest() {
    
    private lateinit var guildRepository: GuildRepository
    private lateinit var useCase: GetGuildConfigUseCase
    
    @BeforeEach
    fun setup() {
        guildRepository = mockk()
        useCase = GetGuildConfigUseCase(guildRepository)
    }
    
    @Test
    fun `should get guild configuration when guild exists`() {
        // Given
        val guildId = GuildId("test-guild")
        val guild = createTestGuild(guildId)
        
        every { guildRepository.findActiveById(guildId) } returns guild
        
        // When
        val result = useCase.execute(guildId)
        
        // Then
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe guild
        verify(exactly = 1) { guildRepository.findActiveById(guildId) }
    }
    
    @Test
    fun `should create default guild when guild does not exist`() {
        // Given
        val guildId = GuildId("new-guild")
        val savedGuild = slot<Guild>()
        
        every { guildRepository.findActiveById(guildId) } returns null
        every { guildRepository.save(capture(savedGuild)) } answers { savedGuild.captured }
        
        // When
        val result = useCase.execute(guildId)
        
        // Then
        result.isSuccess shouldBe true
        val guild = result.getOrNull()
        guild?.id shouldBe guildId
        guild?.name shouldBe "Guild new-guild"
        verify(exactly = 1) { guildRepository.findActiveById(guildId) }
        verify(exactly = 1) { guildRepository.save(any()) }
    }
    
    @Test
    fun `should get all active guilds`() {
        // Given
        val guilds = listOf(
            createTestGuild(GuildId("guild-1")),
            createTestGuild(GuildId("guild-2"))
        )
        
        every { guildRepository.findAllActive() } returns guilds
        
        // When
        val result = useCase.executeAll()
        
        // Then
        result.isSuccess shouldBe true
        result.getOrNull()?.size shouldBe 2
        verify(exactly = 1) { guildRepository.findAllActive() }
    }
    
    private fun createTestGuild(guildId: GuildId): Guild {
        val now = OffsetDateTime.now()
        return Guild(
            id = guildId,
            name = "Test Guild",
            description = "Test description",
            wowauditApiKeyEncrypted = "encrypted-key",
            wowauditGuildUri = "guild-uri",
            wowauditBaseUrl = "https://wowaudit.com",
            syncEnabled = true,
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
