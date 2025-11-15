package com.edgerush.lootman.application.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.loot.service.LootDistributionService
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.LootBanActiveException
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class AwardLootUseCaseTest : UnitTest() {
    private lateinit var lootAwardRepository: LootAwardRepository
    private lateinit var lootBanRepository: LootBanRepository
    private lateinit var lootDistributionService: LootDistributionService
    private lateinit var useCase: AwardLootUseCase

    @org.junit.jupiter.api.BeforeEach
    fun setup() {
        lootAwardRepository = mockk()
        lootBanRepository = mockk()
        lootDistributionService = mockk()
        useCase =
            AwardLootUseCase(
                lootAwardRepository,
                lootBanRepository,
                lootDistributionService,
            )
    }

    @Test
    fun `should award loot successfully when raider is eligible`() {
        // Given
        val command =
            AwardLootCommand(
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-456"),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

        every { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) } returns emptyList()
        every { lootDistributionService.isEligibleForLoot(command.raiderId, emptyList(), any()) } returns true
        every { lootAwardRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.execute(command)

        // Then
        result.isSuccess shouldBe true
        val lootAward = result.getOrThrow()
        lootAward.itemId shouldBe command.itemId
        lootAward.raiderId shouldBe command.raiderId
        lootAward.guildId shouldBe command.guildId
        lootAward.flpsScore shouldBe command.flpsScore
        lootAward.tier shouldBe command.tier
        lootAward.isActive() shouldBe true

        verify(exactly = 1) { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) }
        verify(exactly = 1) { lootDistributionService.isEligibleForLoot(command.raiderId, emptyList(), any()) }
        verify(exactly = 1) { lootAwardRepository.save(any()) }
    }

    @Test
    fun `should fail to award loot when raider has active ban`() {
        // Given
        val command =
            AwardLootCommand(
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-456"),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

        val activeBan =
            LootBan.create(
                raiderId = command.raiderId,
                guildId = command.guildId,
                reason = "Behavioral issues",
                expiresAt = Instant.now().plusSeconds(86400),
            )

        every { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) } returns listOf(activeBan)
        every { lootDistributionService.isEligibleForLoot(command.raiderId, listOf(activeBan), any()) } returns false

        // When
        val result = useCase.execute(command)

        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is LootBanActiveException) shouldBe true

        verify(exactly = 1) { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) }
        verify(exactly = 1) { lootDistributionService.isEligibleForLoot(command.raiderId, listOf(activeBan), any()) }
        verify(exactly = 0) { lootAwardRepository.save(any()) }
    }

    @Test
    fun `should handle repository errors gracefully`() {
        // Given
        val command =
            AwardLootCommand(
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-456"),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

        every { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) } throws RuntimeException("Database error")

        // When
        val result = useCase.execute(command)

        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is RuntimeException) shouldBe true

        verify(exactly = 1) { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) }
        verify(exactly = 0) { lootAwardRepository.save(any()) }
    }

    @Test
    fun `should award loot with expired ban present`() {
        // Given
        val command =
            AwardLootCommand(
                itemId = ItemId(12345),
                raiderId = RaiderId("raider-456"),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

        val expiredBan =
            LootBan.create(
                raiderId = command.raiderId,
                guildId = command.guildId,
                reason = "Past issues",
                expiresAt = Instant.now().minusSeconds(86400),
            )

        every { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) } returns listOf(expiredBan)
        every { lootDistributionService.isEligibleForLoot(command.raiderId, listOf(expiredBan), any()) } returns true
        every { lootAwardRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.execute(command)

        // Then
        result.isSuccess shouldBe true
        verify(exactly = 1) { lootAwardRepository.save(any()) }
    }
}
