package com.edgerush.lootman.domain.application.model

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.shared.GuildId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Unit tests for Application entity.
 */
class ApplicationTest : UnitTest() {
    @Nested
    inner class CreationTests {
        @Test
        fun `should create valid application with all required fields`() {
            // Arrange & Act
            val application =
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
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

            // Assert
            application shouldNotBe null
            application.guildId shouldBe GuildId("test-guild")
            application.battleNetId shouldBe "Player#1234"
            application.discordId shouldBe "123456789012345678"
            application.email shouldBe "player@example.com"
            application.characterName shouldBe "Arthas"
            application.characterRealm shouldBe "Illidan"
            application.characterClass shouldBe "Death Knight"
            application.specialization shouldBe "Frost"
            application.itemLevel shouldBe 489.5
            application.raiderIOScore shouldBe 2850.0
            application.bestParseAverage shouldBe 85.5
            application.age shouldBe 28
            application.location shouldBe "United States"
            application.timezone shouldBe "America/New_York"
            application.raidDaysAvailable shouldBe listOf("Tuesday", "Wednesday", "Thursday")
            application.previousGuilds shouldBe "Previous Guild 1, Previous Guild 2"
            application.reasonForLeaving shouldBe "Guild disbanded"
            application.whyThisGuild shouldBe "Looking for a competitive mythic raiding guild"
            application.status shouldBe ApplicationStatus.PENDING
        }

        @Test
        fun `should generate unique ID on create`() {
            // Arrange & Act
            val app1 = createValidApplication()
            val app2 = createValidApplication()

            // Assert
            app1.id shouldNotBe app2.id
        }

        @Test
        fun `should set createdAt on create`() {
            // Arrange
            val before = Instant.now()

            // Act
            val application = createValidApplication()
            val after = Instant.now()

            // Assert
            (application.createdAt >= before) shouldBe true
            (application.createdAt <= after) shouldBe true
        }

        @Test
        fun `should set updatedAt same as createdAt on create`() {
            // Arrange & Act
            val application = createValidApplication()

            // Assert
            application.updatedAt shouldBe application.createdAt
        }

        @Test
        fun `should set status to PENDING on create`() {
            // Arrange & Act
            val application = createValidApplication()

            // Assert
            application.status shouldBe ApplicationStatus.PENDING
        }

        @Test
        fun `should have null reviewedAt and reviewedBy on create`() {
            // Arrange & Act
            val application = createValidApplication()

            // Assert
            application.reviewedAt shouldBe null
            application.reviewedBy shouldBe null
        }
    }

