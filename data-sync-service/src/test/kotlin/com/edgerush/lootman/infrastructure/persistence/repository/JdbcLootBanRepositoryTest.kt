package com.edgerush.lootman.infrastructure.persistence.repository

import com.edgerush.datasync.entity.LootBanEntity
import com.edgerush.datasync.repository.LootBanRepository as SpringLootBanRepository
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.infrastructure.persistence.mapper.LootBanMapper
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Optional

class JdbcLootBanRepositoryTest {
    private lateinit var springRepository: SpringLootBanRepository
    private lateinit var mapper: LootBanMapper
    private lateinit var repository: JdbcLootBanRepository

    @BeforeEach
    fun setup() {
        springRepository = mockk()
        mapper = LootBanMapper()
        repository = JdbcLootBanRepository(springRepository, mapper)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should find loot ban by id`() {
        // Given
        val entity = createTestEntity(id = 123L)
        every { springRepository.findById(123L) } returns Optional.of(entity)

        // When
        val result = repository.findById(LootBanId("123"))

        // Then
        result shouldNotBe null
        result?.id?.value shouldBe "123"
        result?.raiderId?.value shouldBe "TestCharacter"
        verify(exactly = 1) { springRepository.findById(123L) }
    }

    @Test
    fun `should return null when loot ban not found`() {
        // Given
        every { springRepository.findById(999L) } returns Optional.empty()

        // When
        val result = repository.findById(LootBanId("999"))

        // Then
        result shouldBe null
    }

    @Test
    fun `should find active bans by raider id`() {
        // Given
        val entity = createTestEntity(id = 1L, characterName = "Player1")
        every {
            springRepository.findActiveBanForCharacter(
                guildId = "test-guild",
                characterName = "Player1",
                currentTime = any(),
            )
        } returns entity

        // When
        val results = repository.findActiveByRaiderId(RaiderId("Player1"), GuildId("test-guild"))

        // Then
        results shouldHaveSize 1
        results.first().raiderId.value shouldBe "Player1"
        verify(exactly = 1) {
            springRepository.findActiveBanForCharacter(
                guildId = "test-guild",
                characterName = "Player1",
                currentTime = any(),
            )
        }
    }

    @Test
    fun `should return empty list when no active bans found`() {
        // Given
        every {
            springRepository.findActiveBanForCharacter(
                guildId = "test-guild",
                characterName = "Player1",
                currentTime = any(),
            )
        } returns null

        // When
        val results = repository.findActiveByRaiderId(RaiderId("Player1"), GuildId("test-guild"))

        // Then
        results.shouldBeEmpty()
    }

    @Test
    fun `should save new loot ban`() {
        // Given
        val domain =
            LootBan.create(
                raiderId = RaiderId("Player1"),
                guildId = GuildId("test-guild"),
                reason = "Test reason",
                expiresAt = Instant.now().plusSeconds(86400),
            )
        val savedEntity = createTestEntity(id = 123L, characterName = "Player1")
        every { springRepository.save(any()) } returns savedEntity

        // When
        val result = repository.save(domain)

        // Then
        result.raiderId shouldBe domain.raiderId
        result.guildId shouldBe domain.guildId
        verify(exactly = 1) { springRepository.save(any()) }
    }

    @Test
    fun `should delete loot ban by id`() {
        // Given
        every { springRepository.deleteById(123L) } returns Unit

        // When
        repository.delete(LootBanId("123"))

        // Then
        verify(exactly = 1) { springRepository.deleteById(123L) }
    }

    private fun createTestEntity(
        id: Long = 123L,
        characterName: String = "TestCharacter",
        guildId: String = "test-guild",
    ): LootBanEntity =
        LootBanEntity(
            id = id,
            guildId = guildId,
            characterName = characterName,
            reason = "Test reason",
            bannedBy = "Admin",
            bannedAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusDays(30),
            isActive = true,
        )
}
