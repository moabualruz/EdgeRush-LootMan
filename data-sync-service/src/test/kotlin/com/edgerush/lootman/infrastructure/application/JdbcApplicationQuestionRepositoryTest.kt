package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationQuestionEntity
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
 * Unit tests for JdbcApplicationQuestionRepository.
 *
 * These tests mock the JdbcTemplate to verify SQL queries and mappings.
 * The repository operates on the application_questions table.
 */
class JdbcApplicationQuestionRepositoryTest : UnitTest() {
    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var repository: JdbcApplicationQuestionRepository

    @BeforeEach
    fun setUp() {
        jdbcTemplate = mockk(relaxed = true)
        repository = JdbcApplicationQuestionRepository(jdbcTemplate)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return application question when found`() {
            // Given
            val questionId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationQuestionEntity>>(),
                    eq(questionId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionEntity>>()
                listOf(rowMapper.mapRow(mockResultSet(questionId, 100L), 0))
            }

            // When
            val result = repository.findById(questionId)

            // Then
            result shouldNotBe null
            result?.id shouldBe questionId
            result?.applicationId shouldBe 100L
        }

        @Test
        fun `should return null when application question not found`() {
            // Given
            val questionId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationQuestionEntity>>(),
                    eq(questionId),
                )
            } returns emptyList()

            // When
            val result = repository.findById(questionId)

            // Then
            result shouldBe null
        }

        @Test
        fun `should map all database fields to entity`() {
            // Given
            val questionId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationQuestionEntity>>(),
                    eq(questionId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionEntity>>()
                val rs =
                    mockResultSet(
                        id = questionId,
                        applicationId = 100L,
                        position = 1,
                        question = "Why do you want to join?",
                        answer = "I love raiding!",
                        filesJson = "[\"file1.png\", \"file2.jpg\"]",
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(questionId)

            // Then
            result shouldNotBe null
            result?.id shouldBe questionId
            result?.applicationId shouldBe 100L
            result?.position shouldBe 1
            result?.question shouldBe "Why do you want to join?"
            result?.answer shouldBe "I love raiding!"
            result?.filesJson shouldBe "[\"file1.png\", \"file2.jpg\"]"
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val questionId = 1L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("id = ?") },
                    any<RowMapper<ApplicationQuestionEntity>>(),
                    eq(questionId),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionEntity>>()
                val rs =
                    mockResultSet(
                        id = questionId,
                        applicationId = 100L,
                        position = null,
                        question = null,
                        answer = null,
                        filesJson = null,
                    )
                listOf(rowMapper.mapRow(rs, 0))
            }

            // When
            val result = repository.findById(questionId)

            // Then
            result shouldNotBe null
            result?.id shouldBe questionId
            result?.position shouldBe null
            result?.question shouldBe null
            result?.answer shouldBe null
            result?.filesJson shouldBe null
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated application questions`() {
            // Given
            val offset = 10L
            val limit = 5

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") && it.contains("OFFSET") },
                    any<RowMapper<ApplicationQuestionEntity>>(),
                    eq(limit),
                    eq(offset),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionEntity>>()
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
        fun `should return empty list when no application questions`() {
            // Given
            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("LIMIT") },
                    any<RowMapper<ApplicationQuestionEntity>>(),
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
        fun `should return questions for application`() {
            // Given
            val applicationId = 100L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationQuestionEntity>>(),
                    eq(applicationId),
                    any<Int>(),
                    any<Long>(),
                )
            } answers {
                val rowMapper = secondArg<RowMapper<ApplicationQuestionEntity>>()
                listOf(
                    rowMapper.mapRow(mockResultSet(1L, applicationId, position = 1), 0),
                    rowMapper.mapRow(mockResultSet(2L, applicationId, position = 2), 1),
                )
            }

            // When
            val result = repository.findByApplicationId(applicationId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.applicationId == applicationId } shouldBe true
        }

        @Test
        fun `should return empty list when application has no questions`() {
            // Given
            val applicationId = 999L

            every {
                jdbcTemplate.query(
                    match<String> { it.contains("SELECT") && it.contains("application_id = ?") },
                    any<RowMapper<ApplicationQuestionEntity>>(),
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
                    match<String> { it.contains("COUNT(*)") && it.contains("application_questions") },
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
            } returns 5L

            // When
            val result = repository.countByApplicationId(applicationId)

            // Then
            result shouldBe 5L
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when application question exists`() {
            // Given
            val questionId = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(questionId),
                )
            } returns 1

            // When
            val result = repository.existsById(questionId)

            // Then
            result shouldBe true
        }

        @Test
        fun `should return false when application question does not exist`() {
            // Given
            val questionId = 999L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(questionId),
                )
            } returns 0

            // When
            val result = repository.existsById(questionId)

            // Then
            result shouldBe false
        }

        @Test
        fun `should handle null count result as false`() {
            // Given
            val questionId = 1L

            every {
                jdbcTemplate.queryForObject(
                    match<String> { it.contains("COUNT(*)") && it.contains("id = ?") },
                    Int::class.java,
                    eq(questionId),
                )
            } returns null

            // When
            val result = repository.existsById(questionId)

            // Then
            result shouldBe false
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should insert new application question when id is null`() {
            // Given
            val entity = createApplicationQuestionEntity(id = null)
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
        fun `should update existing application question when id is not null`() {
            // Given
            val entity = createApplicationQuestionEntity(id = 1L)
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
        fun `should delete application question by id`() {
            // Given
            val questionId = 1L

            every {
                jdbcTemplate.update(
                    match<String> { it.contains("DELETE") },
                    eq(questionId),
                )
            } returns 1

            // When
            repository.delete(questionId)

            // Then
            verify {
                jdbcTemplate.update(
                    match { it.contains("DELETE") && it.contains("id = ?") },
                    questionId,
                )
            }
        }
    }

    // Helper methods

    private fun mockResultSet(
        id: Long,
        applicationId: Long,
        position: Int? = 1,
        question: String? = "Test question?",
        answer: String? = "Test answer",
        filesJson: String? = null,
    ): ResultSet {
        val rs = mockk<ResultSet>()
        every { rs.getLong("id") } returns id
        every { rs.getLong("application_id") } returns applicationId
        every { rs.getInt("position") } returns (position ?: 0)
        every { rs.wasNull() } returns (position == null)
        every { rs.getString("question") } returns question
        every { rs.getString("answer") } returns answer
        every { rs.getString("files_json") } returns filesJson
        return rs
    }

    private fun createApplicationQuestionEntity(
        id: Long? = 1L,
        applicationId: Long = 100L,
        position: Int? = 1,
        question: String? = "Test question?",
        answer: String? = "Test answer",
        filesJson: String? = null,
    ): ApplicationQuestionEntity =
        ApplicationQuestionEntity(
            id = id,
            applicationId = applicationId,
            position = position,
            question = question,
            answer = answer,
            filesJson = filesJson,
        )
}
