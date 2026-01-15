package com.edgerush.lootman.infrastructure.gear

import com.edgerush.datasync.entity.RaiderGearItemEntity
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
 * Unit tests for JdbcRaiderGearItemRepository.
 */
class JdbcRaiderGearItemRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaiderGearItemRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaiderGearItemRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return gear item when found`() {
            val id = 1L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("id = ?")
                    },
                    any<RowMapper<RaiderGearItemEntity>>(), eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderGearItemEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, 100L), 0))
            }
            val result = repository.findById(id)
            result shouldNotBe null
            result?.id shouldBe id
        }

        @Test
        fun `should return null when gear item not found`() {
            val id = 999L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("id = ?")
                    },
                    any<RowMapper<RaiderGearItemEntity>>(), eq(id),
                )
            } returns emptyList()
            repository.findById(id) shouldBe null
        }

        @Test
        fun `should map all database fields to entity`() {
            val id = 1L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("id = ?")
                    },
                    any<RowMapper<RaiderGearItemEntity>>(), eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderGearItemEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, 100L, gearSet = "equipped", slot = "head", itemId = 12345L, itemLevel = 450, quality = 4, enchant = "Enchant", enchantQuality = 3, upgradeLevel = 2, sockets = 1, name = "Helm of Power"), 0))
            }
            val result = repository.findById(id)
            result shouldNotBe null
            result?.gearSet shouldBe "equipped"
            result?.slot shouldBe "head"
            result?.itemId shouldBe 12345L
            result?.itemLevel shouldBe 450
            result?.quality shouldBe 4
            result?.enchant shouldBe "Enchant"
            result?.enchantQuality shouldBe 3
            result?.upgradeLevel shouldBe 2
            result?.sockets shouldBe 1
            result?.name shouldBe "Helm of Power"
        }

        @Test
        fun `should handle null optional fields`() {
            val id = 1L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("id = ?")
                    },
                    any<RowMapper<RaiderGearItemEntity>>(), eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderGearItemEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, 100L, itemId = null, itemLevel = null, quality = null, enchant = null, enchantQuality = null, upgradeLevel = null, sockets = null, name = null), 0))
            }
            val result = repository.findById(id)
            result shouldNotBe null
            result?.itemId shouldBe null
            result?.itemLevel shouldBe null
            result?.quality shouldBe null
            result?.enchant shouldBe null
        }
    }

    @Nested
    inner class FindByRaiderIdTests {
        @Test
        fun `should return gear items for raider`() {
            val raiderId = 100L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("raider_id = ?") && !it.contains("gear_set = ?")
                    },
                    any<RowMapper<RaiderGearItemEntity>>(), eq(raiderId), any<Int>(), any<Long>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderGearItemEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(1L, raiderId, slot = "head"), 0), rowMapper.mapRow(mockResultSet(2L, raiderId, slot = "chest"), 1))
            }
            val result = repository.findByRaiderId(raiderId, 0L, 10)
            result.size shouldBe 2
        }
    }

    @Nested
    inner class FindByRaiderIdAndGearSetTests {
        @Test
        fun `should return gear items for raider and gear set`() {
            val raiderId = 100L
            val gearSet = "equipped"
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("raider_id = ?") && it.contains("gear_set = ?")
                    },
                    any<RowMapper<RaiderGearItemEntity>>(), eq(raiderId), eq(gearSet), any<Int>(), any<Long>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderGearItemEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(1L, raiderId, gearSet = gearSet), 0))
            }
            val result = repository.findByRaiderIdAndGearSet(raiderId, gearSet, 0L, 10)
            result.size shouldBe 1
        }
    }

    @Nested
    inner class CountTests {
        @Test
        fun `should return total count`() {
            every {
                jdbcTemplate.queryForObject(match<String> { it.contains("COUNT(*)") && it.contains("raider_gear_items") }, Long::class.java)
            } returns 42L
            repository.count() shouldBe 42L
        }

        @Test
        fun `should return count by raider id`() {
            val raiderId = 100L
            every {
                jdbcTemplate.queryForObject(
                    match<String> {
                        it.contains("COUNT(*)") && it.contains("raider_id = ?") && !it.contains("gear_set = ?")
                    },
                    Long::class.java, eq(raiderId),
                )
            } returns 16L
            repository.countByRaiderId(raiderId) shouldBe 16L
        }

        @Test
        fun `should return count by raider id and gear set`() {
            val raiderId = 100L
            val gearSet = "equipped"
            every {
                jdbcTemplate.queryForObject(
                    match<String> {
                        it.contains("COUNT(*)") && it.contains("raider_id = ?") && it.contains("gear_set = ?")
                    },
                    Long::class.java, eq(raiderId), eq(gearSet),
                )
            } returns 16L
            repository.countByRaiderIdAndGearSet(raiderId, gearSet) shouldBe 16L
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new gear item when id is null`() {
            val entity = createEntity(id = null)
            val generatedId = 1L
            every { jdbcTemplate.update(any<org.springframework.jdbc.core.PreparedStatementCreator>(), any<GeneratedKeyHolder>()) } answers {
                secondArg<GeneratedKeyHolder>().keyList.add(mapOf("id" to generatedId))
                1
            }
            val result = repository.save(entity)
            result.id shouldBe generatedId
        }

        @Test
        fun `should update existing gear item when id is not null`() {
            val entity = createEntity(id = 1L)
            val sqlSlot = slot<String>()
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1
            repository.save(entity)
            sqlSlot.captured.contains("UPDATE") shouldBe true
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete gear item by id`() {
            val id = 1L
            every { jdbcTemplate.update(match<String> { it.contains("DELETE") }, eq(id)) } returns 1
            repository.delete(id)
            verify { jdbcTemplate.update(match { it.contains("DELETE") }, id) }
        }
    }

    private fun mockResultSet(
        id: Long,
        raiderId: Long,
        gearSet: String = "equipped",
        slot: String = "head",
        itemId: Long? = 12345L,
        itemLevel: Int? = 450,
        quality: Int? = 4,
        enchant: String? = "Enchant",
        enchantQuality: Int? = 3,
        upgradeLevel: Int? = 2,
        sockets: Int? = 1,
        name: String? = "Helm",
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("raider_id") } returns raiderId
        every { rs.getString("gear_set") } returns gearSet
        every { rs.getString("slot") } returns slot
        every { rs.getLong("item_id") } returns (itemId ?: 0L)
        every { rs.getInt("item_level") } returns (itemLevel ?: 0)
        every { rs.getInt("quality") } returns (quality ?: 0)
        every { rs.getString("enchant") } returns enchant
        every { rs.getInt("enchant_quality") } returns (enchantQuality ?: 0)
        every { rs.getInt("upgrade_level") } returns (upgradeLevel ?: 0)
        every { rs.getInt("sockets") } returns (sockets ?: 0)
        every { rs.getString("name") } returns name
        var wasNullCount = 0
        every { rs.wasNull() } answers {
            val isNull =
                when (wasNullCount) {
                    0 -> itemId == null
                    1 -> itemLevel == null
                    2 -> quality == null
                    3 -> enchantQuality == null
                    4 -> upgradeLevel == null
                    5 -> sockets == null
                    else -> false
                }
            wasNullCount++
            isNull
        }
        return rs
    }

    private fun createEntity(
        id: Long? = 1L,
        raiderId: Long = 100L,
        gearSet: String = "equipped",
        slot: String = "head",
        itemId: Long? = 12345L,
        itemLevel: Int? = 450,
        quality: Int? = 4,
        enchant: String? = "Enchant",
        enchantQuality: Int? = 3,
        upgradeLevel: Int? = 2,
        sockets: Int? = 1,
        name: String? = "Helm",
    ) = RaiderGearItemEntity(id, raiderId, gearSet, slot, itemId, itemLevel, quality, enchant, enchantQuality, upgradeLevel, sockets, name)
}
