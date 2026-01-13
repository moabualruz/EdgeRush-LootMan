package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.shared.GuildId
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
import java.time.temporal.ChronoUnit

class InMemoryLootBanRepositoryTest : UnitTest() {
    private lateinit var repository: InMemoryLootBanRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryLootBanRepository()
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save and return the loot ban`() {
            // Arrange
            val ban = createLootBan()

            // Act
            val saved = repository.save(ban)

            // Assert
            saved shouldBe ban
        }

        @Test
        fun `should persist loot ban to storage`() {
            // Arrange
            val ban = createLootBan()

            // Act
            repository.save(ban)
            val retrieved = repository.findById(ban.id)

            // Assert
            retrieved shouldBe ban
        }

        @Test
        fun `should overwrite existing ban when saving with same id`() {
            // Arrange
            val originalBan = createLootBan(reason = "Original reason")
            repository.save(originalBan)

            val modifiedBan =
                LootBan(
                    id = originalBan.id,
                    raiderId = originalBan.raiderId,
                    guildId = originalBan.guildId,
                    reason = "Modified reason",
                    bannedAt = originalBan.bannedAt,
                    expiresAt = originalBan.expiresAt,
                )

            // Act
            repository.save(modifiedBan)
            val retrieved = repository.findById(originalBan.id)

            // Assert
            retrieved?.reason shouldBe "Modified reason"
        }

        @Test
        fun `should save multiple bans with different ids`() {
            // Arrange
            val ban1 = createLootBan(raiderId = RaiderId(1L))
            val ban2 = createLootBan(raiderId = RaiderId(2L))
            val ban3 = createLootBan(raiderId = RaiderId(3L))

            // Act
            repository.save(ban1)
            repository.save(ban2)
            repository.save(ban3)

            // Assert
            repository.findById(ban1.id) shouldBe ban1
            repository.findById(ban2.id) shouldBe ban2
            repository.findById(ban3.id) shouldBe ban3
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return loot ban when found`() {
            // Arrange
            val ban = createLootBan()
            repository.save(ban)

            // Act
            val retrieved = repository.findById(ban.id)

            // Assert
            retrieved shouldNotBe null
            retrieved shouldBe ban
        }

