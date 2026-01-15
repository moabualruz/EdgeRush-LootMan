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
            createdAt = Instant.now(),
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
                    eq("TestRealm"),
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
                    *anyVararg(),
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
                    eq("TestRealm"),
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
                    *anyVararg(),
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
                    eq("PENDING"),
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
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = Instant.now(),
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
                    any<Timestamp>(),
                )
            }
        }
    }

    @Nested
    inner class FindLatestResultForItem {
        @Test
        fun `should order by simulated_at desc and limit 1`() {
            // Arrange
            val result =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Test Item",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = Instant.now(),
                )

            every {
                jdbcTemplate.query(
                    match { it.contains("ORDER BY simulated_at DESC") && it.contains("LIMIT 1") },
                    any<RowMapper<SimulationResult>>(),
                    eq(1L),
                    eq(12345L),
                )
            } returns listOf(result)

            // Act
            val found = repository.findLatestResultForItem(1L, 12345L)

            // Assert
            found shouldBe result
        }

        @Test
        fun `should return null when no results found`() {
            // Arrange
            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<SimulationResult>>(),
                    any<Long>(),
                    any<Long>(),
                )
            } returns emptyList<SimulationResult>()

            // Act
            val found = repository.findLatestResultForItem(1L, 99999L)

            // Assert
            found shouldBe null
        }
    }

    @Nested
    inner class FindProfileById {
        @Test
        fun `should query by profile id`() {
            // Arrange
            val profile = createProfile()
            every {
                jdbcTemplate.query(
                    match { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    any<RowMapper<SimulationProfile>>(),
                    eq(42L),
                )
            } returns listOf(profile)

            // Act
            val result = repository.findProfileById(42L)

            // Assert
            result shouldBe profile
        }

        @Test
        fun `should return null when profile not found`() {
            // Arrange
            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<SimulationProfile>>(),
                    any<Long>(),
                )
            } returns emptyList<SimulationProfile>()

            // Act
            val result = repository.findProfileById(99999L)

            // Assert
            result shouldBe null
        }
    }

    @Nested
    inner class FindRequestById {
        @Test
        fun `should return null when request not found`() {
            // Arrange
            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<SimulationRequest>>(),
                    any<Long>(),
                )
            } returns emptyList<SimulationRequest>()

            // Act
            val result = repository.findRequestById(99999L)

            // Assert
            result shouldBe null
        }
    }

    @Nested
    inner class FindResultsByProfile {
        @Test
        fun `should return all results for profile ordered by date`() {
            // Arrange
            val result1 =
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "Item 1",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = Instant.now(),
                )
            val result2 =
                SimulationResult.create(
                    itemId = 12346L,
                    itemName = "Item 2",
                    slot = "neck",
                    dpsGain = 500.0,
                    percentGain = 0.5,
                    simulatedAt = Instant.now(),
                )

            every {
                jdbcTemplate.query(
                    match { it.contains("WHERE profile_id = ?") && it.contains("ORDER BY simulated_at DESC") },
                    any<RowMapper<SimulationResult>>(),
                    eq(1L),
                )
            } returns listOf(result1, result2)

            // Act
            val results = repository.findResultsByProfile(1L)

            // Assert
            results shouldHaveSize 2
            results[0] shouldBe result1
            results[1] shouldBe result2
        }

        @Test
        fun `should return empty list when no results found`() {
            // Arrange
            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<SimulationResult>>(),
                    any<Long>(),
                )
            } returns emptyList<SimulationResult>()

            // Act
            val results = repository.findResultsByProfile(99999L)

            // Assert
            results.shouldBeEmpty()
        }
    }

    @Nested
    inner class RowMapperTests {
        @Test
        fun `should invoke profileRowMapper and map fields correctly`() {
            // Arrange
            val now = Instant.now()
            val rs = mockk<ResultSet>()
            every { rs.getString("guild_id") } returns "guild-123"
            every { rs.getString("character_name") } returns "TestChar"
            every { rs.getString("character_realm") } returns "TestRealm"
            every { rs.getString("profile_content") } returns "warrior=\"TestChar\""
            every { rs.getTimestamp("created_at") } returns Timestamp.from(now)

            val capturedMapper = slot<RowMapper<SimulationProfile>>()
            every {
                jdbcTemplate.query(
                    match { it.contains("SELECT") && it.contains("WHERE id = ?") },
                    capture(capturedMapper),
                    eq(42L),
                )
            } answers {
                listOf(capturedMapper.captured.mapRow(rs, 0))
            }

            // Act
            val result = repository.findProfileById(42L)

            // Assert
            result shouldNotBe null
            result?.guildId shouldBe "guild-123"
            result?.characterName shouldBe "TestChar"
            result?.characterRealm shouldBe "TestRealm"
        }

        @Test
        fun `should invoke resultRowMapper and map fields correctly`() {
            // Arrange
            val now = Instant.now()
            val rs = mockk<ResultSet>()
            every { rs.getLong("item_id") } returns 12345L
            every { rs.getString("item_name") } returns "Test Item"
            every { rs.getString("slot") } returns "head"
            every { rs.getDouble("dps_gain") } returns 1000.0
            every { rs.getDouble("percent_gain") } returns 1.5
            every { rs.getTimestamp("simulated_at") } returns Timestamp.from(now)

            val capturedMapper = slot<RowMapper<SimulationResult>>()
            every {
                jdbcTemplate.query(
                    match { it.contains("ORDER BY simulated_at DESC") && it.contains("LIMIT 1") },
                    capture(capturedMapper),
                    eq(1L),
                    eq(12345L),
                )
            } answers {
                listOf(capturedMapper.captured.mapRow(rs, 0))
            }

            // Act
            val found = repository.findLatestResultForItem(1L, 12345L)

            // Assert
            found shouldNotBe null
            found?.itemId shouldBe 12345L
            found?.itemName shouldBe "Test Item"
            found?.slot shouldBe "head"
            found?.dpsGain shouldBe 1000.0
            found?.percentGain shouldBe 1.5
        }
    }

    @Nested
    inner class MapRequestRowTests {
        private fun setupMockResultSetForRequest(
            status: String,
            errorMessage: String? = null,
        ): ResultSet {
            val now = Instant.now()
            val rs = mockk<ResultSet>()

            // Profile fields
            every { rs.getString("guild_id") } returns "guild-123"
            every { rs.getString("character_name") } returns "TestChar"
            every { rs.getString("character_realm") } returns "TestRealm"
            every { rs.getString("profile_content") } returns "warrior=\"TestChar\""
            every { rs.getTimestamp("profile_created_at") } returns Timestamp.from(now)

            // Request fields
            every { rs.getLong("id") } returns 42L
            every { rs.getInt("iterations") } returns 10000
            every { rs.getInt("fight_length_seconds") } returns 300
            every { rs.getString("status") } returns status
            every { rs.getTimestamp("submitted_at") } returns Timestamp.from(now)
            every { rs.getTimestamp("completed_at") } returns Timestamp.from(now)
            every { rs.getString("error_message") } returns errorMessage

            return rs
        }

        @Test
        fun `should map PENDING status correctly`() {
            // Arrange
            val rs = setupMockResultSetForRequest("PENDING")
            val mapperSlot = slot<RowMapper<SimulationRequest>>()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("simulation_requests") && it.contains("WHERE r.id = ?") },
                    capture(mapperSlot),
                    *varargAll { true },
                )
            } answers {
                listOf(mapperSlot.captured.mapRow(rs, 0)!!)
            }

            // Act
            val result = repository.findRequestById(42L)

            // Assert
            result shouldNotBe null
            result?.id shouldBe 42L
            result?.status shouldBe SimulationStatus.PENDING
        }

        @Test
        fun `should map RUNNING status correctly`() {
            // Arrange
            val rs = setupMockResultSetForRequest("RUNNING")
            val mapperSlot = slot<RowMapper<SimulationRequest>>()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("simulation_requests") && it.contains("WHERE r.id = ?") },
                    capture(mapperSlot),
                    *varargAll { true },
                )
            } answers {
                listOf(mapperSlot.captured.mapRow(rs, 0)!!)
            }

            // Act
            val result = repository.findRequestById(42L)

            // Assert
            result shouldNotBe null
            result?.status shouldBe SimulationStatus.RUNNING
        }

        @Test
        fun `should map COMPLETED status correctly`() {
            // Arrange
            val rs = setupMockResultSetForRequest("COMPLETED")
            val mapperSlot = slot<RowMapper<SimulationRequest>>()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("simulation_requests") && it.contains("WHERE r.id = ?") },
                    capture(mapperSlot),
                    *varargAll { true },
                )
            } answers {
                listOf(mapperSlot.captured.mapRow(rs, 0)!!)
            }

            // Act
            val result = repository.findRequestById(42L)

            // Assert
            result shouldNotBe null
            result?.status shouldBe SimulationStatus.COMPLETED
        }

        @Test
        fun `should map FAILED status with error message`() {
            // Arrange
            val rs = setupMockResultSetForRequest("FAILED", "Simulation timeout")
            val mapperSlot = slot<RowMapper<SimulationRequest>>()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("simulation_requests") && it.contains("WHERE r.id = ?") },
                    capture(mapperSlot),
                    *varargAll { true },
                )
            } answers {
                listOf(mapperSlot.captured.mapRow(rs, 0)!!)
            }

            // Act
            val result = repository.findRequestById(42L)

            // Assert
            result shouldNotBe null
            result?.status shouldBe SimulationStatus.FAILED
            result?.errorMessage shouldBe "Simulation timeout"
        }

        @Test
        fun `should default to Unknown error when error message is null for FAILED status`() {
            // Arrange
            val rs = setupMockResultSetForRequest("FAILED", null)
            val mapperSlot = slot<RowMapper<SimulationRequest>>()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("simulation_requests") && it.contains("WHERE r.id = ?") },
                    capture(mapperSlot),
                    *varargAll { true },
                )
            } answers {
                listOf(mapperSlot.captured.mapRow(rs, 0)!!)
            }

            // Act
            val result = repository.findRequestById(42L)

            // Assert
            result shouldNotBe null
            result?.status shouldBe SimulationStatus.FAILED
            result?.errorMessage shouldBe "Unknown error"
        }

        @Test
        fun `should map pending request via findPendingRequests`() {
            // Arrange
            val rs = setupMockResultSetForRequest("PENDING")
            val mapperSlot = slot<RowMapper<SimulationRequest>>()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("simulation_requests") && it.contains("WHERE r.status = ?") },
                    capture(mapperSlot),
                    *varargAll { true },
                )
            } answers {
                listOf(mapperSlot.captured.mapRow(rs, 0)!!)
            }

            // Act
            val results = repository.findPendingRequests()

            // Assert
            results shouldHaveSize 1
            results[0].status shouldBe SimulationStatus.PENDING
        }
    }

    @Nested
    inner class InsertWithCompletedAt {
        @Test
        fun `should insert request with non-null completedAt`() {
            // Arrange
            val profile = createProfile()
            val completedAt = Instant.now()
            val request =
                SimulationRequest.create(profile = profile)
                    .markRunning()
                    .markCompleted(emptyList())

            every { jdbcTemplate.queryForObject(any<String>(), eq(Long::class.java), *anyVararg()) } returns 1L
            every { jdbcTemplate.queryForObject(any<String>(), eq(Long::class.java)) } returns 1L
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // Act
            val result = repository.saveRequest(request)

            // Assert
            result.id shouldNotBe null
            result.completedAt shouldNotBe null
            verify { jdbcTemplate.update(match { it.contains("INSERT INTO simulation_requests") }, *anyVararg()) }
        }
    }

    @Nested
    inner class MapRequestRowNullHandlingTests {
        private fun setupMockResultSetWithNullCompletedAt(
            status: String,
            errorMessage: String? = null,
        ): ResultSet {
            val now = Instant.now()
            val rs = mockk<ResultSet>()

            // Profile fields
            every { rs.getString("guild_id") } returns "guild-123"
            every { rs.getString("character_name") } returns "TestChar"
            every { rs.getString("character_realm") } returns "TestRealm"
            every { rs.getString("profile_content") } returns "warrior=\"TestChar\""
            every { rs.getTimestamp("profile_created_at") } returns Timestamp.from(now)

            // Request fields
            every { rs.getLong("id") } returns 42L
            every { rs.getInt("iterations") } returns 10000
            every { rs.getInt("fight_length_seconds") } returns 300
            every { rs.getString("status") } returns status
            every { rs.getTimestamp("submitted_at") } returns Timestamp.from(now)
            every { rs.getTimestamp("completed_at") } returns null // NULL completed_at
            every { rs.getString("error_message") } returns errorMessage

            return rs
        }

        @Test
        fun `should handle null completedAt for PENDING request`() {
            // Arrange
            val rs = setupMockResultSetWithNullCompletedAt("PENDING")
            val mapperSlot = slot<RowMapper<SimulationRequest>>()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("simulation_requests") && it.contains("WHERE r.id = ?") },
                    capture(mapperSlot),
                    *varargAll { true },
                )
            } answers {
                listOf(mapperSlot.captured.mapRow(rs, 0)!!)
            }

            // Act
            val result = repository.findRequestById(42L)

            // Assert
            result shouldNotBe null
            result?.id shouldBe 42L
            result?.status shouldBe SimulationStatus.PENDING
            result?.completedAt shouldBe null
        }

        @Test
        fun `should handle null completedAt for RUNNING request`() {
            // Arrange
            val rs = setupMockResultSetWithNullCompletedAt("RUNNING")
            val mapperSlot = slot<RowMapper<SimulationRequest>>()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("simulation_requests") && it.contains("WHERE r.id = ?") },
                    capture(mapperSlot),
                    *varargAll { true },
                )
            } answers {
                listOf(mapperSlot.captured.mapRow(rs, 0)!!)
            }

            // Act
            val result = repository.findRequestById(42L)

            // Assert
            result shouldNotBe null
            result?.status shouldBe SimulationStatus.RUNNING
            result?.completedAt shouldBe null
        }
    }

    @Nested
    inner class SaveRequestUpdate {
        @Test
        fun `should update existing request when id is present`() {
            // Arrange
            val profile = createProfile()
            val request =
                SimulationRequest.create(profile = profile)
                    .withId(42L)
                    .markRunning()

            every { jdbcTemplate.queryForObject(any<String>(), eq(Long::class.java), *anyVararg()) } returns 1L
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // Act
            val result = repository.saveRequest(request)

            // Assert
            result.id shouldBe 42L
            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE simulation_requests") && it.contains("SET status = ?") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should update status to completed`() {
            // Arrange
            val profile = createProfile()
            val request =
                SimulationRequest.create(profile = profile)
                    .withId(42L)
                    .markRunning()
                    .markCompleted(emptyList())

            every { jdbcTemplate.queryForObject(any<String>(), eq(Long::class.java), *anyVararg()) } returns 1L
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // Act
            val result = repository.saveRequest(request)

            // Assert
            result.status shouldBe SimulationStatus.COMPLETED
        }

        @Test
        fun `should update status to failed with error message`() {
            // Arrange
            val profile = createProfile()
            val request =
                SimulationRequest.create(profile = profile)
                    .withId(42L)
                    .markRunning()
                    .markFailed("Test error")

            every { jdbcTemplate.queryForObject(any<String>(), eq(Long::class.java), *anyVararg()) } returns 1L
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // Act
            val result = repository.saveRequest(request)

            // Assert
            result.status shouldBe SimulationStatus.FAILED
            result.errorMessage shouldBe "Test error"
        }
    }
}
