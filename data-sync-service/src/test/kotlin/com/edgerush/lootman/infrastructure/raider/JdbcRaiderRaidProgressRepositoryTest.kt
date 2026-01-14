package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderRaidProgressEntity
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
 * Unit tests for JdbcRaiderRaidProgressRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the raider_raid_progress table.
 */
class JdbcRaiderRaidProgressRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaiderRaidProgressRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaiderRaidProgressRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return raid progress when found`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderRaidProgressEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderRaidProgressEntity>>()
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
        fun `should return null when raid progress not found`() {
            // Given
            val id = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderRaidProgressEntity>>(),
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
                    any<RowMapper<RaiderRaidProgressEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderRaidProgressEntity>>()
                val rs = mockResultSet(
                    id = id,
                    raiderId = 100L,
                    raid = "Nerub-ar Palace",
                    difficulty = "Mythic",
                    bossesDefeated = 8
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
            result?.raid shouldBe "Nerub-ar Palace"
            result?.difficulty shouldBe "Mythic"
            result?.bossesDefeated shouldBe 8
        }

        @Test
        fun `should handle null bosses defeated`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderRaidProgressEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderRaidProgressEntity>>()
                val rs = mockResultSet(
                    id = id,
                    raiderId = 100L,
                    raid = "Nerub-ar Palace",
                    difficulty = "Heroic",
                    bossesDefeated = null
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.bossesDefeated shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paginated raid progress`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<RaiderRaidProgressEntity>>(),
                    eq(limit),
                    eq(offset)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderRaidProgressEntity>>()
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
        fun `should return raid progress for raider`() {
            // Given
            val raiderId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<RaiderRaidProgressEntity>>(),
                    eq(raiderId),
                    any<Int>(),
                    any<Long>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderRaidProgressEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, raiderId, difficulty = "Heroic"), 0),
                    rowMapper.mapRow(mockResultSet(2L, raiderId, difficulty = "Mythic"), 1)
                )
            }

            // When
            val result = repository.findByRaiderId(raiderId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId } shouldBe true
        }

        @Test
        fun `should return empty list when raider has no raid progress`() {
            // Given
            val raiderId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<RaiderRaidProgressEntity>>(),
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
                    match<String> { it.contains("COUNT(*)") && it.contains("raider_raid_progress") },
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
            } returns 4L

            // When
            val result = repository.countByRaiderId(raiderId)

            // Then
            result shouldBe 4L
        }
    }

    @Nested
    inner class ExistsByIdTests {

        @Test
        fun `should return true when raid progress exists`() {
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
        fun `should return false when raid progress does not exist`() {
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
        fun `should insert new raid progress when id is null`() {
            // Given
            val entity = createRaidProgressEntity(id = null)
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
        fun `should update existing raid progress when id is not null`() {
            // Given
            val entity = createRaidProgressEntity(id = 1L)
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
        fun `should delete raid progress by id`() {
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
        raid: String = "Nerub-ar Palace",
        difficulty: String = "Heroic",
        bossesDefeated: Int? = 8
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("raider_id") } returns raiderId
        every { rs.getString("raid") } returns raid
        every { rs.getString("difficulty") } returns difficulty
        every { rs.getInt("bosses_defeated") } returns (bossesDefeated ?: 0)
        every { rs.wasNull() } returns (bossesDefeated == null)
        return rs
    }

    private fun createRaidProgressEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        raid: String = "Nerub-ar Palace",
        difficulty: String = "Heroic",
        bossesDefeated: Int? = 8
    ): RaiderRaidProgressEntity = RaiderRaidProgressEntity(
        id = id,
        raiderId = raiderId,
        raid = raid,
        difficulty = difficulty,
        bossesDefeated = bossesDefeated
    )
}
