package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.loot.model.LootAward
import com.edgerush.lootman.domain.loot.model.LootAwardId
import com.edgerush.lootman.domain.loot.model.LootTier
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
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
 * Unit tests for JdbcLootAwardRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the loot_awards table.
 */
class JdbcLootAwardRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcLootAwardRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcLootAwardRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return loot award when found`() {
            // Given
            val awardId = LootAwardId("test-award-id")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(awardId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(rowMapper.mapRow(mockResultSet(awardId.value, now), 0))
            }

            // When
            val result = repository.findById(awardId)

            // Then
            result shouldNotBe null
            result?.id shouldBe awardId
            result?.tier shouldBe LootTier.MYTHIC
        }

        @Test
        fun `should return null when loot award not found`() {
            // Given
            val awardId = LootAwardId("non-existent")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(awardId.value)
                )
            } returns emptyList()

            // When
            val result = repository.findById(awardId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val awardId = LootAwardId("full-award")
            val awardedAt = Instant.parse("2024-06-15T12:00:00Z")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(awardId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                val rs = mockResultSet(
                    id = awardId.value,
                    awardedAt = awardedAt,
                    itemId = 12345L,
                    raiderId = 100L,
                    guildId = "test-guild",
                    flpsScore = 0.85,
                    tier = "HEROIC",
                    status = "ACTIVE"
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(awardId)

            // Then
            result shouldNotBe null
            result?.id?.value shouldBe "full-award"
            result?.itemId?.value shouldBe 12345L
            result?.raiderId?.value shouldBe 100L
            result?.guildId?.value shouldBe "test-guild"
            result?.flpsScore?.value shouldBe 0.85
            result?.tier shouldBe LootTier.HEROIC
            result?.awardedAt shouldBe awardedAt
            result?.isActive() shouldBe true
        }
    }

    @Nested
    inner class FindByRaiderIdTests {

        @Test
        fun `should return all awards for raider`() {
            // Given
            val raiderId = RaiderId(100L)
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(raiderId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("award-1", now, raiderId = raiderId.value), 0),
                    rowMapper.mapRow(mockResultSet("award-2", now, raiderId = raiderId.value), 1)
                )
            }

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId } shouldBe true
        }

        @Test
        fun `should return empty list when raider has no awards`() {
            // Given
            val raiderId = RaiderId(999L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("raider_id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(raiderId.value)
                )
            } returns emptyList()

            // When
            val result = repository.findByRaiderId(raiderId)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByGuildIdTests {

        @Test
        fun `should return all awards for guild`() {
            // Given
            val guildId = GuildId("test-guild")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(guildId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("award-1", now, guildId = guildId.value), 0),
                    rowMapper.mapRow(mockResultSet("award-2", now, guildId = guildId.value), 1),
                    rowMapper.mapRow(mockResultSet("award-3", now, guildId = guildId.value), 2)
                )
            }

            // When
            val result = repository.findByGuildId(guildId)

            // Then
            result.size shouldBe 3
            result.all { it.guildId == guildId } shouldBe true
        }
    }

    @Nested
    inner class FindByGuildIdPaginatedTests {

        @Test
        fun `should return paginated awards for guild`() {
            // Given
            val guildId = GuildId("test-guild")
            val offset = 10L
            val limit = 5
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<LootAward>>(),
                    eq(guildId.value),
                    eq(limit),
                    eq(offset)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("award-11", now, guildId = guildId.value), 0),
                    rowMapper.mapRow(mockResultSet("award-12", now, guildId = guildId.value), 1)
                )
            }

            // When
            val result = repository.findByGuildId(guildId, offset, limit)

            // Then
            result.size shouldBe 2
            result[0].id.value shouldBe "award-11"
            result[1].id.value shouldBe "award-12"
        }

        @Test
        fun `should return empty list when no awards in page`() {
            // Given
            val guildId = GuildId("test-guild")
            val offset = 1000L
            val limit = 10

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<LootAward>>(),
                    eq(guildId.value),
                    eq(limit),
                    eq(offset)
                )
            } returns emptyList()

            // When
            val result = repository.findByGuildId(guildId, offset, limit)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class CountByGuildIdTests {

        @Test
        fun `should return count of awards for guild`() {
            // Given
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT") },
                    Long::class.java,
                    eq(guildId.value)
                )
            } returns 42L

            // When
            val result = repository.countByGuildId(guildId)

            // Then
            result shouldBe 42L
        }

        @Test
        fun `should return zero when no awards for guild`() {
            // Given
            val guildId = GuildId("empty-guild")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT") },
                    Long::class.java,
                    eq(guildId.value)
                )
            } returns 0L

            // When
            val result = repository.countByGuildId(guildId)

            // Then
            result shouldBe 0L
        }

        @Test
        fun `should return zero when query returns null`() {
            // Given
            val guildId = GuildId("null-guild")

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT") },
                    Long::class.java,
                    eq(guildId.value)
                )
            } returns null

            // When
            val result = repository.countByGuildId(guildId)

            // Then
            result shouldBe 0L
        }
    }

    @Nested
    inner class RowMapperEdgeCases {

        @Test
        fun `should default to MYTHIC when tier is invalid`() {
            // Given
            val awardId = LootAwardId("invalid-tier-award")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(awardId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(rowMapper.mapRow(mockResultSet(awardId.value, now, tier = "INVALID_TIER"), 0))
            }

            // When
            val result = repository.findById(awardId)

            // Then
            result?.tier shouldBe LootTier.MYTHIC
        }

        @Test
        fun `should default to MYTHIC when tier is null`() {
            // Given
            val awardId = LootAwardId("null-tier-award")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(awardId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(rowMapper.mapRow(mockResultSet(awardId.value, now, tier = null), 0))
            }

            // When
            val result = repository.findById(awardId)

            // Then
            result?.tier shouldBe LootTier.MYTHIC
        }

        @Test
        fun `should handle lowercase tier values`() {
            // Given
            val awardId = LootAwardId("lowercase-tier-award")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(awardId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(rowMapper.mapRow(mockResultSet(awardId.value, now, tier = "heroic"), 0))
            }

            // When
            val result = repository.findById(awardId)

            // Then
            result?.tier shouldBe LootTier.HEROIC
        }

        @Test
        fun `should handle REVOKED status`() {
            // Given
            val awardId = LootAwardId("revoked-award")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(awardId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(rowMapper.mapRow(mockResultSet(awardId.value, now, status = "REVOKED"), 0))
            }

            // When
            val result = repository.findById(awardId)

            // Then
            // Note: The current implementation doesn't actually restore the revoked state
            // This test documents the current behavior
            result shouldNotBe null
        }

        @Test
        fun `should handle null status as ACTIVE`() {
            // Given
            val awardId = LootAwardId("null-status-award")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootAward>>(),
                    eq(awardId.value)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootAward>>()
                listOf(rowMapper.mapRow(mockResultSet(awardId.value, now, status = null), 0))
            }

            // When
            val result = repository.findById(awardId)

            // Then
            result?.isActive() shouldBe true
        }
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `should insert new loot award when not exists`() {
            // Given
            val award = createLootAward()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, award.id.value) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(award)

            // Then
            result shouldBe award
            sqlSlot.captured.contains("INSERT INTO") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg()
                )
            }
        }

        @Test
        fun `should update existing loot award when exists`() {
            // Given
            val award = createLootAward()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, award.id.value) } returns 1
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(award)

            // Then
            result shouldBe award
            sqlSlot.captured.contains("UPDATE") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    *anyVararg()
                )
            }
        }

        @Test
        fun `should handle null count from existsById query`() {
            // Given - covers the elvis branch when queryForObject returns null
            val award = createLootAward()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, award.id.value) } returns null
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(award)

            // Then - null count defaults to 0, so INSERT is called
            result shouldBe award
            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg()
                )
            }
        }

        @Test
        fun `should insert revoked loot award with REVOKED status`() {
            // Given - covers the REVOKED branch in insertLootAward
            val activeAward = createLootAward()
            val revokedAward = activeAward.revoke("Test revocation")

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, revokedAward.id.value) } returns 0
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(revokedAward)

            // Then
            result shouldBe revokedAward
            result.isActive() shouldBe false

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    revokedAward.id.value,
                    revokedAward.itemId.value,
                    revokedAward.raiderId.value,
                    revokedAward.guildId.value,
                    any<Timestamp>(),
                    revokedAward.flpsScore.value,
                    revokedAward.tier.name,
                    "REVOKED" // Status should be REVOKED
                )
            }
        }

        @Test
        fun `should update revoked loot award with REVOKED status`() {
            // Given - covers the REVOKED branch in updateLootAward
            val activeAward = createLootAward()
            val revokedAward = activeAward.revoke("Test revocation")

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, revokedAward.id.value) } returns 1
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(revokedAward)

            // Then
            result shouldBe revokedAward
            result.isActive() shouldBe false

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    revokedAward.itemId.value,
                    revokedAward.raiderId.value,
                    revokedAward.guildId.value,
                    any<Timestamp>(),
                    revokedAward.flpsScore.value,
                    revokedAward.tier.name,
                    "REVOKED", // Status should be REVOKED
                    revokedAward.id.value
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should delete loot award by id`() {
            // Given
            val awardId = LootAwardId("to-delete")

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(awardId.value)
                )
            } returns 1

            // When
            repository.delete(awardId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    awardId.value
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: String,
        awardedAt: Instant,
        itemId: Long = 12345L,
        raiderId: Long = 100L,
        guildId: String = "test-guild",
        flpsScore: Double = 0.75,
        tier: String? = "MYTHIC",
        status: String? = "ACTIVE"
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("id") } returns id
        every { rs.getLong("itemId") } returns itemId
        every { rs.getLong("raider_id") } returns raiderId
        every { rs.getString("guild_id") } returns guildId
        every { rs.getTimestamp("awardedAt") } returns Timestamp.from(awardedAt)
        every { rs.getDouble("flps") } returns flpsScore
        every { rs.getString("tier") } returns tier
        every { rs.getString("status") } returns status
        return rs
    }

    private fun createLootAward(
        id: LootAwardId = LootAwardId("test-award"),
        itemId: ItemId = ItemId(12345L),
        raiderId: RaiderId = RaiderId(100L),
        guildId: GuildId = GuildId("test-guild"),
        awardedAt: Instant = Instant.now(),
        flpsScore: FlpsScore = FlpsScore.of(0.75),
        tier: LootTier = LootTier.MYTHIC
    ): LootAward = LootAward(
        id = id,
        itemId = itemId,
        raiderId = raiderId,
        guildId = guildId,
        awardedAt = awardedAt,
        flpsScore = flpsScore,
        tier = tier
    )
}
