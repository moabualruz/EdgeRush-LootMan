package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderPvpBracketEntity
import com.edgerush.datasync.test.base.UnitTest
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

/**
 * Unit tests for JdbcRaiderPvpBracketRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the raider_pvp_bracket_stats table.
 */
class JdbcRaiderPvpBracketRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaiderPvpBracketRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaiderPvpBracketRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return pvp bracket when found`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderPvpBracketEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPvpBracketEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, 100L), 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
        }

        @Test
        fun `should return null when pvp bracket not found`() {
            // Given
            val id = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderPvpBracketEntity>>(),
                    eq(id)
                )
            } returns emptyList()

            // When
            val result = repository.findById(id)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to entity`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderPvpBracketEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPvpBracketEntity>>()
                val rs = mockResultSet(
                    id = id,
                    raiderId = 100L,
                    bracket = "2v2",
                    rating = 1800,
                    seasonPlayed = 50,
                    weekPlayed = 10,
                    maxRating = 2000
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
            result?.bracket shouldBe "2v2"
            result?.rating shouldBe 1800
            result?.seasonPlayed shouldBe 50
            result?.weekPlayed shouldBe 10
            result?.maxRating shouldBe 2000
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderPvpBracketEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPvpBracketEntity>>()
                val rs = mockResultSet(
                    id = id,
                    raiderId = 100L,
                    bracket = "3v3",
                    rating = null,
                    seasonPlayed = null,
                    weekPlayed = null,
                    maxRating = null
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.rating shouldBe null
            result?.seasonPlayed shouldBe null
            result?.weekPlayed shouldBe null
            result?.maxRating shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paginated pvp brackets`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<RaiderPvpBracketEntity>>(),
                    eq(limit),
                    eq(offset)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPvpBracketEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, 100L), 0),
                    rowMapper.mapRow(mockResultSet(2L, 100L), 1)
                )
            }

            // When
            val result = repository.findAll(offset, limit)

            // Then
            result.size shouldBe 2
        }
    }

    @Nested
    inner class FindByRaiderIdTests {

        @Test
        fun `should return pvp brackets for raider`() {
            // Given
            val raiderId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<RaiderPvpBracketEntity>>(),
                    eq(raiderId),
                    any<Int>(),
                    any<Long>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderPvpBracketEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, raiderId, bracket = "2v2"), 0),
                    rowMapper.mapRow(mockResultSet(2L, raiderId, bracket = "3v3"), 1)
                )
            }

            // When
            val result = repository.findByRaiderId(raiderId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId } shouldBe true
        }

        @Test
        fun `should return empty list when raider has no pvp brackets`() {
            // Given
            val raiderId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<RaiderPvpBracketEntity>>(),
                    eq(raiderId),
                    any<Int>(),
                    any<Long>()
                )
            } returns emptyList()

            // When
            val result = repository.findByRaiderId(raiderId, 0L, 10)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class CountTests {

        @Test
        fun `should return total count`() {
            // Given
            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("raider_pvp_bracket_stats") },
                    Long::class.java
                )
            } returns 42L

            // When
            val result = repository.count()

            // Then
            result shouldBe 42L
        }

        @Test
        fun `should handle null count result`() {
            // Given
            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") },
                    Long::class.java
                )
            } returns null

            // When
            val result = repository.count()

            // Then
            result shouldBe 0L
        }

        @Test
        fun `should return count by raider id`() {
            // Given
            val raiderId = 100L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("raider_id = ?") },
                    Long::class.java,
                    eq(raiderId)
                )
            } returns 3L

            // When
            val result = repository.countByRaiderId(raiderId)

            // Then
            result shouldBe 3L
        }
    }

    @Nested
    inner class ExistsByIdTests {

        @Test
        fun `should return true when pvp bracket exists`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(id)
                )
            } returns 1

            // When
            val result = repository.existsById(id)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when pvp bracket does not exist`() {
            // Given
            val id = 999L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(id)
                )
            } returns 0

            // When
            val result = repository.existsById(id)

            // Then
            result shouldBe false
        }

        @Test
        fun `should handle null count result as false`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(id)
                )
            } returns null

            // When
            val result = repository.existsById(id)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `should insert new pvp bracket when id is null`() {
            // Given
            val entity = createPvpBracketEntity(id = null)
            val generatedId = 1L

            every {
                jdbcTemplate.update(any<org.springframework.jdbc.core.PreparedStatementCreator>(), any<GeneratedKeyHolder>())
            } answers {
                val keyHolder = secondArg<GeneratedKeyHolder>()
                keyHolder.keyList.add(mapOf("id" to generatedId))
                1
            }

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe generatedId
            result.raiderId shouldBe entity.raiderId
        }

        @Test
        fun `should update existing pvp bracket when id is not null`() {
            // Given
            val entity = createPvpBracketEntity(id = 1L)
            val sqlSlot = slot<String>()

            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            sqlSlot.captured.contains("UPDATE") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    *anyVararg()
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should delete pvp bracket by id`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(id)
                )
            } returns 1

            // When
            repository.delete(id)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    id
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: Long,
        raiderId: Long,
        bracket: String = "2v2",
        rating: Int? = 1500,
        seasonPlayed: Int? = 30,
        weekPlayed: Int? = 5,
        maxRating: Int? = 1700
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("raider_id") } returns raiderId
        every { rs.getString("bracket") } returns bracket
        every { rs.getInt("rating") } returns (rating ?: 0)
        every { rs.getInt("season_played") } returns (seasonPlayed ?: 0)
        every { rs.getInt("week_played") } returns (weekPlayed ?: 0)
        every { rs.getInt("max_rating") } returns (maxRating ?: 0)
        // Handle wasNull() for multiple nullable int fields
        var wasNullCalled = 0
        every { rs.wasNull() } answers {
            val result = when (wasNullCalled) {
                0 -> rating == null
                1 -> seasonPlayed == null
                2 -> weekPlayed == null
                3 -> maxRating == null
                else -> false
            }
            wasNullCalled++
            result
        }
        return rs
    }

    private fun createPvpBracketEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        bracket: String = "2v2",
        rating: Int? = 1500,
        seasonPlayed: Int? = 30,
        weekPlayed: Int? = 5,
        maxRating: Int? = 1700
    ): RaiderPvpBracketEntity = RaiderPvpBracketEntity(
        id = id,
        raiderId = raiderId,
        bracket = bracket,
        rating = rating,
        seasonPlayed = seasonPlayed,
        weekPlayed = weekPlayed,
        maxRating = maxRating
    )
}
