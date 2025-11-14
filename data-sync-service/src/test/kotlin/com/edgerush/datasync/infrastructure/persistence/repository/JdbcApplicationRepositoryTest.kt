package com.edgerush.datasync.infrastructure.persistence.repository

import com.edgerush.datasync.domain.applications.model.*
import com.edgerush.datasync.entity.ApplicationEntity
import com.edgerush.datasync.infrastructure.persistence.mapper.ApplicationAltMapper
import com.edgerush.datasync.infrastructure.persistence.mapper.ApplicationMapper
import com.edgerush.datasync.infrastructure.persistence.mapper.ApplicationQuestionMapper
import com.edgerush.datasync.repository.ApplicationAltRepository
import com.edgerush.datasync.repository.ApplicationQuestionFileRepository
import com.edgerush.datasync.repository.ApplicationQuestionRepository
import com.edgerush.datasync.repository.ApplicationRepository as SpringApplicationRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class JdbcApplicationRepositoryTest {

    private lateinit var repository: JdbcApplicationRepository
    private lateinit var springRepository: SpringApplicationRepository
    private lateinit var altRepository: ApplicationAltRepository
    private lateinit var questionRepository: ApplicationQuestionRepository
    private lateinit var questionFileRepository: ApplicationQuestionFileRepository
    private lateinit var mapper: ApplicationMapper
    private lateinit var altMapper: ApplicationAltMapper
    private lateinit var questionMapper: ApplicationQuestionMapper

    @BeforeEach
    fun setup() {
        springRepository = mockk()
        altRepository = mockk()
        questionRepository = mockk()
        questionFileRepository = mockk()
        mapper = ApplicationMapper()
        altMapper = ApplicationAltMapper()
        questionMapper = mockk()
        
        repository = JdbcApplicationRepository(
            springRepository,
            altRepository,
            questionRepository,
            questionFileRepository,
            mapper,
            altMapper,
            questionMapper
        )
    }

    @Test
    fun `should find application by id`() {
        // Given
        val applicationId = ApplicationId(1L)
        val entity = createTestEntity()

        every { springRepository.findById(1L) } returns Optional.of(entity)
        every { altRepository.findAll() } returns emptyList()
        every { questionRepository.findAll() } returns emptyList()
        every { questionFileRepository.findAll() } returns emptyList()

        // When
        val result = repository.findById(applicationId)

        // Then
        result shouldNotBe null
        result?.id shouldBe applicationId
    }

    @Test
    fun `should return null when application not found`() {
        // Given
        val applicationId = ApplicationId(999L)

        every { springRepository.findById(999L) } returns Optional.empty()

        // When
        val result = repository.findById(applicationId)

        // Then
        result shouldBe null
    }

    @Test
    fun `should find applications by status`() {
        // Given
        val entities = listOf(createTestEntity(), createTestEntity(2L))

        every { springRepository.findByStatus("PENDING") } returns entities
        every { altRepository.findAll() } returns emptyList()
        every { questionRepository.findAll() } returns emptyList()
        every { questionFileRepository.findAll() } returns emptyList()

        // When
        val result = repository.findByStatus(ApplicationStatus.PENDING)

        // Then
        result.size shouldBe 2
    }

    @Test
    fun `should save application`() {
        // Given
        val application = createTestApplication()

        every { springRepository.save(any()) } returns createTestEntity()
        every { altRepository.findAll() } returns emptyList()
        every { questionRepository.findAll() } returns emptyList()
        every { questionFileRepository.findAll() } returns emptyList()

        // When
        val result = repository.save(application)

        // Then
        result shouldBe application
        verify(exactly = 1) { springRepository.save(any()) }
    }

    @Test
    fun `should delete application`() {
        // Given
        val applicationId = ApplicationId(1L)

        every { springRepository.deleteById(1L) } just Runs

        // When
        repository.delete(applicationId)

        // Then
        verify(exactly = 1) { springRepository.deleteById(1L) }
    }

    private fun createTestEntity(id: Long = 1L): ApplicationEntity {
        return ApplicationEntity(
            applicationId = id,
            appliedAt = OffsetDateTime.now(),
            status = "PENDING",
            role = "DPS",
            age = 25,
            country = "USA",
            battletag = "player#1234",
            discordId = "discord123",
            mainCharacterName = "TestChar",
            mainCharacterRealm = "TestRealm",
            mainCharacterClass = "Warrior",
            mainCharacterRole = "Tank",
            mainCharacterRace = "Human",
            mainCharacterFaction = "Alliance",
            mainCharacterLevel = 80,
            mainCharacterRegion = "US",
            syncedAt = OffsetDateTime.now()
        )
    }

    private fun createTestApplication(): Application {
        return Application.create(
            id = ApplicationId(1L),
            appliedAt = OffsetDateTime.now(),
            applicantInfo = ApplicantInfo(25, "USA", "player#1234", "discord123", "DPS"),
            mainCharacter = CharacterInfo("TestChar", "TestRealm", "US", "Warrior", "Tank", "Human", "Alliance", 80)
        )
    }
}
