package com.edgerush.lootman.application.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.loot.repository.LootAwardRepository
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for additional Loot use cases.
 *
 * Tests verify:
 * - GetLootAwardUseCase
 * - ListLootAwardsUseCase
 * - RevokeLootAwardUseCase
 * - GetLootBanUseCase
 * - UpdateLootBanUseCase
 */
class LootUseCasesTest : UnitTest() {
    private lateinit var lootAwardRepository: LootAwardRepository
    private lateinit var lootBanRepository: LootBanRepository

    @BeforeEach
    fun setUp() {
        lootAwardRepository = mockk(relaxed = true)
        lootBanRepository = mockk(relaxed = true)
    }

    @Nested
    inner class GetLootAwardUseCaseTests {
        private lateinit var useCase: GetLootAwardUseCase

        @BeforeEach
        fun setUp() {
            useCase = GetLootAwardUseCase(lootAwardRepository)
        }

        @Test
        fun `should return loot award when found`() {
            // Given
            val awardId = "award-123"
            val award = createLootAward(awardId)
            every { lootAwardRepository.findById(LootAwardId(awardId)) } returns award

            // When
            val result = useCase.execute(GetLootAwardQuery(awardId))

            // Then
            result.isSuccess shouldBe true
            result.getOrNull()?.id?.value shouldBe awardId
        }

        @Test
        fun `should return failure when award not found`() {
            // Given
            val awardId = "non-existent"
            every { lootAwardRepository.findById(LootAwardId(awardId)) } returns null

            // When
            val result = useCase.execute(GetLootAwardQuery(awardId))

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldNotBe null
            result.exceptionOrNull()?.message shouldBe "Loot award not found: $awardId"
        }
    }

    @Nested
    inner class ListLootAwardsUseCaseTests {
        private lateinit var useCase: ListLootAwardsUseCase

        @BeforeEach
        fun setUp() {
            useCase = ListLootAwardsUseCase(lootAwardRepository)
        }

        @Test
        fun `should return loot awards for guild`() {
            // Given
            val guildId = "test-guild"
            val awards =
                listOf(
                    createLootAward("award-1"),
                    createLootAward("award-2"),
                )
            every { lootAwardRepository.findByGuildId(GuildId(guildId)) } returns awards

            // When
            val result = useCase.executeByGuild(ListLootAwardsByGuildQuery(guildId))

            // Then
            result.isSuccess shouldBe true
            result.getOrNull()?.size shouldBe 2
        }

        @Test
        fun `should return empty list when no awards found`() {
            // Given
            val guildId = "empty-guild"
            every { lootAwardRepository.findByGuildId(GuildId(guildId)) } returns emptyList()

            // When
            val result = useCase.executeByGuild(ListLootAwardsByGuildQuery(guildId))

            // Then
            result.isSuccess shouldBe true
            result.getOrNull() shouldBe emptyList()
        }

        @Test
        fun `should return paginated loot awards`() {
            // Given
            val guildId = "test-guild"
            val awards =
                listOf(
                    createLootAward("award-1"),
                    createLootAward("award-2"),
                )
            every { lootAwardRepository.findByGuildId(GuildId(guildId), 0, 10) } returns awards
            every { lootAwardRepository.countByGuildId(GuildId(guildId)) } returns 50L

            // When
            val result =
                useCase.executeByGuildPaginated(
                    ListLootAwardsByGuildPaginatedQuery(guildId, offset = 0, limit = 10),
                )

            // Then
            result.isSuccess shouldBe true
            val paginated = result.getOrNull()!!
            paginated.awards.size shouldBe 2
            paginated.totalCount shouldBe 50L
        }

        @Test
        fun `should return paginated results with offset`() {
            // Given
            val guildId = "test-guild"
            val awards = listOf(createLootAward("award-3"))
            every { lootAwardRepository.findByGuildId(GuildId(guildId), 20, 10) } returns awards
            every { lootAwardRepository.countByGuildId(GuildId(guildId)) } returns 25L

            // When
            val result =
                useCase.executeByGuildPaginated(
                    ListLootAwardsByGuildPaginatedQuery(guildId, offset = 20, limit = 10),
                )

            // Then
            result.isSuccess shouldBe true
            val paginated = result.getOrNull()!!
            paginated.awards.size shouldBe 1
            paginated.totalCount shouldBe 25L
        }
    }

    @Nested
    inner class RevokeLootAwardUseCaseTests {
        private lateinit var useCase: RevokeLootAwardUseCase

        @BeforeEach
        fun setUp() {
            useCase = RevokeLootAwardUseCase(lootAwardRepository)
        }

        @Test
        fun `should revoke loot award successfully`() {
            // Given
            val awardId = "award-to-revoke"
            val award = createLootAward(awardId)
            every { lootAwardRepository.findById(LootAwardId(awardId)) } returns award
            every { lootAwardRepository.delete(LootAwardId(awardId)) } returns Unit

            // When
            val result = useCase.execute(RevokeLootAwardCommand(awardId))

            // Then
            result.isSuccess shouldBe true
            verify { lootAwardRepository.delete(LootAwardId(awardId)) }
        }

        @Test
        fun `should return failure when award not found for revoke`() {
            // Given
            val awardId = "non-existent"
            every { lootAwardRepository.findById(LootAwardId(awardId)) } returns null

            // When
            val result = useCase.execute(RevokeLootAwardCommand(awardId))

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldBe "Loot award not found: $awardId"
        }
    }

