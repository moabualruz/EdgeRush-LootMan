package com.edgerush.lootman.application.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class ManageLootBansUseCaseTest : UnitTest() {
    private val lootBanRepository = mockk<LootBanRepository>()
    private val useCase = ManageLootBansUseCase(lootBanRepository)

    @Test
    fun `should create loot ban successfully`() {
        // Arrange
        val command =
            CreateLootBanCommand(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Violation of loot rules",
                durationDays = 7,
            )

        every { lootBanRepository.save(any()) } answers { firstArg() }

        // Act
        val result = useCase.createBan(command)

        // Assert
        result.isSuccess shouldBe true
        val ban = result.getOrThrow()
        ban.reason shouldBe "Violation of loot rules"
        verify(exactly = 1) { lootBanRepository.save(any()) }
    }

    @Test
    fun `should get active bans for raider`() {
        // Arrange
        val raiderId = RaiderId("test-raider")
        val guildId = GuildId("test-guild")
        val activeBan =
            LootBan.create(
                raiderId = raiderId,
                guildId = guildId,
                reason = "Test ban",
                expiresAt = Instant.now().plusSeconds(7 * 24 * 60 * 60),
            )

        every { lootBanRepository.findActiveByRaiderId(raiderId, guildId) } returns listOf(activeBan)

        // Act
        val result = useCase.getActiveBans(raiderId, guildId)

        // Assert
        result.isSuccess shouldBe true
        result.getOrThrow().size shouldBe 1
    }
}

