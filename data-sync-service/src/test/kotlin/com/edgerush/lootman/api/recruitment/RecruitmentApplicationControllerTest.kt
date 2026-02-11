package com.edgerush.lootman.api.recruitment

import com.edgerush.lootman.domain.application.client.RaiderIOCharacterProfile
import com.edgerush.lootman.domain.application.client.RaiderIOClient
import com.edgerush.lootman.domain.application.client.RaiderIOGear
import com.edgerush.lootman.domain.application.client.RaiderIOMythicPlusSeasonScore
import com.edgerush.lootman.domain.application.client.RaiderIOScores
import com.edgerush.lootman.domain.application.model.Application
import com.edgerush.lootman.domain.application.model.ApplicationId
import com.edgerush.lootman.domain.application.model.ApplicationStatus
import com.edgerush.lootman.domain.application.service.ApplicationDataFetchService
import com.edgerush.lootman.domain.application.service.ApplicationService
import com.edgerush.lootman.domain.shared.GuildId
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import reactor.core.publisher.Mono

/**
 * Unit tests for RecruitmentApplicationController.
 */
class RecruitmentApplicationControllerTest {
    private lateinit var mockMvc: MockMvc
    private lateinit var applicationService: ApplicationService
    private lateinit var raiderIOClient: RaiderIOClient
    private lateinit var dataFetchService: ApplicationDataFetchService
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        applicationService = mockk()
        raiderIOClient = mockk()
        dataFetchService = mockk()
        objectMapper = ObjectMapper().findAndRegisterModules()