    @Nested
    inner class ValidationTests {
        @Test
        fun `should throw exception when character name is blank`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "player@example.com",
                    characterName = "",
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
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )
            }.message shouldBe "Character name cannot be blank"
        }

        @Test
        fun `should throw exception when character realm is blank`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "player@example.com",
                    characterName = "Arthas",
                    characterRealm = "",
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
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )
            }.message shouldBe "Character realm cannot be blank"
        }

        @Test
        fun `should throw exception when email is invalid`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "invalid-email",
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
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )
            }.message shouldBe "Invalid email format"
        }

        @Test
        fun `should throw exception when item level is negative`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "player@example.com",
                    characterName = "Arthas",
                    characterRealm = "Illidan",
                    characterClass = "Death Knight",
                    specialization = "Frost",
                    itemLevel = -10.0,
                    raiderIOScore = 2850.0,
                    bestParseAverage = 85.5,
                    age = 28,
                    location = "United States",
                    timezone = "America/New_York",
                    raidDaysAvailable = listOf("Tuesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )
            }.message shouldBe "Item level cannot be negative"
        }

        @Test
        fun `should throw exception when raider IO score is negative`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "player@example.com",
                    characterName = "Arthas",
                    characterRealm = "Illidan",
                    characterClass = "Death Knight",
                    specialization = "Frost",
                    itemLevel = 489.5,
                    raiderIOScore = -100.0,
                    bestParseAverage = 85.5,
                    age = 28,
                    location = "United States",
                    timezone = "America/New_York",
                    raidDaysAvailable = listOf("Tuesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )
            }.message shouldBe "Raider.IO score cannot be negative"
        }

        @Test
        fun `should throw exception when best parse average is out of range`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "player@example.com",
                    characterName = "Arthas",
                    characterRealm = "Illidan",
                    characterClass = "Death Knight",
                    specialization = "Frost",
                    itemLevel = 489.5,
                    raiderIOScore = 2850.0,
                    bestParseAverage = 150.0,
                    age = 28,
                    location = "United States",
                    timezone = "America/New_York",
                    raidDaysAvailable = listOf("Tuesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )
            }.message shouldBe "Best parse average must be between 0 and 100"
        }

        @Test
        fun `should throw exception when age is too young`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "player@example.com",
                    characterName = "Arthas",
                    characterRealm = "Illidan",
                    characterClass = "Death Knight",
                    specialization = "Frost",
                    itemLevel = 489.5,
                    raiderIOScore = 2850.0,
                    bestParseAverage = 85.5,
                    age = 12,
                    location = "United States",
                    timezone = "America/New_York",
                    raidDaysAvailable = listOf("Tuesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )
            }.message shouldBe "Applicant must be at least 18 years old"
        }

        @Test
        fun `should throw exception when raid days available is empty`() {
            // Arrange, Act & Assert
            shouldThrow<IllegalArgumentException> {
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
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
                    raidDaysAvailable = emptyList(),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )
            }.message shouldBe "At least one raid day must be available"
        }
    }

    @Nested
    inner class StatusTransitionTests {
        @Test
        fun `should approve application`() {
            // Arrange
            val application = createValidApplication()
            val reviewerId = "officer-123"

            // Act
            val approved = application.approve(reviewerId)

            // Assert
            approved.status shouldBe ApplicationStatus.APPROVED
            approved.reviewedBy shouldBe reviewerId
            approved.reviewedAt shouldNotBe null
        }

        @Test
        fun `should reject application`() {
            // Arrange
            val application = createValidApplication()
            val reviewerId = "officer-123"

            // Act
            val rejected = application.reject(reviewerId)

            // Assert
            rejected.status shouldBe ApplicationStatus.REJECTED
            rejected.reviewedBy shouldBe reviewerId
            rejected.reviewedAt shouldNotBe null
        }

        @Test
        fun `should withdraw application`() {
            // Arrange
            val application = createValidApplication()

            // Act
            val withdrawn = application.withdraw()

            // Assert
            withdrawn.status shouldBe ApplicationStatus.WITHDRAWN
        }

        @Test
        fun `should mark application as under review`() {
            // Arrange
            val application = createValidApplication()
            val reviewerId = "officer-123"

            // Act
            val underReview = application.startReview(reviewerId)

            // Assert
            underReview.status shouldBe ApplicationStatus.UNDER_REVIEW
            underReview.reviewedBy shouldBe reviewerId
        }

        @Test
        fun `should throw exception when approving non-pending or non-under-review application`() {
            // Arrange
            val application = createValidApplication().reject("officer-123")

            // Act & Assert
            shouldThrow<IllegalStateException> {
                application.approve("officer-456")
            }.message shouldBe "Can only approve PENDING or UNDER_REVIEW applications"
        }

        @Test
        fun `should throw exception when rejecting non-pending or non-under-review application`() {
            // Arrange
            val application = createValidApplication().approve("officer-123")

            // Act & Assert
            shouldThrow<IllegalStateException> {
                application.reject("officer-456")
            }.message shouldBe "Can only reject PENDING or UNDER_REVIEW applications"
        }

        @Test
        fun `should throw exception when withdrawing already reviewed application`() {
            // Arrange
            val application = createValidApplication().approve("officer-123")

            // Act & Assert
            shouldThrow<IllegalStateException> {
                application.withdraw()
            }.message shouldBe "Cannot withdraw an already reviewed application"
        }
    }

    @Nested
    inner class OptionalFieldsTests {
        @Test
        fun `should allow null raider IO score`() {
            // Arrange & Act
            val application =
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "player@example.com",
                    characterName = "Arthas",
                    characterRealm = "Illidan",
                    characterClass = "Death Knight",
                    specialization = "Frost",
                    itemLevel = 489.5,
                    raiderIOScore = null,
                    bestParseAverage = null,
                    age = 28,
                    location = "United States",
                    timezone = "America/New_York",
                    raidDaysAvailable = listOf("Tuesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )

            // Assert
            application.raiderIOScore shouldBe null
            application.bestParseAverage shouldBe null
        }

        @Test
        fun `should allow zero item level for fresh characters`() {
            // Arrange & Act
            val application =
                Application.create(
                    guildId = GuildId("test-guild"),
                    battleNetId = "Player#1234",
                    discordId = "123456789012345678",
                    email = "player@example.com",
                    characterName = "Arthas",
                    characterRealm = "Illidan",
                    characterClass = "Death Knight",
                    specialization = "Frost",
                    itemLevel = 0.0,
                    raiderIOScore = null,
                    bestParseAverage = null,
                    age = 28,
                    location = "United States",
                    timezone = "America/New_York",
                    raidDaysAvailable = listOf("Tuesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )

            // Assert
            application.itemLevel shouldBe 0.0
        }
    }

    private fun createValidApplication(): Application =
        Application.create(
            guildId = GuildId("test-guild"),
            battleNetId = "Player#1234",
            discordId = "123456789012345678",
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
