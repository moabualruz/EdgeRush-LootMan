package com.edgerush.lootman.infrastructure.guild

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.guild.model.GuildPermission
import com.edgerush.lootman.domain.guild.model.GuildPermissionId
import com.edgerush.lootman.domain.guild.model.GuildPermissionType
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
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
import java.sql.Timestamp
import java.time.Instant

/**
 * Unit tests for JdbcGuildPermissionRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 */
class JdbcGuildPermissionRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcGuildPermissionRepository

    @BeforeEach
    fun setup() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcGuildPermissionRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByGuildIdTests {
        @Test
        fun `should return permissions for guild`() {
            // Given
            val guildId = GuildId("test-guild")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("guild_id = ?") && it.contains("ORDER BY rank_name") },
                    any<RowMapper<GuildPermission>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuildPermission>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, "test-guild", "Officer", "SETTINGS_ACCESS", now), 0),
                    rowMapper.mapRow(mockResultSet(2L, "test-guild", "Officer", "LOOT_MANAGEMENT", now), 1),
                )
            }

            // When
            val result = repository.findByGuildId(guildId)

            // Then
            result shouldHaveSize 2
            result[0].guildId shouldBe guildId
            result[0].rankName shouldBe "Officer"
            result[0].permissionType shouldBe GuildPermissionType.SETTINGS_ACCESS
        }

        @Test
        fun `should return empty list when guild has no permissions`() {
            // Given
            val guildId = GuildId("empty-guild")

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<GuildPermission>>(),
                    eq(guildId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findByGuildId(guildId)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByGuildIdAndRankNameTests {
        @Test
        fun `should return permissions for guild and rank`() {
            // Given
            val guildId = GuildId("test-guild")
            val rankName = "Officer"
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("guild_id = ?") && it.contains("rank_name = ?") },
                    any<RowMapper<GuildPermission>>(),
                    eq(guildId.value),
                    eq(rankName),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuildPermission>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, "test-guild", "Officer", "SETTINGS_ACCESS", now), 0),
                )
            }

            // When
            val result = repository.findByGuildIdAndRankName(guildId, rankName)

            // Then
            result shouldHaveSize 1
            result[0].rankName shouldBe rankName
        }

        @Test
        fun `should return empty list when rank has no permissions`() {
            // Given
            val guildId = GuildId("test-guild")
            val rankName = "Member"

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<GuildPermission>>(),
                    eq(guildId.value),
                    eq(rankName),
                )
            } returns emptyList()

            // When
            val result = repository.findByGuildIdAndRankName(guildId, rankName)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class HasPermissionTests {
        @Test
        fun `should return true when permission exists`() {
            // Given
            val guildId = GuildId("test-guild")
            val rankName = "Officer"
            val permissionType = GuildPermissionType.SETTINGS_ACCESS

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") },
                    Int::class.java,
                    guildId.value,
                    rankName,
                    permissionType.name,
                )
            } returns 1

            // When
            val result = repository.hasPermission(guildId, rankName, permissionType)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when permission does not exist`() {
            // Given
            val guildId = GuildId("test-guild")
            val rankName = "Member"
            val permissionType = GuildPermissionType.SETTINGS_ACCESS

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") },
                    Int::class.java,
                    guildId.value,
                    rankName,
                    permissionType.name,
                )
            } returns 0

            // When
            val result = repository.hasPermission(guildId, rankName, permissionType)

            // Then
            result shouldBe false
        }

        @Test
        fun `should return false when query returns null`() {
            // Given
            val guildId = GuildId("test-guild")
            val rankName = "Member"
            val permissionType = GuildPermissionType.SETTINGS_ACCESS

            every {
                jdbcTemplate.queryForObject(
                    any<String>(),
                    Int::class.java,
                    any(),
                    any(),
                    any(),
                )
            } returns null

            // When
            val result = repository.hasPermission(guildId, rankName, permissionType)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return permission when found`() {
            // Given
            val id = GuildPermissionId(1L)
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("WHERE id = ?") },
                    any<RowMapper<GuildPermission>>(),
                    eq(id.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuildPermission>>()
                listOf(rowMapper.mapRow(mockResultSet(1L, "test-guild", "Officer", "SETTINGS_ACCESS", now), 0))
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
        }

        @Test
        fun `should return null when not found`() {
            // Given
            val id = GuildPermissionId(999L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("WHERE id = ?") },
                    any<RowMapper<GuildPermission>>(),
                    eq(id.value),
                )
            } returns emptyList()

            // When
            val result = repository.findById(id)

            // Then
            result shouldBe null
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new permission`() {
            // Given
            val permission = GuildPermission.create(
                guildId = GuildId("test-guild"),
                rankName = "Officer",
                permissionType = GuildPermissionType.SETTINGS_ACCESS,
            )

            every {
                jdbcTemplate.update(any<org.springframework.jdbc.core.PreparedStatementCreator>(), any<GeneratedKeyHolder>())
            } answers {
                val keyHolder = secondArg<GeneratedKeyHolder>()
                // Simulate key generation
                1
            }

            // When
            val result = repository.save(permission)

            // Then
            verify {
                jdbcTemplate.update(any<org.springframework.jdbc.core.PreparedStatementCreator>(), any<GeneratedKeyHolder>())
            }
        }

        @Test
        fun `should update existing permission`() {
            // Given
            val permission = GuildPermission(
                id = GuildPermissionId(1L),
                guildId = GuildId("test-guild"),
                rankName = "Officer",
                permissionType = GuildPermissionType.SETTINGS_ACCESS,
            )

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("UPDATE") },
                    any(),
                    any(),
                    any(),
                    any(),
                )
            } returns 1

            // When
            val result = repository.save(permission)

            // Then
            result shouldBe permission
            verify {
                jdbcTemplate.update(
                    match<String> { it.contains("UPDATE") },
                    permission.guildId.value,
                    permission.rankName,
                    permission.permissionType.name,
                    permission.id?.value,
                )
            }
        }
    }

    @Nested
    inner class DeleteByIdTests {
        @Test
        fun `should delete permission by id`() {
            // Given
            val id = GuildPermissionId(1L)

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") && it.contains("WHERE id = ?") },
                    id.value,
                )
            } returns 1

            // When
            repository.deleteById(id)

            // Then
            verify {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    id.value,
                )
            }
        }
    }

    @Nested
    inner class DeleteByGuildIdTests {
        @Test
        fun `should delete all permissions for guild`() {
            // Given
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") && it.contains("WHERE guild_id = ?") },
                    guildId.value,
                )
            } returns 5

            // When
            repository.deleteByGuildId(guildId)

            // Then
            verify {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") && it.contains("guild_id") },
                    guildId.value,
                )
            }
        }
    }

    @Nested
    inner class FindDistinctRankNamesByGuildIdTests {
        @Test
        fun `should return distinct rank names`() {
            // Given
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.queryForList(
                    match<String> { it.contains("DISTINCT rank_name") },
                    String::class.java,
                    guildId.value,
                )
            } returns listOf("Guild Master", "Officer", "Raider")

            // When
            val result = repository.findDistinctRankNamesByGuildId(guildId)

            // Then
            result shouldHaveSize 3
            result shouldContain "Guild Master"
            result shouldContain "Officer"
            result shouldContain "Raider"
        }

        @Test
        fun `should return empty list when no permissions exist`() {
            // Given
            val guildId = GuildId("empty-guild")

            every {
                jdbcTemplate.queryForList(
                    any<String>(),
                    String::class.java,
                    guildId.value,
                )
            } returns emptyList()

            // When
            val result = repository.findDistinctRankNamesByGuildId(guildId)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class RowMapperTests {
        @Test
        fun `should map all database fields correctly`() {
            // Given
            val guildId = GuildId("test-guild")
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<GuildPermission>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuildPermission>>()
                val rs = mockResultSet(
                    id = 42L,
                    guildId = "test-guild",
                    rankName = "Guild Master",
                    permissionType = "LOOT_MANAGEMENT",
                    createdAt = now,
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByGuildId(guildId)

            // Then
            result shouldHaveSize 1
            result[0].id?.value shouldBe 42L
            result[0].guildId.value shouldBe "test-guild"
            result[0].rankName shouldBe "Guild Master"
            result[0].permissionType shouldBe GuildPermissionType.LOOT_MANAGEMENT
            result[0].createdAt shouldBe now
        }

        @Test
        fun `should handle null timestamp with fallback to now`() {
            // Given
            val guildId = GuildId("test-guild")

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<GuildPermission>>(),
                    eq(guildId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<GuildPermission>>()
                val rs = mockResultSetWithNullTimestamp(1L, "test-guild", "Officer", "SETTINGS_ACCESS")
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByGuildId(guildId)

            // Then
            result shouldHaveSize 1
            result[0].createdAt shouldNotBe null
        }
    }

    // Helper methods
    private fun mockResultSet(
        id: Long,
        guildId: String,
        rankName: String,
        permissionType: String,
        createdAt: Instant,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("rank_name") } returns rankName
        every { rs.getString("permission_type") } returns permissionType
        every { rs.getTimestamp("created_at") } returns Timestamp.from(createdAt)
        return rs
    }

    private fun mockResultSetWithNullTimestamp(
        id: Long,
        guildId: String,
        rankName: String,
        permissionType: String,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("rank_name") } returns rankName
        every { rs.getString("permission_type") } returns permissionType
        every { rs.getTimestamp("created_at") } returns null
        return rs
    }
}
