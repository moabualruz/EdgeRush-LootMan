package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationQuestionEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.ApplicationQuestionEntitySpringRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.Optional

/**
 * Unit tests for JdbcApplicationQuestionRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcApplicationQuestionRepositoryTest : UnitTest() {
    private lateinit var springRepository: ApplicationQuestionEntitySpringRepository
    private lateinit var repository: JdbcApplicationQuestionRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcApplicationQuestionRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return application question when found`() {
            // Given
            val questionId = 1L
            val entity = createApplicationQuestionEntity(id = questionId)
            every { springRepository.findById(questionId) } returns Optional.of(entity)

            // When
            val result = repository.findById(questionId)

            // Then
            result shouldNotBe null
            result?.id shouldBe questionId
            result?.applicationId shouldBe 100L
            verify { springRepository.findById(questionId) }
        }

        @Test
        fun `should return null when application question not found`() {
            // Given
            val questionId = 999L
            every { springRepository.findById(questionId) } returns Optional.empty()

            // When
            val result = repository.findById(questionId)

            // Then
            result shouldBe null
            verify { springRepository.findById(questionId) }
        }

        @Test
        fun `should map all entity fields correctly`() {
            // Given
            val questionId = 1L
            val entity = createApplicationQuestionEntity(
                id = questionId,
                applicationId = 100L,
                position = 1,
                question = "Why do you want to join?",
                answer = "I love raiding!",
                filesJson = "[\"file1.png\", \"file2.jpg\"]",
            )
            every { springRepository.findById(questionId) } returns Optional.of(entity)

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
            verify { springRepository.findById(questionId) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val questionId = 1L
            val entity = createApplicationQuestionEntity(
                id = questionId,
                applicationId = 100L,
                position = null,
                question = null,
                answer = null,
                filesJson = null,
            )
            every { springRepository.findById(questionId) } returns Optional.of(entity)

            // When
            val result = repository.findById(questionId)

            // Then
            result shouldNotBe null
            result?.id shouldBe questionId
            result?.position shouldBe null
            result?.question shouldBe null
            result?.answer shouldBe null
            result?.filesJson shouldBe null
            verify { springRepository.findById(questionId) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated application questions`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities = listOf(
                createApplicationQuestionEntity(1L, 100L),
                createApplicationQuestionEntity(2L, 100L),
            )
            val page = PageImpl(entities)

            every { springRepository.findAll(any<Pageable>()) } returns page

            // When
            val result = repository.findAll(offset, limit)

            // Then
            result.size shouldBe 2
            verify { springRepository.findAll(any<Pageable>()) }
        }

        @Test
        fun `should return empty list when no application questions`() {
            // Given
            val page = PageImpl(emptyList<ApplicationQuestionEntity>())
            every { springRepository.findAll(any<Pageable>()) } returns page

            // When
            val result = repository.findAll(0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findAll(any<Pageable>()) }
        }
    }

    @Nested
    inner class FindByApplicationIdTests {
        @Test
        fun `should return questions for application`() {
            // Given
            val applicationId = 100L
            val entities = listOf(
                createApplicationQuestionEntity(1L, applicationId, position = 1),
                createApplicationQuestionEntity(2L, applicationId, position = 2),
            )
            val page = PageImpl(entities)

            every { springRepository.findByApplicationId(applicationId, any<Pageable>()) } returns page

            // When
            val result = repository.findByApplicationId(applicationId, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.applicationId == applicationId } shouldBe true
            verify { springRepository.findByApplicationId(applicationId, any<Pageable>()) }
        }

        @Test
        fun `should return empty list when application has no questions`() {
            // Given
            val applicationId = 999L
            val page = PageImpl(emptyList<ApplicationQuestionEntity>())

            every { springRepository.findByApplicationId(applicationId, any<Pageable>()) } returns page

            // When
            val result = repository.findByApplicationId(applicationId, 0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findByApplicationId(applicationId, any<Pageable>()) }
        }
    }

    @Nested
    inner class CountTests {
        @Test
        fun `should return total count`() {
            // Given
            every { springRepository.count() } returns 42L

            // When
            val result = repository.count()

            // Then
            result shouldBe 42L
            verify { springRepository.count() }
        }

        @Test
        fun `should return count by application id`() {
            // Given
            val applicationId = 100L
            every { springRepository.countByApplicationId(applicationId) } returns 5L

            // When
            val result = repository.countByApplicationId(applicationId)

            // Then
            result shouldBe 5L
            verify { springRepository.countByApplicationId(applicationId) }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when application question exists`() {
            // Given
            val questionId = 1L
            every { springRepository.existsById(questionId) } returns true

            // When
            val result = repository.existsById(questionId)

            // Then
            result shouldBe true
            verify { springRepository.existsById(questionId) }
        }

        @Test
        fun `should return false when application question does not exist`() {
            // Given
            val questionId = 999L
            every { springRepository.existsById(questionId) } returns false

            // When
            val result = repository.existsById(questionId)

            // Then
            result shouldBe false
            verify { springRepository.existsById(questionId) }
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save entity and return saved result`() {
            // Given
            val entity = createApplicationQuestionEntity(id = null)
            val savedEntity = createApplicationQuestionEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.applicationId shouldBe entity.applicationId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing application question`() {
            // Given
            val entity = createApplicationQuestionEntity(id = 1L)
            every { springRepository.save(entity) } returns entity

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            verify { springRepository.save(entity) }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete application question by id`() {
            // Given
            val questionId = 1L

            // When
            repository.delete(questionId)

            // Then
            verify { springRepository.deleteById(questionId) }
        }
    }

    // Helper methods

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
