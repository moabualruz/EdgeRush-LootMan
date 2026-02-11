package com.edgerush.lootman.domain.application.service

import com.edgerush.datasync.test.base.UnitTest
import com.edgerush.lootman.domain.application.client.EncounterParse
import com.edgerush.lootman.domain.application.client.RaiderIOCharacterProfile
import com.edgerush.lootman.domain.application.client.RaiderIOClient
import com.edgerush.lootman.domain.application.client.RaiderIOGear
import com.edgerush.lootman.domain.application.client.RaiderIOMythicPlusSeasonScore
import com.edgerush.lootman.domain.application.client.RaiderIOScores
import com.edgerush.lootman.domain.application.client.WarcraftLogsClient
import com.edgerush.lootman.domain.application.client.WarcraftLogsParseResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

/**
 * Unit tests for ApplicationDataFetchService.
 *
 * Tests the service that fetches character data from external APIs
 * (Raider.IO and Warcraft Logs) for recruitment applications.
 */
class ApplicationDataFetchServiceTest : UnitTest() {
    private lateinit var raiderIOClient: RaiderIOClient
    private lateinit var warcraftLogsClient: WarcraftLogsClient
    private lateinit var service: ApplicationDataFetchService

    @BeforeEach
    fun setUp() {
        raiderIOClient = mockk()
        warcraftLogsClient = mockk()
        service = ApplicationDataFetchService(raiderIOClient, warcraftLogsClient)
    }

    @Nested
    inner class FetchCharacterDataTests {
        @Test
        fun `should fetch data from both RaiderIO and WarcraftLogs`() {
            // Given
            val region = "us"
            val realm = "Illidan"
            val name = "Arthas"

            every { raiderIOClient.fetchCharacterProfile(region, realm, name) } returns
                Mono.just(
                    createRaiderIOProfile(),
                )
            every { warcraftLogsClient.fetchCharacterParses(region, realm, name) } returns
                Mono.just(
                    createWarcraftLogsResult(),
                )

            // When
            val result = service.fetchCharacterData(region, realm, name)

            // Then
            result shouldNotBe null
            result.characterName shouldBe "Arthas"
            result.itemLevel shouldBe 495.5
            result.raiderIOScore shouldBe 2850.0
            result.bestParseAverage shouldBe 85.5

            verify { raiderIOClient.fetchCharacterProfile(region, realm, name) }
            verify { warcraftLogsClient.fetchCharacterParses(region, realm, name) }
        }

        @Test
        fun `should return data with null parses when WarcraftLogs returns empty`() {
            // Given
            val region = "us"
            val realm = "Illidan"
            val name = "Arthas"

            every { raiderIOClient.fetchCharacterProfile(region, realm, name) } returns
                Mono.just(
                    createRaiderIOProfile(),
                )
            every { warcraftLogsClient.fetchCharacterParses(region, realm, name) } returns Mono.empty()

            // When
            val result = service.fetchCharacterData(region, realm, name)

            // Then
            result shouldNotBe null
            result.characterName shouldBe "Arthas"
            result.itemLevel shouldBe 495.5
            result.raiderIOScore shouldBe 2850.0
            result.bestParseAverage shouldBe null
        }

        @Test
        fun `should return data with null RaiderIO score when RaiderIO has no M+ data`() {
            // Given
            val region = "us"
            val realm = "Illidan"
            val name = "Arthas"

            every { raiderIOClient.fetchCharacterProfile(region, realm, name) } returns
                Mono.just(
                    createRaiderIOProfile(mythicPlusScores = null),
                )
            every { warcraftLogsClient.fetchCharacterParses(region, realm, name) } returns
                Mono.just(
                    createWarcraftLogsResult(),
                )

            // When
            val result = service.fetchCharacterData(region, realm, name)

            // Then
            result shouldNotBe null
            result.raiderIOScore shouldBe null
            result.bestParseAverage shouldBe 85.5
        }

        @Test
        fun `should throw exception when RaiderIO returns not found`() {
            // Given
            val region = "us"
            val realm = "Illidan"
            val name = "NonExistent"

            every { raiderIOClient.fetchCharacterProfile(region, realm, name) } returns Mono.empty()
            every { warcraftLogsClient.fetchCharacterParses(region, realm, name) } returns Mono.empty()

            // When / Then
            val result =
                runCatching {
                    service.fetchCharacterData(region, realm, name)
                }

            result.isFailure shouldBe true
            result.exceptionOrNull()?.message shouldBe "Character not found: $name-$realm-$region"
        }

        @Test
        fun `should continue when WarcraftLogs throws exception`() {
            // Given
            val region = "us"
            val realm = "Illidan"
            val name = "Arthas"

            every { raiderIOClient.fetchCharacterProfile(region, realm, name) } returns
                Mono.just(
                    createRaiderIOProfile(),
                )
            every { warcraftLogsClient.fetchCharacterParses(region, realm, name) } returns
                Mono.error(
                    RuntimeException("WCL API error"),
                )

            // When
            val result = service.fetchCharacterData(region, realm, name)

            // Then - should still return RaiderIO data
            result shouldNotBe null
            result.characterName shouldBe "Arthas"
            result.itemLevel shouldBe 495.5
            result.raiderIOScore shouldBe 2850.0
            result.bestParseAverage shouldBe null // WCL failed, so null
        }
    }

    @Nested
    inner class FetchRaiderIOOnlyTests {
        @Test
        fun `should fetch RaiderIO data only`() {
            // Given
            val region = "us"
            val realm = "Illidan"
            val name = "Arthas"

            every { raiderIOClient.fetchCharacterProfile(region, realm, name) } returns
                Mono.just(
                    createRaiderIOProfile(),
                )

            // When
            val result = service.fetchRaiderIOData(region, realm, name)

            // Then
            result shouldNotBe null
            result?.name shouldBe "Arthas"
            result?.characterClass shouldBe "Death Knight"
            result?.getItemLevel() shouldBe 495.5
            result?.getCurrentMythicPlusScore() shouldBe 2850.0
        }
    }

    @Nested
    inner class FetchWarcraftLogsOnlyTests {
        @Test
        fun `should fetch WarcraftLogs data only`() {
            // Given
            val region = "us"
            val realm = "Illidan"
            val name = "Arthas"

            every { warcraftLogsClient.fetchCharacterParses(region, realm, name) } returns
                Mono.just(
                    createWarcraftLogsResult(),
                )

            // When
            val result = service.fetchWarcraftLogsData(region, realm, name)

            // Then
            result shouldNotBe null
            result?.characterName shouldBe "Arthas"
            result?.bestPerformanceAverage shouldBe 85.5
        }
    }

    // Helper methods

    private fun createRaiderIOProfile(
        mythicPlusScores: List<RaiderIOMythicPlusSeasonScore>? =
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
    ) = RaiderIOCharacterProfile(
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
        gear = RaiderIOGear(itemLevelEquipped = 495.5, itemLevelTotal = 500.0, items = null),
        mythicPlusScoresBySeason = mythicPlusScores,
        raidProgression = null,
    )

    private fun createWarcraftLogsResult() =
        WarcraftLogsParseResult(
            characterName = "Arthas",
            serverName = "Illidan",
            region = "us",
            bestPerformanceAverage = 85.5,
            medianPerformanceAverage = 78.2,
            encounterParses =
                listOf(
                    EncounterParse("Ulgrax", 90.5),
                    EncounterParse("Bloodbound Horror", 82.3),
                ),
        )
}
