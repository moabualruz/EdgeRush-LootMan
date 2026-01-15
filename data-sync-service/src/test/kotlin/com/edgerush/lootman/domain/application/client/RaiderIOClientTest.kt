package com.edgerush.lootman.domain.application.client

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.StepVerifier

/**
 * Unit tests for RaiderIOClient.
 */
class RaiderIOClientTest : UnitTest() {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: RaiderIOClient

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val webClientBuilder = WebClient.builder()

        client = RaiderIOClient(webClientBuilder, mockWebServer.url("/").toString())
    }

    @AfterEach
    fun tearDownMockServer() {
        mockWebServer.shutdown()
    }

    @Nested
    inner class FetchCharacterProfileTests {

        @Test
        fun `should fetch character profile successfully`() {
            // Arrange
            val responseJson = """
                {
                    "name": "Arthas",
                    "race": "Human",
                    "class": "Death Knight",
                    "active_spec_name": "Frost",
                    "active_spec_role": "DPS",
                    "gender": "male",
                    "faction": "alliance",
                    "region": "us",
                    "realm": "Illidan",
                    "profile_url": "https://raider.io/characters/us/illidan/Arthas",
                    "gear": {
                        "item_level_equipped": 489.5,
                        "item_level_total": 495.0
                    },
                    "mythic_plus_scores_by_season": [
                        {
                            "season": "season-tww-1",
                            "scores": {
                                "all": 2850.5,
                                "dps": 2800.0,
                                "healer": 0,
                                "tank": 0
                            }
                        }
                    ],
                    "raid_progression": {
                        "nerubar-palace": {
                            "summary": "8/8 M",
                            "total_bosses": 8,
                            "normal_bosses_killed": 8,
                            "heroic_bosses_killed": 8,
                            "mythic_bosses_killed": 8
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseJson)
            )

            // Act
            val result = client.fetchCharacterProfile("us", "Illidan", "Arthas")

            // Assert
            StepVerifier.create(result)
                .assertNext { profile ->
                    profile.name shouldBe "Arthas"
                    profile.characterClass shouldBe "Death Knight"
                    profile.activeSpecName shouldBe "Frost"
                    profile.getItemLevel() shouldBe 489.5
                    profile.getCurrentMythicPlusScore() shouldBe 2850.5
                }
                .verifyComplete()
        }

        @Test
        fun `should throw exception when character not found`() {
            // Arrange
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(400)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody("""{"statusCode": 400, "error": "Bad Request", "message": "Character not found"}""")
            )

            // Act
            val result = client.fetchCharacterProfile("us", "InvalidRealm", "NonExistentChar")

            // Assert
            StepVerifier.create(result)
                .expectError(RaiderIONotFoundException::class.java)
                .verify()
        }

        @Test
        fun `should throw exception on server error`() {
            // Arrange
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(500)
                    .setBody("Internal Server Error")
            )

            // Act
            val result = client.fetchCharacterProfile("us", "Illidan", "Arthas")

            // Assert
            StepVerifier.create(result)
                .expectError(RaiderIOServerException::class.java)
                .verify()
        }

        @Test
        fun `should normalize realm name with spaces`() {
            // Arrange
            val responseJson = """
                {
                    "name": "Test",
                    "race": "Human",
                    "class": "Warrior",
                    "gender": "male",
                    "faction": "alliance",
                    "region": "us",
                    "realm": "Area 52",
                    "profile_url": "https://raider.io/characters/us/area-52/Test"
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseJson)
            )

            // Act
            val result = client.fetchCharacterProfile("us", "Area 52", "Test")

            // Assert
            StepVerifier.create(result)
                .assertNext { profile ->
                    profile shouldNotBe null
                }
                .verifyComplete()

            // Verify the request used normalized realm name
            val request = mockWebServer.takeRequest()
            request.path shouldBe "/characters/profile?region=us&realm=area-52&name=Test&fields=mythic_plus_scores_by_season:current,raid_progression,gear"
        }
    }

    @Nested
    inner class CharacterProfileDataTests {

        @Test
        fun `should get current mythic plus score from profile`() {
            // Arrange
            val profile = RaiderIOCharacterProfile(
                name = "Test",
                race = "Human",
                characterClass = "Warrior",
                activeSpecName = "Arms",
                activeSpecRole = "DPS",
                gender = "male",
                faction = "alliance",
                region = "us",
                realm = "Illidan",
                profileUrl = "https://raider.io/characters/us/illidan/Test",
                gear = RaiderIOGear(itemLevelEquipped = 489.5, itemLevelTotal = 495.0),
                mythicPlusScoresBySeason = listOf(
                    RaiderIOMythicPlusSeasonScore(
                        season = "season-tww-1",
                        scores = RaiderIOScores(all = 2850.5, dps = 2800.0, healer = 0.0, tank = 0.0, spec0 = null, spec1 = null, spec2 = null, spec3 = null)
                    )
                ),
                raidProgression = null
            )

            // Act
            val score = profile.getCurrentMythicPlusScore()

            // Assert
            score shouldBe 2850.5
        }

        @Test
        fun `should return null when no mythic plus scores`() {
            // Arrange
            val profile = RaiderIOCharacterProfile(
                name = "Test",
                race = "Human",
                characterClass = "Warrior",
                activeSpecName = "Arms",
                activeSpecRole = "DPS",
                gender = "male",
                faction = "alliance",
                region = "us",
                realm = "Illidan",
                profileUrl = "https://raider.io/characters/us/illidan/Test",
                gear = null,
                mythicPlusScoresBySeason = null,
                raidProgression = null
            )

            // Act
            val score = profile.getCurrentMythicPlusScore()

            // Assert
            score shouldBe null
        }

        @Test
        fun `should get item level from gear`() {
            // Arrange
            val profile = RaiderIOCharacterProfile(
                name = "Test",
                race = "Human",
                characterClass = "Warrior",
                activeSpecName = "Arms",
                activeSpecRole = "DPS",
                gender = "male",
                faction = "alliance",
                region = "us",
                realm = "Illidan",
                profileUrl = "https://raider.io/characters/us/illidan/Test",
                gear = RaiderIOGear(itemLevelEquipped = 489.5, itemLevelTotal = 495.0),
                mythicPlusScoresBySeason = null,
                raidProgression = null
            )

            // Act
            val itemLevel = profile.getItemLevel()

            // Assert
            itemLevel shouldBe 489.5
        }
    }
}
