package com.edgerush.lootman.infrastructure.persistence.repository

import com.edgerush.datasync.entity.LootAwardEntity
import com.edgerush.datasync.repository.LootAwardRepository as SpringLootAwardRepository
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import com.edgerush.lootman.infrastructure.persistence.mapper.LootAwardMapper
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
import java.time.OffsetDateTime
import java.util.Optional

class JdbcLootAwardRepositoryTest {
    private lateinit var springRepository: SpringLootAwardRepository
    private lateinit var mapper: LootAwardMapper
    private lateinit var repository: JdbcLootAwardRepository

    @BeforeEach
    fun setup() {
        springRepository = mockk()
        mapper = LootAwardMapper()
        repository = JdbcLootAwardRepository(springRepository, mapper)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should find loot award by id`() {
        // Given
        val entity = createTestEntity(id = 123L)
        every { springRepository.findById(123L) } returns Optional.of(entity)

        // When
        val result = repository.findById(LootAwardId("123"))

        // Then
        result shouldNotBe null
        result?.id?.value shouldBe "123"
        result?.itemId?.value shouldBe 789L
        verify(exactly = 1) { springRepository.findById(123L) }
    }

    @Test
    fun `should return null when loot award not found`() {
        // Given
        every { springRepository.findById(999L) } returns Optional.empty()

        // When
        val result = repository.findById(LootAwardId("999"))

        // Then
        result shouldBe null
    }

    @Test
    fun `should find loot awards by raider id`() {
        // Given
        val entities =
            listOf(
                createTestEntity(id = 1L, raiderId = 456L),
                createTestEntity(id = 2L, raiderId = 456L),
            )
        every { springRepository.findByRaiderId(456L) } returns entities

        // When
        val results = repository.findByRaiderId(RaiderId("456"))

        // Then
        results shouldHaveSize 2
        results.all { it.raiderId.value == "456" } shouldBe true
        verify(exactly = 1) { springRepository.findByRaiderId(456L) }
    }

    @Test
    fun `should find loot awards by guild id`() {
        // Given
        val entities =
            listOf(
                createTestEntity(id = 1L),
                createTestEntity(id = 2L),
            )
        every { springRepository.findAll() } returns entities

        // When
        val results = repository.findByGuildId(GuildId("test-guild"))

        // Then
        results shouldHaveSize 2
        verify(exactly = 1) { springRepository.findAll() }
    }

    @Test
    fun `should save new loot award`() {
        // Given
        val domain =
            LootAward.create(
                itemId = ItemId(789L),
                raiderId = RaiderId("456"),
                guildId = GuildId("test-guild"),
                flpsScore = FlpsScore.of(0.85),
                tier = LootTier.MYTHIC,
            )
        val savedEntity = createTestEntity(id = 123L)
        every { springRepository.save(any()) } returns savedEntity

        // When
        val result = repository.save(domain)

        // Then
        result.itemId shouldBe domain.itemId
        result.raiderId shouldBe domain.raiderId
        verify(exactly = 1) { springRepository.save(any()) }
    }

    @Test
    fun `should delete loot award by id`() {
        // Given
        every { springRepository.deleteById(123L) } returns Unit

        // When
        repository.delete(LootAwardId("123"))

        // Then
        verify(exactly = 1) { springRepository.deleteById(123L) }
    }

    private fun createTestEntity(
        id: Long = 123L,
        raiderId: Long = 456L,
        itemId: Long = 789L,
    ): LootAwardEntity =
        LootAwardEntity(
            id = id,
            raiderId = raiderId,
            itemId = itemId,
            itemName = "Test Item",
            tier = "MYTHIC",
            flps = 0.85,
            rdf = 0.95,
            awardedAt = OffsetDateTime.now(),
            rclootcouncilId = null,
            icon = null,
            slot = null,
            quality = null,
            responseTypeId = null,
            responseTypeName = null,
            responseTypeRgba = null,
            responseTypeExcluded = null,
            propagatedResponseTypeId = null,
            propagatedResponseTypeName = null,
            propagatedResponseTypeRgba = null,
            propagatedResponseTypeExcluded = null,
            sameResponseAmount = null,
            note = null,
            wishValue = null,
            difficulty = null,
            discarded = null,
            characterId = null,
            awardedByCharacterId = null,
            awardedByName = null,
        )
}
