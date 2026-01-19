package com.edgerush.lootman.infrastructure.loot

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.loot.model.LootBan
import com.edgerush.lootman.domain.loot.model.LootBanId
import com.edgerush.lootman.domain.shared.GuildId
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
import java.time.temporal.ChronoUnit

/**
 * Unit tests for JdbcLootBanRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the loot_bans table.
 */
class JdbcLootBanRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcLootBanRepository

    private val now = Instant.now()
    private val oneWeekFromNow = now.plus(7, ChronoUnit.DAYS)
    private val oneWeekAgo = now.minus(7, ChronoUnit.DAYS)

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcLootBanRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return loot ban when found`() {
            // Given
            val banId = LootBanId("test-ban-id")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootBan>>(),
                    eq(banId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootBan>>()
                listOf(rowMapper.mapRow(mockResultSet(banId.value), 0))
            }

            // When
            val result = repository.findById(banId)

            // Then
            result shouldNotBe null
            result?.id shouldBe banId
            result?.reason shouldBe "Test ban reason"
        }

        @Test
        fun `should return null when loot ban not found`() {
            // Given
            val banId = LootBanId("non-existent")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootBan>>(),
                    eq(banId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findById(banId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val banId = LootBanId("full-ban")
            val bannedAt = Instant.parse("2024-06-01T12:00:00Z")
            val expiresAt = Instant.parse("2024-07-01T12:00:00Z")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootBan>>(),
                    eq(banId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootBan>>()
                val rs =
                    mockResultSet(
                        id = banId.value,
                        raiderId = 100L,
                        guildId = "test-guild",
                        reason = "Excessive loot hoarding",
                        bannedAt = bannedAt,
                        expiresAt = expiresAt,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(banId)

            // Then
            result shouldNotBe null
            result?.id?.value shouldBe "full-ban"
            result?.raiderId?.value shouldBe 100L
            result?.guildId?.value shouldBe "test-guild"
            result?.reason shouldBe "Excessive loot hoarding"
            result?.bannedAt shouldBe bannedAt
            result?.expiresAt shouldBe expiresAt
        }

        @Test
        fun `should handle null expiresAt for permanent bans`() {
            // Given
            val banId = LootBanId("permanent-ban")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<LootBan>>(),
                    eq(banId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootBan>>()
                val rs =
                    mockResultSet(
                        id = banId.value,
                        expiresAt = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(banId)

            // Then
            result shouldNotBe null
            result?.expiresAt shouldBe null
            result?.isActive() shouldBe true // Permanent bans are always active
        }
    }

    @Nested
    inner class FindActiveByRaiderIdTests {
        @Test
        fun `should return active bans for raider`() {
            // Given
            val raiderId = RaiderId(100L)
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("raider_id = ?") &&
                            it.contains("guild_id = ?") &&
                            it.contains("is_active = true")
                    },
                    any<RowMapper<LootBan>>(),
                    eq(raiderId.value.toString()),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<LootBan>>()
                listOf(
                    rowMapper.mapRow(mockResultSet("ban-1", raiderId = raiderId.value, guildId = guildId.value), 0),
                    rowMapper.mapRow(mockResultSet("ban-2", raiderId = raiderId.value, guildId = guildId.value), 1),
                )
            }

            // When
            val result = repository.findActiveByRaiderId(raiderId, guildId)

            // Then
            result.size shouldBe 2
            result.all { it.raiderId == raiderId } shouldBe true
            result.all { it.guildId == guildId } shouldBe true
        }

        @Test
        fun `should return empty list when raider has no active bans`() {
            // Given
            val raiderId = RaiderId(999L)
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.query(
                    match<String> {
                        it.contains("SELECT") &&
                            it.contains("raider_id = ?") &&
                            it.contains("guild_id = ?")
                    },
                    any<RowMapper<LootBan>>(),
                    eq(raiderId.value.toString()),
                    eq(guildId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findActiveByRaiderId(raiderId, guildId)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new loot ban when not exists`() {
            // Given
            val ban = createLootBan()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, ban.id.value) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(ban)

            // Then
            result shouldBe ban
            sqlSlot.captured.contains("INSERT INTO") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should update existing loot ban when exists`() {
            // Given
            val ban = createLootBan()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, ban.id.value) } returns 1
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(ban)

            // Then
            result shouldBe ban
            sqlSlot.captured.contains("UPDATE") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should handle null count from existsById query`() {
            // Given - covers the elvis branch when queryForObject returns null
            val ban = createLootBan()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, ban.id.value) } returns null
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(ban)

            // Then - null count defaults to 0, so INSERT is called
            result shouldBe ban
            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg(),
                )
            }
        }

        @Test
        fun `should insert loot ban with null expiresAt for permanent ban`() {
            // Given - covers the null case for expiresAt in insertLootBan
            val permanentBan = createLootBan(expiresAt = null)

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, permanentBan.id.value) } returns 0
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(permanentBan)

            // Then
            result shouldBe permanentBan
            result.expiresAt shouldBe null

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    permanentBan.id.value,
                    permanentBan.raiderId.value.toString(),
                    permanentBan.guildId.value,
                    permanentBan.reason,
                    any<Timestamp>(),
                    null, // expiresAt should be null
                    true,
                )
            }
        }

        @Test
        fun `should update loot ban with null expiresAt for permanent ban`() {
            // Given - covers the null case for expiresAt in updateLootBan
            val permanentBan = createLootBan(expiresAt = null)

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, permanentBan.id.value) } returns 1
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(permanentBan)

            // Then
            result shouldBe permanentBan
            result.expiresAt shouldBe null

            verify {
                jdbcTemplate.update(
                    match { it.contains("UPDATE") },
                    permanentBan.raiderId.value.toString(),
                    permanentBan.guildId.value,
                    permanentBan.reason,
                    any<Timestamp>(),
                    null, // expiresAt should be null
                    true,
                    permanentBan.id.value,
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete loot ban by id`() {
            // Given
            val banId = LootBanId("to-delete")

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(banId.value),
                )
            } returns 1

            // When
            repository.delete(banId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    banId.value,
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: String,
        raiderId: Long = 100L,
        guildId: String = "test-guild",
        reason: String = "Test ban reason",
        bannedAt: Instant = now,
        expiresAt: Instant? = oneWeekFromNow,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getString("id") } returns id
        every { rs.getString("raider_id") } returns raiderId.toString()
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("reason") } returns reason
        every { rs.getTimestamp("banned_at") } returns Timestamp.from(bannedAt)
        every { rs.getTimestamp("expires_at") } returns expiresAt?.let { Timestamp.from(it) }
        every { rs.getBoolean("is_active") } returns true
        return rs
    }

    private fun createLootBan(
        id: LootBanId = LootBanId("test-ban"),
        raiderId: RaiderId = RaiderId(100L),
        guildId: GuildId = GuildId("test-guild"),
        reason: String = "Test ban reason",
        bannedAt: Instant = now,
        expiresAt: Instant? = oneWeekFromNow,
    ): LootBan =
        LootBan(
            id = id,
            raiderId = raiderId,
            guildId = guildId,
            reason = reason,
            bannedAt = bannedAt,
            expiresAt = expiresAt,
        )
}
