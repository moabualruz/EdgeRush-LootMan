package com.edgerush.lootman.api.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.application.loot.AwardLootUseCase
import com.edgerush.lootman.application.loot.GetActiveBansQuery
import com.edgerush.lootman.application.loot.GetLootAwardUseCase
import com.edgerush.lootman.application.loot.GetLootBanUseCase
import com.edgerush.lootman.application.loot.GetLootHistoryByGuildQuery
import com.edgerush.lootman.application.loot.GetLootHistoryByRaiderQuery
import com.edgerush.lootman.application.loot.GetLootHistoryUseCase
import com.edgerush.lootman.application.loot.ListLootAwardsUseCase
import com.edgerush.lootman.application.loot.ManageLootBansUseCase
import com.edgerush.lootman.application.loot.RevokeLootAwardUseCase
import com.edgerush.lootman.application.loot.UpdateLootBanUseCase
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.time.Instant

/**
 * Unit tests for LootController.
 *
 * Tests controller methods directly without Spring context,
 * mocking use cases as dependencies.
 */
class LootControllerTest : UnitTest() {
    private lateinit var awardLootUseCase: AwardLootUseCase
    private lateinit var getLootHistoryUseCase: GetLootHistoryUseCase
    private lateinit var manageLootBansUseCase: ManageLootBansUseCase
    private lateinit var getLootAwardUseCase: GetLootAwardUseCase
    private lateinit var listLootAwardsUseCase: ListLootAwardsUseCase
    private lateinit var revokeLootAwardUseCase: RevokeLootAwardUseCase
    private lateinit var getLootBanUseCase: GetLootBanUseCase
    private lateinit var updateLootBanUseCase: UpdateLootBanUseCase
    private lateinit var controller: LootController

    @BeforeEach
    fun setup() {
        awardLootUseCase = mockk()
        getLootHistoryUseCase = mockk()
        manageLootBansUseCase = mockk()
        getLootAwardUseCase = mockk()
        listLootAwardsUseCase = mockk()
        revokeLootAwardUseCase = mockk()
        getLootBanUseCase = mockk()
        updateLootBanUseCase = mockk()
        controller = LootController(
            awardLootUseCase,
            getLootHistoryUseCase,
            manageLootBansUseCase,
            getLootAwardUseCase,
            listLootAwardsUseCase,
            revokeLootAwardUseCase,
            getLootBanUseCase,
            updateLootBanUseCase,
        )
    }

    @Test
    fun `awardLoot should return CREATED status with loot award dto`() {
        // Given
        val request = AwardLootRequest(
            itemId = 12345L,
            raiderId = "456",
            guildId = "guild-789",
            flpsScore = 0.85,
            tier = "MYTHIC",
        )

        val lootAward = LootAward.create(
            itemId = ItemId(12345L),
            raiderId = RaiderId(456L),
            guildId = GuildId("guild-789"),
            flpsScore = FlpsScore.of(0.85),
            tier = LootTier.MYTHIC,
        )

        every { awardLootUseCase.execute(any()) } returns Result.success(lootAward)

        // When
        val response = controller.awardLoot(request)

        // Then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body?.itemId shouldBe 12345L
        response.body?.raiderId shouldBe "456"
        response.body?.guildId shouldBe "guild-789"
        response.body?.flpsScore shouldBe 0.85
        response.body?.tier shouldBe "MYTHIC"
        response.body?.isActive shouldBe true

        verify(exactly = 1) { awardLootUseCase.execute(any()) }
    }

    @Test
    fun `awardLoot should pass correct command to use case`() {
        // Given
        val request = AwardLootRequest(
            itemId = 99999L,
            raiderId = "123",
            guildId = "test-guild",
            flpsScore = 0.72,
            tier = "HEROIC",
        )

        val commandSlot = slot<com.edgerush.lootman.application.loot.AwardLootCommand>()

        val lootAward = LootAward.create(
            itemId = ItemId(99999L),
            raiderId = RaiderId(123L),
            guildId = GuildId("test-guild"),
            flpsScore = FlpsScore.of(0.72),
            tier = LootTier.HEROIC,
        )

        every { awardLootUseCase.execute(capture(commandSlot)) } returns Result.success(lootAward)

        // When
        controller.awardLoot(request)

        // Then
        commandSlot.captured.itemId shouldBe ItemId(99999L)
        commandSlot.captured.raiderId shouldBe RaiderId(123L)
        commandSlot.captured.guildId shouldBe GuildId("test-guild")
        commandSlot.captured.flpsScore.value shouldBe 0.72
        commandSlot.captured.tier shouldBe LootTier.HEROIC
    }

