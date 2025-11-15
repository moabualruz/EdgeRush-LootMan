package com.edgerush.lootman.application.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.loot.repository.LootBanRepository
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class ManageLootBansUseCaseTest : UnitTest() {
    private lateinit var lootBanRepository: LootBanRepository
    private lateinit var useCase: ManageLootBansUseCase

    @org.junit.jupiter.api.BeforeEach
    fun setup() {
        lootBanRepository = mockk()
        useCase = ManageLootBansUseCase(lootBanRepository)
    }

    @Test
    fun `should create temporary loot ban successfully`() {
        // Given
        val command =
            CreateLootBanCommand(
                raiderId = RaiderId("raider-123"),
                guildId = GuildId("guild-456"),
                reason = "Missed raid without notice",
                expiresAt = Instant.now().plusSeconds(604800), // 7 days
            )

        every { lootBanRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.createBan(command)

        // Then
        result.isSuccess shouldBe true
        val ban = result.getOrThrow()
        ban.raiderId shouldBe command.raiderId
        ban.guildId shouldBe command.guildId
        ban.reason shouldBe command.reason
        ban.expiresAt shouldBe command.expiresAt
        ban.isActive() shouldBe true

        verify(exactly = 1) { lootBanRepository.save(any()) }
    }

    @Test
    fun `should create permanent loot ban successfully`() {
        // Given
        val command =
            CreateLootBanCommand(
                raiderId = RaiderId("raider-123"),
                guildId = GuildId("guild-456"),
                reason = "Severe behavioral violation",
                expiresAt = null, // Permanent
            )

        every { lootBanRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.createBan(command)

        // Then
        result.isSuccess shouldBe true
        val ban = result.getOrThrow()
        ban.expiresAt shouldBe null
        ban.isActive() shouldBe true

        verify(exactly = 1) { lootBanRepository.save(any()) }
    }

    @Test
    fun `should remove loot ban successfully`() {
        // Given
        val banId = LootBanId.generate()
        val command = RemoveLootBanCommand(banId)

        every { lootBanRepository.delete(banId) } just Runs

        // When
        val result = useCase.removeBan(command)

        // Then
        result.isSuccess shouldBe true

        verify(exactly = 1) { lootBanRepository.delete(banId) }
    }

    @Test
    fun `should get active bans for raider`() {
        // Given
        val raiderId = RaiderId("raider-123")
        val guildId = GuildId("guild-456")
        val query = GetActiveBansQuery(raiderId, guildId)

        val activeBan1 =
            LootBan.create(
                raiderId = raiderId,
                guildId = guildId,
                reason = "Reason 1",
                expiresAt = Instant.now().plusSeconds(86400),
            )
        val activeBan2 =
            LootBan.create(
                raiderId = raiderId,
                guildId = guildId,
                reason = "Reason 2",
                expiresAt = null,
            )

        every { lootBanRepository.findActiveByRaiderId(raiderId, guildId) } returns listOf(activeBan1, activeBan2)

        // When
        val result = useCase.getActiveBans(query)

        // Then
        result.isSuccess shouldBe true
        val bans = result.getOrThrow()
        bans.size shouldBe 2
        bans.all { it.isActive() } shouldBe true

        verify(exactly = 1) { lootBanRepository.findActiveByRaiderId(raiderId, guildId) }
    }

    @Test
    fun `should return empty list when no active bans exist`() {
        // Given
        val raiderId = RaiderId("raider-123")
        val guildId = GuildId("guild-456")
        val query = GetActiveBansQuery(raiderId, guildId)

        every { lootBanRepository.findActiveByRaiderId(raiderId, guildId) } returns emptyList()

        // When
        val result = useCase.getActiveBans(query)

        // Then
        result.isSuccess shouldBe true
        val bans = result.getOrThrow()
        bans.isEmpty() shouldBe true

        verify(exactly = 1) { lootBanRepository.findActiveByRaiderId(raiderId, guildId) }
    }

    @Test
    fun `should handle repository errors when creating ban`() {
        // Given
        val command =
            CreateLootBanCommand(
                raiderId = RaiderId("raider-123"),
                guildId = GuildId("guild-456"),
                reason = "Test reason",
                expiresAt = Instant.now().plusSeconds(86400),
            )

        every { lootBanRepository.save(any()) } throws RuntimeException("Database error")

        // When
        val result = useCase.createBan(command)

        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is RuntimeException) shouldBe true

        verify(exactly = 1) { lootBanRepository.save(any()) }
    }

    @Test
    fun `should handle repository errors when removing ban`() {
        // Given
        val banId = LootBanId.generate()
        val command = RemoveLootBanCommand(banId)

        every { lootBanRepository.delete(banId) } throws RuntimeException("Database error")

        // When
        val result = useCase.removeBan(command)

        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is RuntimeException) shouldBe true

        verify(exactly = 1) { lootBanRepository.delete(banId) }
    }

    @Test
    fun `should validate ban reason is not blank`() {
        // Given
        val command =
            CreateLootBanCommand(
                raiderId = RaiderId("raider-123"),
                guildId = GuildId("guild-456"),
                reason = "",
                expiresAt = Instant.now().plusSeconds(86400),
            )

        every { lootBanRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.createBan(command)

        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is IllegalArgumentException) shouldBe true

        verify(exactly = 0) { lootBanRepository.save(any()) }
    }

    @Test
    fun `should validate expiration is in the future when provided`() {
        // Given
        val command =
            CreateLootBanCommand(
                raiderId = RaiderId("raider-123"),
                guildId = GuildId("guild-456"),
                reason = "Test reason",
                expiresAt = Instant.now().minusSeconds(86400), // Past
            )

        every { lootBanRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.createBan(command)

        // Then
        result.isFailure shouldBe true
        (result.exceptionOrNull() is IllegalArgumentException) shouldBe true

        verify(exactly = 0) { lootBanRepository.save(any()) }
    }
}
