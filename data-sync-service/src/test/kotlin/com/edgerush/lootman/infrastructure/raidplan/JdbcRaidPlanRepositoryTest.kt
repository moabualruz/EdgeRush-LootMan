package com.edgerush.lootman.infrastructure.raidplan

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.raidplan.model.*
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
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
 * Unit tests for JdbcRaidPlanRepository.
 */
class JdbcRaidPlanRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcRaidPlanRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcRaidPlanRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return null when plan not found`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT * FROM raid_plans WHERE id = ?") },
                    any<RowMapper<RaidPlan>>(),
                    eq("non-existent-id"),
                )
            } returns emptyList()

            // When
            val result = repository.findById("non-existent-id")

            // Then
            result shouldBe null
        }

        @Test
        fun `should return plan when found by id`() {
            // Given
            val planId = "test-plan-id"
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT * FROM raid_plans WHERE id = ?") },
                    any<RowMapper<RaidPlan>>(),
                    eq(planId),
                )
            } answers {
                val mapper = secondArg<RowMapper<RaidPlan>>()
                listOf(mapper.mapRow(mockPlanResultSet(planId, "test-guild", 2902, "Queen Ansurek", "Test Plan", now), 0))
            }

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT * FROM raid_plan_steps") },
                    any<RowMapper<Pair<Long, PlanStep>>>(),
                    eq(planId),
                )
            } returns emptyList()

            // When
            val result = repository.findById(planId)

            // Then
            result shouldNotBe null
            result?.id shouldBe planId
            result?.guildId?.value shouldBe "test-guild"
            result?.encounterName shouldBe "Queen Ansurek"
        }
    }

    @Nested
    inner class FindByGuildIdTests {
        @Test
        fun `should return empty list when no plans for guild`() {
            // Given
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT * FROM raid_plans WHERE guild_id = ?") && it.contains("ORDER BY") },
                    any<RowMapper<RaidPlan>>(),
                    eq(guildId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findByGuildId(guildId)

            // Then
            result shouldHaveSize 0
        }
    }

    @Nested
    inner class CountByGuildIdTests {
        @Test
        fun `should return count of plans for guild`() {
            // Given
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("SELECT COUNT(*)") },
                    Long::class.java,
                    eq(guildId.value),
                )
            } returns 5L

            // When
            val result = repository.countByGuildId(guildId)

            // Then
            result shouldBe 5L
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new plan when it does not exist`() {
            // Given
            val plan =
                RaidPlan.create(
                    guildId = GuildId("test-guild"),
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "Test Plan",
                    createdBy = 1L,
                )

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("SELECT COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(plan.id),
                )
            } returns 0

            // When
            val result = repository.save(plan)

            // Then
            result shouldBe plan
            verify(exactly = 1) {
                jdbcTemplate.update(
                    match<String> { it.contains("INSERT INTO raid_plans") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should update existing plan`() {
            // Given
            val plan =
                RaidPlan.create(
                    guildId = GuildId("test-guild"),
                    encounterId = 2902,
                    encounterName = "Queen Ansurek",
                    name = "Test Plan",
                    createdBy = 1L,
                )

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("SELECT COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(plan.id),
                )
            } returns 1

            // When
            val result = repository.save(plan)

            // Then
            result shouldBe plan
            verify(exactly = 1) {
                jdbcTemplate.update(
                    match<String> { it.contains("UPDATE raid_plans") },
                    *anyVararg(),
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete plan by id`() {
            // Given
            val planId = "test-plan-id"

            // When
            repository.delete(planId)

            // Then
            verify(exactly = 1) {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE FROM raid_plans WHERE id = ?") },
                    eq(planId),
                )
            }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when plan exists`() {
            // Given
            val planId = "test-plan-id"

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("SELECT COUNT(*)") },
                    Int::class.java,
                    eq(planId),
                )
            } returns 1

            // When
            val result = repository.existsById(planId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when plan does not exist`() {
            // Given
            val planId = "non-existent-id"

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("SELECT COUNT(*)") },
                    Int::class.java,
                    eq(planId),
                )
            } returns 0

            // When
            val result = repository.existsById(planId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class FindByShareTokenTests {
        @Test
        fun `should return null when share token not found`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("share_token = ?") },
                    any<RowMapper<RaidPlan>>(),
                    eq("invalid-token"),
                )
            } returns emptyList()

            // When
            val result = repository.findByShareToken("invalid-token")

            // Then
            result shouldBe null
        }
    }

    private fun mockPlanResultSet(
        id: String,
        guildId: String,
        encounterId: Int,
        encounterName: String,
        name: String,
        createdAt: Instant,
    ): ResultSet =
        mockk {
            every { getString("id") } returns id
            every { getString("guild_id") } returns guildId
            every { getInt("encounter_id") } returns encounterId
            every { getString("encounter_name") } returns encounterName
            every { getString("name") } returns name
            every { getString("visibility") } returns "GUILD"
            every { getString("share_token") } returns null
            every { getLong("created_by") } returns 1L
            every { getTimestamp("created_at") } returns Timestamp.from(createdAt)
            every { getTimestamp("updated_at") } returns Timestamp.from(createdAt)
        }
}
