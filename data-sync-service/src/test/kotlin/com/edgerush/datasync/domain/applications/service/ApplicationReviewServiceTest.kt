package com.edgerush.datasync.domain.applications.service

import com.edgerush.datasync.domain.applications.model.*
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class ApplicationReviewServiceTest {

    private lateinit var service: ApplicationReviewService

    @BeforeEach
    fun setup() {
        service = ApplicationReviewService()
    }

    @Test
    fun `should determine application meets basic requirements`() {
        // Given
        val application = createCompleteApplication()

        // When
        val meetsRequirements = service.meetsBasicRequirements(application)

        // Then
        meetsRequirements shouldBe true
    }

    @Test
    fun `should determine application does not meet requirements when character level is low`() {
        // Given
        val application = createCompleteApplication(characterLevel = 60)

        // When
        val meetsRequirements = service.meetsBasicRequirements(application)

        // Then
        meetsRequirements shouldBe false
    }

    @Test
    fun `should determine application does not meet requirements when questions are not answered`() {
        // Given
        val application = createCompleteApplication(questions = emptyList())

        // When
        val meetsRequirements = service.meetsBasicRequirements(application)

        // Then
        meetsRequirements shouldBe false
    }

    @Test
    fun `should determine application does not meet requirements when no contact info`() {
        // Given
        val applicantInfo = ApplicantInfo(25, "USA", null, null, "DPS")
        val application = createCompleteApplication(applicantInfo = applicantInfo)

        // When
        val meetsRequirements = service.meetsBasicRequirements(application)

        // Then
        meetsRequirements shouldBe false
    }

    @Test
    fun `should calculate completeness score for complete application`() {
        // Given
        val application = createCompleteApplication()

        // When
        val score = service.calculateCompletenessScore(application)

        // Then
        score shouldBe 1.0
    }

    @Test
    fun `should calculate lower completeness score for incomplete application`() {
        // Given
        val application = createCompleteApplication(
            characterLevel = 60,
            altCharacters = emptyList()
        )

        // When
        val score = service.calculateCompletenessScore(application)

        // Then
        score shouldBe 0.6 // 40% questions + 20% contact, missing 30% for level and 10% for alts
    }

    @Test
    fun `should determine application can be auto-approved`() {
        // Given
        val application = createCompleteApplication(characterLevel = 80)

        // When
        val canAutoApprove = service.canAutoApprove(application)

        // Then
        canAutoApprove shouldBe true
    }

    @Test
    fun `should determine application cannot be auto-approved when level is not max`() {
        // Given
        val application = createCompleteApplication(characterLevel = 75)

        // When
        val canAutoApprove = service.canAutoApprove(application)

        // Then
        canAutoApprove shouldBe false
    }

    @Test
    fun `should determine application cannot be auto-approved when completeness is low`() {
        // Given
        val applicantInfo = ApplicantInfo(25, "USA", null, null, "DPS") // No contact info
        val application = createCompleteApplication(
            characterLevel = 80,
            applicantInfo = applicantInfo,
            questions = listOf(
                ApplicationQuestion(0, "Question 1", "Answer 1")
            ),
            altCharacters = emptyList() // Missing alts to lower score
        )

        // When
        val canAutoApprove = service.canAutoApprove(application)

        // Then
        canAutoApprove shouldBe false
    }

    private fun createCompleteApplication(
        characterLevel: Int = 80,
        applicantInfo: ApplicantInfo = ApplicantInfo(25, "USA", "player#1234", "discord123", "DPS"),
        questions: List<ApplicationQuestion> = listOf(
            ApplicationQuestion(0, "Why do you want to join?", "I love raiding"),
            ApplicationQuestion(1, "What is your experience?", "10 years of WoW")
        ),
        altCharacters: List<CharacterInfo> = listOf(
            CharacterInfo("AltChar", "TestRealm", "US", "Mage", "DPS", "Human", "Alliance", 80)
        )
    ): Application {
        val id = ApplicationId(1L)
        val appliedAt = OffsetDateTime.now()
        val mainCharacter = CharacterInfo(
            "TestChar", "TestRealm", "US", "Warrior", "Tank", "Human", "Alliance", characterLevel
        )

        return Application.create(
            id = id,
            appliedAt = appliedAt,
            applicantInfo = applicantInfo,
            mainCharacter = mainCharacter,
            altCharacters = altCharacters,
            questions = questions
        )
    }
}
