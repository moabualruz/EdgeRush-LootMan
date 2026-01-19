package com.edgerush.lootman.infrastructure.application

import com.edgerush.datasync.entity.ApplicationEntity
import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.infrastructure.springdata.ApplicationEntitySpringRepository
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
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Optional

/**
 * Unit tests for JdbcApplicationRepository.
 *
 * These tests mock the Spring Data repository to verify delegation behavior.
 */
class JdbcApplicationRepositoryTest : UnitTest() {
    private lateinit var springRepository: ApplicationEntitySpringRepository
    private lateinit var repository: JdbcApplicationRepository

    private val now = OffsetDateTime.now(ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        springRepository = mockk(relaxed = true)
        repository = JdbcApplicationRepository(springRepository)
    }

    @Nested
    inner class FindByIdTests {
        @Test
        fun `should return application when found`() {
            // Given
            val applicationId = 123L
            val entity = createApplicationEntity(applicationId = applicationId)
            every { springRepository.findById(applicationId) } returns Optional.of(entity)

            // When
            val result = repository.findById(applicationId)

            // Then
            result shouldNotBe null
            result?.applicationId shouldBe applicationId
            result?.status shouldBe "pending"
            verify { springRepository.findById(applicationId) }
        }

        @Test
        fun `should return null when application not found`() {
            // Given
            val applicationId = 999L
            every { springRepository.findById(applicationId) } returns Optional.empty()

            // When
            val result = repository.findById(applicationId)

            // Then
            result shouldBe null
            verify { springRepository.findById(applicationId) }
        }

        @Test
        fun `should map all database fields to entity`() {
            // Given
            val applicationId = 456L
            val appliedAt = OffsetDateTime.parse("2024-06-01T12:00:00Z")
            val entity = createApplicationEntity(
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
                mainCharacterRegion = "US",
            )
            every { springRepository.findById(applicationId) } returns Optional.of(entity)

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
            verify { springRepository.findById(applicationId) }
        }

        @Test
        fun `should handle null optional fields`() {
            // Given
            val applicationId = 789L
            val entity = createApplicationEntity(
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
                mainCharacterRegion = null,
            )
            every { springRepository.findById(applicationId) } returns Optional.of(entity)

            // When
            val result = repository.findById(applicationId)

            // Then
            result shouldNotBe null
            result?.applicationId shouldBe applicationId
            result?.appliedAt shouldBe null
            result?.status shouldBe null
            result?.age shouldBe null
            result?.mainCharacterLevel shouldBe null
            verify { springRepository.findById(applicationId) }
        }
    }

    @Nested
    inner class FindAllTests {
        @Test
        fun `should return paginated applications`() {
            // Given
            val offset = 10L
            val limit = 5
            val entities = listOf(
                createApplicationEntity(1L),
                createApplicationEntity(2L),
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
        fun `should return empty list when no applications`() {
            // Given
            val page = PageImpl(emptyList<ApplicationEntity>())

            every { springRepository.findAll(any<Pageable>()) } returns page

            // When
            val result = repository.findAll(0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findAll(any<Pageable>()) }
        }
    }

    @Nested
    inner class FindByStatusTests {
        @Test
        fun `should return applications with matching status`() {
            // Given
            val status = "pending"
            val entities = listOf(
                createApplicationEntity(1L, status = status),
                createApplicationEntity(2L, status = status),
            )
            val page = PageImpl(entities)

            every { springRepository.findByStatus(status, any<Pageable>()) } returns page

            // When
            val result = repository.findByStatus(status, 0L, 10)

            // Then
            result.size shouldBe 2
            result.all { it.status == status } shouldBe true
            verify { springRepository.findByStatus(status, any<Pageable>()) }
        }

        @Test
        fun `should return empty list when no applications with status`() {
            // Given
            val status = "rejected"
            val page = PageImpl(emptyList<ApplicationEntity>())

            every { springRepository.findByStatus(status, any<Pageable>()) } returns page

            // When
            val result = repository.findByStatus(status, 0L, 10)

            // Then
            result shouldBe emptyList()
            verify { springRepository.findByStatus(status, any<Pageable>()) }
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
        fun `should return count by status`() {
            // Given
            val status = "approved"
            every { springRepository.countByStatus(status) } returns 15L

            // When
            val result = repository.countByStatus(status)

            // Then
            result shouldBe 15L
            verify { springRepository.countByStatus(status) }
        }
    }

    @Nested
    inner class ExistsByIdTests {
        @Test
        fun `should return true when application exists`() {
            // Given
            val applicationId = 123L
            every { springRepository.existsById(applicationId) } returns true

            // When
            val result = repository.existsById(applicationId)

            // Then
            result shouldBe true
            verify { springRepository.existsById(applicationId) }
        }

        @Test
        fun `should return false when application does not exist`() {
            // Given
            val applicationId = 999L
            every { springRepository.existsById(applicationId) } returns false

            // When
            val result = repository.existsById(applicationId)

            // Then
            result shouldBe false
            verify { springRepository.existsById(applicationId) }
        }
    }

    @Nested
    inner class SaveTests {
        @Test
        fun `should save new entity and return saved result`() {
            // Given
            val entity = createApplicationEntity()
            every { springRepository.save(entity) } returns entity

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should update existing application`() {
            // Given
            val entity = createApplicationEntity(applicationId = 123L)
            every { springRepository.save(entity) } returns entity

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            verify { springRepository.save(entity) }
        }

        @Test
        fun `should handle null appliedAt in save`() {
            // Given
            val entity = createApplicationEntity(appliedAt = null)
            every { springRepository.save(entity) } returns entity

            // When
            val result = repository.save(entity)

            // Then
            result shouldBe entity
            result.appliedAt shouldBe null
            verify { springRepository.save(entity) }
        }
    }

    @Nested
    inner class DeleteTests {
        @Test
        fun `should delete application by id`() {
            // Given
            val applicationId = 123L

            // When
            repository.delete(applicationId)

            // Then
            verify { springRepository.deleteById(applicationId) }
        }
    }

    // Helper methods

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
        syncedAt: OffsetDateTime = now,
    ): ApplicationEntity =
        ApplicationEntity(
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
            syncedAt = syncedAt,
        )
}
