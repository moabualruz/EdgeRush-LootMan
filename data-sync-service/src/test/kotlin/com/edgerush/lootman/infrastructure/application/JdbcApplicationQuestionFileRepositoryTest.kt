package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationQuestionFileEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.ApplicationQuestionFileEntitySpringRepository
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
 * Unit tests for JdbcApplicationQuestionFileRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcApplicationQuestionFileRepositoryTest : UnitTest() {
    private lateinit var springRepository: ApplicationQuestionFileEntitySpringRepository
    private lateinit var repository: JdbcApplicationQuestionFileRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcApplicationQuestionFileRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return application question file when found`() {
            // Given
            val fileId = 1L
            val entity = createApplicationQuestionFileEntity(id = fileId)
            every { springRepository.findById(fileId) } returns Optional.of(entity)

            // When
            val result = repository.findById(fileId)

            // Then
            result shouldNotBe null
            result?.id shouldBe fileId
            result?.applicationId shouldBe 100L
            verify { springRepository.findById(fileId) }
        }

        @Test
        fun `should return null when application question file not found`() {
            // Given
            val fileId = 999L
            every { springRepository.findById(fileId) } returns Optional.empty()

            // When
            val result = repository.findById(fileId)

            // Then
            result shouldBe null
            verify { springRepository.findById(fileId) }
        }

        @Test
        fun `should map all entity fields correctly`() {
            // Given
            val fileId = 1L
            val entity =
                createApplicationQuestionFileEntity(
                    id = fileId,
                    applicationId = 100L,
                    questionPosition = 1,
                    question = "Upload your logs",
                    originalFilename = "raid_logs.txt",
                    url = "https://example.com/files/raid_logs.txt",
                )
            every { springRepository.findById(fileId) } returns Optional.of(entity)

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
            verify { springRepository.findById(fileId) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val fileId = 1L
            val entity =
                createApplicationQuestionFileEntity(
                    id = fileId,
                    applicationId = 100L,
                    questionPosition = null,
                    question = null,
                    originalFilename = null,
                    url = null,
                )
            every { springRepository.findById(fileId) } returns Optional.of(entity)

            // When
            val result = repository.findById(fileId)

            // Then
            result shouldNotBe null
            result?.id shouldBe fileId
            result?.questionPosition shouldBe null
            result?.question shouldBe null
            result?.originalFilename shouldBe null
            result?.url shouldBe null
            verify { springRepository.findById(fileId) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated application question files`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities =
                listOf(
                    createApplicationQuestionFileEntity(1L, 100L),
                    createApplicationQuestionFileEntity(2L, 100L),
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
        fun `should return empty list when no application question files`() {
            // Given
            val page = PageImpl(emptyList<ApplicationQuestionFileEntity>())
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
        fun `should return files for application`() {
            // Given
            val applicationId = 100L
            val entities =
                listOf(
                    createApplicationQuestionFileEntity(1L, applicationId, questionPosition = 1),
                    createApplicationQuestionFileEntity(2L, applicationId, questionPosition = 2),
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
        fun `should return empty list when application has no files`() {
            // Given
            val applicationId = 999L
            val page = PageImpl(emptyList<ApplicationQuestionFileEntity>())

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
            every { springRepository.countByApplicationId(applicationId) } returns 3L

            // When
            val result = repository.countByApplicationId(applicationId)

            // Then
            result shouldBe 3L
            verify { springRepository.countByApplicationId(applicationId) }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when application question file exists`() {
            // Given
            val fileId = 1L
            every { springRepository.existsById(fileId) } returns true

            // When
            val result = repository.existsById(fileId)

            // Then
            result shouldBe true
            verify { springRepository.existsById(fileId) }
        }

        @Test
        fun `should return false when application question file does not exist`() {
            // Given
            val fileId = 999L
            every { springRepository.existsById(fileId) } returns false

            // When
            val result = repository.existsById(fileId)

            // Then
            result shouldBe false
            verify { springRepository.existsById(fileId) }
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save entity and return saved result`() {
            // Given
            val entity = createApplicationQuestionFileEntity(id = null)
            val savedEntity = createApplicationQuestionFileEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.applicationId shouldBe entity.applicationId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing application question file`() {
            // Given
            val entity = createApplicationQuestionFileEntity(id = 1L)
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
        fun `should delete application question file by id`() {
            // Given
            val fileId = 1L

            // When
            repository.delete(fileId)

            // Then
            verify { springRepository.deleteById(fileId) }
        }
    }

    // Helper methods

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