        @Test
        fun `should return null when ban not found`() {
            // Arrange
            val nonExistentId = LootBanId.generate()

            // Act
            val retrieved = repository.findById(nonExistentId)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should return null for id that was never saved`() {
            // Arrange
            val ban = createLootBan()
            val differentId = LootBanId.generate()

            repository.save(ban)

            // Act
            val retrieved = repository.findById(differentId)

            // Assert
            retrieved shouldBe null
        }
    }

    @Nested
    inner class FindActiveByRaiderIdTests {
        @Test
        fun `should return active bans for raider in guild`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")
            val activeBan =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )

            repository.save(activeBan)

            // Act
            val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            activeBans shouldHaveSize 1
            activeBans shouldContain activeBan
        }

        @Test
        fun `should not return expired bans`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")
            val activeBan =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    reason = "Active ban",
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )
            val expiredBan =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    reason = "Expired ban",
                    expiresAt = Instant.now().minus(1, ChronoUnit.SECONDS),
                )

            repository.save(activeBan)
            repository.save(expiredBan)

            // Act
            val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            activeBans shouldHaveSize 1
            activeBans.first().reason shouldBe "Active ban"
        }

        @Test
        fun `should return permanent bans with null expiresAt`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")
            val permanentBan =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    reason = "Permanent ban",
                    expiresAt = null,
                )

            repository.save(permanentBan)

            // Act
            val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            activeBans shouldHaveSize 1
            activeBans.first().reason shouldBe "Permanent ban"
        }

        @Test
        fun `should filter by guild id`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guild1 = GuildId("guild-1")
            val guild2 = GuildId("guild-2")

            val banGuild1 =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guild1,
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )
            val banGuild2 =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guild2,
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )

            repository.save(banGuild1)
            repository.save(banGuild2)

            // Act
            val guild1Bans = repository.findActiveByRaiderId(raiderId, guild1)
            val guild2Bans = repository.findActiveByRaiderId(raiderId, guild2)

            // Assert
            guild1Bans shouldHaveSize 1
            guild1Bans shouldContain banGuild1
            guild2Bans shouldHaveSize 1
            guild2Bans shouldContain banGuild2
        }

        @Test
        fun `should filter by raider id`() {
            // Arrange
            val raider1 = RaiderId(1L)
            val raider2 = RaiderId(2L)
            val guildId = GuildId("test-guild")

            val banRaider1 =
                createLootBan(
                    raiderId = raider1,
                    guildId = guildId,
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )
            val banRaider2 =
                createLootBan(
                    raiderId = raider2,
                    guildId = guildId,
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )

            repository.save(banRaider1)
            repository.save(banRaider2)

            // Act
            val raider1Bans = repository.findActiveByRaiderId(raider1, guildId)
            val raider2Bans = repository.findActiveByRaiderId(raider2, guildId)

            // Assert
            raider1Bans shouldHaveSize 1
            raider1Bans shouldContain banRaider1
            raider2Bans shouldHaveSize 1
            raider2Bans shouldContain banRaider2
        }

        @Test
        fun `should return empty list when no active bans exist`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")

            // Act
            val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            activeBans.shouldBeEmpty()
        }

        @Test
        fun `should return empty list when only expired bans exist`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")
            val expiredBan =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    expiresAt = Instant.now().minus(1, ChronoUnit.HOURS),
                )

            repository.save(expiredBan)

            // Act
            val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            activeBans.shouldBeEmpty()
        }

        @Test
        fun `should return multiple active bans for same raider`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")
            val ban1 =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    reason = "Ban 1",
                    expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
                )
            val ban2 =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    reason = "Ban 2",
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )
            val permanentBan =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    reason = "Permanent",
                    expiresAt = null,
                )

            repository.save(ban1)
            repository.save(ban2)
            repository.save(permanentBan)

            // Act
            val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            activeBans shouldHaveSize 3
            activeBans shouldContainExactlyInAnyOrder listOf(ban1, ban2, permanentBan)
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete existing ban`() {
            // Arrange
            val ban = createLootBan()
            repository.save(ban)

            // Act
            repository.delete(ban.id)
            val retrieved = repository.findById(ban.id)

            // Assert
            retrieved shouldBe null
        }

        @Test
        fun `should not throw when deleting non-existent ban`() {
            // Arrange
            val nonExistentId = LootBanId.generate()

            // Act & Assert - should not throw
            repository.delete(nonExistentId)
        }

        @Test
        fun `should only delete specified ban and leave others intact`() {
            // Arrange
            val ban1 = createLootBan(raiderId = RaiderId(1L))
            val ban2 = createLootBan(raiderId = RaiderId(2L))
            val ban3 = createLootBan(raiderId = RaiderId(3L))

            repository.save(ban1)
            repository.save(ban2)
            repository.save(ban3)

            // Act
            repository.delete(ban2.id)

            // Assert
            repository.findById(ban1.id) shouldBe ban1
            repository.findById(ban2.id) shouldBe null
            repository.findById(ban3.id) shouldBe ban3
        }

        @Test
        fun `should remove ban from active query results after deletion`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")
            val ban1 =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    reason = "Ban 1",
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )
            val ban2 =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    reason = "Ban 2",
                    expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                )

            repository.save(ban1)
            repository.save(ban2)

            // Act
            repository.delete(ban1.id)
            val results = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            results shouldHaveSize 1
            results shouldContain ban2
        }
    }

    @Nested
    inner class ConcurrencyTests {
        @Test
        fun `should handle concurrent saves without data loss`() {
            // Arrange
            val bans =
                (1..100).map { index ->
                    createLootBan(raiderId = RaiderId(index.toLong()))
                }

            // Act - simulate concurrent saves
            bans.parallelStream().forEach { ban ->
                repository.save(ban)
            }

            // Assert - all bans should be saved
            bans.forEach { ban ->
                repository.findById(ban.id) shouldBe ban
            }
        }

        @Test
        fun `should handle concurrent reads and writes`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val initialBans =
                (1..50).map { index ->
                    createLootBan(
                        guildId = guildId,
                        raiderId = RaiderId(index.toLong()),
                        expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                    )
                }

            initialBans.forEach { repository.save(it) }

            // Act - concurrent reads while writing
            val newBans =
                (51..100).map { index ->
                    createLootBan(
                        guildId = guildId,
                        raiderId = RaiderId(index.toLong()),
                        expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                    )
                }

            newBans.parallelStream().forEach { ban ->
                repository.save(ban)
                repository.findActiveByRaiderId(ban.raiderId, guildId) // concurrent read
            }

            // Assert
            (initialBans + newBans).forEach { ban ->
                repository.findById(ban.id) shouldBe ban
            }
        }
    }

    @Nested
    inner class ExpirationEdgeCaseTests {
        @Test
        fun `should treat ban expiring exactly now as expired`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")
            // Ban that expires right now - due to time passing during test execution,
            // this should be treated as expired
            val banExpiringNow =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    expiresAt = Instant.now().minus(1, ChronoUnit.MILLIS),
                )

            repository.save(banExpiringNow)

            // Act
            val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            activeBans.shouldBeEmpty()
        }

        @Test
        fun `should handle very far future expiration`() {
            // Arrange
            val raiderId = RaiderId(1L)
            val guildId = GuildId("test-guild")
            val farFutureBan =
                createLootBan(
                    raiderId = raiderId,
                    guildId = guildId,
                    expiresAt = Instant.now().plus(365 * 100, ChronoUnit.DAYS), // 100 years
                )

            repository.save(farFutureBan)

            // Act
            val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

            // Assert
            activeBans shouldHaveSize 1
            activeBans shouldContain farFutureBan
        }
    }

    private fun createLootBan(
        raiderId: RaiderId = RaiderId(1L),
        guildId: GuildId = GuildId("test-guild"),
        reason: String = "Test ban reason",
        expiresAt: Instant? = Instant.now().plus(7, ChronoUnit.DAYS),
    ): LootBan =
        LootBan.create(
            raiderId = raiderId,
            guildId = guildId,
            reason = reason,
            expiresAt = expiresAt,
        )
}
