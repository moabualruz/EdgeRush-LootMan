package com.edgerush.lootman.api.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for loot DTOs.
 *
 * Tests DTO construction, mapping from domain models, and data integrity.
 */
class LootDtoTest : UnitTest() {

    @Nested
    inner class AwardLootRequestTest {

        @Test
        fun `should create request with all fields`() {
            val request = AwardLootRequest(
                itemId = 12345L,
                raiderId = "raider-123",
                guildId = "guild-456",
                flpsScore = 0.85,
                tier = "MYTHIC"
            )

            request.itemId shouldBe 12345L
            request.raiderId shouldBe "raider-123"
            request.guildId shouldBe "guild-456"
            request.flpsScore shouldBe 0.85
            request.tier shouldBe "MYTHIC"
        }

        @Test
        fun `should support equality`() {
            val request1 = AwardLootRequest(
                itemId = 12345L,
                raiderId = "raider-123",
                guildId = "guild-456",
                flpsScore = 0.85,
                tier = "MYTHIC"
            )
            val request2 = AwardLootRequest(
                itemId = 12345L,
                raiderId = "raider-123",
                guildId = "guild-456",
                flpsScore = 0.85,
                tier = "MYTHIC"
            )

            request1 shouldBe request2
        }
    }

    @Nested
    inner class CreateLootBanRequestTest {

        @Test
        fun `should create request with expiration`() {
            val expiresAt = Instant.now().plusSeconds(86400)
            val request = CreateLootBanRequest(
                raiderId = "raider-123",
                guildId = "guild-456",
                reason = "Repeated loot hoarding",
                expiresAt = expiresAt
            )

            request.raiderId shouldBe "raider-123"
            request.guildId shouldBe "guild-456"
            request.reason shouldBe "Repeated loot hoarding"
            request.expiresAt shouldBe expiresAt
        }

        @Test
        fun `should create permanent ban request`() {
            val request = CreateLootBanRequest(
                raiderId = "raider-123",
                guildId = "guild-456",
                reason = "Permanent loot ban",
                expiresAt = null
            )

            request.expiresAt shouldBe null
        }
    }

    @Nested
    inner class UpdateLootBanRequestTest {

        @Test
        fun `should create request with all fields`() {
            val newExpiry = Instant.now().plusSeconds(86400 * 7)
            val request = UpdateLootBanRequest(
                reason = "Updated reason",
                expiresAt = newExpiry
            )

            request.reason shouldBe "Updated reason"
            request.expiresAt shouldBe newExpiry
        }

        @Test
        fun `should create request with default null fields`() {
            val request = UpdateLootBanRequest()

            request.reason shouldBe null
            request.expiresAt shouldBe null
        }

        @Test
        fun `should create request with only reason`() {
            val request = UpdateLootBanRequest(
                reason = "New reason only"
            )

            request.reason shouldBe "New reason only"
            request.expiresAt shouldBe null
        }

        @Test
        fun `should create request with only expiresAt`() {
            val newExpiry = Instant.now().plusSeconds(86400)
            val request = UpdateLootBanRequest(
                expiresAt = newExpiry
            )

            request.reason shouldBe null
            request.expiresAt shouldBe newExpiry
        }

        @Test
        fun `should support equality`() {
            val expiry = Instant.now()
            val request1 = UpdateLootBanRequest(reason = "test", expiresAt = expiry)
            val request2 = UpdateLootBanRequest(reason = "test", expiresAt = expiry)

            request1 shouldBe request2
        }

        @Test
        fun `should support copy`() {
            val request = UpdateLootBanRequest(reason = "original")
            val copied = request.copy(reason = "updated")

            copied.reason shouldBe "updated"
            request.reason shouldBe "original"
        }
    }

