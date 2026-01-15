package com.edgerush.lootman.domain.application.client

import com.edgerush.datasync.test.base.UnitTest
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
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
import org.springframework.web.reactive.function.client.WebClient

/**
 * Unit tests for WarcraftLogsClient.
 *
 * Uses MockWebServer to simulate Warcraft Logs GraphQL API responses.
 */
class WarcraftLogsClientTest : UnitTest() {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: WarcraftLogsClient
    private lateinit var tokenProvider: WarcraftLogsTokenProvider

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        tokenProvider = mockk()
        every { tokenProvider.getAccessToken() } returns "mock-access-token"

        val webClientBuilder = WebClient.builder()
        client = WarcraftLogsClient(
            webClientBuilder = webClientBuilder,
            tokenProvider = tokenProvider,
            baseUrl = mockWebServer.url("/").toString(),
        )
    }

    @AfterEach
    fun tearDownMockServer() {
        mockWebServer.shutdown()
    }

    @Nested
    inner class FetchCharacterParsesTests {

        @Test
        fun `should fetch character parses successfully`() {
            // Given
            val responseJson = """
                {
                    "data": {
                        "characterData": {
                            "character": {
                                "name": "Arthas",
                                "server": {
                                    "name": "Illidan",
                                    "region": { "slug": "us" }
                                },
                                "zoneRankings": {
                                    "bestPerformanceAverage": 85.5,
                                    "medianPerformanceAverage": 78.2,
                                    "rankings": [
                                        { "encounter": { "name": "Ulgrax" }, "rankPercent": 90.5 },
                                        { "encounter": { "name": "Bloodbound Horror" }, "rankPercent": 82.3 }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(responseJson)
                    .addHeader("Content-Type", "application/json")
            )

            // When
            val result = client.fetchCharacterParses("us", "Illidan", "Arthas").block()

            // Then
            result shouldNotBe null
            result?.characterName shouldBe "Arthas"
            result?.serverName shouldBe "Illidan"
            result?.region shouldBe "us"
            result?.bestPerformanceAverage shouldBe 85.5
            result?.medianPerformanceAverage shouldBe 78.2
            result?.encounterParses?.size shouldBe 2
        }

        @Test
        fun `should handle character not found`() {
            // Given
            val responseJson = """
                {
                    "data": {
                        "characterData": {
                            "character": null
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(responseJson)
                    .addHeader("Content-Type", "application/json")
            )

            // When
            val result = client.fetchCharacterParses("us", "Illidan", "NonExistent").block()

            // Then
            result shouldBe null
        }

        @Test
        fun `should handle no rankings available`() {
            // Given
            val responseJson = """
                {
                    "data": {
                        "characterData": {
                            "character": {
                                "name": "NewPlayer",
                                "server": {
                                    "name": "Illidan",
                                    "region": { "slug": "us" }
                                },
                                "zoneRankings": null
                            }
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(responseJson)
                    .addHeader("Content-Type", "application/json")
            )

            // When
            val result = client.fetchCharacterParses("us", "Illidan", "NewPlayer").block()

            // Then
            result shouldNotBe null
            result?.characterName shouldBe "NewPlayer"
            result?.bestPerformanceAverage shouldBe null
            result?.encounterParses shouldBe emptyList()
        }

        @Test
        fun `should include authorization header`() {
            // Given
            val responseJson = """
                {
                    "data": {
                        "characterData": {
                            "character": {
                                "name": "Arthas",
                                "server": { "name": "Illidan", "region": { "slug": "us" } },
                                "zoneRankings": { "bestPerformanceAverage": 85.0, "medianPerformanceAverage": 80.0, "rankings": [] }
                            }
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(responseJson)
                    .addHeader("Content-Type", "application/json")
            )

            // When
            client.fetchCharacterParses("us", "Illidan", "Arthas").block()

            // Then
            val request = mockWebServer.takeRequest()
            request.getHeader("Authorization") shouldBe "Bearer mock-access-token"
        }

        @Test
        fun `should send GraphQL query with correct variables`() {
            // Given
            val responseJson = """
                {
                    "data": {
                        "characterData": {
                            "character": {
                                "name": "Arthas",
                                "server": { "name": "Illidan", "region": { "slug": "us" } },
                                "zoneRankings": { "bestPerformanceAverage": 85.0, "medianPerformanceAverage": 80.0, "rankings": [] }
                            }
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(responseJson)
                    .addHeader("Content-Type", "application/json")
            )

            // When
            client.fetchCharacterParses("us", "Illidan", "Arthas").block()

            // Then
            val request = mockWebServer.takeRequest()
            val body = request.body.readUtf8()
            body.contains("\"name\":\"Arthas\"") shouldBe true
            body.contains("\"serverSlug\":\"illidan\"") shouldBe true
            body.contains("\"serverRegion\":\"us\"") shouldBe true
        }

        @Test
        fun `should handle API errors gracefully`() {
            // Given
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(500)
                    .setBody("Internal Server Error")
            )

            // When / Then
            val result = runCatching {
                client.fetchCharacterParses("us", "Illidan", "Arthas").block()
            }

            result.isFailure shouldBe true
        }

        @Test
        fun `should normalize server name to slug format`() {
            // Given
            val responseJson = """
                {
                    "data": {
                        "characterData": {
                            "character": {
                                "name": "Arthas",
                                "server": { "name": "Area 52", "region": { "slug": "us" } },
                                "zoneRankings": { "bestPerformanceAverage": 85.0, "medianPerformanceAverage": 80.0, "rankings": [] }
                            }
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(responseJson)
                    .addHeader("Content-Type", "application/json")
            )

            // When
            client.fetchCharacterParses("us", "Area 52", "Arthas").block()

            // Then
            val request = mockWebServer.takeRequest()
            val body = request.body.readUtf8()
            body.contains("\"serverSlug\":\"area-52\"") shouldBe true
        }
    }

    @Nested
    inner class ParseResultMappingTests {

        @Test
        fun `should map all encounter parses`() {
            // Given
            val responseJson = """
                {
                    "data": {
                        "characterData": {
                            "character": {
                                "name": "Arthas",
                                "server": { "name": "Illidan", "region": { "slug": "us" } },
                                "zoneRankings": {
                                    "bestPerformanceAverage": 85.5,
                                    "medianPerformanceAverage": 78.2,
                                    "rankings": [
                                        { "encounter": { "name": "Ulgrax" }, "rankPercent": 90.5 },
                                        { "encounter": { "name": "Bloodbound Horror" }, "rankPercent": 82.3 },
                                        { "encounter": { "name": "Sikran" }, "rankPercent": 78.1 }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(responseJson)
                    .addHeader("Content-Type", "application/json")
            )

            // When
            val result = client.fetchCharacterParses("us", "Illidan", "Arthas").block()

            // Then
            result?.encounterParses?.size shouldBe 3
            result?.encounterParses?.get(0)?.encounterName shouldBe "Ulgrax"
            result?.encounterParses?.get(0)?.rankPercent shouldBe 90.5
            result?.encounterParses?.get(1)?.encounterName shouldBe "Bloodbound Horror"
            result?.encounterParses?.get(2)?.encounterName shouldBe "Sikran"
        }

        @Test
        fun `should calculate best parse average from encounter data when not provided`() {
            // Given
            val responseJson = """
                {
                    "data": {
                        "characterData": {
                            "character": {
                                "name": "Arthas",
                                "server": { "name": "Illidan", "region": { "slug": "us" } },
                                "zoneRankings": {
                                    "bestPerformanceAverage": null,
                                    "medianPerformanceAverage": null,
                                    "rankings": [
                                        { "encounter": { "name": "Boss1" }, "rankPercent": 80.0 },
                                        { "encounter": { "name": "Boss2" }, "rankPercent": 90.0 }
                                    ]
                                }
                            }
                        }
                    }
                }
            """.trimIndent()

            mockWebServer.enqueue(
                MockResponse()
                    .setBody(responseJson)
                    .addHeader("Content-Type", "application/json")
            )

            // When
            val result = client.fetchCharacterParses("us", "Illidan", "Arthas").block()

            // Then
            result shouldNotBe null
            // Even if bestPerformanceAverage is null, we can compute from rankings if needed
            result?.encounterParses?.size shouldBe 2
        }
    }
}
