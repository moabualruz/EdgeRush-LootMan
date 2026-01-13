package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class InMemoryLootAwardRepositoryTest : UnitTest() {
    private lateinit var repository: InMemoryLootAwardRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryLootAwardRepository()
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save and return the loot award`() {
            // Arrange
            val award = createLootAward()

            // Act
            val saved = repository.save(award)

            // Assert
            saved shouldBe award
        }

        @Test
        fun `should persist loot award to storage`() {
            // Arrange
            val award = createLootAward()

            // Act
            repository.save(award)
            val retrieved = repository.findById(award.id)

            // Assert
            retrieved shouldBe award
        }

        @Test
        fun `should overwrite existing award when saving with same id`() {
            // Arrange
            val originalAward = createLootAward(tier = LootTier.NORMAL)
            repository.save(originalAward)

            // Create a modified version with the same ID
            val modifiedAward =
                LootAward(
                    id = originalAward.id,
                    itemId = ItemId(999L),
                    raiderId = originalAward.raiderId,
                    guildId = originalAward.guildId,
                    awardedAt = Instant.now(),
                    flpsScore = FlpsScore.of(0.9),
                    tier = LootTier.MYTHIC,
                )

            // Act
            repository.save(modifiedAward)
            val retrieved = repository.findById(originalAward.id)

            // Assert
            retrieved shouldBe modifiedAward
            retrieved?.tier shouldBe LootTier.MYTHIC
            retrieved?.itemId shouldBe ItemId(999L)
        }

        @Test
        fun `should save multiple awards with different ids`() {
            // Arrange
            val award1 = createLootAward(raiderId = RaiderId(1L))
            val award2 = createLootAward(raiderId = RaiderId(2L))
            val award3 = createLootAward(raiderId = RaiderId(3L))

            // Act
            repository.save(award1)
            repository.save(award2)
            repository.save(award3)

            // Assert
            repository.findById(award1.id) shouldBe award1
            repository.findById(award2.id) shouldBe award2
            repository.findById(award3.id) shouldBe award3
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return loot award when found`() {
            // Arrange
            val award = createLootAward()
            repository.save(award)

            // Act
            val retrieved = repository.findById(award.id)

            // Assert
            retrieved shouldNotBe null
            retrieved shouldBe award
        }

        @Test
        fun `should return null when award not found`() {
            // Arrange
            val nonExistentId = LootAwardId.generate()

            // Act
            val retrieved = repository.findById(nonExistentId)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return null for id that was never saved`() {
            // Arrange
            val award = createLootAward()
            val differentId = LootAwardId.generate()

            repository.save(award)

            // Act
            val retrieved = repository.findById(differentId)

            // Assert
            retrieved shouldBe null
        }
    }

    @Nested
    inner class FindByRaiderIdTests {
        @Test
        fun `should return all awards for a specific raider`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val award1 = createLootAward(raiderId = raiderId, itemId = ItemId(100L))
            val award2 = createLootAward(raiderId = raiderId, itemId = ItemId(200L))
            val award3 = createLootAward(raiderId = RaiderId(2L), itemId = ItemId(300L))

            repository.save(award1)
            repository.save(award2)
            repository.save(award3)

            // Act
            val results = repository.findByRaiderId(raiderId)

            // Assert
            results shouldHaveSize 2
            results shouldContainExactlyInAnyOrder listOf(award1, award2)
        }

        @Test
        fun `should return empty list when raider has no awards`() {
            // Arrange
            val raiderWithAwards = RaiderId(1L)
            val raiderWithoutAwards = RaiderId(2L)
            val award = createLootAward(raiderId = raiderWithAwards)

            repository.save(award)

            // Act
            val results = repository.findByRaiderId(raiderWithoutAwards)

            // Assert
            results.shouldBeEmpty()
        }

        @Test
        fun `should return empty list when repository is empty`() {
            // Arrange
            val raiderId = RaiderId(1L)

            // Act
            val results = repository.findByRaiderId(raiderId)

            // Assert
            results.shouldBeEmpty()
        }

        @Test
        fun `should return awards for correct raider only`() {
            // Arrange
            val raider1 = RaiderId(1L)
            val raider2 = RaiderId(2L)
            val raider3 = RaiderId(3L)

            val award1 = createLootAward(raiderId = raider1)
            val award2 = createLootAward(raiderId = raider2)
            val award3 = createLootAward(raiderId = raider2)
            val award4 = createLootAward(raiderId = raider3)

            repository.save(award1)
            repository.save(award2)
            repository.save(award3)
            repository.save(award4)

            // Act
            val results = repository.findByRaiderId(raider2)

            // Assert
            results shouldHaveSize 2
            results shouldContain award2
            results shouldContain award3
        }
    }

    @Nested
    inner class FindByGuildIdTests {
        @Test
        fun `should return all awards for a specific guild`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val award1 = createLootAward(guildId = guildId, raiderId = RaiderId(1L))
            val award2 = createLootAward(guildId = guildId, raiderId = RaiderId(2L))
            val award3 = createLootAward(guildId = GuildId("other-guild"), raiderId = RaiderId(3L))

            repository.save(award1)
            repository.save(award2)
            repository.save(award3)

            // Act
            val results = repository.findByGuildId(guildId)

            // Assert
            results shouldHaveSize 2
            results shouldContainExactlyInAnyOrder listOf(award1, award2)
        }

        @Test
        fun `should return empty list when guild has no awards`() {
            // Arrange
            val guildWithAwards = GuildId("guild-with-awards")
            val guildWithoutAwards = GuildId("guild-without-awards")
            val award = createLootAward(guildId = guildWithAwards)

            repository.save(award)

            // Act
            val results = repository.findByGuildId(guildWithoutAwards)

            // Assert
            results.shouldBeEmpty()
        }

        @Test
        fun `should return empty list when repository is empty`() {
            // Arrange
            val guildId = GuildId("test-guild")

            // Act
            val results = repository.findByGuildId(guildId)

            // Assert
            results.shouldBeEmpty()
        }

        @Test
        fun `should handle multiple guilds with awards correctly`() {
            // Arrange
            val guild1 = GuildId("guild-1")
            val guild2 = GuildId("guild-2")
            val guild3 = GuildId("guild-3")

            val award1 = createLootAward(guildId = guild1, raiderId = RaiderId(1L))
            val award2 = createLootAward(guildId = guild1, raiderId = RaiderId(2L))
            val award3 = createLootAward(guildId = guild2, raiderId = RaiderId(3L))
            val award4 = createLootAward(guildId = guild3, raiderId = RaiderId(4L))
            val award5 = createLootAward(guildId = guild3, raiderId = RaiderId(5L))
            val award6 = createLootAward(guildId = guild3, raiderId = RaiderId(6L))

            repository.save(award1)
            repository.save(award2)
            repository.save(award3)
            repository.save(award4)
            repository.save(award5)
            repository.save(award6)

            // Act & Assert
            repository.findByGuildId(guild1) shouldHaveSize 2
            repository.findByGuildId(guild2) shouldHaveSize 1
            repository.findByGuildId(guild3) shouldHaveSize 3
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete existing award`() {
            // Arrange
            val award = createLootAward()
            repository.save(award)

            // Act
            repository.delete(award.id)

            // Assert
            repository.findById(award.id) shouldBe null
        }

        @Test
        fun `should not throw when deleting non-existent award`() {
            // Arrange
            val nonExistentId = LootAwardId.generate()

            // Act & Assert - should not throw
            repository.delete(nonExistentId)
        }

        @Test
        fun `should only delete specified award and leave others intact`() {
            // Arrange
            val award1 = createLootAward(raiderId = RaiderId(1L))
            val award2 = createLootAward(raiderId = RaiderId(2L))
            val award3 = createLootAward(raiderId = RaiderId(3L))

            repository.save(award1)
            repository.save(award2)
            repository.save(award3)

            // Act
            repository.delete(award2.id)

            // Assert
            repository.findById(award1.id) shouldBe award1
            repository.findById(award2.id) shouldBe null
            repository.findById(award3.id) shouldBe award3
        }

        @Test
        fun `should remove award from raider query results after deletion`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val award1 = createLootAward(raiderId = raiderId, itemId = ItemId(100L))
            val award2 = createLootAward(raiderId = raiderId, itemId = ItemId(200L))

            repository.save(award1)
            repository.save(award2)

            // Act
            repository.delete(award1.id)
            val results = repository.findByRaiderId(raiderId)

            // Assert
            results shouldHaveSize 1
            results shouldContain award2
        }

        @Test
        fun `should remove award from guild query results after deletion`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val award1 = createLootAward(guildId = guildId, raiderId = RaiderId(1L))
            val award2 = createLootAward(guildId = guildId, raiderId = RaiderId(2L))

            repository.save(award1)
            repository.save(award2)

            // Act
            repository.delete(award1.id)
            val results = repository.findByGuildId(guildId)

            // Assert
            results shouldHaveSize 1
            results shouldContain award2
        }
    }

    @Nested
    inner class ConcurrencyTests {
        @Test
        fun `should handle concurrent saves without data loss`() {
            // Arrange
            val awards =
                (1..100).map { index ->
                    createLootAward(raiderId = RaiderId(index.toLong()), itemId = ItemId(index.toLong()))
                }

            // Act - simulate concurrent saves
            awards.parallelStream().forEach { award ->
                repository.save(award)
            }

            // Assert - all awards should be saved
            awards.forEach { award ->
                repository.findById(award.id) shouldBe award
            }
        }

        @Test
        fun `should handle concurrent reads and writes`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val initialAwards =
                (1..50).map { index ->
                    createLootAward(guildId = guildId, raiderId = RaiderId(index.toLong()))
                }

            initialAwards.forEach { repository.save(it) }

            // Act - concurrent reads while writing
            val newAwards =
                (51..100).map { index ->
                    createLootAward(guildId = guildId, raiderId = RaiderId(index.toLong()))
                }

            newAwards.parallelStream().forEach { award ->
                repository.save(award)
                repository.findByGuildId(guildId) // concurrent read
            }

            // Assert
            val allResults = repository.findByGuildId(guildId)
            allResults shouldHaveSize 100
        }
    }

    @Nested
    inner class LootTierTests {
        @Test
        fun `should correctly store and retrieve awards with different tiers`() {
            // Arrange
            val mythicAward = createLootAward(tier = LootTier.MYTHIC, raiderId = RaiderId(1L))
            val heroicAward = createLootAward(tier = LootTier.HEROIC, raiderId = RaiderId(2L))
            val normalAward = createLootAward(tier = LootTier.NORMAL, raiderId = RaiderId(3L))
            val lfrAward = createLootAward(tier = LootTier.LFR, raiderId = RaiderId(4L))

            // Act
            repository.save(mythicAward)
            repository.save(heroicAward)
            repository.save(normalAward)
            repository.save(lfrAward)

            // Assert
            repository.findById(mythicAward.id)?.tier shouldBe LootTier.MYTHIC
            repository.findById(heroicAward.id)?.tier shouldBe LootTier.HEROIC
            repository.findById(normalAward.id)?.tier shouldBe LootTier.NORMAL
            repository.findById(lfrAward.id)?.tier shouldBe LootTier.LFR
        }
    }

    @Nested
    inner class FlpsScoreTests {
        @Test
        fun `should correctly store and retrieve awards with various FLPS scores`() {
            // Arrange
            val zeroScoreAward = createLootAward(flpsScore = FlpsScore.zero(), raiderId = RaiderId(1L))
            val maxScoreAward = createLootAward(flpsScore = FlpsScore.max(), raiderId = RaiderId(2L))
            val midScoreAward = createLootAward(flpsScore = FlpsScore.of(0.5), raiderId = RaiderId(3L))

            // Act
            repository.save(zeroScoreAward)
            repository.save(maxScoreAward)
            repository.save(midScoreAward)

            // Assert
            repository.findById(zeroScoreAward.id)?.flpsScore shouldBe FlpsScore.zero()
            repository.findById(maxScoreAward.id)?.flpsScore shouldBe FlpsScore.max()
            repository.findById(midScoreAward.id)?.flpsScore shouldBe FlpsScore.of(0.5)
        }
    }

    private fun createLootAward(
        raiderId: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("test-guild"),
        itemId: ItemId = ItemId(12345L),
        tier: LootTier = LootTier.MYTHIC,
        flpsScore: FlpsScore = FlpsScore.of(0.75),
    ): LootAward =
        LootAward.create(
            itemId = itemId,
            raiderId = raiderId,
            guildId = guildId,
            flpsScore = flpsScore,
            tier = tier,
        )
}
