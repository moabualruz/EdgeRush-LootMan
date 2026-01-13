package com.edgerush.lootman.infrastructure.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for InMemoryRaiderPerformanceRepository.
 *
 * Tests the in-memory storage and retrieval of raider performance data.
 */
class InMemoryRaiderPerformanceRepositoryTest : UnitTest() {

    private lateinit var repository: InMemoryRaiderPerformanceRepository

    private val guildId = GuildId("test-guild")
    private val now = Instant.now()
    private val oneWeekAgo = now.minus(7, ChronoUnit.DAYS)
    private val twoWeeksAgo = now.minus(14, ChronoUnit.DAYS)
    private val threeWeeksAgo = now.minus(21, ChronoUnit.DAYS)

    @BeforeEach
    fun setUp() {
        repository = InMemoryRaiderPerformanceRepository()
    }

    @Nested
    inner class SaveAndFindByRaiderAndPeriodTests {

        @Test
        fun `should save and retrieve performance data by raider and period`() {
            // Given
            val raiderId = RaiderId(1L)
            val data = createPerformanceData(
                raiderId = raiderId,
                periodStart = oneWeekAgo,
                periodEnd = now
            )

            repository.save(guildId, data)

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldNotBe null
            result?.raiderId shouldBe raiderId
            result?.totalDeaths shouldBe 5
            result?.totalFights shouldBe 20
        }

        @Test
        fun `should return null when no data found for raider`() {
            // Given
            val raiderId = RaiderId(999L)

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldBe null
        }

        @Test
        fun `should return null when data exists but period does not match`() {
            // Given
            val raiderId = RaiderId(1L)
            val data = createPerformanceData(
                raiderId = raiderId,
                periodStart = twoWeeksAgo,
                periodEnd = oneWeekAgo
            )

            repository.save(guildId, data)

            // When - query for a different period
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldBe null
        }

        @Test
        fun `should return null when data exists but guild does not match`() {
            // Given
            val raiderId = RaiderId(1L)
            val data = createPerformanceData(raiderId = raiderId)

            repository.save(GuildId("other-guild"), data)

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldBe null
        }
    }

    @Nested
    inner class FindByCharacterAndPeriodTests {

        @Test
        fun `should find performance data by character name and realm`() {
            // Given
            val data = createPerformanceData(
                characterName = "UniqueChar",
                characterRealm = "UniqueRealm"
            )

            repository.save(guildId, data)

            // When
            val result = repository.findByCharacterAndPeriod(
                "UniqueChar",
                "UniqueRealm",
                guildId,
                oneWeekAgo,
                now
            )

            // Then
            result shouldNotBe null
            result?.characterName shouldBe "UniqueChar"
            result?.characterRealm shouldBe "UniqueRealm"
        }

        @Test
        fun `should return null when character not found`() {
            // When
            val result = repository.findByCharacterAndPeriod(
                "NonExistent",
                "Unknown",
                guildId,
                oneWeekAgo,
                now
            )

            // Then
            result shouldBe null
        }

        @Test
        fun `should be case-sensitive for character name`() {
            // Given
            val data = createPerformanceData(
                characterName = "TestRaider",
                characterRealm = "Area52"
            )

            repository.save(guildId, data)

            // When - search with different case
            val result = repository.findByCharacterAndPeriod(
                "testraider", // lowercase
                "Area52",
                guildId,
                oneWeekAgo,
                now
            )

            // Then
            result shouldBe null
        }
    }

    @Nested
    inner class FindAllByGuildAndPeriodTests {

        @Test
        fun `should return all performance data for guild in period`() {
            // Given
            val data1 = createPerformanceData(raiderId = RaiderId(1L), characterName = "Raider1")
            val data2 = createPerformanceData(raiderId = RaiderId(2L), characterName = "Raider2")
            val data3 = createPerformanceData(raiderId = RaiderId(3L), characterName = "Raider3")

            repository.save(guildId, data1)
            repository.save(guildId, data2)
            repository.save(guildId, data3)

            // When
            val result = repository.findAllByGuildAndPeriod(guildId, oneWeekAgo, now)

            // Then
            result.size shouldBe 3
            result.map { it.characterName } shouldBe listOf("Raider1", "Raider2", "Raider3")
        }

        @Test
        fun `should return empty list when no data for guild`() {
            // Given - data saved for different guild
            val data = createPerformanceData()
            repository.save(GuildId("other-guild"), data)

            // When
            val result = repository.findAllByGuildAndPeriod(guildId, oneWeekAgo, now)

            // Then
            result shouldBe emptyList()
        }

        @Test
        fun `should only return data matching the period`() {
            // Given
            val dataCurrentPeriod = createPerformanceData(
                raiderId = RaiderId(1L),
                characterName = "Current",
                periodStart = oneWeekAgo,
                periodEnd = now
            )
            val dataOldPeriod = createPerformanceData(
                raiderId = RaiderId(2L),
                characterName = "Old",
                periodStart = threeWeeksAgo,
                periodEnd = twoWeeksAgo
            )

            repository.save(guildId, dataCurrentPeriod)
            repository.save(guildId, dataOldPeriod)

            // When
            val result = repository.findAllByGuildAndPeriod(guildId, oneWeekAgo, now)

            // Then
            result.size shouldBe 1
            result[0].characterName shouldBe "Current"
        }
    }

    @Nested
    inner class UpdateTests {

        @Test
        fun `should update existing data for same raider, guild, and period`() {
            // Given
            val raiderId = RaiderId(1L)
            val initialData = createPerformanceData(
                raiderId = raiderId,
                totalDeaths = 5,
                totalFights = 20
            )
            val updatedData = createPerformanceData(
                raiderId = raiderId,
                totalDeaths = 8,
                totalFights = 25
            )

            repository.save(guildId, initialData)
            repository.save(guildId, updatedData)

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldNotBe null
            result?.totalDeaths shouldBe 8
            result?.totalFights shouldBe 25
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `should clear all data`() {
            // Given
            repository.save(guildId, createPerformanceData(raiderId = RaiderId(1L)))
            repository.save(guildId, createPerformanceData(raiderId = RaiderId(2L)))

            // When
            repository.clear()

            // Then
            val result = repository.findAllByGuildAndPeriod(guildId, oneWeekAgo, now)
            result shouldBe emptyList()
        }
    }

    // Helper methods

    private fun createPerformanceData(
        raiderId: RaiderId = RaiderId(1L),
        characterName: String = "TestRaider",
        characterRealm: String = "Area52",
        totalDeaths: Int = 5,
        totalFights: Int = 20,
        deathsPerAttempt: Double = 0.25,
        avoidableDamagePercentage: Double = 10.0,
        periodStart: Instant = oneWeekAgo,
        periodEnd: Instant = now
    ): RaiderPerformanceData = RaiderPerformanceData(
        raiderId = raiderId,
        characterName = characterName,
        characterRealm = characterRealm,
        totalDeaths = totalDeaths,
        totalFights = totalFights,
        deathsPerAttempt = deathsPerAttempt,
        avoidableDamagePercentage = avoidableDamagePercentage,
        periodStart = periodStart,
        periodEnd = periodEnd
    )
}