    @Nested
    inner class LootAwardDtoTest {

        @Test
        fun `should create from loot award`() {
            val award = LootAward.create(
                itemId = ItemId(12345L),
                raiderId = RaiderId(999L),
                guildId = GuildId("guild-456"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC
            )

            val dto = LootAwardDto.from(award)

            dto.id shouldBe award.id.value
            dto.itemId shouldBe 12345L
            dto.raiderId shouldBe "999"
            dto.guildId shouldBe "guild-456"
            dto.flpsScore shouldBe 0.85
            dto.tier shouldBe "MYTHIC"
            dto.isActive shouldBe true
            dto.awardedAt shouldNotBe null
        }

        @Test
        fun `should create from revoked loot award`() {
            val award = LootAward.create(
                itemId = ItemId(12345L),
                raiderId = RaiderId(999L),
                guildId = GuildId("guild-456"),
                flpsScore = FlpsScore.of(0.75),
                tier = LootTier.HEROIC
            ).revoke("Item was traded to wrong player")

            val dto = LootAwardDto.from(award)

            dto.isActive shouldBe false
            dto.tier shouldBe "HEROIC"
        }

        @Test
        fun `should handle different tiers`() {
            val tiers = listOf(LootTier.NORMAL, LootTier.HEROIC, LootTier.MYTHIC)

            tiers.forEach { tier ->
                val award = LootAward.create(
                    itemId = ItemId(12345L),
                    raiderId = RaiderId(999L),
                    guildId = GuildId("guild-456"),
                    flpsScore = FlpsScore.of(0.5),
                    tier = tier
                )

                val dto = LootAwardDto.from(award)
                dto.tier shouldBe tier.name
            }
        }
    }

    @Nested
    inner class LootBanDtoTest {

        @Test
        fun `should create from loot ban`() {
            val expiresAt = Instant.now().plusSeconds(86400)
            val ban = LootBan.create(
                raiderId = RaiderId(999L),
                guildId = GuildId("guild-456"),
                reason = "Test ban reason",
                expiresAt = expiresAt
            )

            val dto = LootBanDto.from(ban)

            dto.id shouldBe ban.id.value
            dto.raiderId shouldBe "999"
            dto.guildId shouldBe "guild-456"
            dto.reason shouldBe "Test ban reason"
            dto.expiresAt shouldBe expiresAt
            dto.isActive shouldBe true
            dto.bannedAt shouldNotBe null
        }

        @Test
        fun `should create from permanent ban`() {
            val ban = LootBan.create(
                raiderId = RaiderId(999L),
                guildId = GuildId("guild-456"),
                reason = "Permanent ban",
                expiresAt = null
            )

            val dto = LootBanDto.from(ban)

            dto.expiresAt shouldBe null
            dto.isActive shouldBe true
        }

        @Test
        fun `should create from expired ban`() {
            // Create a ban that already expired
            val expiredBan = LootBan(
                id = com.edgerush.lootman.domain.loot.model.LootBanId.generate(),
                raiderId = RaiderId(999L),
                guildId = GuildId("guild-456"),
                reason = "Expired ban",
                bannedAt = Instant.now().minusSeconds(86400 * 7), // 7 days ago
                expiresAt = Instant.now().minusSeconds(86400) // expired 1 day ago
            )

            val dto = LootBanDto.from(expiredBan)

            dto.isActive shouldBe false
        }
    }

    @Nested
    inner class LootHistoryResponseTest {

        @Test
        fun `should create from awards list`() {
            val awards = listOf(
                LootAward.create(
                    itemId = ItemId(111L),
                    raiderId = RaiderId(999L),
                    guildId = GuildId("guild-456"),
                    flpsScore = FlpsScore.of(0.8),
                    tier = LootTier.MYTHIC
                ),
                LootAward.create(
                    itemId = ItemId(222L),
                    raiderId = RaiderId(999L),
                    guildId = GuildId("guild-456"),
                    flpsScore = FlpsScore.of(0.7),
                    tier = LootTier.HEROIC
                )
            )

            val response = LootHistoryResponse.from(awards)

            response.awards.size shouldBe 2
            response.awards[0].itemId shouldBe 111L
            response.awards[1].itemId shouldBe 222L
        }

        @Test
        fun `should create empty response`() {
            val response = LootHistoryResponse.from(emptyList())

            response.awards shouldBe emptyList()
        }
    }

    @Nested
    inner class LootBansResponseTest {

        @Test
        fun `should create from bans list`() {
            val bans = listOf(
                LootBan.create(
                    raiderId = RaiderId(111L),
                    guildId = GuildId("guild-456"),
                    reason = "Ban 1",
                    expiresAt = null
                ),
                LootBan.create(
                    raiderId = RaiderId(222L),
                    guildId = GuildId("guild-456"),
                    reason = "Ban 2",
                    expiresAt = Instant.now().plusSeconds(86400)
                )
            )

            val response = LootBansResponse.from(bans)

            response.bans.size shouldBe 2
            response.bans[0].raiderId shouldBe "111"
            response.bans[1].raiderId shouldBe "222"
        }

        @Test
        fun `should create empty response`() {
            val response = LootBansResponse.from(emptyList())

            response.bans shouldBe emptyList()
        }
    }

    @Nested
    inner class LootAwardsListResponseTest {

        @Test
        fun `should create from awards with active count`() {
            val activeAward = LootAward.create(
                itemId = ItemId(111L),
                raiderId = RaiderId(999L),
                guildId = GuildId("guild-456"),
                flpsScore = FlpsScore.of(0.8),
                tier = LootTier.MYTHIC
            )
            val revokedAward = LootAward.create(
                itemId = ItemId(222L),
                raiderId = RaiderId(999L),
                guildId = GuildId("guild-456"),
                flpsScore = FlpsScore.of(0.7),
                tier = LootTier.HEROIC
            ).revoke("Misassigned item")

            val awards = listOf(activeAward, revokedAward)
            val response = LootAwardsListResponse.from(awards)

            response.awards.size shouldBe 2
            response.totalCount shouldBe 2
            response.activeCount shouldBe 1
        }

        @Test
        fun `should create empty response`() {
            val response = LootAwardsListResponse.from(emptyList())

            response.awards shouldBe emptyList()
            response.totalCount shouldBe 0
            response.activeCount shouldBe 0
        }

        @Test
        fun `should count all active awards`() {
            val awards = (1..5).map { i ->
                LootAward.create(
                    itemId = ItemId(i.toLong()),
                    raiderId = RaiderId(999L),
                    guildId = GuildId("guild-456"),
                    flpsScore = FlpsScore.of(0.5 + i * 0.05),
                    tier = LootTier.MYTHIC
                )
            }

            val response = LootAwardsListResponse.from(awards)

            response.totalCount shouldBe 5
            response.activeCount shouldBe 5
        }

        @Test
        fun `should support data class operations`() {
            val response = LootAwardsListResponse(
                awards = emptyList(),
                totalCount = 10,
                activeCount = 8
            )

            response.totalCount shouldBe 10
            response.activeCount shouldBe 8
        }
    }

    @Nested
    inner class LootBansListResponseTest {

        @Test
        fun `should create from bans with active count`() {
            val activeBan = LootBan.create(
                raiderId = RaiderId(111L),
                guildId = GuildId("guild-456"),
                reason = "Active ban",
                expiresAt = null
            )
            // Create an expired ban (simulates inactive ban)
            val expiredBan = LootBan(
                id = com.edgerush.lootman.domain.loot.model.LootBanId.generate(),
                raiderId = RaiderId(222L),
                guildId = GuildId("guild-456"),
                reason = "Expired ban",
                bannedAt = Instant.now().minusSeconds(86400 * 7),
                expiresAt = Instant.now().minusSeconds(86400) // expired yesterday
            )

            val bans = listOf(activeBan, expiredBan)
            val response = LootBansListResponse.from(bans)

            response.bans.size shouldBe 2
            response.totalCount shouldBe 2
            response.activeCount shouldBe 1
        }

        @Test
        fun `should create empty response`() {
            val response = LootBansListResponse.from(emptyList())

            response.bans shouldBe emptyList()
            response.totalCount shouldBe 0
            response.activeCount shouldBe 0
        }

        @Test
        fun `should count all active bans`() {
            val bans = (1..3).map { i ->
                LootBan.create(
                    raiderId = RaiderId(i.toLong()),
                    guildId = GuildId("guild-456"),
                    reason = "Ban $i",
                    expiresAt = null
                )
            }

            val response = LootBansListResponse.from(bans)

            response.totalCount shouldBe 3
            response.activeCount shouldBe 3
        }

        @Test
        fun `should support data class operations`() {
            val response = LootBansListResponse(
                bans = emptyList(),
                totalCount = 5,
                activeCount = 3
            )

            response.totalCount shouldBe 5
            response.activeCount shouldBe 3
        }
    }
}
