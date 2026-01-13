package com.edgerush.lootman.e2e

import com.edgerush.datasync.test.base.IntegrationTest
import com.edgerush.lootman.application.simulation.SimulationService
import com.edgerush.lootman.application.simulation.UpgradeValueCalculator
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.Instant

/**
 * End-to-End Workflow Tests for SimulationCraft Integration.
 *
 * These tests verify complete user workflows:
 * - From HTTP request to database persistence and back
 * - Full simulation submission → status check → results retrieval flow
 * - Integration with FLPS calculation (UpgradeValueCalculator)
 * - Multi-character scenarios
 * - Guild-scoped isolation
 *
 * Note: Actual Docker simulation execution is not tested here
 * as it requires Docker runtime. These tests verify the workflow
 * with manually inserted simulation results.
 */
class SimulationE2EWorkflowTest : IntegrationTest() {

    @Autowired
    private lateinit var simulationService: SimulationService

    @Autowired
    private lateinit var simulationRepository: SimulationRepository

    @Autowired
    private lateinit var upgradeValueCalculator: UpgradeValueCalculator

    private val objectMapper = ObjectMapper()

    private fun createSubmitRequest(
        characterRealm: String = "TestRealm",
        characterClass: String = "warrior",
        characterSpec: String = "fury"
    ): HttpEntity<String> {
        val json = """
            {
                "characterRealm": "$characterRealm",
                "characterClass": "$characterClass",
                "characterSpec": "$characterSpec"
            }
        """.trimIndent()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(json, headers)
    }

