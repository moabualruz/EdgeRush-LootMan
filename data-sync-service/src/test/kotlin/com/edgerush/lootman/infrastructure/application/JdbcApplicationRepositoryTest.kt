package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationEntity
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
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Unit tests for JdbcApplicationRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the applications table.
 */
class JdbcApplicationRepositoryTest : UnitTest() {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcApplicationRepository

    private val now = OffsetDateTime.now(ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcApplicationRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {

        @Test
        fun `should return application when found`() {
            // Given
            val applicationId = 123L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationEntity>>(),
                    eq(applicationId)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(applicationId), 0))
            }

            // When
            val result = repository.findById(applicationId)

            // Then
            result shouldNotBe null
            result?.applicationId shouldBe applicationId
            result?.status shouldBe "pending"
        }

        @Test
        fun `should return null when application not found`() {
            // Given
            val applicationId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationEntity>>(),
                    eq(applicationId)
                )
            } returns emptyList()

            // When
            val result = repository.findById(applicationId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to entity`() {
            // Given
            val applicationId = 456L
            val appliedAt = OffsetDateTime.parse("2024-06-01T12:00:00Z")

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationEntity>>(),
                    eq(applicationId)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationEntity>>()
                val rs = mockResultSet(
                    applicationId = applicationId,
                    appliedAt = appliedAt,
                    status = "approved",
                    role = "Healer",
                    age = 25,
                    country = "US",
                    battletag = "Player#1234",
                    discordId = "discord123",
                    mainCharacterName = "TestChar",
                    mainCharacterRealm = "Illidan",
                    mainCharacterClass = "Priest",
                    mainCharacterRole = "Healer",
                    mainCharacterRace = "Human",
                    mainCharacterFaction = "Alliance",
                    mainCharacterLevel = 70,
                    mainCharacterRegion = "US"
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(applicationId)

            // Then
            result shouldNotBe null
            result?.applicationId shouldBe applicationId
            result?.status shouldBe "approved"
            result?.role shouldBe "Healer"
            result?.age shouldBe 25
            result?.country shouldBe "US"
            result?.battletag shouldBe "Player#1234"
            result?.discordId shouldBe "discord123"
            result?.mainCharacterName shouldBe "TestChar"
            result?.mainCharacterRealm shouldBe "Illidan"
            result?.mainCharacterClass shouldBe "Priest"
            result?.mainCharacterRole shouldBe "Healer"
            result?.mainCharacterRace shouldBe "Human"
            result?.mainCharacterFaction shouldBe "Alliance"
            result?.mainCharacterLevel shouldBe 70
            result?.mainCharacterRegion shouldBe "US"
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val applicationId = 789L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationEntity>>(),
                    eq(applicationId)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationEntity>>()
                val rs = mockResultSet(
                    applicationId = applicationId,
                    appliedAt = null,
                    status = null,
                    role = null,
                    age = null,
                    country = null,
                    battletag = null,
                    discordId = null,
                    mainCharacterName = null,
                    mainCharacterRealm = null,
                    mainCharacterClass = null,
                    mainCharacterRole = null,
                    mainCharacterRace = null,
                    mainCharacterFaction = null,
                    mainCharacterLevel = null,
                    mainCharacterRegion = null
                )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(applicationId)

            // Then
            result shouldNotBe null
            result?.applicationId shouldBe applicationId
            result?.appliedAt shouldBe null
            result?.status shouldBe null
            result?.age shouldBe null
            result?.mainCharacterLevel shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {

        @Test
        fun `should return paginated applications`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<ApplicationEntity>>(),
                    eq(limit),
                    eq(offset)
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L), 0),
                    rowMapper.mapRow(mockResultSet(2L), 1)
                )
            }

            // When
            val result = repository.findAll(offset, limit)

            // Then
            result.size shouldBe 2
        }

        @Test
        fun `should return empty list when no applications`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") },
                    any<RowMapper<ApplicationEntity>>(),
                    any<Int>(),
                    any<Long>()
                )
            } returns emptyList()

            // When
            val result = repository.findAll(0L, 10)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByStatusTests {

        @Test
        fun `should return applications with matching status`() {
            // Given
            val status = "pending"

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("status = ?") },
                    any<RowMapper<ApplicationEntity>>(),
                    eq(status),
                    any<Int>(),
                    any<Long>()
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, status = status), 0),
                    rowMapper.mapRow(mockResultSet(2L, status = status), 1)
                )
            }

            // When
            val result = repository.findByStatus(status, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.status == status } shouldBe true
        }
    }

    @Nested
    inner class CountTests {

        @Test
        fun `should return total count`() {
            // Given
            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("applications") },
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
        fun `should return count by status`() {
            // Given
            val status = "approved"

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("status = ?") },
                    Long::class.java,
                    eq(status)
                )
            } returns 15L

            // When
            val result = repository.countByStatus(status)

            // Then
            result shouldBe 15L
        }
    }

    @Nested
    inner class ExistsByIdTests {

        @Test
        fun `should return true when application exists`() {
            // Given
            val applicationId = 123L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("application_id = ?") },
                    Int::class.java,
                    eq(applicationId)
                )
            } returns 1

            // When
            val result = repository.existsById(applicationId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when application does not exist`() {
            // Given
            val applicationId = 999L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("application_id = ?") },
                    Int::class.java,
                    eq(applicationId)
                )
            } returns 0

            // When
            val result = repository.existsById(applicationId)

            // Then
            result shouldBe false
        }

        @Test
        fun `should handle null count result as false`() {
            // Given
            val applicationId = 123L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("application_id = ?") },
                    Int::class.java,
                    eq(applicationId)
                )
            } returns null

            // When
            val result = repository.existsById(applicationId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `should insert new application when not exists`() {
            // Given
            val entity = createApplicationEntity()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, entity.applicationId) } returns 0
            every { jdbcTemplate.update(capture(sqlSlot), *anyVararg()) } returns 1

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            sqlSlot.captured.contains("INSERT INTO") shouldBe true

            verify {
                jdbcTemplate.update(
                    match { it.contains("INSERT INTO") },
                    *anyVararg()
                )
            }
        }

        @Test
        fun `should update existing application when exists`() {
            // Given
            val entity = createApplicationEntity()
            val sqlSlot = slot<String>()

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, entity.applicationId) } returns 1
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

        @Test
        fun `should handle null appliedAt in insert`() {
            // Given
            val entity = createApplicationEntity(appliedAt = null)

            every { jdbcTemplate.queryForObject(any<String>(), Int::class.java, entity.applicationId) } returns 0
            every { jdbcTemplate.update(any<String>(), *anyVararg()) } returns 1

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            result.appliedAt shouldBe null
        }
    }

    @Nested
    inner class DeleteTests {

        @Test
        fun `should delete application by id`() {
            // Given
            val applicationId = 123L

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(applicationId)
                )
            } returns 1

            // When
            repository.delete(applicationId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("application_id = ?") },
                    applicationId
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        applicationId: Long,
        appliedAt: OffsetDateTime? = now,
        status: String? = "pending",
        role: String? = "DPS",
        age: Int? = 30,
        country: String? = "US",
        battletag: String? = "TestPlayer#1234",
        discordId: String? = "discord456",
        mainCharacterName: String? = "MainChar",
        mainCharacterRealm: String? = "Illidan",
        mainCharacterClass: String? = "Mage",
        mainCharacterRole: String? = "DPS",
        mainCharacterRace: String? = "Human",
        mainCharacterFaction: String? = "Alliance",
        mainCharacterLevel: Int? = 70,
        mainCharacterRegion: String? = "US"
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("application_id") } returns applicationId
        every { rs.getTimestamp("applied_at") } returns appliedAt?.let { Timestamp.from(it.toInstant()) }
        every { rs.getString("status") } returns status
        every { rs.getString("role") } returns role
        every { rs.getInt("age") } returns (age ?: 0)
        every { rs.wasNull() } returns (age == null)
        every { rs.getString("country") } returns country
        every { rs.getString("battletag") } returns battletag
        every { rs.getString("discord_id") } returns discordId
        every { rs.getString("main_character_name") } returns mainCharacterName
        every { rs.getString("main_character_realm") } returns mainCharacterRealm
        every { rs.getString("main_character_class") } returns mainCharacterClass
        every { rs.getString("main_character_role") } returns mainCharacterRole
        every { rs.getString("main_character_race") } returns mainCharacterRace
        every { rs.getString("main_character_faction") } returns mainCharacterFaction
        every { rs.getInt("main_character_level") } returns (mainCharacterLevel ?: 0)
        every { rs.getString("main_character_region") } returns mainCharacterRegion
        every { rs.getTimestamp("synced_at") } returns Timestamp.from(now.toInstant())
        return rs
    }

    private fun createApplicationEntity(
        applicationId: Long = 123L,
        appliedAt: OffsetDateTime? = now,
        status: String? = "pending",
        role: String? = "DPS",
        age: Int? = 30,
        country: String? = "US",
        battletag: String? = "TestPlayer#1234",
        discordId: String? = "discord456",
        mainCharacterName: String? = "MainChar",
        mainCharacterRealm: String? = "Illidan",
        mainCharacterClass: String? = "Mage",
        mainCharacterRole: String? = "DPS",
        mainCharacterRace: String? = "Human",
        mainCharacterFaction: String? = "Alliance",
        mainCharacterLevel: Int? = 70,
        mainCharacterRegion: String? = "US",
        syncedAt: OffsetDateTime = now
    ): ApplicationEntity = ApplicationEntity(
        applicationId = applicationId,
        appliedAt = appliedAt,
        status = status,
        role = role,
        age = age,
        country = country,
        battletag = battletag,
        discordId = discordId,
        mainCharacterName = mainCharacterName,
        mainCharacterRealm = mainCharacterRealm,
        mainCharacterClass = mainCharacterClass,
        mainCharacterRole = mainCharacterRole,
        mainCharacterRace = mainCharacterRace,
        mainCharacterFaction = mainCharacterFaction,
        mainCharacterLevel = mainCharacterLevel,
        mainCharacterRegion = mainCharacterRegion,
        syncedAt = syncedAt
    )
}
