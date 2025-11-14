package com.edgerush.datasync.domain.applications.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ApplicationTest {

    @Test
    fun `should create application with pending status`() {
        // Given
        val id = ApplicationId(1L)
        val appliedAt = OffsetDateTime.now()
        val applicantInfo = ApplicantInfo(25, "USA", "player#1234", "discord123", "DPS")
        val mainCharacter = CharacterInfo("TestChar", "TestRealm", "US", "Warrior", "Tank", "Human", "Alliance", 80)

        // When
        val application = Application.create(id, appliedAt, applicantInfo, mainCharacter)

        // Then
        application.id shouldBe id
        application.status shouldBe ApplicationStatus.PENDING
        application.isActive() shouldBe true
        application.isClosed() shouldBe false
    }

    @Test
    fun `should approve pending application`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.PENDING)

        // When
        val approved = application.approve()

        // Then
        approved.status shouldBe ApplicationStatus.APPROVED
        approved.isClosed() shouldBe true
    }

    @Test
    fun `should reject pending application`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.PENDING)

        // When
        val rejected = application.reject()

        // Then
        rejected.status shouldBe ApplicationStatus.REJECTED
        rejected.isClosed() shouldBe true
    }

    @Test
    fun `should withdraw pending application`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.PENDING)

        // When
        val withdrawn = application.withdraw()

        // Then
        withdrawn.status shouldBe ApplicationStatus.WITHDRAWN
        withdrawn.isClosed() shouldBe true
    }

    @Test
    fun `should start review on pending application`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.PENDING)

        // When
        val underReview = application.startReview()

        // Then
        underReview.status shouldBe ApplicationStatus.UNDER_REVIEW
        underReview.isActive() shouldBe true
    }

    @Test
    fun `should throw exception when approving already approved application`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.APPROVED)

        // When/Then
        shouldThrow<IllegalArgumentException> {
            application.approve()
        }
    }

    @Test
    fun `should throw exception when starting review on non-pending application`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.UNDER_REVIEW)

        // When/Then
        shouldThrow<IllegalArgumentException> {
            application.startReview()
        }
    }

    @Test
    fun `should approve application under review`() {
        // Given
        val application = createTestApplication(status = ApplicationStatus.UNDER_REVIEW)

        // When
        val approved = application.approve()

        // Then
        approved.status shouldBe ApplicationStatus.APPROVED
    }

    private fun createTestApplication(status: ApplicationStatus = ApplicationStatus.PENDING): Application {
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