        val controller = RecruitmentApplicationController(applicationService, raiderIOClient, dataFetchService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Nested
    inner class GetApplicationsByGuildTests {
        @Test
        fun `should return applications for a guild`() {
            // Arrange
            val guildId = "test-guild"
            val applications = listOf(createValidApplication())

            every { applicationService.getApplicationsByGuild(GuildId(guildId), 0, 50) } returns applications

            // Act & Assert
            mockMvc.perform(get("/api/v1/recruitment/applications/guilds/$guildId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].characterName").value("Arthas"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
        }

        @Test
        fun `should filter applications by status`() {
            // Arrange
            val guildId = "test-guild"
            val applications = listOf(createValidApplication())

            every {
                applicationService.getApplicationsByStatus(GuildId(guildId), ApplicationStatus.PENDING, 0, 50)
            } returns applications

            // Act & Assert
            mockMvc.perform(
                get("/api/v1/recruitment/applications/guilds/$guildId")
                    .param("status", "PENDING"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].status").value("PENDING"))
        }
    }

    @Nested
    inner class GetApplicationByIdTests {
        @Test
        fun `should return application when found`() {
            // Arrange
            val applicationId = "app-123"
            val application = createValidApplication()

            every { applicationService.getApplicationById(ApplicationId(applicationId)) } returns application

            // Act & Assert
            mockMvc.perform(get("/api/v1/recruitment/applications/$applicationId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.characterName").value("Arthas"))
        }

        @Test
        fun `should return 404 when application not found`() {
            // Arrange
            val applicationId = "non-existent"

            every { applicationService.getApplicationById(ApplicationId(applicationId)) } returns null

            // Act & Assert
            mockMvc.perform(get("/api/v1/recruitment/applications/$applicationId"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class SubmitApplicationTests {
        @Test
        fun `should create application successfully`() {
            // Arrange
            val guildId = "test-guild"
            val application = createValidApplication()

            every {
                applicationService.submitApplication(
                    guildId = GuildId(guildId),
                    battleNetId = any(),
                    discordId = any(),
                    email = any(),
                    characterName = any(),
                    characterRealm = any(),
                    characterClass = any(),
                    specialization = any(),
                    itemLevel = any(),
                    raiderIOScore = any(),
                    bestParseAverage = any(),
                    age = any(),
                    location = any(),
                    timezone = any(),
                    raidDaysAvailable = any(),
                    previousGuilds = any(),
                    reasonForLeaving = any(),
                    whyThisGuild = any(),
                )
            } returns application

            val request =
                SubmitApplicationRequest(
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
                    raidDaysAvailable = listOf("Tuesday", "Wednesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Guild disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )

            // Act & Assert
            mockMvc.perform(
                post("/api/v1/recruitment/applications/guilds/$guildId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.characterName").value("Arthas"))
        }

        @Test
        fun `should return 409 when application already exists`() {
            // Arrange
            val guildId = "test-guild"

            every {
                applicationService.submitApplication(
                    guildId = GuildId(guildId),
                    battleNetId = any(),
                    discordId = any(),
                    email = any(),
                    characterName = any(),
                    characterRealm = any(),
                    characterClass = any(),
                    specialization = any(),
                    itemLevel = any(),
                    raiderIOScore = any(),
                    bestParseAverage = any(),
                    age = any(),
                    location = any(),
                    timezone = any(),
                    raidDaysAvailable = any(),
                    previousGuilds = any(),
                    reasonForLeaving = any(),
                    whyThisGuild = any(),
                )
            } throws IllegalStateException("Application already exists")

            val request =
                SubmitApplicationRequest(
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
                    raidDaysAvailable = listOf("Tuesday"),
                    previousGuilds = "Previous Guild",
                    reasonForLeaving = "Guild disbanded",
                    whyThisGuild = "Looking for competitive guild",
                )

            // Act & Assert
            mockMvc.perform(
                post("/api/v1/recruitment/applications/guilds/$guildId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isConflict)
        }
    }

    @Nested
    inner class ReviewApplicationTests {
        @Test
        fun `should approve application`() {
            // Arrange
            val applicationId = "app-123"
            val application = createValidApplication().approve("officer-123")

            every {
                applicationService.approveApplication(ApplicationId(applicationId), "officer-123")
            } returns application

            val request = ReviewRequest(reviewerId = "officer-123")

            // Act & Assert
            mockMvc.perform(
                put("/api/v1/recruitment/applications/$applicationId/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("APPROVED"))
        }

        @Test
        fun `should reject application`() {
            // Arrange
            val applicationId = "app-123"
            val application = createValidApplication().reject("officer-123")

            every {
                applicationService.rejectApplication(ApplicationId(applicationId), "officer-123")
            } returns application

            val request = ReviewRequest(reviewerId = "officer-123")

            // Act & Assert
            mockMvc.perform(
                put("/api/v1/recruitment/applications/$applicationId/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("REJECTED"))
        }

        @Test
        fun `should return 404 when approving non-existent application`() {
            // Arrange
            val applicationId = "non-existent"

            every {
                applicationService.approveApplication(ApplicationId(applicationId), "officer-123")
            } throws IllegalArgumentException("Application not found")

            val request = ReviewRequest(reviewerId = "officer-123")

            // Act & Assert
            mockMvc.perform(
                put("/api/v1/recruitment/applications/$applicationId/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    inner class CharacterLookupTests {
        @Test
        fun `should fetch character data from RaiderIO`() {
            // Arrange
            val profile =
                RaiderIOCharacterProfile(
                    name = "Arthas",
                    race = "Human",
                    characterClass = "Death Knight",
                    activeSpecName = "Frost",
                    activeSpecRole = "DPS",
                    gender = "male",
                    faction = "alliance",
                    region = "us",
                    realm = "Illidan",
                    profileUrl = "https://raider.io/characters/us/illidan/Arthas",
                    gear = RaiderIOGear(itemLevelEquipped = 489.5, itemLevelTotal = 495.0, items = null),
                    mythicPlusScoresBySeason =
                        listOf(
                            RaiderIOMythicPlusSeasonScore(
                                season = "season-tww-1",
                                scores =
                                    RaiderIOScores(
                                        all = 2850.0,
                                        dps = 2800.0,
                                        healer = 0.0,
                                        tank = 0.0,
                                        spec0 = null,
                                        spec1 = null,
                                        spec2 = null,
                                        spec3 = null,
                                    ),
                            ),
                        ),
                    raidProgression = null,
                )

            every {
                raiderIOClient.fetchCharacterProfile("us", "Illidan", "Arthas")
            } returns Mono.just(profile)

            // Act & Assert
            mockMvc.perform(
                get("/api/v1/recruitment/applications/character-lookup")
                    .param("region", "us")
                    .param("realm", "Illidan")
                    .param("name", "Arthas"),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Arthas"))
                .andExpect(jsonPath("$.characterClass").value("Death Knight"))
                .andExpect(jsonPath("$.itemLevel").value(489.5))
                .andExpect(jsonPath("$.raiderIOScore").value(2850.0))
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
