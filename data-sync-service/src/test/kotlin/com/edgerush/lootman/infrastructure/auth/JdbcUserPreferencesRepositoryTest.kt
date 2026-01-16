package com.edgerush.lootman.infrastructure.auth

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.auth.model.UserCharacterMappingId
import com.edgerush.lootman.domain.auth.model.UserId
import com.edgerush.lootman.domain.auth.model.UserPreferences
import com.edgerush.lootman.domain.auth.model.UserPreferencesId
import com.edgerush.lootman.domain.shared.GuildId
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
import org.springframework.jdbc.support.GeneratedKeyHolder
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

/**
 * Unit tests for JdbcUserPreferencesRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 */
class JdbcUserPreferencesRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcUserPreferencesRepository

    @BeforeEach
    fun setup() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcUserPreferencesRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByUserIdTests {
        @Test
        fun `should return preferences when found`() {
            // Given
            val userId = UserId(1L)
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("WHERE user_id = ?") },
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<UserPreferences>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = 1L,
                            userId = 1L,
                            mappingId = 42L,
                            guildId = "test-guild",
                            updatedAt = now,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findByUserId(userId)

            // Then
            result shouldNotBe null
            result?.userId shouldBe userId
            result?.activeCharacterMappingId shouldBe UserCharacterMappingId(42L)
            result?.lastGuildId shouldBe GuildId("test-guild")
        }

        @Test
        fun `should return null when not found`() {
            // Given
            val userId = UserId(999L)

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } returns emptyList()

            // When
            val result = repository.findByUserId(userId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should handle preferences with no active character`() {
            // Given
            val userId = UserId(1L)
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<UserPreferences>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSetWithNulls(
                            id = 1L,
                            userId = 1L,
                            updatedAt = now,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findByUserId(userId)

            // Then
            result shouldNotBe null
            result?.activeCharacterMappingId shouldBe null
            result?.lastGuildId shouldBe null
        }
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return preferences when found by id`() {
            // Given
            val id = UserPreferencesId(1L)
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("WHERE id = ?") },
                    any<RowMapper<UserPreferences>>(),
                    eq(id.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<UserPreferences>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            id = 1L,
                            userId = 1L,
                            mappingId = 42L,
                            guildId = "test-guild",
                            updatedAt = now,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(id)

            // Then
            result shouldNotBe null
            result?.id shouldBe id
        }

        @Test
        fun `should return null when id not found`() {
            // Given
            val id = UserPreferencesId(999L)

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("WHERE id = ?") },
                    any<RowMapper<UserPreferences>>(),
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
        fun `should insert new preferences`() {
            // Given
            val preferences = UserPreferences.create(
                userId = UserId(1L),
                activeCharacterMappingId = UserCharacterMappingId(42L),
                guildId = GuildId("test-guild"),
            )

            every {
                jdbcTemplate.update(any<org.springframework.jdbc.core.PreparedStatementCreator>(), any<GeneratedKeyHolder>())
            } answers {
                val keyHolder = secondArg<GeneratedKeyHolder>()
                // Simulate key generation
                1
            }

            // When
            val result = repository.save(preferences)

            // Then
            verify {
                jdbcTemplate.update(any<org.springframework.jdbc.core.PreparedStatementCreator>(), any<GeneratedKeyHolder>())
            }
        }

        @Test
        fun `should update existing preferences`() {
            // Given
            val preferences = UserPreferences(
                id = UserPreferencesId(1L),
                userId = UserId(1L),
                activeCharacterMappingId = UserCharacterMappingId(42L),
                lastGuildId = GuildId("test-guild"),
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
            val result = repository.save(preferences)

            // Then
            result shouldBe preferences
            verify {
                jdbcTemplate.update(
                    match<String> { it.contains("UPDATE") },
                    preferences.activeCharacterMappingId?.value,
                    preferences.lastGuildId?.value,
                    any<Timestamp>(),
                    preferences.id?.value,
                )
            }
        }

        @Test
        fun `should update with null active character`() {
            // Given
            val preferences = UserPreferences(
                id = UserPreferencesId(1L),
                userId = UserId(1L),
                activeCharacterMappingId = null,
                lastGuildId = null,
            )

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("UPDATE") },
                    isNull(),
                    isNull(),
                    any<Timestamp>(),
                    eq(1L),
                )
            } returns 1

            // When
            val result = repository.save(preferences)

            // Then
            result shouldBe preferences
        }
    }

    @Nested
    inner class UpdateActiveCharacterTests {
        @Test
        fun `should update existing preferences when user has preferences`() {
            // Given
            val userId = UserId(1L)
            val newMappingId = UserCharacterMappingId(100L)
            val newGuildId = GuildId("new-guild")
            val now = Instant.now()

            // Mock findByUserId to return existing preferences
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("WHERE user_id = ?") },
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<UserPreferences>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(1L, 1L, 42L, "old-guild", now),
                        0,
                    ),
                )
            }

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
            val result = repository.updateActiveCharacter(userId, newMappingId, newGuildId)

            // Then
            result.activeCharacterMappingId shouldBe newMappingId
            result.lastGuildId shouldBe newGuildId
        }

        @Test
        fun `should create new preferences when user has no preferences`() {
            // Given
            val userId = UserId(1L)
            val mappingId = UserCharacterMappingId(42L)
            val guildId = GuildId("test-guild")

            // Mock findByUserId to return null (no existing preferences)
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("WHERE user_id = ?") },
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } returns emptyList()

            every {
                jdbcTemplate.update(any<org.springframework.jdbc.core.PreparedStatementCreator>(), any<GeneratedKeyHolder>())
            } returns 1

            // When
            val result = repository.updateActiveCharacter(userId, mappingId, guildId)

            // Then
            result.userId shouldBe userId
            result.activeCharacterMappingId shouldBe mappingId
            result.lastGuildId shouldBe guildId
        }

        @Test
        fun `should clear active character when mappingId is null`() {
            // Given
            val userId = UserId(1L)
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("WHERE user_id = ?") },
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<UserPreferences>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(1L, 1L, 42L, "old-guild", now),
                        0,
                    ),
                )
            }

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
            val result = repository.updateActiveCharacter(userId, null, null)

            // Then
            result.activeCharacterMappingId shouldBe null
            result.lastGuildId shouldBe null
        }
    }

    @Nested
    inner class DeleteByUserIdTests {
        @Test
        fun `should delete preferences by user id`() {
            // Given
            val userId = UserId(1L)

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") && it.contains("WHERE user_id = ?") },
                    userId.value,
                )
            } returns 1

            // When
            repository.deleteByUserId(userId)

            // Then
            verify {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    userId.value,
                )
            }
        }

        @Test
        fun `should not fail when deleting non-existent preferences`() {
            // Given
            val userId = UserId(999L)

            every {
                jdbcTemplate.update(
                    any<String>(),
                    userId.value,
                )
            } returns 0

            // When
            repository.deleteByUserId(userId)

            // Then
            verify {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    userId.value,
                )
            }
        }
    }

    @Nested
    inner class RowMapperTests {
        @Test
        fun `should map all database fields correctly`() {
            // Given
            val userId = UserId(1L)
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<UserPreferences>>()
                val rs = mockResultSet(
                    id = 42L,
                    userId = 1L,
                    mappingId = 100L,
                    guildId = "my-guild",
                    updatedAt = now,
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByUserId(userId)

            // Then
            result shouldNotBe null
            result?.id?.value shouldBe 42L
            result?.userId?.value shouldBe 1L
            result?.activeCharacterMappingId?.value shouldBe 100L
            result?.lastGuildId?.value shouldBe "my-guild"
            result?.updatedAt shouldBe now
        }

        @Test
        fun `should handle null timestamp with fallback to now`() {
            // Given
            val userId = UserId(1L)

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<UserPreferences>>()
                val rs = mockResultSetWithNullTimestamp(1L, 1L, 42L, "test-guild")
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByUserId(userId)

            // Then
            result shouldNotBe null
            result?.updatedAt shouldNotBe null
        }

        @Test
        fun `should handle null mapping id correctly`() {
            // Given
            val userId = UserId(1L)
            val now = Instant.now()

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<UserPreferences>>(),
                    eq(userId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<UserPreferences>>()
                val rs = mockResultSetWithNulls(1L, 1L, now)
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findByUserId(userId)

            // Then
            result shouldNotBe null
            result?.activeCharacterMappingId shouldBe null
        }
    }

    // Helper methods
    private fun mockResultSet(
        id: Long,
        userId: Long,
        mappingId: Long,
        guildId: String,
        updatedAt: Instant,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("user_id") } returns userId
        every { rs.getLong("active_character_mapping_id") } returns mappingId
        every { rs.getString("last_guild_id") } returns guildId
        every { rs.getTimestamp("updated_at") } returns Timestamp.from(updatedAt)
        every { rs.wasNull() } returns false
        return rs
    }

    private fun mockResultSetWithNulls(
        id: Long,
        userId: Long,
        updatedAt: Instant,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("user_id") } returns userId
        every { rs.getLong("active_character_mapping_id") } returns 0L
        every { rs.getString("last_guild_id") } returns null
        every { rs.getTimestamp("updated_at") } returns Timestamp.from(updatedAt)
        every { rs.wasNull() } returns true
        return rs
    }

    private fun mockResultSetWithNullTimestamp(
        id: Long,
        userId: Long,
        mappingId: Long,
        guildId: String,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("user_id") } returns userId
        every { rs.getLong("active_character_mapping_id") } returns mappingId
        every { rs.getString("last_guild_id") } returns guildId
        every { rs.getTimestamp("updated_at") } returns null
        every { rs.wasNull() } returns false
        return rs
    }
}