    @Nested
    inner class FullSubmissionWorkflow {
        @Test
        fun `should complete full submission to status check workflow via HTTP`() {
            // Step 1: Submit simulation via HTTP
            val guildId = "e2e-guild-1"
            val characterName = "E2EChar1"

            val submitResponse = restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                createSubmitRequest(),
                String::class.java
            )
            submitResponse.statusCode shouldBe HttpStatus.ACCEPTED

            val submitJson = objectMapper.readTree(submitResponse.body)
            val requestId = submitJson.get("id").asLong()
            requestId shouldNotBe null

            // Step 2: Check status via HTTP
            val statusResponse = restTemplate.getForEntity(
                "/api/v1/simulation/requests/$requestId",
                String::class.java
            )
            statusResponse.statusCode shouldBe HttpStatus.OK

            val statusJson = objectMapper.readTree(statusResponse.body)
            statusJson.get("status").asText() shouldBe SimulationStatus.PENDING.name

            // Step 3: Verify in pending list
            val pendingResponse = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/$guildId/pending",
                String::class.java
            )
            val pendingJson = objectMapper.readTree(pendingResponse.body)
            pendingJson.size() shouldBe 1
        }

        @Test
        fun `should complete full workflow from submission to results retrieval`() {
            // Step 1: Submit simulation
            val guildId = "e2e-full-guild"
            val characterName = "E2EFullChar"
            val characterRealm = "TestRealm"

            val submitResponse = restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                createSubmitRequest(characterRealm),
                String::class.java
            )
            submitResponse.statusCode shouldBe HttpStatus.ACCEPTED

            // Step 2: Simulate completed simulation by inserting results
            val profileId = simulationRepository.findProfileIdByCharacter(
                guildId, characterName, characterRealm
            )!!

            simulationRepository.saveResult(
                profileId,
                SimulationResult.create(
                    itemId = 12345L,
                    itemName = "E2E Test Weapon",
                    slot = "main_hand",
                    dpsGain = 5000.0,
                    percentGain = 5.0,
                    simulatedAt = Instant.now()
                )
            )

            // Step 3: Retrieve results via HTTP
            val resultsResponse = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/$characterName/realms/$characterRealm/results",
                String::class.java
            )
            resultsResponse.statusCode shouldBe HttpStatus.OK

            val resultsJson = objectMapper.readTree(resultsResponse.body)
            resultsJson.get("results").size() shouldBe 1
            resultsJson.get("results").get(0).get("itemId").asLong() shouldBe 12345L
            resultsJson.get("results").get(0).get("dpsGain").asDouble() shouldBe 5000.0
        }
    }

    @Nested
    inner class FlpsIntegrationWorkflow {
        @Test
        fun `should calculate upgrade value from simulation results`() {
            // Given - create profile and results via service
            val guildId = "flps-e2e-guild"
            val characterName = "FlpsE2EChar"
            val characterRealm = "FlpsRealm"

            simulationService.submitSimulation(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
                characterClass = "warrior",
                characterSpec = "fury"
            )

            val profileId = simulationRepository.findProfileIdByCharacter(
                guildId, characterName, characterRealm
            )!!

            // Add simulation result with 5% upgrade
            simulationRepository.saveResult(
                profileId,
                SimulationResult.create(
                    itemId = 54321L,
                    itemName = "FLPS Test Item",
                    slot = "chest",
                    dpsGain = 5000.0,
                    percentGain = 5.0,  // 5% should normalize to 0.5
                    simulatedAt = Instant.now()
                )
            )

            // When - calculate UV using UpgradeValueCalculator
            val uv = upgradeValueCalculator.calculateUpgradeValue(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
                itemId = ItemId(54321L),
                wishlistFallback = null
            )

            // Then
            uv.value shouldBe 0.5  // 5% / 10% max = 0.5
        }

        @Test
        fun `should report simulation data availability correctly`() {
            // Given - create profile without results
            val guildId = "availability-guild"
            val characterName = "AvailabilityChar"
            val characterRealm = "AvailabilityRealm"

            simulationService.submitSimulation(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
                characterClass = "mage",
                characterSpec = "fire"
            )

            // When - check availability
            val hasDataBefore = upgradeValueCalculator.hasSimulationData(
                guildId, characterName, characterRealm
            )

            // Then - no results yet
            hasDataBefore shouldBe false

            // When - add results
            val profileId = simulationRepository.findProfileIdByCharacter(
                guildId, characterName, characterRealm
            )!!
            simulationRepository.saveResult(
                profileId,
                SimulationResult.create(
                    itemId = 11111L,
                    itemName = "Availability Test",
                    slot = "head",
                    dpsGain = 1000.0,
                    percentGain = 1.0,
                    simulatedAt = Instant.now()
                )
            )

            val hasDataAfter = upgradeValueCalculator.hasSimulationData(
                guildId, characterName, characterRealm
            )

            // Then
            hasDataAfter shouldBe true
        }
    }

    @Nested
    inner class MultiCharacterWorkflow {
        @Test
        fun `should handle multiple characters in same guild independently`() {
            // Given
            val guildId = "multi-char-guild"
            val chars = listOf(
                Triple("Tank1", "warrior", "protection"),
                Triple("Healer1", "priest", "holy"),
                Triple("DPS1", "mage", "fire")
            )

            // When - submit simulations for all characters
            chars.forEach { (name, clazz, spec) ->
                restTemplate.postForEntity(
                    "/api/v1/simulation/guilds/$guildId/characters/$name",
                    createSubmitRequest(characterClass = clazz, characterSpec = spec),
                    String::class.java
                )
            }

            // Then - all should be pending
            val pendingResponse = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/$guildId/pending",
                String::class.java
            )
            val pendingJson = objectMapper.readTree(pendingResponse.body)
            pendingJson.size() shouldBe 3

            // And - each character has separate profile
            chars.forEach { (name, _, _) ->
                val profile = simulationRepository.findProfileByCharacter(
                    guildId, name, "TestRealm"
                )
                profile shouldNotBe null
            }
        }

        @Test
        fun `should isolate results per character`() {
            // Given - two characters with different results
            val guildId = "isolate-guild"
            val char1 = "IsolateChar1"
            val char2 = "IsolateChar2"

            listOf(char1, char2).forEach { charName ->
                simulationService.submitSimulation(
                    guildId = guildId,
                    characterName = charName,
                    characterRealm = "TestRealm",
                    characterClass = "warrior",
                    characterSpec = "fury"
                )
            }

            // Add different results
            val profile1Id = simulationRepository.findProfileIdByCharacter(guildId, char1, "TestRealm")!!
            val profile2Id = simulationRepository.findProfileIdByCharacter(guildId, char2, "TestRealm")!!

            simulationRepository.saveResult(
                profile1Id,
                SimulationResult.create(11111L, "Item1", "head", 1000.0, 1.0, Instant.now())
            )
            simulationRepository.saveResult(
                profile2Id,
                SimulationResult.create(22222L, "Item2", "chest", 2000.0, 2.0, Instant.now())
            )

            // When
            val results1 = simulationService.getSimulationResults(guildId, char1, "TestRealm")
            val results2 = simulationService.getSimulationResults(guildId, char2, "TestRealm")

            // Then - results are isolated
            results1.size shouldBe 1
            results1.first().itemId shouldBe 11111L

            results2.size shouldBe 1
            results2.first().itemId shouldBe 22222L
        }
    }

    @Nested
    inner class GuildIsolationWorkflow {
        @Test
        fun `should isolate simulations between guilds`() {
            // Given
            val guild1 = "isolation-guild-A"
            val guild2 = "isolation-guild-B"
            val characterName = "SharedCharName"  // Same name, different guilds

            // When - submit to both guilds
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guild1/characters/$characterName",
                createSubmitRequest(),
                String::class.java
            )
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guild2/characters/$characterName",
                createSubmitRequest(),
                String::class.java
            )

            // Then - each guild sees only its own pending
            val pending1 = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/$guild1/pending",
                String::class.java
            )
            val pending2 = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/$guild2/pending",
                String::class.java
            )

            val pending1Json = objectMapper.readTree(pending1.body)
            val pending2Json = objectMapper.readTree(pending2.body)

            pending1Json.size() shouldBe 1
            pending1Json.get(0).get("guildId").asText() shouldBe guild1

            pending2Json.size() shouldBe 1
            pending2Json.get(0).get("guildId").asText() shouldBe guild2
        }
    }

    @Nested
    inner class ServiceStatusWorkflow {
        @Test
        fun `should report accurate pending count in status`() {
            // Given - clean state
            val guildId = "status-workflow-guild"

            // Check initial status
            val initialStatus = restTemplate.getForEntity(
                "/api/v1/simulation/status",
                String::class.java
            )
            val initialJson = objectMapper.readTree(initialStatus.body)
            val initialPending = initialJson.get("pendingSimulations").asInt()

            // When - add simulations
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/StatusChar1",
                createSubmitRequest(),
                String::class.java
            )
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/StatusChar2",
                createSubmitRequest(),
                String::class.java
            )

            // Then - status shows increased count
            val updatedStatus = restTemplate.getForEntity(
                "/api/v1/simulation/status",
                String::class.java
            )
            val updatedJson = objectMapper.readTree(updatedStatus.body)
            val updatedPending = updatedJson.get("pendingSimulations").asInt()

            updatedPending shouldBe initialPending + 2
        }
    }

    @Nested
    inner class ErrorRecoveryWorkflow {
        @Test
        fun `should handle duplicate submission gracefully`() {
            // Given
            val guildId = "duplicate-guild"
            val characterName = "DuplicateChar"

            // When - submit twice
            val response1 = restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                createSubmitRequest(),
                String::class.java
            )
            val response2 = restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                createSubmitRequest(),
                String::class.java
            )

            // Then - both succeed
            response1.statusCode shouldBe HttpStatus.ACCEPTED
            response2.statusCode shouldBe HttpStatus.ACCEPTED

            // And - profile is reused
            val profileCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM simulation_profiles WHERE guild_id = ? AND character_name = ?",
                Long::class.java,
                guildId,
                characterName
            )
            profileCount shouldBe 1L
        }

        @Test
        fun `should return empty results for nonexistent character without error`() {
            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/nonexistent-guild/characters/NonexistentChar/realms/TestRealm/results",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            val json = objectMapper.readTree(response.body)
            json.get("results").size() shouldBe 0
        }
    }
}
