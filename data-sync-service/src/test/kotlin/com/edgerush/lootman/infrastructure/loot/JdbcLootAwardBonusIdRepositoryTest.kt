package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardBonusIdEntity
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
 * Unit tests for JdbcLootAwardBonusIdRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the loot_award_bonus_ids table.
 */
class JdbcLootAwardBonusIdRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcLootAwardBonusIdRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcLootAwardBonusIdRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return bonus id when found`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAwardBonusIdEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardBonusIdEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, 100L), 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.lootAwardId shouldBe 100L
        }

        @Test
        fun `should return null when bonus id not found`() {
            // Given
            val id = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAwardBonusIdEntity>>(),
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
                    any<RowMapper<LootAwardBonusIdEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardBonusIdEntity>>()
                val rs = mockResultSet(
                    id = id,
                    lootAwardId = 100L,
                    bonusId = "6652"
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.lootAwardId shouldBe 100L
            result?.bonusId shouldBe "6652"
        }

        @Test
        fun `should handle null bonus id`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAwardBonusIdEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardBonusIdEntity>>()
                val rs = mockResultSet(
                    id = id,
                    lootAwardId = 100L,
                    bonusId = null
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.bonusId shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paginated bonus ids`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<LootAwardBonusIdEntity>>(),
                    eq(limit),
                    eq(offset)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardBonusIdEntity>>()
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
    inner class FindByLootAwardIdTests {

        @Test
        fun `should return bonus ids for loot award`() {
            // Given
            val lootAwardId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("loot_award_id = ?") },
                    any<RowMapper<LootAwardBonusIdEntity>>(),
                    eq(lootAwardId),
                    any<Int>(),
                    any<Long>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardBonusIdEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, lootAwardId, "6652"), 0),
                    rowMapper.mapRow(mockResultSet(2L, lootAwardId, "6653"), 1)
                )
            }

            // When
            val result = repository.findByLootAwardId(lootAwardId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.lootAwardId == lootAwardId } shouldBe true
        }

        @Test
        fun `should return empty list when loot award has no bonus ids`() {
            // Given
            val lootAwardId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("loot_award_id = ?") },
                    any<RowMapper<LootAwardBonusIdEntity>>(),
                    eq(lootAwardId),
                    any<Int>(),
                    any<Long>()
                )
            } returns emptyList()

            // When
            val result = repository.findByLootAwardId(lootAwardId, 0L, 10)

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
                    match<String> { it.contains("COUNT(*)") && it.contains("loot_award_bonus_ids") },
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
        fun `should return count by loot award id`() {
            // Given
            val lootAwardId = 100L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("loot_award_id = ?") },
                    Long::class.java,
                    eq(lootAwardId)
                )
            } returns 3L

            // When
            val result = repository.countByLootAwardId(lootAwardId)

            // Then
            result shouldBe 3L
        }
    }

    @Nested
    inner class ExistsByIdTests {

        @Test
        fun `should return true when bonus id exists`() {
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
        fun `should return false when bonus id does not exist`() {
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
        fun `should insert new bonus id when id is null`() {
            // Given
            val entity = createBonusIdEntity(id = null)
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
            result.lootAwardId shouldBe entity.lootAwardId
        }

        @Test
        fun `should update existing bonus id when id is not null`() {
            // Given
            val entity = createBonusIdEntity(id = 1L)
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
        fun `should delete bonus id by id`() {
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
        lootAwardId: Long,
        bonusId: String? = "6652"
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("loot_award_id") } returns lootAwardId
        every { rs.getString("bonus_id") } returns bonusId
        return rs
    }

    private fun createBonusIdEntity(
        id: Long? = 1L,
        lootAwardId: Long = 100L,
        bonusId: String? = "6652"
    ): LootAwardBonusIdEntity = LootAwardBonusIdEntity(
        id = id,
        lootAwardId = lootAwardId,
        bonusId = bonusId
    )
}
