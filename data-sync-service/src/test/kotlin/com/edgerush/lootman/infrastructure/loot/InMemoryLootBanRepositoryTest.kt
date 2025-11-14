package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class InMemoryLootBanRepositoryTest : UnitTest() {
    private lateinit var repository: InMemoryLootBanRepository

    @BeforeEach
    fun setup() {
        repository = InMemoryLootBanRepository()
    }

    @Test
    fun `should save and retrieve loot ban by id`() {
        // Arrange
        val ban =
            LootBan.create(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Test ban",
                expiresAt = Instant.now().plusSeconds(7 * 24 * 60 * 60),
            )

        // Act
        val saved = repository.save(ban)
        val retrieved = repository.findById(ban.id)

        // Assert
        saved shouldBe ban
        retrieved shouldBe ban
    }

    @Test
    fun `should find active bans for raider`() {
        // Arrange
        val raiderId = RaiderId("test-raider")
        val guildId = GuildId("test-guild")
        val activeBan =
            LootBan.create(
                raiderId = raiderId,
                guildId = guildId,
                reason = "Active ban",
                expiresAt = Instant.now().plusSeconds(7 * 24 * 60 * 60),
            )
        val expiredBan =
            LootBan.create(
                raiderId = raiderId,
                guildId = guildId,
                reason = "Expired ban",
                expiresAt = Instant.now().minusSeconds(1),
            )

        repository.save(activeBan)
        repository.save(expiredBan)

        // Act
        val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

        // Assert
        activeBans.size shouldBe 1
        activeBans.first().id shouldBe activeBan.id
    }

    @Test
    fun `should delete loot ban`() {
        // Arrange
        val ban =
            LootBan.create(
                raiderId = RaiderId("test-raider"),
                guildId = GuildId("test-guild"),
                reason = "Test ban",
                expiresAt = Instant.now().plusSeconds(7 * 24 * 60 * 60),
            )
        repository.save(ban)

        // Act
        repository.delete(ban.id)
        val retrieved = repository.findById(ban.id)

        // Assert
        retrieved shouldBe null
    }

    @Test
    fun `should return empty list when no active bans exist`() {
        // Arrange
        val raiderId = RaiderId("test-raider")
        val guildId = GuildId("test-guild")

        // Act
        val activeBans = repository.findActiveByRaiderId(raiderId, guildId)

        // Assert
        activeBans.size shouldBe 0
    }
}

