package com.edgerush.lootman.domain.application.service

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.application.model.Application
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.application.model.ApplicationStatus
import com.edgerush.lootman.domain.application.repository.EnhancedApplicationRepository
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for ApplicationService domain service.
 */
class ApplicationServiceTest : UnitTest() {
    @MockK
    private lateinit var repository: EnhancedApplicationRepository

    @InjectMockKs
    private lateinit var service: ApplicationService

    @Nested
    inner class SubmitApplicationTests {
        @Test
        fun `should submit new application successfully`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val discordId = "123456789012345678"
            val battleNetId = "Player#1234"

            every { repository.findByGuildIdAndDiscordId(guildId, discordId) } returns null
            every { repository.findByGuildIdAndBattleNetId(guildId, battleNetId) } returns null
            every { repository.save(any()) } answers { firstArg() }

            // Act
            val result =
                service.submitApplication(
                    guildId = guildId,
                    battleNetId = battleNetId,
                    discordId = discordId,
                    email = "player@example.com",
                    characterName = "Arthas",
                    characterRealm = "Illidan",
                    characterClass = "Death Knight",
                    specialization = "Frost",
                    itemLevel = 489.5,
                    raiderIOScore = 2850.0,
                    bestParseAverage = 85.5,
                    age = 28,
                    location = "United States",
                    timezone = "America/New_York",
                    raidDaysAvailable = listOf("Tuesday", "Wednesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Guild disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )

            // Assert
            result.status shouldBe ApplicationStatus.PENDING
            result.characterName shouldBe "Arthas"
            verify(exactly = 1) { repository.save(any()) }
        }

        @Test
        fun `should throw exception when Discord ID already has pending application`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val discordId = "123456789012345678"
            val existingApplication = createValidApplication(guildId, discordId = discordId)

            every { repository.findByGuildIdAndDiscordId(guildId, discordId) } returns existingApplication

            // Act & Assert
            val exception =
                shouldThrow<IllegalStateException> {
                    service.submitApplication(
                        guildId = guildId,
                        battleNetId = "Player#1234",
                        discordId = discordId,
                        email = "player@example.com",
                        characterName = "Arthas",
                        characterRealm = "Illidan",
                        characterClass = "Death Knight",
                        specialization = "Frost",
                        itemLevel = 489.5,
                        raiderIOScore = 2850.0,
                        bestParseAverage = 85.5,
                        age = 28,
                        location = "United States",
                        timezone = "America/New_York",
                        raidDaysAvailable = listOf("Tuesday"),
                        previousGuilds = "Previous Guild",
                        reasonForLeaving = "Guild disbanded",
                        whyThisGuild = "Looking for competitive guild",
                    )
                }
            exception.message shouldBe "An application already exists for this Discord account"
        }

        @Test
        fun `should throw exception when BattleNet ID already has pending application`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val battleNetId = "Player#1234"
            val existingApplication = createValidApplication(guildId, battleNetId = battleNetId)

            every { repository.findByGuildIdAndDiscordId(any(), any()) } returns null
            every { repository.findByGuildIdAndBattleNetId(guildId, battleNetId) } returns existingApplication

            // Act & Assert
            val exception =
                shouldThrow<IllegalStateException> {
                    service.submitApplication(
                        guildId = guildId,
                        battleNetId = battleNetId,
                        discordId = "999999999999999999",
                        email = "player@example.com",
                        characterName = "Arthas",
                        characterRealm = "Illidan",
                        characterClass = "Death Knight",
                        specialization = "Frost",
                        itemLevel = 489.5,
                        raiderIOScore = 2850.0,
                        bestParseAverage = 85.5,
                        age = 28,
                        location = "United States",
                        timezone = "America/New_York",
                        raidDaysAvailable = listOf("Tuesday"),
                        previousGuilds = "Previous Guild",
                        reasonForLeaving = "Guild disbanded",
                        whyThisGuild = "Looking for competitive guild",
                    )
                }
            exception.message shouldBe "An application already exists for this Battle.net account"
        }
    }

    @Nested
    inner class ReviewApplicationTests {
        @Test
        fun `should start review of pending application`() {
            // Arrange
            val applicationId = ApplicationId.generate()
            val application = createValidApplication(GuildId("test-guild"))
            val reviewerId = "officer-123"

            every { repository.findById(applicationId) } returns application
            every { repository.save(any()) } answers { firstArg() }

            // Act
            val result = service.startReview(applicationId, reviewerId)

            // Assert
            result.status shouldBe ApplicationStatus.UNDER_REVIEW
            result.reviewedBy shouldBe reviewerId
        }

        @Test
        fun `should approve application`() {
            // Arrange
            val applicationId = ApplicationId.generate()
            val application = createValidApplication(GuildId("test-guild"))
            val reviewerId = "officer-123"

            every { repository.findById(applicationId) } returns application
            every { repository.save(any()) } answers { firstArg() }

            // Act
            val result = service.approveApplication(applicationId, reviewerId)

            // Assert
            result.status shouldBe ApplicationStatus.APPROVED
            result.reviewedBy shouldBe reviewerId
            result.reviewedAt shouldNotBe null
        }

        @Test
        fun `should reject application`() {
            // Arrange
            val applicationId = ApplicationId.generate()
            val application = createValidApplication(GuildId("test-guild"))
            val reviewerId = "officer-123"

            every { repository.findById(applicationId) } returns application
            every { repository.save(any()) } answers { firstArg() }

            // Act
            val result = service.rejectApplication(applicationId, reviewerId)

            // Assert
            result.status shouldBe ApplicationStatus.REJECTED
            result.reviewedBy shouldBe reviewerId
            result.reviewedAt shouldNotBe null
        }

        @Test
        fun `should throw exception when approving non-existent application`() {
            // Arrange
            val applicationId = ApplicationId.generate()

            every { repository.findById(applicationId) } returns null

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    service.approveApplication(applicationId, "officer-123")
                }
            exception.message shouldBe "Application not found: ${applicationId.value}"
        }
    }

    @Nested
    inner class WithdrawApplicationTests {
        @Test
        fun `should withdraw pending application`() {
            // Arrange
            val applicationId = ApplicationId.generate()
            val application = createValidApplication(GuildId("test-guild"))

            every { repository.findById(applicationId) } returns application
            every { repository.save(any()) } answers { firstArg() }

            // Act
            val result = service.withdrawApplication(applicationId)

            // Assert
            result.status shouldBe ApplicationStatus.WITHDRAWN
        }

        @Test
        fun `should throw exception when withdrawing non-existent application`() {
            // Arrange
            val applicationId = ApplicationId.generate()

            every { repository.findById(applicationId) } returns null

            // Act & Assert
            val exception =
                shouldThrow<IllegalArgumentException> {
                    service.withdrawApplication(applicationId)
                }
            exception.message shouldBe "Application not found: ${applicationId.value}"
        }
    }

    @Nested
    inner class GetApplicationsTests {
        @Test
        fun `should get applications by guild`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val applications =
                listOf(
                    createValidApplication(guildId),
                    createValidApplication(guildId),
                )

            every { repository.findByGuildId(guildId, 0, 50) } returns applications

            // Act
            val result = service.getApplicationsByGuild(guildId)

            // Assert
            result.size shouldBe 2
        }

        @Test
        fun `should get pending applications by guild`() {
            // Arrange
            val guildId = GuildId("test-guild")
            val applications = listOf(createValidApplication(guildId))

            every { repository.findByGuildIdAndStatus(guildId, ApplicationStatus.PENDING, 0, 50) } returns applications

            // Act
            val result = service.getPendingApplications(guildId)

            // Assert
            result.size shouldBe 1
        }

        @Test
        fun `should get application by ID`() {
            // Arrange
            val applicationId = ApplicationId.generate()
            val application = createValidApplication(GuildId("test-guild"))

            every { repository.findById(applicationId) } returns application

            // Act
            val result = service.getApplicationById(applicationId)

            // Assert
            result shouldNotBe null
            result?.characterName shouldBe "Arthas"
        }

        @Test
        fun `should return null when application not found`() {
            // Arrange
            val applicationId = ApplicationId.generate()

            every { repository.findById(applicationId) } returns null

            // Act
            val result = service.getApplicationById(applicationId)

            // Assert
            result shouldBe null
        }
    }

    private fun createValidApplication(
        guildId: GuildId,
        discordId: String = "123456789012345678",
        battleNetId: String = "Player#1234",
    ): Application =
        Application.create(
            guildId = guildId,
            battleNetId = battleNetId,
            discordId = discordId,
            email = "player@example.com",
            characterName = "Arthas",
            characterRealm = "Illidan",
            characterClass = "Death Knight",
            specialization = "Frost",
            itemLevel = 489.5,
            raiderIOScore = 2850.0,
            bestParseAverage = 85.5,
            age = 28,
            location = "United States",
            timezone = "America/New_York",
            raidDaysAvailable = listOf("Tuesday", "Wednesday", "Thursday"),
            previousGuilds = "Previous Guild 1, Previous Guild 2",
            reasonForLeaving = "Guild disbanded",
            whyThisGuild = "Looking for a competitive mythic raiding guild",
        )
}
