package com.edgerush.lootman.application.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.flps.FlpsComponentCalculator
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.flps.repository.RaiderPerformanceRepository
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.loot.service.LootDistributionService
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.InsufficientPerformanceException
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
                raiderId = RaiderId(456L),
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
                raiderId = RaiderId(456L),
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
                raiderId = RaiderId(456L),
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
                raiderId = RaiderId(456L),
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

    /**
     * EdgeRush Upgrade: Contextual Loot Distribution
     * Tests for MAS (Mechanical Adherence Score) threshold validation.
     */
    @org.junit.jupiter.api.Nested
    inner class MasThresholdValidation {

        private lateinit var raiderPerformanceRepository: RaiderPerformanceRepository
        private lateinit var flpsComponentCalculator: FlpsComponentCalculator
        private lateinit var useCaseWithMas: AwardLootUseCase

        @org.junit.jupiter.api.BeforeEach
        fun setupMasTests() {
            raiderPerformanceRepository = mockk()
            flpsComponentCalculator = mockk()
            // Note: This will initially fail to compile because AwardLootUseCase
            // doesn't accept these new dependencies yet - that's the TDD approach
            useCaseWithMas = AwardLootUseCase(
                lootAwardRepository = lootAwardRepository,
                lootBanRepository = lootBanRepository,
                lootDistributionService = lootDistributionService,
                raiderPerformanceRepository = raiderPerformanceRepository,
                flpsComponentCalculator = flpsComponentCalculator,
            )
        }

        @Test
        fun `should reject loot award when raider MAS is below threshold`() {
            // Given
            val command = AwardLootCommand(
                itemId = ItemId(12345),
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

            val lowPerformanceData = RaiderPerformanceData.create(
                raiderId = command.raiderId,
                characterName = "TestChar",
                characterRealm = "TestRealm",
                totalDeaths = 10,
                totalFights = 5,
                avoidableDamagePercentage = 80.0,
                periodStart = Instant.now().minusSeconds(2592000),
                periodEnd = Instant.now(),
            )

            every { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) } returns emptyList()
            every { lootDistributionService.isEligibleForLoot(command.raiderId, emptyList(), any()) } returns true
            every { raiderPerformanceRepository.findByRaiderAndPeriod(command.raiderId, command.guildId, any(), any()) } returns lowPerformanceData
            every { flpsComponentCalculator.calculateMAS(lowPerformanceData) } returns MechanicalAdherenceScore.of(0.3)

            // When
            val result = useCaseWithMas.execute(command)

            // Then
            result.isFailure shouldBe true
            (result.exceptionOrNull() is InsufficientPerformanceException) shouldBe true
            val exception = result.exceptionOrNull() as InsufficientPerformanceException
            exception.actualScore.value shouldBe 0.3
            exception.requiredThreshold shouldBe 0.5

            verify(exactly = 0) { lootAwardRepository.save(any()) }
        }

        @Test
        fun `should award loot when raider MAS meets threshold`() {
            // Given
            val command = AwardLootCommand(
                itemId = ItemId(12345),
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )

            val goodPerformanceData = RaiderPerformanceData.create(
                raiderId = command.raiderId,
                characterName = "TestChar",
                characterRealm = "TestRealm",
                totalDeaths = 2,
                totalFights = 10,
                avoidableDamagePercentage = 15.0,
                periodStart = Instant.now().minusSeconds(2592000),
                periodEnd = Instant.now(),
            )

            every { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) } returns emptyList()
            every { lootDistributionService.isEligibleForLoot(command.raiderId, emptyList(), any()) } returns true
            every { raiderPerformanceRepository.findByRaiderAndPeriod(command.raiderId, command.guildId, any(), any()) } returns goodPerformanceData
            every { flpsComponentCalculator.calculateMAS(goodPerformanceData) } returns MechanicalAdherenceScore.of(0.75)
            every { lootAwardRepository.save(any()) } answers { firstArg() }

            // When
            val result = useCaseWithMas.execute(command)

            // Then
            result.isSuccess shouldBe true
            val lootAward = result.getOrThrow()
            lootAward.itemId shouldBe command.itemId

            verify(exactly = 1) { lootAwardRepository.save(any()) }
        }

        @Test
        fun `should award loot when no performance data exists with bypass flag`() {
            // Given - No performance data available (new raider)
            val command = AwardLootCommand(
                itemId = ItemId(12345),
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
                bypassMasCheck = true, // Officer override
            )

            every { lootBanRepository.findActiveByRaiderId(command.raiderId, command.guildId) } returns emptyList()
            every { lootDistributionService.isEligibleForLoot(command.raiderId, emptyList(), any()) } returns true
            every { raiderPerformanceRepository.findByRaiderAndPeriod(command.raiderId, command.guildId, any(), any()) } returns null
            every { lootAwardRepository.save(any()) } answers { firstArg() }

            // When
            val result = useCaseWithMas.execute(command)

            // Then
            result.isSuccess shouldBe true
            verify(exactly = 1) { lootAwardRepository.save(any()) }
        }
    }
}