    @Test
    fun `awardLoot should throw exception when use case fails`() {
        // Given
        val request = AwardLootRequest(
            itemId = 12345L,
            raiderId = "456",
            guildId = "guild-789",
            flpsScore = 0.85,
            tier = "MYTHIC",
        )

        every { awardLootUseCase.execute(any()) } returns Result.failure(RuntimeException("Award failed"))

        // When/Then
        try {
            controller.awardLoot(request)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            e.message shouldBe "Award failed"
        }
    }

    @Test
    fun `getGuildLootHistory should return loot history for guild`() {
        // Given
        val guildId = "test-guild"
        val lootAward = LootAward.create(
            itemId = ItemId(12345L),
            raiderId = RaiderId(456L),
            guildId = GuildId(guildId),
            flpsScore = FlpsScore.of(0.85),
            tier = LootTier.MYTHIC,
        )

        every { getLootHistoryUseCase.getByGuild(any()) } returns Result.success(listOf(lootAward))

        // When
        val response = controller.getGuildLootHistory(guildId, false)

        // Then
        response.awards shouldHaveSize 1
        response.awards[0].itemId shouldBe 12345L
        response.awards[0].guildId shouldBe guildId

        verify(exactly = 1) { getLootHistoryUseCase.getByGuild(any()) }
    }

    @Test
    fun `getGuildLootHistory should pass activeOnly parameter to use case`() {
        // Given
        val guildId = "test-guild"
        val querySlot = slot<GetLootHistoryByGuildQuery>()

        every { getLootHistoryUseCase.getByGuild(capture(querySlot)) } returns Result.success(emptyList())

        // When
        controller.getGuildLootHistory(guildId, true)

        // Then
        querySlot.captured.guildId shouldBe GuildId(guildId)
        querySlot.captured.activeOnly shouldBe true
    }

    @Test
    fun `getGuildLootHistory should return empty list when no history exists`() {
        // Given
        val guildId = "empty-guild"

        every { getLootHistoryUseCase.getByGuild(any()) } returns Result.success(emptyList())

        // When
        val response = controller.getGuildLootHistory(guildId, false)

        // Then
        response.awards shouldHaveSize 0
    }

    @Test
    fun `getRaiderLootHistory should return loot history for raider`() {
        // Given
        val raiderId = "456"
        val lootAward = LootAward.create(
            itemId = ItemId(12345L),
            raiderId = RaiderId(456L),
            guildId = GuildId("test-guild"),
            flpsScore = FlpsScore.of(0.85),
            tier = LootTier.MYTHIC,
        )

        every { getLootHistoryUseCase.getByRaider(any()) } returns Result.success(listOf(lootAward))

        // When
        val response = controller.getRaiderLootHistory(raiderId, false)

        // Then
        response.awards shouldHaveSize 1
        response.awards[0].raiderId shouldBe "456"

        verify(exactly = 1) { getLootHistoryUseCase.getByRaider(any()) }
    }

    @Test
    fun `getRaiderLootHistory should pass correct query to use case`() {
        // Given
        val raiderId = "789"
        val querySlot = slot<GetLootHistoryByRaiderQuery>()

        every { getLootHistoryUseCase.getByRaider(capture(querySlot)) } returns Result.success(emptyList())

        // When
        controller.getRaiderLootHistory(raiderId, true)

        // Then
        querySlot.captured.raiderId shouldBe RaiderId(789L)
        querySlot.captured.activeOnly shouldBe true
    }

