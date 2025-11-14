package com.edgerush.datasync.application.applications

import com.edgerush.datasync.domain.applications.model.*
import com.edgerush.datasync.domain.applications.repository.ApplicationRepository
import com.edgerush.datasync.domain.applications.service.ApplicationReviewService
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ReviewApplicationUseCaseTest {

    private lateinit var useCase: ReviewApplicationUseCase
    private lateinit var applicationRepository: ApplicationRepository
    private lateinit var reviewService: ApplicationReviewService

    @BeforeEach
    fun setup() {
        applicationRepository = mockk()
        reviewService = mockk()
        useCase = ReviewApplicationUseCase(applicationRepository, reviewService)
    }

    @Test
    fun `should start review on pending application`() {
        // Given
        val applicationId = ApplicationId(1L)
        val application = createTestApplication(status = ApplicationStatus.PENDING)
        val command = ReviewApplicationCommand(applicationId, ReviewAction.START_REVIEW)

        every { applicationRepository.findById(applicationId) } returns application
        every { applicationRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.execute(command)

        // Then
        result.isSuccess shouldBe true
        result.getOrNull()?.status shouldBe ApplicationStatus.UNDER_REVIEW
        verify(exactly = 1) { applicationRepository.save(any()) }
    }

    @Test
    fun `should approve application when requirements are met`() {
        // Given
        val applicationId = ApplicationId(1L)
        val application = createTestApplication(status = ApplicationStatus.UNDER_REVIEW)
        val command = ReviewApplicationCommand(applicationId, ReviewAction.APPROVE)

        every { applicationRepository.findById(applicationId) } returns application
        every { reviewService.meetsBasicRequirements(application) } returns true
        every { applicationRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.execute(command)

        // Then
        result.isSuccess shouldBe true
        result.getOrNull()?.status shouldBe ApplicationStatus.APPROVED
        verify(exactly = 1) { reviewService.meetsBasicRequirements(application) }
        verify(exactly = 1) { applicationRepository.save(any()) }
    }

    @Test
    fun `should fail to approve application when requirements are not met`() {
        // Given
        val applicationId = ApplicationId(1L)
        val application = createTestApplication(status = ApplicationStatus.UNDER_REVIEW)
        val command = ReviewApplicationCommand(applicationId, ReviewAction.APPROVE)

        every { applicationRepository.findById(applicationId) } returns application
        every { reviewService.meetsBasicRequirements(application) } returns false

        // When
        val result = useCase.execute(command)

        // Then
        result.isFailure shouldBe true
        result.exceptionOrNull()!!::class shouldBe ApplicationRequirementsNotMetException::class
        verify(exactly = 0) { applicationRepository.save(any()) }
    }

    @Test
    fun `should reject application`() {
        // Given
        val applicationId = ApplicationId(1L)
        val application = createTestApplication(status = ApplicationStatus.UNDER_REVIEW)
        val command = ReviewApplicationCommand(applicationId, ReviewAction.REJECT)

        every { applicationRepository.findById(applicationId) } returns application
        every { applicationRepository.save(any()) } answers { firstArg() }

        // When
        val result = useCase.execute(command)

        // Then
        result.isSuccess shouldBe true
        result.getOrNull()?.status shouldBe ApplicationStatus.REJECTED
        verify(exactly = 1) { applicationRepository.save(any()) }
    }

    @Test
    fun `should fail when application not found`() {
        // Given
        val applicationId = ApplicationId(999L)
        val command = ReviewApplicationCommand(applicationId, ReviewAction.APPROVE)

        every { applicationRepository.findById(applicationId) } returns null

        // When
        val result = useCase.execute(command)

        // Then
        result.isFailure shouldBe true
        result.exceptionOrNull()!!::class shouldBe ApplicationNotFoundException::class
    }

    private fun createTestApplication(status: ApplicationStatus): Application {
        val id = ApplicationId(1L)
        val appliedAt = OffsetDateTime.now()
        val applicantInfo = ApplicantInfo(25, "USA", "player#1234", "discord123", "DPS")
        val mainCharacter = CharacterInfo("TestChar", "TestRealm", "US", "Warrior", "Tank", "Human", "Alliance", 80)

        return Application(
            id = id,
            appliedAt = appliedAt,
            status = status,
            applicantInfo = applicantInfo,
            mainCharacter = mainCharacter
        )
    }
}
