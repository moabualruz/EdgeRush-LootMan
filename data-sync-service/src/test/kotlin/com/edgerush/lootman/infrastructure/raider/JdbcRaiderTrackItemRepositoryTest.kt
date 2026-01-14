package com.edgerush.lootman.infrastructure.raider

import com.edgerush.datasync.entity.RaiderTrackItemEntity
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
 * Unit tests for JdbcRaiderTrackItemRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the raider_track_items table.
 */
class JdbcRaiderTrackItemRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaiderTrackItemRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaiderTrackItemRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return track item when found`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderTrackItemEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderTrackItemEntity>>()
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
        fun `should return null when track item not found`() {
            // Given
            val id = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderTrackItemEntity>>(),
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
                    any<RowMapper<RaiderTrackItemEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderTrackItemEntity>>()
                val rs = mockResultSet(
                    id = id,
                    raiderId = 100L,
                    tier = "Heroic",
                    itemCount = 5
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.raiderId shouldBe 100L
            result?.tier shouldBe "Heroic"
            result?.itemCount shouldBe 5
        }

        @Test
        fun `should handle null item count`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<RaiderTrackItemEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderTrackItemEntity>>()
                val rs = mockResultSet(
                    id = id,
                    raiderId = 100L,
                    tier = "Mythic",
                    itemCount = null
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.itemCount shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paginated track items`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<RaiderTrackItemEntity>>(),
                    eq(limit),
                    eq(offset)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderTrackItemEntity>>()
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
        fun `should return track items for raider`() {
            // Given
            val raiderId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<RaiderTrackItemEntity>>(),
                    eq(raiderId),
                    any<Int>(),
                    any<Long>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderTrackItemEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, raiderId, tier = "Normal"), 0),
                    rowMapper.mapRow(mockResultSet(2L, raiderId, tier = "Heroic"), 1)
                )
            }

            // When
            val result = repository.findByRaiderId(raiderId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId } shouldBe true
        }

        @Test
        fun `should return empty list when raider has no track items`() {
            // Given
            val raiderId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<RaiderTrackItemEntity>>(),
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
                    match<String> { it.contains("COUNT(*)") && it.contains("raider_track_items") },
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
        fun `should return true when track item exists`() {
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
        fun `should return false when track item does not exist`() {
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
        fun `should insert new track item when id is null`() {
            // Given
            val entity = createTrackItemEntity(id = null)
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
        fun `should update existing track item when id is not null`() {
            // Given
            val entity = createTrackItemEntity(id = 1L)
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
        fun `should delete track item by id`() {
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
        tier: String = "Heroic",
        itemCount: Int? = 3
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("raider_id") } returns raiderId
        every { rs.getString("tier") } returns tier
        every { rs.getInt("item_count") } returns (itemCount ?: 0)
        every { rs.wasNull() } returns (itemCount == null)
        return rs
    }

    private fun createTrackItemEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        tier: String = "Heroic",
        itemCount: Int? = 3
    ): RaiderTrackItemEntity = RaiderTrackItemEntity(
        id = id,
        raiderId = raiderId,
        tier = tier,
        itemCount = itemCount
    )
}
