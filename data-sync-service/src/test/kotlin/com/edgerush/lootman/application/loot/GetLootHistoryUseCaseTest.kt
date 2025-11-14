package com.edgerush.lootman.application.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test

class GetLootHistoryUseCaseTest : UnitTest() {
    private lateinit var lootAwardRepository: LootAwardRepository
    private lateinit var useCase: GetLootHistoryUseCase

    @org.junit.jupiter.api.BeforeEach
    fun setup() {
        lootAwardRepository = mockk()
        useCase = GetLootHistoryUseCase(lootAwardRepository)
    }

    @Test
    fun `should get loot history by guild ID`() {
        // Given
        val guildId = GuildId("guild-123")
        val query = GetLootHistoryByGuildQuery(guildId)

        val award1 = LootAward.create(
            itemId = ItemId(12345),
            raiderId = RaiderId("raider-1"),
            guildId = guildId,
            flpsScore = FlpsScore.of(0.85),
            tier = LootTier.MYTHIC
        )
        val award2 = LootAward.create(
            itemId = ItemId(12346),
            raiderId = RaiderId("raider-2"),
            guildId = guildId,
            flpsScore = FlpsScore.of(0.72),
            tier = LootTier.HEROIC
        )

        every { lootAwardRepository.findByGuildId(guildId) } returns listOf(award1, award2)

        // When
        val result = useCase.getByGuild(query)

        // Then
        result.isSuccess shouldBe true
        val awards = result.getOrThrow()
        awards.size shouldBe 2
        awards.all { it.guildId == guildId } shouldBe true

        verify(exactly = 1) { lootAwardRepository.findByGuildId(guildId) }
    }

    @Test
    fun `should get loot history by raider ID`() {
        // Given
        val raiderId = RaiderId("raider-123")
        val query = GetLootHistoryByRaiderQuery(raiderId)

        val award1 = LootAward.create(
            itemId = ItemId(12345),
            raiderId = raiderId,
            guildId = GuildId("guild-1"),
            flpsScore = FlpsScore.of(0.85),
            tier = LootTier.MYTHIC
        )
        val award2 = LootAward.create(
            itemId = ItemId(12346),
            raiderId = raiderId,
            guildId = GuildId("guild-1"),
            flpsScore = FlpsScore.of(0.72),
            tier = LootTier.HEROIC
        )

        every { lootAwardRepository.findByRaiderId(raiderId) } returns listOf(award1, award2)

        // When
        val result = useCase.getByRaider(query)

        // Then
        result.isSuccess shouldBe true
        val awards = result.getOrThrow()
        awards.size shouldBe 2
        awards.all { it.raiderId == raiderId } shouldBe true

        verify(exactly = 1) { lootAwardRepository.findByRaiderId(raiderId) }
    }

    @Test
    fun `should return empty list when no loot history exists for guild`() {
        // Given
        val guildId = GuildId("guild-123")
        val query = GetLootHistoryByGuildQuery(guildId)

        every { lootAwardRepository.findByGuildId(guildId) } returns emptyList()

        // When
        val result = useCase.getByGuild(query)

        // Then
        result.isSuccess shouldBe true
        val awards = result.getOrThrow()
        awards.isEmpty() shouldBe true

        verify(exactly = 1) { lootAwardRepository.findByGuildId(guildId) }
    }

    @Test
    fun `should return empty list when no loot history exists for raider`() {
        // Given
        val raiderId = RaiderId("raider-123")
        val query = GetLootHistoryByRaiderQuery(raiderId)

        every { lootAwardRepository.findByRaiderId(raiderId) } returns emptyList()

        // When
        val result = useCase.getByRaider(query)

        // Then
        result.isSuccess shouldBe true
        val awards = result.getOrThrow()
        awards.isEmpty() shouldBe true

        verify(exactly = 1) { lootAwardRepository.findByRaiderId(raiderId) }
    }

    @Test
    fun `should filter active awards only`() {
        // Given
        val guildId = GuildId("guild-123")
        val query = GetLootHistoryByGuildQuery(guildId, activeOnly = true)

        val activeAward = LootAward.create(
            itemId = ItemId(12345),
            raiderId = RaiderId("raider-1"),
            guildId = guildId,
            flpsScore = FlpsScore.of(0.85),
            tier = LootTier.MYTHIC
        )
        val revokedAward = LootAward.create(
            itemId = ItemId(12346),
            raiderId = RaiderId("raider-2"),
            guildId = guildId,
            flpsScore = FlpsScore.of(0.72),
            tier = LootTier.HEROIC
        ).revoke("Mistake")

        every { lootAwardRepository.findByGuildId(guildId) } returns listOf(activeAward, revokedAward)

        // When
        val result = useCase.getByGuild(query)

        // Then
        result.isSuccess shouldBe true
        val awards = result.getOrThrow()
        awards.size shouldBe 1
        awards.all { it.isActive() } shouldBe true

        verify(exactly = 1) { lootAwardRepository.findByGuildId(guildId) }
    }

    @Test
    fun `should include revoked awards when activeOnly is false`() {
        // Given
        val guildId = GuildId("guild-123")
        val query = GetLootHistoryByGuildQuery(guildId, activeOnly = false)

        val activeAward = LootAward.create(
            itemId = ItemId(12345),
            raiderId = RaiderId("raider-1"),
            guildId = guildId,
            flpsScore = FlpsScore.of(0.85),
            tier = LootTier.MYTHIC
        )
        val revokedAward = LootAward.create(
            itemId = ItemId(12346),
            raiderId = RaiderId("raider-2"),
            guildId = guildId,
            flpsScore = FlpsScore.of(0.72),
            tier = LootTier.HEROIC
        ).revoke("Mistake")

        every { lootAwardRepository.findByGuildId(guildId) } returns listOf(activeAward, revokedAward)

        // When
        val result = useCase.getByGuild(query)

        // Then
        result.isSuccess shouldBe true
        val awards = result.getOrThrow()
        awards.size shouldBe 2

        verify(exactly = 1) { lootAwardRepository.findByGuildId(guildId) }
    }

    @Test
    fun `should handle repository errors when getting by guild`() {
        // Given
        val guildId = GuildId("guild-123")
        val query = GetLootHistoryByGuildQuery(guildId)

        every { lootAwardRepository.findByGuildId(guildId) } throws RuntimeException("Database error")

        // When
        val result = useCase.getByGuild(query)

        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is RuntimeException) shouldBe true

        verify(exactly = 1) { lootAwardRepository.findByGuildId(guildId) }
    }

    @Test
    fun `should handle repository errors when getting by raider`() {
        // Given
        val raiderId = RaiderId("raider-123")
        val query = GetLootHistoryByRaiderQuery(raiderId)

        every { lootAwardRepository.findByRaiderId(raiderId) } throws RuntimeException("Database error")

        // When
        val result = useCase.getByRaider(query)

        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is RuntimeException) shouldBe true

        verify(exactly = 1) { lootAwardRepository.findByRaiderId(raiderId) }
    }
}