    @Nested
    inner class GetLootBanUseCaseTests {
        private lateinit var useCase: GetLootBanUseCase

        @BeforeEach
        fun setUp() {
            useCase = GetLootBanUseCase(lootBanRepository)
        }

        @Test
        fun `should return loot ban when found`() {
            // Given
            val banId = "ban-123"
            val ban = createLootBan(banId)
            every { lootBanRepository.findById(LootBanId(banId)) } returns ban

            // When
            val result = useCase.execute(GetLootBanQuery(banId))

            // Then
            result.isSuccess shouldBe true
            result.getOrNull()?.id?.value shouldBe banId
        }

        @Test
        fun `should return failure when ban not found`() {
            // Given
            val banId = "non-existent"
            every { lootBanRepository.findById(LootBanId(banId)) } returns null

            // When
            val result = useCase.execute(GetLootBanQuery(banId))

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldNotBe null
            result.exceptionOrNull()?.message shouldBe "Loot ban not found: $banId"
        }
    }

    @Nested
    inner class UpdateLootBanUseCaseTests {
        private lateinit var useCase: UpdateLootBanUseCase

        @BeforeEach
        fun setUp() {
            useCase = UpdateLootBanUseCase(lootBanRepository)
        }

        @Test
        fun `should update loot ban successfully`() {
            // Given
            val banId = "ban-456"
            val existingBan = createLootBan(banId, reason = "Original reason")
            every { lootBanRepository.findById(LootBanId(banId)) } returns existingBan

            val savedBanSlot = slot<LootBan>()
            every { lootBanRepository.save(capture(savedBanSlot)) } answers { savedBanSlot.captured }

            val newExpiry = Instant.now().plusSeconds(86400 * 7)
            val command =
                UpdateLootBanCommand(
                    banId = banId,
                    reason = "Updated reason",
                    expiresAt = newExpiry,
                )

            // When
            val result = useCase.execute(command)

            // Then
            result.isSuccess shouldBe true
            savedBanSlot.captured.reason shouldBe "Updated reason"
            savedBanSlot.captured.expiresAt shouldBe newExpiry
            verify { lootBanRepository.save(any()) }
        }

        @Test
        fun `should return failure when ban not found for update`() {
            // Given
            val banId = "non-existent"
            every { lootBanRepository.findById(LootBanId(banId)) } returns null

            val command =
                UpdateLootBanCommand(
                    banId = banId,
                    reason = "New reason",
                )

            // When
            val result = useCase.execute(command)

            // Then
            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldBe "Loot ban not found: $banId"
        }

        @Test
        fun `should preserve unchanged fields`() {
            // Given
            val banId = "ban-789"
            val originalExpiry = Instant.now().plusSeconds(86400)
            val existingBan = createLootBan(banId, reason = "Original reason", expiresAt = originalExpiry)
            every { lootBanRepository.findById(LootBanId(banId)) } returns existingBan

            val savedBanSlot = slot<LootBan>()
            every { lootBanRepository.save(capture(savedBanSlot)) } answers { savedBanSlot.captured }

            // Only update reason
            val command =
                UpdateLootBanCommand(
                    banId = banId,
                    reason = "Updated reason",
                )

            // When
            useCase.execute(command)

            // Then
            savedBanSlot.captured.reason shouldBe "Updated reason"
            savedBanSlot.captured.expiresAt shouldBe originalExpiry
        }

        @Test
        fun `should update only expiration when reason is null`() {
            // Given
            val banId = "ban-abc"
            val existingBan = createLootBan(banId, reason = "Keep this reason")
            every { lootBanRepository.findById(LootBanId(banId)) } returns existingBan

            val savedBanSlot = slot<LootBan>()
            every { lootBanRepository.save(capture(savedBanSlot)) } answers { savedBanSlot.captured }

            val newExpiry = Instant.now().plusSeconds(86400 * 14)
            val command =
                UpdateLootBanCommand(
                    banId = banId,
                    expiresAt = newExpiry,
                )

            // When
            useCase.execute(command)

            // Then
            savedBanSlot.captured.reason shouldBe "Keep this reason"
            savedBanSlot.captured.expiresAt shouldBe newExpiry
        }
    }

    // Helper methods
    private fun createLootAward(
        id: String = "award-1",
        itemId: Long = 12345L,
        raiderId: Long = 1L,
        guildId: String = "test-guild",
        tier: LootTier = LootTier.MYTHIC,
    ): LootAward =
        LootAward(
            id = LootAwardId(id),
            itemId = ItemId(itemId),
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            awardedAt = Instant.now(),
            flpsScore = FlpsScore.of(0.85),
            tier = tier,
        )

    private fun createLootBan(
        id: String = "ban-1",
        raiderId: Long = 1L,
        guildId: String = "test-guild",
        reason: String = "Test ban reason",
        expiresAt: Instant? = Instant.now().plusSeconds(86400),
    ): LootBan =
        LootBan(
            id = LootBanId(id),
            raiderId = RaiderId(raiderId),
            guildId = GuildId(guildId),
            reason = reason,
            bannedAt = Instant.now(),
            expiresAt = expiresAt,
        )
}
