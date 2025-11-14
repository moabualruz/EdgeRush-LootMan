package com.edgerush.datasync.application.applications

import com.edgerush.datasync.domain.applications.model.*
import com.edgerush.datasync.domain.applications.repository.ApplicationRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class GetApplicationsUseCaseTest {

    private lateinit var useCase: GetApplicationsUseCase
    private lateinit var applicationRepository: ApplicationRepository

    @BeforeEach
    fun setup() {
        applicationRepository = mockk()
        useCase = GetApplicationsUseCase(applicationRepository)
    }

    @Test
    fun `should get application by id`() {
        // Given
        val applicationId = ApplicationId(1L)
        val application = createTestApplication(applicationId)
        val query = GetApplicationQuery(applicationId)

        every { applicationRepository.findById(applicationId) } returns application

        // When
        val result = useCase.execute(query)

        // Then
        result.isSuccess shouldBe true
        result.getOrNull() shouldBe application
    }

    @Test
    fun `should fail when application not found`() {
        // Given
        val applicationId = ApplicationId(999L)
        val query = GetApplicationQuery(applicationId)

        every { applicationRepository.findById(applicationId) } returns null

        // When
        val result = useCase.execute(query)

        // Then
        result.isFailure shouldBe true
    }

    @Test
    fun `should get applications by status`() {
        // Given
        val applications = listOf(
            createTestApplication(ApplicationId(1L), ApplicationStatus.PENDING),
            createTestApplication(ApplicationId(2L), ApplicationStatus.PENDING)
        )
        val query = GetApplicationsByStatusQuery("PENDING")

        every { applicationRepository.findByStatus(ApplicationStatus.PENDING) } returns applications

        // When
        val result = useCase.execute(query)

        // Then
        result.isSuccess shouldBe true
        result.getOrNull()?.size shouldBe 2
    }

    @Test
    fun `should get all applications when status is null`() {
        // Given
        val applications = listOf(
            createTestApplication(ApplicationId(1L), ApplicationStatus.PENDING),
            createTestApplication(ApplicationId(2L), ApplicationStatus.APPROVED)
        )
        val query = GetApplicationsByStatusQuery(null)

        every { applicationRepository.findAll() } returns applications

        // When
        val result = useCase.execute(query)

        // Then
        result.isSuccess shouldBe true
        result.getOrNull()?.size shouldBe 2
    }

    private fun createTestApplication(
        id: ApplicationId,
        status: ApplicationStatus = ApplicationStatus.PENDING
    ): Application {
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
