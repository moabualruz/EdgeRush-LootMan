package com.edgerush.lootman.infrastructure.statistics

import com.edgerush.datasync.entity.RaiderStatisticsEntity
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
 * Unit tests for JdbcRaiderStatisticsRepository.
 */
class JdbcRaiderStatisticsRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaiderStatisticsRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaiderStatisticsRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return statistics when found`() {
            val id = 1L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("id = ?")
                    },
                    any<RowMapper<RaiderStatisticsEntity>>(), eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderStatisticsEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, 100L), 0))
            }
            val result = repository.findById(id)
            result shouldNotBe null
            result?.id shouldBe id
        }

        @Test
        fun `should return null when statistics not found`() {
            val id = 999L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("id = ?")
                    },
                    any<RowMapper<RaiderStatisticsEntity>>(), eq(id),
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
                    any<RowMapper<RaiderStatisticsEntity>>(), eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderStatisticsEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, 100L, mythicPlusScore = 2500.5, weeklyHighestMplus = 20, seasonHighestMplus = 22, worldQuestsTotal = 1000, worldQuestsThisWeek = 50, collectiblesMounts = 300, collectiblesToys = 150, collectiblesUniquePets = 200, collectiblesLevel25Pets = 50, honorLevel = 100), 0))
            }
            val result = repository.findById(id)
            result shouldNotBe null
            result?.mythicPlusScore shouldBe 2500.5
            result?.weeklyHighestMplus shouldBe 20
            result?.seasonHighestMplus shouldBe 22
            result?.worldQuestsTotal shouldBe 1000
            result?.worldQuestsThisWeek shouldBe 50
            result?.collectiblesMounts shouldBe 300
            result?.collectiblesToys shouldBe 150
            result?.collectiblesUniquePets shouldBe 200
            result?.collectiblesLevel25Pets shouldBe 50
            result?.honorLevel shouldBe 100
        }

        @Test
        fun `should handle null optional fields`() {
            val id = 1L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") && it.contains("id = ?")
                    },
                    any<RowMapper<RaiderStatisticsEntity>>(), eq(id),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderStatisticsEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(id, 100L, mythicPlusScore = null, weeklyHighestMplus = null, seasonHighestMplus = null, worldQuestsTotal = null, worldQuestsThisWeek = null, collectiblesMounts = null, collectiblesToys = null, collectiblesUniquePets = null, collectiblesLevel25Pets = null, honorLevel = null), 0))
            }
            val result = repository.findById(id)
            result shouldNotBe null
            result?.mythicPlusScore shouldBe null
            result?.weeklyHighestMplus shouldBe null
            result?.honorLevel shouldBe null
        }
    }

    @Nested
    inner class FindByRaiderIdTests {
        @Test
        fun `should return statistics for raider`() {
            val raiderId = 100L
            every {
                jdbcTemplate.query(match<String> { it.contains("raider_id = ?") }, any<RowMapper<RaiderStatisticsEntity>>(), eq(raiderId))
            } answers {
                val rowMapper = secondArg<RowMapper<RaiderStatisticsEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(1L, raiderId), 0))
            }
            val result = repository.findByRaiderId(raiderId)
            result shouldNotBe null
            result?.raiderId shouldBe raiderId
        }

        @Test
        fun `should return null when raider has no statistics`() {
            val raiderId = 999L
            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("raider_id = ?")
                    },
                    any<RowMapper<RaiderStatisticsEntity>>(), eq(raiderId),
                )
            } returns emptyList()
            repository.findByRaiderId(raiderId) shouldBe null
        }
    }

    @Nested
    inner class ExistsByRaiderIdTests {
        @Test
        fun `should return true when statistics exist for raider`() {
            val raiderId = 100L
            every {
                jdbcTemplate.queryForObject(
                    match<String> {
                        it.contains("COUNT(*)") && it.contains("raider_id = ?")
                    },
                    Int::class.java, eq(raiderId),
                )
            } returns 1
            repository.existsByRaiderId(raiderId) shouldBe true
        }

        @Test
        fun `should return false when statistics do not exist for raider`() {
            val raiderId = 999L
            every {
                jdbcTemplate.queryForObject(
                    match<String> {
                        it.contains("COUNT(*)") && it.contains("raider_id = ?")
                    },
                    Int::class.java, eq(raiderId),
                )
            } returns 0
            repository.existsByRaiderId(raiderId) shouldBe false
        }
    }

    @Nested
    inner class CountTests {
        @Test
        fun `should return total count`() {
            every {
                jdbcTemplate.queryForObject(match<String> { it.contains("COUNT(*)") && it.contains("raider_statistics") }, Long::class.java)
            } returns 42L
            repository.count() shouldBe 42L
        }

        @Test
        fun `should handle null count result`() {
            every { jdbcTemplate.queryForObject(match<String> { it.contains("COUNT(*)") }, Long::class.java) } returns null
            repository.count() shouldBe 0L
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new statistics when id is null`() {
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
        fun `should update existing statistics when id is not null`() {
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
        fun `should delete statistics by id`() {
            val id = 1L
            every { jdbcTemplate.update(match<String> { it.contains("DELETE") }, eq(id)) } returns 1
            repository.delete(id)
            verify { jdbcTemplate.update(match { it.contains("DELETE") }, id) }
        }
    }

    private fun mockResultSet(
        id: Long,
        raiderId: Long,
        mythicPlusScore: Double? = 2500.0,
        weeklyHighestMplus: Int? = 20,
        seasonHighestMplus: Int? = 22,
        worldQuestsTotal: Int? = 1000,
        worldQuestsThisWeek: Int? = 50,
        collectiblesMounts: Int? = 300,
        collectiblesToys: Int? = 150,
        collectiblesUniquePets: Int? = 200,
        collectiblesLevel25Pets: Int? = 50,
        honorLevel: Int? = 100,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("raider_id") } returns raiderId
        every { rs.getDouble("mythic_plus_score") } returns (mythicPlusScore ?: 0.0)
        every { rs.getInt("weekly_highest_mplus") } returns (weeklyHighestMplus ?: 0)
        every { rs.getInt("season_highest_mplus") } returns (seasonHighestMplus ?: 0)
        every { rs.getInt("world_quests_total") } returns (worldQuestsTotal ?: 0)
        every { rs.getInt("world_quests_this_week") } returns (worldQuestsThisWeek ?: 0)
        every { rs.getInt("collectibles_mounts") } returns (collectiblesMounts ?: 0)
        every { rs.getInt("collectibles_toys") } returns (collectiblesToys ?: 0)
        every { rs.getInt("collectibles_unique_pets") } returns (collectiblesUniquePets ?: 0)
        every { rs.getInt("collectibles_level_25_pets") } returns (collectiblesLevel25Pets ?: 0)
        every { rs.getInt("honor_level") } returns (honorLevel ?: 0)
        var wasNullCount = 0
        every { rs.wasNull() } answers {
            val isNull =
                when (wasNullCount) {
                    0 -> mythicPlusScore == null
                    1 -> weeklyHighestMplus == null
                    2 -> seasonHighestMplus == null
                    3 -> worldQuestsTotal == null
                    4 -> worldQuestsThisWeek == null
                    5 -> collectiblesMounts == null
                    6 -> collectiblesToys == null
                    7 -> collectiblesUniquePets == null
                    8 -> collectiblesLevel25Pets == null
                    9 -> honorLevel == null
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
        mythicPlusScore: Double? = 2500.0,
        weeklyHighestMplus: Int? = 20,
        seasonHighestMplus: Int? = 22,
        worldQuestsTotal: Int? = 1000,
        worldQuestsThisWeek: Int? = 50,
        collectiblesMounts: Int? = 300,
        collectiblesToys: Int? = 150,
        collectiblesUniquePets: Int? = 200,
        collectiblesLevel25Pets: Int? = 50,
        honorLevel: Int? = 100,
    ) =
        RaiderStatisticsEntity(id, raiderId, mythicPlusScore, weeklyHighestMplus, seasonHighestMplus, worldQuestsTotal, worldQuestsThisWeek, collectiblesMounts, collectiblesToys, collectiblesUniquePets, collectiblesLevel25Pets, honorLevel)
}
