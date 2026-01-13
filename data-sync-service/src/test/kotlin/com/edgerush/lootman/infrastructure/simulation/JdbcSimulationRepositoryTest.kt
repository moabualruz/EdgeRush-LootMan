package com.edgerush.lootman.infrastructure.simulation

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

/**
 * Unit tests for JdbcSimulationRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * Integration tests with real database should be in a separate test class.
 */
class JdbcSimulationRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcSimulationRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcSimulationRepository(jdbcTemplate)
    }

    private fun createProfile(): SimulationProfile {
        return SimulationProfile.create(
            guildId = "guild-123",
            characterName = "Testchar",
            characterRealm = "TestRealm",
            profileContent = """warrior="Testchar"""",
            createdAt = Instant.now()
        )
    }

    @Nested
    inner class SaveProfile {
        @Test
        fun `should execute upsert query for profile`() {
            // Arrange
            val profile = createProfile()
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1
            every { jdbcTemplate.queryForObject(any<String>(), eq(Long::class.java), *anyVararg()) } returns 1L

            // Act
            val result = repository.saveProfile(profile)

            // Assert
            result.first shouldBe 1L
            result.second shouldBe profile
            verify { jdbcTemplate.update(match { it.contains("INSERT INTO simulation_profiles") }, *anyVararg()) }
        }
    }

    @Nested
    inner class FindProfileByCharacter {
        @Test
        fun `should query by guild, character, and realm`() {
            // Arrange
            val profile = createProfile()
            every {
                jdbcTemplate.query(
                    match { it.contains("SELECT") && it.contains("simulation_profiles") },
                    any<RowMapper<SimulationProfile>>(),
                    eq("guild-123"),
                    eq("Testchar"),
                    eq("TestRealm")
                )
            } returns listOf(profile)

            // Act
            val result = repository.findProfileByCharacter("guild-123", "Testchar", "TestRealm")

            // Assert
            result shouldBe profile
        }

        @Test
        fun `should return null when not found`() {
            // Arrange
            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<SimulationProfile>>(),
                    *anyVararg()
                )
            } returns emptyList<SimulationProfile>()

            // Act
            val result = repository.findProfileByCharacter("guild-123", "Unknown", "TestRealm")

            // Assert
            result shouldBe null
        }
    }

    @Nested
    inner class FindProfileIdByCharacter {
        @Test
        fun `should return profile ID when found`() {
            // Arrange
            every {
                jdbcTemplate.queryForObject(
                    match { it.contains("SELECT id FROM simulation_profiles") },
                    eq(Long::class.java),
                    eq("guild-123"),
                    eq("Testchar"),
                    eq("TestRealm")
                )
            } returns 42L

            // Act
            val result = repository.findProfileIdByCharacter("guild-123", "Testchar", "TestRealm")

            // Assert
            result shouldBe 42L
        }

        @Test
        fun `should return null when not found`() {
            // Arrange
            every {
                jdbcTemplate.queryForObject(
                    any<String>(),
                    eq(Long::class.java),
                    *anyVararg()
                )
            } throws org.springframework.dao.EmptyResultDataAccessException(1)

            // Act
            val result = repository.findProfileIdByCharacter("guild-123", "Unknown", "TestRealm")

            // Assert
            result shouldBe null
        }
    }

    @Nested
    inner class SaveRequest {
        @Test
        fun `should insert new request`() {
            // Arrange
            val profile = createProfile()
            val request = SimulationRequest.create(profile = profile)

            // Mock all queryForObject calls to return 1L
            every { jdbcTemplate.queryForObject(any<String>(), eq(Long::class.java), *anyVararg()) } returns 1L
            every { jdbcTemplate.queryForObject(any<String>(), eq(Long::class.java)) } returns 1L
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // Act
            val result = repository.saveRequest(request)

            // Assert
            result.id shouldNotBe null
            verify { jdbcTemplate.update(match { it.contains("INSERT INTO simulation_requests") }, *anyVararg()) }
        }
    }

    @Nested
    inner class FindPendingRequests {
        @Test
        fun `should query for pending status`() {
            // Arrange
            every {
                jdbcTemplate.query(
                    match { it.contains("status = ?") && it.contains("PENDING") },
                    any<RowMapper<SimulationRequest>>(),
                    eq("PENDING")
                )
            } returns emptyList<SimulationRequest>()

            // Act
            val results = repository.findPendingRequests()

            // Assert
            results.shouldBeEmpty()
        }
    }

    @Nested
    inner class SaveResult {
        @Test
        fun `should insert simulation result`() {
            // Arrange
            val result = SimulationResult.create(
                itemId = 12345L,
                itemName = "Test Item",
                slot = "head",
                dpsGain = 1000.0,
                percentGain = 1.0,
                simulatedAt = Instant.now()
            )

            // Act
            repository.saveResult(1L, result)

            // Assert
            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO simulation_results") },
                    eq(1L),
                    eq(12345L),
                    eq("Test Item"),
                    eq("head"),
                    eq(1000.0),
                    eq(1.0),
                    any<Timestamp>()
                )
            }
        }
    }

    @Nested
    inner class FindLatestResultForItem {
        @Test
        fun `should order by simulated_at desc and limit 1`() {
            // Arrange
            val result = SimulationResult.create(
                itemId = 12345L,
                itemName = "Test Item",
                slot = "head",
                dpsGain = 1000.0,
                percentGain = 1.0,
                simulatedAt = Instant.now()
            )

            every {
                jdbcTemplate.query(
                    match { it.contains("ORDER BY simulated_at DESC") && it.contains("LIMIT 1") },
                    any<RowMapper<SimulationResult>>(),
                    eq(1L),
                    eq(12345L)
                )
            } returns listOf(result)

            // Act
            val found = repository.findLatestResultForItem(1L, 12345L)

            // Assert
            found shouldBe result
        }
    }
}
