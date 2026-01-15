package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.application.model.Application
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.application.model.ApplicationStatus
import com.edgerush.lootman.domain.shared.GuildId
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
 * Unit tests for JdbcEnhancedApplicationRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository works with the domain Application model for enhanced recruitment.
 */
class JdbcEnhancedApplicationRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcEnhancedApplicationRepository

    private val now = Instant.now()
    private val testGuildId = GuildId("test-guild-123")
    private val testApplicationId = ApplicationId("app-uuid-123")

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcEnhancedApplicationRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return application when found`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("enhanced_application_id = ?") },
                    any<RowMapper<Application>>(),
                    eq(testApplicationId.value),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Application>>()
                listOf(rowMapper.mapRow(mockResultSet(), 0))
            }

            // When
            val result = repository.findById(testApplicationId)

            // Then
            result shouldNotBe null
            result?.id shouldBe testApplicationId
            result?.guildId shouldBe testGuildId
        }

        @Test
        fun `should return null when application not found`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("enhanced_application_id = ?") },
                    any<RowMapper<Application>>(),
                    any<String>(),
                )
            } returns emptyList()

            // When
            val result = repository.findById(ApplicationId("non-existent"))

            // Then
            result shouldBe null
        }
    }

    @Nested
    inner class FindByGuildIdTests {
        @Test
        fun `should return applications for guild`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") && it.contains("LIMIT") },
                    any<RowMapper<Application>>(),
                    eq(testGuildId.value),
                    any<Int>(),
                    any<Long>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Application>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(), 0),
                    rowMapper.mapRow(mockResultSet(applicationId = "app-uuid-456"), 1),
                )
            }

            // When
            val result = repository.findByGuildId(testGuildId, 0L, 50)

            // Then
            result.size shouldBe 2
        }

        @Test
        fun `should return empty list when no applications for guild`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("guild_id = ?") },
                    any<RowMapper<Application>>(),
                    any<String>(),
                    any<Int>(),
                    any<Long>(),
                )
            } returns emptyList()

            // When
            val result = repository.findByGuildId(testGuildId, 0L, 50)

            // Then
            result shouldBe emptyList()
        }

        @Test
        fun `should apply pagination correctly`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<Application>>(),
                    eq(testGuildId.value),
                    eq(10),
                    eq(20L),
                )
            } returns emptyList()

            // When
            repository.findByGuildId(testGuildId, 20L, 10)

            // Then
            verify {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<Application>>(),
                    testGuildId.value,
                    10,
                    20L,
                )
            }
        }
    }

    @Nested
    inner class FindByGuildIdAndStatusTests {
        @Test
        fun `should return applications with matching status`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("guild_id = ?") && it.contains("status = ?") },
                    any<RowMapper<Application>>(),
                    eq(testGuildId.value),
                    eq("PENDING"),
                    any<Int>(),
                    any<Long>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Application>>()
                listOf(rowMapper.mapRow(mockResultSet(), 0))
            }

            // When
            val result = repository.findByGuildIdAndStatus(testGuildId, ApplicationStatus.PENDING, 0L, 50)

            // Then
            result.size shouldBe 1
        }
    }

    @Nested
    inner class FindByGuildIdAndDiscordIdTests {
        @Test
        fun `should return application when found by discord id`() {
            // Given
            val discordId = "123456789012345678"

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("guild_id = ?") && it.contains("discord_id = ?") },
                    any<RowMapper<Application>>(),
                    eq(testGuildId.value),
                    eq(discordId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Application>>()
                listOf(rowMapper.mapRow(mockResultSet(discordId = discordId), 0))
            }

            // When
            val result = repository.findByGuildIdAndDiscordId(testGuildId, discordId)

            // Then
            result shouldNotBe null
            result?.discordId shouldBe discordId
        }

        @Test
        fun `should return null when not found by discord id`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("discord_id = ?") },
                    any<RowMapper<Application>>(),
                    any<String>(),
                    any<String>(),
                )
            } returns emptyList()

            // When
            val result = repository.findByGuildIdAndDiscordId(testGuildId, "non-existent")

            // Then
            result shouldBe null
        }
    }

    @Nested
    inner class FindByGuildIdAndBattleNetIdTests {
        @Test
        fun `should return application when found by battle net id`() {
            // Given
            val battleNetId = "Player#1234"

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("guild_id = ?") && it.contains("battle_net_id = ?") },
                    any<RowMapper<Application>>(),
                    eq(testGuildId.value),
                    eq(battleNetId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Application>>()
                listOf(rowMapper.mapRow(mockResultSet(battleNetId = battleNetId), 0))
            }

            // When
            val result = repository.findByGuildIdAndBattleNetId(testGuildId, battleNetId)

            // Then
            result shouldNotBe null
            result?.battleNetId shouldBe battleNetId
        }
    }

    @Nested
    inner class CountByGuildIdTests {
        @Test
        fun `should return count of applications for guild`() {
            // Given
            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("guild_id = ?") },
                    Long::class.java,
                    eq(testGuildId.value),
                )
            } returns 42L

            // When
            val result = repository.countByGuildId(testGuildId)

            // Then
            result shouldBe 42L
        }

        @Test
        fun `should return zero when null count`() {
            // Given
            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") },
                    Long::class.java,
                    any<String>(),
                )
            } returns null

            // When
            val result = repository.countByGuildId(testGuildId)

            // Then
            result shouldBe 0L
        }
    }

    @Nested
    inner class CountByGuildIdAndStatusTests {
        @Test
        fun `should return count of applications for guild and status`() {
            // Given
            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("guild_id = ?") && it.contains("status = ?") },
                    Long::class.java,
                    eq(testGuildId.value),
                    eq("PENDING"),
                )
            } returns 15L

            // When
            val result = repository.countByGuildIdAndStatus(testGuildId, ApplicationStatus.PENDING)

            // Then
            result shouldBe 15L
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when application exists`() {
            // Given
            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("enhanced_application_id = ?") },
                    Int::class.java,
                    eq(testApplicationId.value),
                )
            } returns 1

            // When
            val result = repository.existsById(testApplicationId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when application does not exist`() {
            // Given
            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") },
                    Int::class.java,
                    any<String>(),
                )
            } returns 0

            // When
            val result = repository.existsById(testApplicationId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new application when not exists`() {
            // Given
            val application = createTestApplication()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, application.id.value) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(application)

            // Then
            result shouldBe application
            sqlSlot.captured.contains("INSERT INTO") shouldBe true
        }

        @Test
        fun `should update existing application when exists`() {
            // Given
            val application = createTestApplication()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, application.id.value) } returns 1
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(application)

            // Then
            result shouldBe application
            sqlSlot.captured.contains("UPDATE") shouldBe true
        }

        @Test
        fun `should persist all application fields`() {
            // Given
            val application = createTestApplication()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, any<String>()) } returns 0
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            repository.save(application)

            // Then
            verify {
                jdbcTemplate.update(
                    match { sql ->
                        sql.contains("enhanced_application_id") &&
                            sql.contains("guild_id") &&
                            sql.contains("battle_net_id") &&
                            sql.contains("discord_id") &&
                            sql.contains("email") &&
                            sql.contains("character_name") &&
                            sql.contains("character_realm") &&
                            sql.contains("character_class") &&
                            sql.contains("specialization") &&
                            sql.contains("item_level") &&
                            sql.contains("raider_io_score") &&
                            sql.contains("best_parse_average") &&
                            sql.contains("age") &&
                            sql.contains("location") &&
                            sql.contains("timezone") &&
                            sql.contains("raid_days_available") &&
                            sql.contains("previous_guilds") &&
                            sql.contains("reason_for_leaving") &&
                            sql.contains("why_this_guild") &&
                            sql.contains("status") &&
                            sql.contains("reviewed_by") &&
                            sql.contains("reviewed_at") &&
                            sql.contains("created_at") &&
                            sql.contains("updated_at")
                    },
                    *anyVararg(),
                )
            }
        }
    }

    @Nested
    inner class DeleteByIdTests {
        @Test
        fun `should delete application by id`() {
            // Given
            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(testApplicationId.value),
                )
            } returns 1

            // When
            repository.deleteById(testApplicationId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("enhanced_application_id = ?") },
                    testApplicationId.value,
                )
            }
        }
    }

    @Nested
    inner class RowMappingTests {
        @Test
        fun `should map all database fields to domain model`() {
            // Given
            val characterName = "TestCharacter"
            val characterClass = "Death Knight"
            val itemLevel = 495.5
            val raiderIOScore = 2850.0
            val bestParseAverage = 85.5

            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<Application>>(),
                    any<String>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Application>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            characterName = characterName,
                            characterClass = characterClass,
                            itemLevel = itemLevel,
                            raiderIOScore = raiderIOScore,
                            bestParseAverage = bestParseAverage,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(testApplicationId)

            // Then
            result shouldNotBe null
            result?.characterName shouldBe characterName
            result?.characterClass shouldBe characterClass
            result?.itemLevel shouldBe itemLevel
            result?.raiderIOScore shouldBe raiderIOScore
            result?.bestParseAverage shouldBe bestParseAverage
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<Application>>(),
                    any<String>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Application>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(
                            raiderIOScore = null,
                            bestParseAverage = null,
                            reviewedBy = null,
                            reviewedAt = null,
                        ),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(testApplicationId)

            // Then
            result shouldNotBe null
            result?.raiderIOScore shouldBe null
            result?.bestParseAverage shouldBe null
            result?.reviewedBy shouldBe null
            result?.reviewedAt shouldBe null
        }

        @Test
        fun `should parse raid days available from JSON`() {
            // Given
            every {
                jdbcTemplate.query(
                    any<String>(),
                    any<RowMapper<Application>>(),
                    any<String>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<Application>>()
                listOf(
                    rowMapper.mapRow(
                        mockResultSet(raidDaysAvailable = "[\"Tuesday\",\"Wednesday\",\"Thursday\"]"),
                        0,
                    ),
                )
            }

            // When
            val result = repository.findById(testApplicationId)

            // Then
            result shouldNotBe null
            result?.raidDaysAvailable shouldBe listOf("Tuesday", "Wednesday", "Thursday")
        }
    }

    // Helper methods

    private fun mockResultSet(
        applicationId: String = testApplicationId.value,
        guildId: String = testGuildId.value,
        battleNetId: String = "Player#1234",
        discordId: String = "123456789012345678",
        email: String = "player@example.com",
        characterName: String = "Arthas",
        characterRealm: String = "Illidan",
        characterClass: String = "Death Knight",
        specialization: String = "Frost",
        itemLevel: Double = 489.5,
        raiderIOScore: Double? = 2850.0,
        bestParseAverage: Double? = 85.5,
        age: Int = 28,
        location: String = "United States",
        timezone: String = "America/New_York",
        raidDaysAvailable: String = "[\"Tuesday\",\"Wednesday\",\"Thursday\"]",
        previousGuilds: String = "Previous Guild",
        reasonForLeaving: String = "Guild disbanded",
        whyThisGuild: String = "Looking for competitive guild",
        status: String = "PENDING",
        reviewedBy: String? = null,
        reviewedAt: Instant? = null,
        createdAt: Instant = now,
        updatedAt: Instant = now,
    ): ResultSet {
        val rs = mockk<ResultSet>()

        every { rs.getString("enhanced_application_id") } returns applicationId
        every { rs.getString("guild_id") } returns guildId
        every { rs.getString("battle_net_id") } returns battleNetId
        every { rs.getString("discord_id") } returns discordId
        every { rs.getString("email") } returns email
        every { rs.getString("character_name") } returns characterName
        every { rs.getString("character_realm") } returns characterRealm
        every { rs.getString("character_class") } returns characterClass
        every { rs.getString("specialization") } returns specialization
        every { rs.getDouble("item_level") } returns itemLevel
        every { rs.getDouble("raider_io_score") } returns (raiderIOScore ?: 0.0)
        every { rs.getDouble("best_parse_average") } returns (bestParseAverage ?: 0.0)
        every { rs.getInt("age") } returns age
        every { rs.getString("location") } returns location
        every { rs.getString("timezone") } returns timezone
        every { rs.getString("raid_days_available") } returns raidDaysAvailable
        every { rs.getString("previous_guilds") } returns previousGuilds
        every { rs.getString("reason_for_leaving") } returns reasonForLeaving
        every { rs.getString("why_this_guild") } returns whyThisGuild
        every { rs.getString("status") } returns status
        every { rs.getString("reviewed_by") } returns reviewedBy
        every { rs.getTimestamp("reviewed_at") } returns reviewedAt?.let { Timestamp.from(it) }
        every { rs.getTimestamp("created_at") } returns Timestamp.from(createdAt)
        every { rs.getTimestamp("updated_at") } returns Timestamp.from(updatedAt)

        // Handle wasNull for nullable Double fields
        var lastWasNull = false
        every { rs.wasNull() } answers { lastWasNull }

        // Override getDouble to track null state
        every { rs.getDouble("raider_io_score") } answers {
            lastWasNull = raiderIOScore == null
            raiderIOScore ?: 0.0
        }
        every { rs.getDouble("best_parse_average") } answers {
            lastWasNull = bestParseAverage == null
            bestParseAverage ?: 0.0
        }

        return rs
    }

    private fun createTestApplication(): Application =
        Application.create(
            guildId = testGuildId,
            battleNetId = "Player#1234",
            discordId = "123456789012345678",
            email = "player@example.com",
            characterName = "Arthas",
            characterRealm = "Illidan",
            characterClass = "Death Knight",
            specialization = "Frost",
            itemLevel = 489.5,
            raiderIOScore = 2850.0,
            bestParseAverage = 85.5,
            age = 28,
            location = "United States",
            timezone = "America/New_York",
            raidDaysAvailable = listOf("Tuesday", "Wednesday", "Thursday"),
            previousGuilds = "Previous Guild 1, Previous Guild 2",
            reasonForLeaving = "Guild disbanded",
            whyThisGuild = "Looking for a competitive mythic raiding guild",
        )
}