    @Test
    fun `createBan should return CREATED status with loot ban dto`() {
        // Given
        val expiresAt = Instant.now().plusSeconds(86400)
        val request = CreateLootBanRequest(
            raiderId = "456",
            guildId = "guild-789",
            reason = "Behavioral issues",
            expiresAt = expiresAt,
        )

        val lootBan = LootBan.create(
            raiderId = RaiderId(456L),
            guildId = GuildId("guild-789"),
            reason = "Behavioral issues",
            expiresAt = expiresAt,
        )

        every { manageLootBansUseCase.createBan(any()) } returns Result.success(lootBan)

        // When
        val response = controller.createBan(request)

        // Then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body?.raiderId shouldBe "456"
        response.body?.guildId shouldBe "guild-789"
        response.body?.reason shouldBe "Behavioral issues"
        response.body?.isActive shouldBe true

        verify(exactly = 1) { manageLootBansUseCase.createBan(any()) }
    }

    @Test
    fun `createBan should handle permanent ban with null expiresAt`() {
        // Given
        val request = CreateLootBanRequest(
            raiderId = "456",
            guildId = "guild-789",
            reason = "Permanent ban reason",
            expiresAt = null,
        )

        val lootBan = LootBan.create(
            raiderId = RaiderId(456L),
            guildId = GuildId("guild-789"),
            reason = "Permanent ban reason",
            expiresAt = null,
        )

        every { manageLootBansUseCase.createBan(any()) } returns Result.success(lootBan)

        // When
        val response = controller.createBan(request)

        // Then
        response.statusCode shouldBe HttpStatus.CREATED
        response.body?.expiresAt shouldBe null
        response.body?.isActive shouldBe true
    }

    @Test
    fun `removeBan should return NO_CONTENT status on success`() {
        // Given
        val banId = "ban-123"

        every { manageLootBansUseCase.removeBan(any()) } returns Result.success(Unit)

        // When
        val response = controller.removeBan(banId)

        // Then
        response.statusCode shouldBe HttpStatus.NO_CONTENT

        verify(exactly = 1) { manageLootBansUseCase.removeBan(any()) }
    }

    @Test
    fun `removeBan should throw exception when ban not found`() {
        // Given
        val banId = "non-existent-ban"

        every { manageLootBansUseCase.removeBan(any()) } returns Result.failure(
            NoSuchElementException("Ban not found")
        )

        // When/Then
        try {
            controller.removeBan(banId)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: NoSuchElementException) {
            e.message shouldBe "Ban not found"
        }
    }

    @Test
    fun `getActiveBans should return active bans for raider`() {
        // Given
        val raiderId = "456"
        val guildId = "guild-789"
        val lootBan = LootBan.create(
            raiderId = RaiderId(456L),
            guildId = GuildId(guildId),
            reason = "Test ban",
            expiresAt = Instant.now().plusSeconds(86400),
        )

        every { manageLootBansUseCase.getActiveBans(any()) } returns Result.success(listOf(lootBan))

        // When
        val response = controller.getActiveBans(raiderId, guildId)

        // Then
        response.bans shouldHaveSize 1
        response.bans[0].raiderId shouldBe "456"
        response.bans[0].guildId shouldBe guildId
        response.bans[0].reason shouldBe "Test ban"

        verify(exactly = 1) { manageLootBansUseCase.getActiveBans(any()) }
    }

    @Test
    fun `getActiveBans should pass correct query to use case`() {
        // Given
        val raiderId = "456"
        val guildId = "guild-789"
        val querySlot = slot<GetActiveBansQuery>()

        every { manageLootBansUseCase.getActiveBans(capture(querySlot)) } returns Result.success(emptyList())

        // When
        controller.getActiveBans(raiderId, guildId)

        // Then
        querySlot.captured.raiderId shouldBe RaiderId(456L)
        querySlot.captured.guildId shouldBe GuildId(guildId)
    }

    @Test
    fun `getActiveBans should return empty list when no active bans`() {
        // Given
        val raiderId = "456"
        val guildId = "guild-789"

        every { manageLootBansUseCase.getActiveBans(any()) } returns Result.success(emptyList())

        // When
        val response = controller.getActiveBans(raiderId, guildId)

        // Then
        response.bans shouldHaveSize 0
    }
}
