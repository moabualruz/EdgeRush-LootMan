package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
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
 * Unit tests for JdbcApplicationQuestionFileRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the application_question_files table.
 */
class JdbcApplicationQuestionFileRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcApplicationQuestionFileRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcApplicationQuestionFileRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return application question file when found`() {
            // Given
            val fileId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationQuestionFileEntity>>(),
                    eq(fileId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionFileEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(fileId, 100L), 0))
            }

            // When
            val result = repository.findById(fileId)

            // Then
            result shouldNotBe null
            result?.id shouldBe fileId
            result?.applicationId shouldBe 100L
        }

        @Test
        fun `should return null when application question file not found`() {
            // Given
            val fileId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationQuestionFileEntity>>(),
                    eq(fileId),
                )
            } returns emptyList()

            // When
            val result = repository.findById(fileId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to entity`() {
            // Given
            val fileId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationQuestionFileEntity>>(),
                    eq(fileId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionFileEntity>>()
                val rs =
                    mockResultSet(
                        id = fileId,
                        applicationId = 100L,
                        questionPosition = 1,
                        question = "Upload your logs",
                        originalFilename = "raid_logs.txt",
                        url = "https://example.com/files/raid_logs.txt",
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(fileId)

            // Then
            result shouldNotBe null
            result?.id shouldBe fileId
            result?.applicationId shouldBe 100L
            result?.questionPosition shouldBe 1
            result?.question shouldBe "Upload your logs"
            result?.originalFilename shouldBe "raid_logs.txt"
            result?.url shouldBe "https://example.com/files/raid_logs.txt"
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val fileId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationQuestionFileEntity>>(),
                    eq(fileId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionFileEntity>>()
                val rs =
                    mockResultSet(
                        id = fileId,
                        applicationId = 100L,
                        questionPosition = null,
                        question = null,
                        originalFilename = null,
                        url = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(fileId)

            // Then
            result shouldNotBe null
            result?.id shouldBe fileId
            result?.questionPosition shouldBe null
            result?.question shouldBe null
            result?.originalFilename shouldBe null
            result?.url shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated application question files`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<ApplicationQuestionFileEntity>>(),
                    eq(limit),
                    eq(offset),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionFileEntity>>()
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
        fun `should return empty list when no application question files`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") },
                    any<RowMapper<ApplicationQuestionFileEntity>>(),
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
        fun `should return files for application`() {
            // Given
            val applicationId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationQuestionFileEntity>>(),
                    eq(applicationId),
                    any<Int>(),
                    any<Long>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionFileEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, applicationId, questionPosition = 1), 0),
                    rowMapper.mapRow(mockResultSet(2L, applicationId, questionPosition = 2), 1),
                )
            }

            // When
            val result = repository.findByApplicationId(applicationId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.applicationId == applicationId } shouldBe true
        }

        @Test
        fun `should return empty list when application has no files`() {
            // Given
            val applicationId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationQuestionFileEntity>>(),
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
                    match<String> { it.contains("COUNT(*)") && it.contains("application_question_files") },
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
        fun `should return true when application question file exists`() {
            // Given
            val fileId = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(fileId),
                )
            } returns 1

            // When
            val result = repository.existsById(fileId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when application question file does not exist`() {
            // Given
            val fileId = 999L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(fileId),
                )
            } returns 0

            // When
            val result = repository.existsById(fileId)

            // Then
            result shouldBe false
        }

        @Test
        fun `should handle null count result as false`() {
            // Given
            val fileId = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(fileId),
                )
            } returns null

            // When
            val result = repository.existsById(fileId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new application question file when id is null`() {
            // Given
            val entity = createApplicationQuestionFileEntity(id = null)
            val generatedId = 1L

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
        fun `should update existing application question file when id is not null`() {
            // Given
            val entity = createApplicationQuestionFileEntity(id = 1L)
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
        fun `should delete application question file by id`() {
            // Given
            val fileId = 1L

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(fileId),
                )
            } returns 1

            // When
            repository.delete(fileId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    fileId,
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: Long,
        applicationId: Long,
        questionPosition: Int? = 1,
        question: String? = "Upload file",
        originalFilename: String? = "test_file.txt",
        url: String? = "https://example.com/files/test.txt",
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("application_id") } returns applicationId
        every { rs.getInt("question_position") } returns (questionPosition ?: 0)
        every { rs.wasNull() } returns (questionPosition == null)
        every { rs.getString("question") } returns question
        every { rs.getString("original_filename") } returns originalFilename
        every { rs.getString("url") } returns url
        return rs
    }

    private fun createApplicationQuestionFileEntity(
        id: Long? = 1L,
        applicationId: Long = 100L,
        questionPosition: Int? = 1,
        question: String? = "Upload file",
        originalFilename: String? = "test_file.txt",
        url: String? = "https://example.com/files/test.txt",
    ): ApplicationQuestionFileEntity =
        ApplicationQuestionFileEntity(
            id = id,
            applicationId = applicationId,
            questionPosition = questionPosition,
            question = question,
            originalFilename = originalFilename,
            url = url,
        )
}
