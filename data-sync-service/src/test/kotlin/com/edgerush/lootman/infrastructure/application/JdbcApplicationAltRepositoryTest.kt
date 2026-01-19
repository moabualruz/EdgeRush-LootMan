package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationAltEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.ApplicationAltEntitySpringRepository
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
 * Unit tests for JdbcApplicationAltRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcApplicationAltRepositoryTest : UnitTest() {
    private lateinit var springRepository: ApplicationAltEntitySpringRepository
    private lateinit var repository: JdbcApplicationAltRepository

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcApplicationAltRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return application alt when found`() {
            // Given
            val altId = 1L
            val entity = createApplicationAltEntity(id = altId, applicationId = 100L)
            every { springRepository.findById(altId) } returns Optional.of(entity)

            // When
            val result = repository.findById(altId)

            // Then
            result shouldNotBe null
            result?.id shouldBe altId
            result?.applicationId shouldBe 100L
            verify { springRepository.findById(altId) }
        }

        @Test
        fun `should return null when application alt not found`() {
            // Given
            val altId = 999L
            every { springRepository.findById(altId) } returns Optional.empty()

            // When
            val result = repository.findById(altId)

            // Then
            result shouldBe null
            verify { springRepository.findById(altId) }
        }

        @Test
        fun `should map all database fields to entity`() {
            // Given
            val altId = 1L
            val entity = createApplicationAltEntity(
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
            every { springRepository.findById(altId) } returns Optional.of(entity)

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
            verify { springRepository.findById(altId) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val altId = 1L
            val entity = createApplicationAltEntity(
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
            every { springRepository.findById(altId) } returns Optional.of(entity)

            // When
            val result = repository.findById(altId)

            // Then
            result shouldNotBe null
            result?.id shouldBe altId
            result?.name shouldBe null
            result?.level shouldBe null
            verify { springRepository.findById(altId) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated application alts`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities = listOf(
                createApplicationAltEntity(1L, 100L),
                createApplicationAltEntity(2L, 100L),
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
        fun `should return empty list when no application alts`() {
            // Given
            val page = PageImpl(emptyList<ApplicationAltEntity>())

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
        fun `should return alts for application`() {
            // Given
            val applicationId = 100L
            val entities = listOf(
                createApplicationAltEntity(1L, applicationId),
                createApplicationAltEntity(2L, applicationId),
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
        fun `should return empty list when application has no alts`() {
            // Given
            val applicationId = 999L
            val page = PageImpl(emptyList<ApplicationAltEntity>())

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
        fun `should return true when application alt exists`() {
            // Given
            val altId = 1L
            every { springRepository.existsById(altId) } returns true

            // When
            val result = repository.existsById(altId)

            // Then
            result shouldBe true
            verify { springRepository.existsById(altId) }
        }

        @Test
        fun `should return false when application alt does not exist`() {
            // Given
            val altId = 999L
            every { springRepository.existsById(altId) } returns false

            // When
            val result = repository.existsById(altId)

            // Then
            result shouldBe false
            verify { springRepository.existsById(altId) }
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save new entity and return saved result`() {
            // Given
            val entity = createApplicationAltEntity(id = null)
            val savedEntity = createApplicationAltEntity(id = 1L)
            every { springRepository.save(entity) } returns savedEntity

            // When
            val result = repository.save(entity)

            // Then
            result.id shouldBe 1L
            result.applicationId shouldBe entity.applicationId
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing application alt`() {
            // Given
            val entity = createApplicationAltEntity(id = 1L)
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
        fun `should delete application alt by id`() {
            // Given
            val altId = 1L

            // When
            repository.delete(altId)

            // Then
            verify { springRepository.deleteById(altId) }
        }
    }

    // Helper methods

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
