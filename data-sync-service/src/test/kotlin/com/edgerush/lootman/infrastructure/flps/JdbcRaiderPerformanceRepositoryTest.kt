package com.edgerush.lootman.infrastructure.flps

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.RaiderPerformanceData
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.RaiderId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit tests for JdbcRaiderPerformanceRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository aggregates data from warcraft_logs_performance, warcraft_logs_fights,
 * and warcraft_logs_reports tables.
 */
class JdbcRaiderPerformanceRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaiderPerformanceRepository

    private val guildId = GuildId("test-guild")
    private val now = Instant.now()
    private val oneWeekAgo = now.minus(7, ChronoUnit.DAYS)

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaiderPerformanceRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByRaiderAndPeriodTests {

        @Test
        fun `should return performance data when found`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("raiders") && it.contains("warcraft_logs_performance") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(raiderId.value),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPerformanceData>>()
                val rs = mockPerformanceResultSet(
                    raiderId = 100L,
                    characterName = "TestRaider",
                    characterRealm = "Area52",
                    totalDeaths = 5,
                    totalFights = 20,
                    avgAvoidableDamage = 12.5,
                    periodStart = oneWeekAgo,
                    periodEnd = now
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldNotBe null
            result?.raiderId shouldBe raiderId
            result?.characterName shouldBe "TestRaider"
            result?.characterRealm shouldBe "Area52"
            result?.totalDeaths shouldBe 5
            result?.totalFights shouldBe 20
            result?.avoidableDamagePercentage shouldBe 12.5
        }

        @Test
        fun `should return null when no data found`() {
            // Given
            val raiderId = RaiderId(999L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("raiders") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(raiderId.value),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } returns emptyList()

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldBe null
        }

        @Test
        fun `should calculate deaths per attempt correctly`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("raiders") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(raiderId.value),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPerformanceData>>()
                val rs = mockPerformanceResultSet(
                    totalDeaths = 10,
                    totalFights = 40
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldNotBe null
            result?.deathsPerAttempt shouldBe 0.25
        }
    }

    @Nested
    inner class FindByCharacterAndPeriodTests {

        @Test
        fun `should return performance data when character found`() {
            // Given
            val characterName = "UniqueChar"
            val characterRealm = "Illidan"

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("character_name") && it.contains("character_realm") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(characterName),
                    eq(characterRealm),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPerformanceData>>()
                val rs = mockPerformanceResultSet(
                    characterName = characterName,
                    characterRealm = characterRealm,
                    totalDeaths = 3,
                    totalFights = 15
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByCharacterAndPeriod(
                characterName,
                characterRealm,
                guildId,
                oneWeekAgo,
                now
            )

            // Then
            result shouldNotBe null
            result?.characterName shouldBe characterName
            result?.characterRealm shouldBe characterRealm
            result?.totalDeaths shouldBe 3
            result?.totalFights shouldBe 15
        }

        @Test
        fun `should return null when character not found`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("character_name") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    any(),
                    any(),
                    any(),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } returns emptyList()

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
    }

    @Nested
    inner class FindAllByGuildAndPeriodTests {

        @Test
        fun `should return all performance data for guild`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("guild_id") && !it.contains("raiders.id") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPerformanceData>>()
                listOf(
                    rowMapper.mapRow(
                        mockPerformanceResultSet(raiderId = 1L, characterName = "Raider1"),
                        0
                    ),
                    rowMapper.mapRow(
                        mockPerformanceResultSet(raiderId = 2L, characterName = "Raider2"),
                        1
                    ),
                    rowMapper.mapRow(
                        mockPerformanceResultSet(raiderId = 3L, characterName = "Raider3"),
                        2
                    )
                )
            }

            // When
            val result = repository.findAllByGuildAndPeriod(guildId, oneWeekAgo, now)

            // Then
            result shouldHaveSize 3
            result.map { it.characterName } shouldBe listOf("Raider1", "Raider2", "Raider3")
        }

        @Test
        fun `should return empty list when no data for guild`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("guild_id") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } returns emptyList()

            // When
            val result = repository.findAllByGuildAndPeriod(guildId, oneWeekAgo, now)

            // Then
            result shouldBe emptyList()
        }

        @Test
        fun `should aggregate deaths and fights across multiple reports`() {
            // Given - The SQL aggregates SUM(deaths), COUNT(*) across fights
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SUM") || it.contains("guild_id") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPerformanceData>>()
                listOf(
                    rowMapper.mapRow(
                        mockPerformanceResultSet(
                            totalDeaths = 15, // Aggregated from multiple fights
                            totalFights = 50  // Total fight count
                        ),
                        0
                    )
                )
            }

            // When
            val result = repository.findAllByGuildAndPeriod(guildId, oneWeekAgo, now)

            // Then
            result shouldHaveSize 1
            result[0].totalDeaths shouldBe 15
            result[0].totalFights shouldBe 50
            result[0].deathsPerAttempt shouldBe 0.3
        }
    }

    @Nested
    inner class EdgeCaseTests {

        @Test
        fun `should handle zero fights gracefully`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("raiders") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(raiderId.value),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPerformanceData>>()
                val rs = mockPerformanceResultSet(
                    totalDeaths = 0,
                    totalFights = 0
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldNotBe null
            result?.deathsPerAttempt shouldBe 0.0
        }

        @Test
        fun `should handle null avoidable damage percentage`() {
            // Given
            val raiderId = RaiderId(100L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("raiders") },
                    any<RowMapper<RaiderPerformanceData>>(),
                    eq(raiderId.value),
                    eq(guildId.value),
                    any<Timestamp>(),
                    any<Timestamp>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPerformanceData>>()
                val rs = mockk<ResultSet>()
                every { rs.getLong("raider_id") } returns 100L
                every { rs.getString("character_name") } returns "TestRaider"
                every { rs.getString("character_realm") } returns "Area52"
                every { rs.getInt("total_deaths") } returns 5
                every { rs.getInt("total_fights") } returns 20
                every { rs.getDouble("avg_avoidable_damage") } returns 0.0
                every { rs.wasNull() } returns true // Null value
                every { rs.getTimestamp("period_start") } returns Timestamp.from(oneWeekAgo)
                every { rs.getTimestamp("period_end") } returns Timestamp.from(now)
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByRaiderAndPeriod(raiderId, guildId, oneWeekAgo, now)

            // Then
            result shouldNotBe null
            result?.avoidableDamagePercentage shouldBe 0.0
        }
    }

    // Helper method to create mock ResultSet
    private fun mockPerformanceResultSet(
        raiderId: Long = 100L,
        characterName: String = "TestRaider",
        characterRealm: String = "Area52",
        totalDeaths: Int = 5,
        totalFights: Int = 20,
        avgAvoidableDamage: Double = 10.0,
        periodStart: Instant = oneWeekAgo,
        periodEnd: Instant = now
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("raider_id") } returns raiderId
        every { rs.getString("character_name") } returns characterName
        every { rs.getString("character_realm") } returns characterRealm
        every { rs.getInt("total_deaths") } returns totalDeaths
        every { rs.getInt("total_fights") } returns totalFights
        every { rs.getDouble("avg_avoidable_damage") } returns avgAvoidableDamage
        every { rs.wasNull() } returns false
        every { rs.getTimestamp("period_start") } returns Timestamp.from(periodStart)
        every { rs.getTimestamp("period_end") } returns Timestamp.from(periodEnd)
        return rs
    }
}
