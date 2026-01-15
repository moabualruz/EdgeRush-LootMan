package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationAltEntity
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
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Unit tests for JdbcApplicationAltRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the application_alts table.
 */
class JdbcApplicationAltRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcApplicationAltRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcApplicationAltRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return application alt when found`() {
            // Given
            val altId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationAltEntity>>(),
                    eq(altId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationAltEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(altId, 100L), 0))
            }

            // When
            val result = repository.findById(altId)

            // Then
            result shouldNotBe null
            result?.id shouldBe altId
            result?.applicationId shouldBe 100L
        }

        @Test
        fun `should return null when application alt not found`() {
            // Given
            val altId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationAltEntity>>(),
                    eq(altId),
                )
            } returns emptyList()

            // When
            val result = repository.findById(altId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to entity`() {
            // Given
            val altId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationAltEntity>>(),
                    eq(altId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationAltEntity>>()
                val rs =
                    mockResultSet(
                        id = altId,
                        applicationId = 100L,
                        name = "AltCharacter",
                        realm = "Illidan",
                        region = "US",
                        clazz = "Warrior",
                        role = "Tank",
                        level = 70,
                        faction = "Alliance",
                        race = "Human",
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(altId)

            // Then
            result shouldNotBe null
            result?.id shouldBe altId
            result?.applicationId shouldBe 100L
            result?.name shouldBe "AltCharacter"
            result?.realm shouldBe "Illidan"
            result?.region shouldBe "US"
            result?.clazz shouldBe "Warrior"
            result?.role shouldBe "Tank"
            result?.level shouldBe 70
            result?.faction shouldBe "Alliance"
            result?.race shouldBe "Human"
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val altId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationAltEntity>>(),
                    eq(altId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationAltEntity>>()
                val rs =
                    mockResultSet(
                        id = altId,
                        applicationId = 100L,
                        name = null,
                        realm = null,
                        region = null,
                        clazz = null,
                        role = null,
                        level = null,
                        faction = null,
                        race = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(altId)

            // Then
            result shouldNotBe null
            result?.id shouldBe altId
            result?.name shouldBe null
            result?.level shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated application alts`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<ApplicationAltEntity>>(),
                    eq(limit),
                    eq(offset),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationAltEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, 100L), 0),
                    rowMapper.mapRow(mockResultSet(2L, 100L), 1),
                )
            }

            // When
            val result = repository.findAll(offset, limit)

            // Then
            result.size shouldBe 2
        }

        @Test
        fun `should return empty list when no application alts`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") },
                    any<RowMapper<ApplicationAltEntity>>(),
                    any<Int>(),
                    any<Long>(),
                )
            } returns emptyList()

            // When
            val result = repository.findAll(0L, 10)

            // Then
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FindByApplicationIdTests {
        @Test
        fun `should return alts for application`() {
            // Given
            val applicationId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationAltEntity>>(),
                    eq(applicationId),
                    any<Int>(),
                    any<Long>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationAltEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, applicationId), 0),
                    rowMapper.mapRow(mockResultSet(2L, applicationId), 1),
                )
            }

            // When
            val result = repository.findByApplicationId(applicationId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.applicationId == applicationId } shouldBe true
        }

        @Test
        fun `should return empty list when application has no alts`() {
            // Given
            val applicationId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationAltEntity>>(),
                    eq(applicationId),
                    any<Int>(),
                    any<Long>(),
                )
            } returns emptyList()

            // When
            val result = repository.findByApplicationId(applicationId, 0L, 10)

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
                    match<String> { it.contains("COUNT(*)") && it.contains("application_alts") },
                    Long::class.java,
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
                    Long::class.java,
                )
            } returns null

            // When
            val result = repository.count()

            // Then
            result shouldBe 0L
        }

        @Test
        fun `should return count by application id`() {
            // Given
            val applicationId = 100L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("application_id = ?") },
                    Long::class.java,
                    eq(applicationId),
                )
            } returns 3L

            // When
            val result = repository.countByApplicationId(applicationId)

            // Then
            result shouldBe 3L
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when application alt exists`() {
            // Given
            val altId = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(altId),
                )
            } returns 1

            // When
            val result = repository.existsById(altId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when application alt does not exist`() {
            // Given
            val altId = 999L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(altId),
                )
            } returns 0

            // When
            val result = repository.existsById(altId)

            // Then
            result shouldBe false
        }

        @Test
        fun `should handle null count result as false`() {
            // Given
            val altId = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(altId),
                )
            } returns null

            // When
            val result = repository.existsById(altId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new application alt when id is null`() {
            // Given
            val entity = createApplicationAltEntity(id = null)
            val generatedId = 1L

            val mockConnection = mockk<Connection>()
            val mockPreparedStatement = mockk<PreparedStatement>(relaxed = true)
            val mockGeneratedKeys = mockk<ResultSet>()

            every { mockConnection.prepareStatement(any(), any<Int>()) } returns mockPreparedStatement
            every { mockPreparedStatement.generatedKeys } returns mockGeneratedKeys
            every { mockGeneratedKeys.next() } returns true
            every { mockGeneratedKeys.getLong(1) } returns generatedId

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
            result.applicationId shouldBe entity.applicationId
        }

        @Test
        fun `should update existing application alt when id is not null`() {
            // Given
            val entity = createApplicationAltEntity(id = 1L)
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
                    *anyVararg(),
                )
            }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete application alt by id`() {
            // Given
            val altId = 1L

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(altId),
                )
            } returns 1

            // When
            repository.delete(altId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    altId,
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: Long,
        applicationId: Long,
        name: String? = "TestAlt",
        realm: String? = "Illidan",
        region: String? = "US",
        clazz: String? = "Warrior",
        role: String? = "DPS",
        level: Int? = 70,
        faction: String? = "Alliance",
        race: String? = "Human",
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("application_id") } returns applicationId
        every { rs.getString("name") } returns name
        every { rs.getString("realm") } returns realm
        every { rs.getString("region") } returns region
        every { rs.getString("class") } returns clazz
        every { rs.getString("role") } returns role
        every { rs.getInt("level") } returns (level ?: 0)
        every { rs.wasNull() } returns (level == null)
        every { rs.getString("faction") } returns faction
        every { rs.getString("race") } returns race
        return rs
    }

    private fun createApplicationAltEntity(
        id: Long? = 1L,
        applicationId: Long = 100L,
        name: String? = "TestAlt",
        realm: String? = "Illidan",
        region: String? = "US",
        clazz: String? = "Warrior",
        role: String? = "DPS",
        level: Int? = 70,
        faction: String? = "Alliance",
        race: String? = "Human",
    ): ApplicationAltEntity =
        ApplicationAltEntity(
            id = id,
            applicationId = applicationId,
            name = name,
            realm = realm,
            region = region,
            clazz = clazz,
            role = role,
            level = level,
            faction = faction,
            race = race,
        )
}
