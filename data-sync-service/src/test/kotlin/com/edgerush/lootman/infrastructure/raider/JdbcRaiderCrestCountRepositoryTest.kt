package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderCrestCountEntity
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
 * Unit tests for JdbcRaiderCrestCountRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the raider_crest_counts table.
 */
class JdbcRaiderCrestCountRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaiderCrestCountRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaiderCrestCountRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return crest count when found`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderCrestCountEntity>>(),
                    eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderCrestCountEntity>>()
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
        fun `should return null when crest count not found`() {
            // Given
            val id = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderCrestCountEntity>>(),
                    eq(id),
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
                    any<RowMapper<RaiderCrestCountEntity>>(),
                    eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderCrestCountEntity>>()
                val rs =
                    mockResultSet(
                        id = id,
                        raiderId = 100L,
                        crestType = "Heroic",
                        crestCount = 15,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
            result?.crestType shouldBe "Heroic"
            result?.crestCount shouldBe 15
        }

        @Test
        fun `should handle null crest count`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderCrestCountEntity>>(),
                    eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderCrestCountEntity>>()
                val rs =
                    mockResultSet(
                        id = id,
                        raiderId = 100L,
                        crestType = "Mythic",
                        crestCount = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.crestCount shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated crest counts`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<RaiderCrestCountEntity>>(),
                    eq(limit),
                    eq(offset),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderCrestCountEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, 100L), 0),
                    rowMapper.mapRow(mockResultSet(2L, 100L), 1),
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
        fun `should return crest counts for raider`() {
            // Given
            val raiderId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<RaiderCrestCountEntity>>(),
                    eq(raiderId),
                    any<Int>(),
                    any<Long>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderCrestCountEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, raiderId, crestType = "Heroic"), 0),
                    rowMapper.mapRow(mockResultSet(2L, raiderId, crestType = "Mythic"), 1),
                )
            }

            // When
            val result = repository.findByRaiderId(raiderId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId } shouldBe true
        }

        @Test
        fun `should return empty list when raider has no crest counts`() {
            // Given
            val raiderId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<RaiderCrestCountEntity>>(),
                    eq(raiderId),
                    any<Int>(),
                    any<Long>(),
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
                    match<String> { it.contains("COUNT(*)") && it.contains("raider_crest_counts") },
                    Long::class.java,
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
                    Long::class.java,
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
                    eq(raiderId),
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
        fun `should return true when crest count exists`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(id),
                )
            } returns 1

            // When
            val result = repository.existsById(id)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when crest count does not exist`() {
            // Given
            val id = 999L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(id),
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
                    eq(id),
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
        fun `should insert new crest count when id is null`() {
            // Given
            val entity = createCrestCountEntity(id = null)
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
        fun `should update existing crest count when id is not null`() {
            // Given
            val entity = createCrestCountEntity(id = 1L)
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
                    *anyVararg(),
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete crest count by id`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(id),
                )
            } returns 1

            // When
            repository.delete(id)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    id,
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: Long,
        raiderId: Long,
        crestType: String = "Heroic",
        crestCount: Int? = 10,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("raider_id") } returns raiderId
        every { rs.getString("crest_type") } returns crestType
        every { rs.getInt("crest_count") } returns (crestCount ?: 0)
        every { rs.wasNull() } returns (crestCount == null)
        return rs
    }

    private fun createCrestCountEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        crestType: String = "Heroic",
        crestCount: Int? = 10,
    ): RaiderCrestCountEntity =
        RaiderCrestCountEntity(
            id = id,
            raiderId = raiderId,
            crestType = crestType,
            crestCount = crestCount,
        )
}
