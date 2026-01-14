package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.entity.LootAwardWishDataEntity
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
 * Unit tests for JdbcLootAwardWishDataRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the loot_award_wish_data table.
 */
class JdbcLootAwardWishDataRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcLootAwardWishDataRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcLootAwardWishDataRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return wish data when found`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAwardWishDataEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardWishDataEntity>>()
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
        fun `should return null when wish data not found`() {
            // Given
            val id = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAwardWishDataEntity>>(),
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
                    any<RowMapper<LootAwardWishDataEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardWishDataEntity>>()
                val rs = mockResultSet(
                    id = id,
                    lootAwardId = 100L,
                    specName = "Frost",
                    specIcon = "spell_deathknight_frostpresence",
                    value = 5
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
            result?.lootAwardId shouldBe 100L
            result?.specName shouldBe "Frost"
            result?.specIcon shouldBe "spell_deathknight_frostpresence"
            result?.value shouldBe 5
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val id = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAwardWishDataEntity>>(),
                    eq(id)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardWishDataEntity>>()
                val rs = mockResultSet(
                    id = id,
                    lootAwardId = 100L,
                    specName = null,
                    specIcon = null,
                    value = null
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.specName shouldBe null
            result?.specIcon shouldBe null
            result?.value shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paginated wish data`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<LootAwardWishDataEntity>>(),
                    eq(limit),
                    eq(offset)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardWishDataEntity>>()
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
        fun `should return wish data for loot award`() {
            // Given
            val lootAwardId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("loot_award_id = ?") },
                    any<RowMapper<LootAwardWishDataEntity>>(),
                    eq(lootAwardId),
                    any<Int>(),
                    any<Long>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAwardWishDataEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, lootAwardId, specName = "Frost"), 0),
                    rowMapper.mapRow(mockResultSet(2L, lootAwardId, specName = "Unholy"), 1)
                )
            }

            // When
            val result = repository.findByLootAwardId(lootAwardId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.lootAwardId == lootAwardId } shouldBe true
        }

        @Test
        fun `should return empty list when loot award has no wish data`() {
            // Given
            val lootAwardId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("loot_award_id = ?") },
                    any<RowMapper<LootAwardWishDataEntity>>(),
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
                    match<String> { it.contains("COUNT(*)") && it.contains("loot_award_wish_data") },
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
        fun `should return true when wish data exists`() {
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
        fun `should return false when wish data does not exist`() {
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
        fun `should insert new wish data when id is null`() {
            // Given
            val entity = createWishDataEntity(id = null)
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
        fun `should update existing wish data when id is not null`() {
            // Given
            val entity = createWishDataEntity(id = 1L)
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
        fun `should delete wish data by id`() {
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
        specName: String? = "Frost",
        specIcon: String? = "spell_icon",
        value: Int? = 5
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("loot_award_id") } returns lootAwardId
        every { rs.getString("spec_name") } returns specName
        every { rs.getString("spec_icon") } returns specIcon
        every { rs.getInt("value") } returns (value ?: 0)
        every { rs.wasNull() } returns (value == null)
        return rs
    }

    private fun createWishDataEntity(
        id: Long? = 1L,
        lootAwardId: Long = 100L,
        specName: String? = "Frost",
        specIcon: String? = "spell_icon",
        value: Int? = 5
    ): LootAwardWishDataEntity = LootAwardWishDataEntity(
        id = id,
        lootAwardId = lootAwardId,
        specName = specName,
        specIcon = specIcon,
        value = value
    )
}
