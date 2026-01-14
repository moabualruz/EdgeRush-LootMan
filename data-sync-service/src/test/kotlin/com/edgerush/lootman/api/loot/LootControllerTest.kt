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
import com.edgerush.lootman.api.common.PaginationProperties
import com.edgerush.lootman.application.loot.RevokeLootAwardUseCase
import com.edgerush.lootman.application.loot.UpdateLootBanUseCase
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.application.loot.GetLootAwardQuery
import com.edgerush.lootman.application.loot.GetLootBanQuery
import com.edgerush.lootman.application.loot.ListLootAwardsByGuildPaginatedQuery
import com.edgerush.lootman.application.loot.ListLootAwardsByGuildQuery
import com.edgerush.lootman.application.loot.PaginatedLootAwards
import com.edgerush.lootman.application.loot.RevokeLootAwardCommand
import com.edgerush.lootman.application.loot.UpdateLootBanCommand
import com.edgerush.lootman.api.auth.CurrentUserService
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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
    private lateinit var paginationProperties: PaginationProperties
    private lateinit var currentUserService: CurrentUserService
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
        paginationProperties = PaginationProperties(defaultPageSize = 20, maxPageSize = 100)
        currentUserService = mockk()
        controller = LootController(
            awardLootUseCase,
            getLootHistoryUseCase,
            manageLootBansUseCase,
            getLootAwardUseCase,
            listLootAwardsUseCase,
            revokeLootAwardUseCase,
            getLootBanUseCase,
            updateLootBanUseCase,
            paginationProperties,
            currentUserService,
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
    fun `getGuildLootHistory should throw exception when use case fails`() {
        // Given
        val guildId = "test-guild"

        every { getLootHistoryUseCase.getByGuild(any()) } returns Result.failure(
            RuntimeException("Database error")
        )

        // When/Then
        try {
            controller.getGuildLootHistory(guildId, false)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            e.message shouldBe "Database error"
        }
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
    fun `getRaiderLootHistory should throw exception when use case fails`() {
        // Given
        val raiderId = "456"

        every { getLootHistoryUseCase.getByRaider(any()) } returns Result.failure(
            RuntimeException("Raider lookup failed")
        )

        // When/Then
        try {
            controller.getRaiderLootHistory(raiderId, false)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            e.message shouldBe "Raider lookup failed"
        }
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
    fun `createBan should throw exception when use case fails`() {
        // Given
        val request = CreateLootBanRequest(
            raiderId = "456",
            guildId = "guild-789",
            reason = "Test ban",
            expiresAt = null,
        )

        every { manageLootBansUseCase.createBan(any()) } returns Result.failure(
            RuntimeException("Ban creation failed")
        )

        // When/Then
        try {
            controller.createBan(request)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            e.message shouldBe "Ban creation failed"
        }
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

    @Test
    fun `getActiveBans should throw exception when use case fails`() {
        // Given
        val raiderId = "456"
        val guildId = "guild-789"

        every { manageLootBansUseCase.getActiveBans(any()) } returns Result.failure(
            RuntimeException("Failed to get active bans")
        )

        // When/Then
        try {
            controller.getActiveBans(raiderId, guildId)
            throw AssertionError("Expected exception was not thrown")
        } catch (e: RuntimeException) {
            e.message shouldBe "Failed to get active bans"
        }
    }

    @Nested
    inner class ListAllLootAwardsTest {

        @Test
        fun `listAllLootAwards should return all awards for guild`() {
            // Given
            val guildId = "test-guild"
            val awards = listOf(
                LootAward.create(
                    itemId = ItemId(111L),
                    raiderId = RaiderId(123L),
                    guildId = GuildId(guildId),
                    flpsScore = FlpsScore.of(0.8),
                    tier = LootTier.MYTHIC
                ),
                LootAward.create(
                    itemId = ItemId(222L),
                    raiderId = RaiderId(456L),
                    guildId = GuildId(guildId),
                    flpsScore = FlpsScore.of(0.7),
                    tier = LootTier.HEROIC
                )
            )

            every { listLootAwardsUseCase.executeByGuild(any()) } returns Result.success(awards)

            // When
            val response = controller.listAllLootAwards(guildId)

            // Then
            response.awards shouldHaveSize 2
            response.totalCount shouldBe 2
            response.activeCount shouldBe 2

            verify(exactly = 1) { listLootAwardsUseCase.executeByGuild(any()) }
        }

        @Test
        fun `listAllLootAwards should pass correct query to use case`() {
            // Given
            val guildId = "test-guild"
            val querySlot = slot<ListLootAwardsByGuildQuery>()

            every { listLootAwardsUseCase.executeByGuild(capture(querySlot)) } returns Result.success(emptyList())

            // When
            controller.listAllLootAwards(guildId)

            // Then
            querySlot.captured.guildId shouldBe guildId
        }

        @Test
        fun `listAllLootAwards should return empty list when no awards`() {
            // Given
            val guildId = "empty-guild"

            every { listLootAwardsUseCase.executeByGuild(any()) } returns Result.success(emptyList())

            // When
            val response = controller.listAllLootAwards(guildId)

            // Then
            response.awards shouldHaveSize 0
            response.totalCount shouldBe 0
            response.activeCount shouldBe 0
        }

        @Test
        fun `listAllLootAwards should throw exception when use case fails`() {
            // Given
            val guildId = "test-guild"

            every { listLootAwardsUseCase.executeByGuild(any()) } returns Result.failure(
                RuntimeException("Database error")
            )

            // When/Then
            try {
                controller.listAllLootAwards(guildId)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: RuntimeException) {
                e.message shouldBe "Database error"
            }
        }
    }

    @Nested
    inner class ListLootAwardsPaginatedTest {

        @Test
        fun `listLootAwards should return paginated awards`() {
            // Given
            val guildId = "test-guild"
            val page = 0
            val size = 10
            val awards = listOf(
                LootAward.create(
                    itemId = ItemId(111L),
                    raiderId = RaiderId(123L),
                    guildId = GuildId(guildId),
                    flpsScore = FlpsScore.of(0.8),
                    tier = LootTier.MYTHIC
                )
            )
            val paginatedResult = PaginatedLootAwards(awards, 25L)

            every { listLootAwardsUseCase.executeByGuildPaginated(any()) } returns Result.success(paginatedResult)

            // When
            val response = controller.listLootAwards(guildId, page, size)

            // Then
            response.content shouldHaveSize 1
            response.totalElements shouldBe 25
            response.page shouldBe 0
            response.size shouldBe 10
            response.totalPages shouldBe 3

            verify(exactly = 1) { listLootAwardsUseCase.executeByGuildPaginated(any()) }
        }

        @Test
        fun `listLootAwards should use default page size when not provided`() {
            // Given
            val guildId = "test-guild"
            val page = 0
            val querySlot = slot<ListLootAwardsByGuildPaginatedQuery>()

            every { listLootAwardsUseCase.executeByGuildPaginated(capture(querySlot)) } returns Result.success(
                PaginatedLootAwards(emptyList(), 0L)
            )

            // When
            controller.listLootAwards(guildId, page, null)

            // Then
            querySlot.captured.guildId shouldBe guildId
            querySlot.captured.limit shouldBe 20 // default page size from paginationProperties
        }

        @Test
        fun `listLootAwards should pass correct offset for page`() {
            // Given
            val guildId = "test-guild"
            val page = 2
            val size = 10
            val querySlot = slot<ListLootAwardsByGuildPaginatedQuery>()

            every { listLootAwardsUseCase.executeByGuildPaginated(capture(querySlot)) } returns Result.success(
                PaginatedLootAwards(emptyList(), 0L)
            )

            // When
            controller.listLootAwards(guildId, page, size)

            // Then
            querySlot.captured.offset shouldBe 20L // page 2 * size 10 = offset 20
            querySlot.captured.limit shouldBe 10
        }

        @Test
        fun `listLootAwards should throw exception when use case fails`() {
            // Given
            val guildId = "test-guild"

            every { listLootAwardsUseCase.executeByGuildPaginated(any()) } returns Result.failure(
                RuntimeException("Query failed")
            )

            // When/Then
            try {
                controller.listLootAwards(guildId, 0, 10)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: RuntimeException) {
                e.message shouldBe "Query failed"
            }
        }
    }

    @Nested
    inner class GetLootAwardTest {

        @Test
        fun `getLootAward should return award when found`() {
            // Given
            val awardId = "award-123"
            val award = LootAward.create(
                itemId = ItemId(12345L),
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC
            )

            every { getLootAwardUseCase.execute(any()) } returns Result.success(award)

            // When
            val response = controller.getLootAward(awardId)

            // Then
            response.itemId shouldBe 12345L
            response.raiderId shouldBe "456"
            response.guildId shouldBe "guild-789"
            response.flpsScore shouldBe 0.85
            response.tier shouldBe "MYTHIC"
            response.isActive shouldBe true

            verify(exactly = 1) { getLootAwardUseCase.execute(any()) }
        }

        @Test
        fun `getLootAward should pass correct query to use case`() {
            // Given
            val awardId = "award-123"
            val querySlot = slot<GetLootAwardQuery>()
            val award = LootAward.create(
                itemId = ItemId(12345L),
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC
            )

            every { getLootAwardUseCase.execute(capture(querySlot)) } returns Result.success(award)

            // When
            controller.getLootAward(awardId)

            // Then
            querySlot.captured.awardId shouldBe awardId
        }

        @Test
        fun `getLootAward should throw exception when not found`() {
            // Given
            val awardId = "non-existent"

            every { getLootAwardUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Award not found: $awardId")
            )

            // When/Then
            try {
                controller.getLootAward(awardId)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Award not found: non-existent"
            }
        }
    }

    @Nested
    inner class RevokeLootAwardTest {

        @Test
        fun `revokeLootAward should return NO_CONTENT when successful`() {
            // Given
            val awardId = "award-123"

            every { revokeLootAwardUseCase.execute(any()) } returns Result.success(Unit)

            // When
            val response = controller.revokeLootAward(awardId)

            // Then
            response.statusCode shouldBe HttpStatus.NO_CONTENT

            verify(exactly = 1) { revokeLootAwardUseCase.execute(any()) }
        }

        @Test
        fun `revokeLootAward should pass correct command to use case`() {
            // Given
            val awardId = "award-456"
            val commandSlot = slot<RevokeLootAwardCommand>()

            every { revokeLootAwardUseCase.execute(capture(commandSlot)) } returns Result.success(Unit)

            // When
            controller.revokeLootAward(awardId)

            // Then
            commandSlot.captured.awardId shouldBe awardId
        }

        @Test
        fun `revokeLootAward should throw exception when award not found`() {
            // Given
            val awardId = "non-existent"

            every { revokeLootAwardUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Award not found: $awardId")
            )

            // When/Then
            try {
                controller.revokeLootAward(awardId)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Award not found: non-existent"
            }
        }
    }

    @Nested
    inner class GetLootBanTest {

        @Test
        fun `getLootBan should return ban when found`() {
            // Given
            val banId = "ban-123"
            val expiresAt = Instant.now().plusSeconds(86400)
            val ban = LootBan.create(
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                reason = "Test ban reason",
                expiresAt = expiresAt
            )

            every { getLootBanUseCase.execute(any()) } returns Result.success(ban)

            // When
            val response = controller.getLootBan(banId)

            // Then
            response.raiderId shouldBe "456"
            response.guildId shouldBe "guild-789"
            response.reason shouldBe "Test ban reason"
            response.isActive shouldBe true

            verify(exactly = 1) { getLootBanUseCase.execute(any()) }
        }

        @Test
        fun `getLootBan should pass correct query to use case`() {
            // Given
            val banId = "ban-123"
            val querySlot = slot<GetLootBanQuery>()
            val ban = LootBan.create(
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                reason = "Test ban",
                expiresAt = null
            )

            every { getLootBanUseCase.execute(capture(querySlot)) } returns Result.success(ban)

            // When
            controller.getLootBan(banId)

            // Then
            querySlot.captured.banId shouldBe banId
        }

        @Test
        fun `getLootBan should throw exception when not found`() {
            // Given
            val banId = "non-existent"

            every { getLootBanUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Ban not found: $banId")
            )

            // When/Then
            try {
                controller.getLootBan(banId)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Ban not found: non-existent"
            }
        }
    }

    @Nested
    inner class UpdateLootBanTest {

        @Test
        fun `updateLootBan should return updated ban`() {
            // Given
            val banId = "ban-123"
            val newExpiry = Instant.now().plusSeconds(86400 * 7)
            val request = UpdateLootBanRequest(
                reason = "Updated reason",
                expiresAt = newExpiry
            )
            val updatedBan = LootBan.create(
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                reason = "Updated reason",
                expiresAt = newExpiry
            )

            every { updateLootBanUseCase.execute(any()) } returns Result.success(updatedBan)

            // When
            val response = controller.updateLootBan(banId, request)

            // Then
            response.raiderId shouldBe "456"
            response.guildId shouldBe "guild-789"
            response.reason shouldBe "Updated reason"
            response.expiresAt shouldBe newExpiry

            verify(exactly = 1) { updateLootBanUseCase.execute(any()) }
        }

        @Test
        fun `updateLootBan should pass correct command to use case`() {
            // Given
            val banId = "ban-123"
            val newExpiry = Instant.now().plusSeconds(86400)
            val request = UpdateLootBanRequest(
                reason = "New reason",
                expiresAt = newExpiry
            )
            val commandSlot = slot<UpdateLootBanCommand>()
            val ban = LootBan.create(
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                reason = "New reason",
                expiresAt = newExpiry
            )

            every { updateLootBanUseCase.execute(capture(commandSlot)) } returns Result.success(ban)

            // When
            controller.updateLootBan(banId, request)

            // Then
            commandSlot.captured.banId shouldBe banId
            commandSlot.captured.reason shouldBe "New reason"
            commandSlot.captured.expiresAt shouldBe newExpiry
        }

        @Test
        fun `updateLootBan should handle partial update with only reason`() {
            // Given
            val banId = "ban-123"
            val request = UpdateLootBanRequest(
                reason = "Only reason updated",
                expiresAt = null
            )
            val commandSlot = slot<UpdateLootBanCommand>()
            val ban = LootBan.create(
                raiderId = RaiderId(456L),
                guildId = GuildId("guild-789"),
                reason = "Only reason updated",
                expiresAt = null
            )

            every { updateLootBanUseCase.execute(capture(commandSlot)) } returns Result.success(ban)

            // When
            controller.updateLootBan(banId, request)

            // Then
            commandSlot.captured.reason shouldBe "Only reason updated"
            commandSlot.captured.expiresAt shouldBe null
        }

        @Test
        fun `updateLootBan should throw exception when ban not found`() {
            // Given
            val banId = "non-existent"
            val request = UpdateLootBanRequest(reason = "New reason")

            every { updateLootBanUseCase.execute(any()) } returns Result.failure(
                NoSuchElementException("Ban not found: $banId")
            )

            // When/Then
            try {
                controller.updateLootBan(banId, request)
                throw AssertionError("Expected exception was not thrown")
            } catch (e: NoSuchElementException) {
                e.message shouldBe "Ban not found: non-existent"
            }
        }
    }
}
