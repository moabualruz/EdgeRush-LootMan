package com.edgerush.lootman.api.simulation

import com.edgerush.datasync.test.base.IntegrationTest
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.Instant

/**
 * Integration tests for Simulation REST API endpoints.
 *
 * Tests verify:
 * - Full request/response cycle through HTTP
 * - Database persistence and retrieval
 * - API contract compliance
 * - Error handling for invalid inputs
 * - Workflow from submission to results retrieval
 */
class SimulationControllerIntegrationTest : IntegrationTest() {
    private fun createSubmitRequest(
        characterRealm: String = "TestRealm",
        characterClass: String = "warrior",
        characterSpec: String = "fury",
    ): HttpEntity<SubmitSimulationRequest> {
        val request =
            SubmitSimulationRequest(
                characterRealm = characterRealm,
                characterClass = characterClass,
                characterSpec = characterSpec,
                characterLevel = 80,
                characterRace = "human",
                iterations = 1000,
                fightLengthSeconds = 300,
            )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(request, headers)
    }

    @Nested
    inner class SubmitSimulation {
        @Test
        fun `should submit simulation and return 202 Accepted`() {
            // Given
            val guildId = "test-guild-123"
            val characterName = "Testchar"
            val entity = createSubmitRequest()

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                    entity,
                    TestSimulationRequestResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.ACCEPTED
            response.body shouldNotBe null
            response.body?.characterName shouldBe characterName
            response.body?.guildId shouldBe guildId
            response.body?.status shouldBe SimulationStatus.PENDING
        }

        @Test
        fun `should persist simulation to database after submission`() {
            // Given
            val guildId = "persist-guild-123"
            val characterName = "PersistChar"
            val entity = createSubmitRequest()

            // When
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                entity,
                TestSimulationRequestResponse::class.java,
            )

            // Then - verify in database
            val profileCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM simulation_profiles WHERE guild_id = ? AND character_name = ?",
                    Long::class.java,
                    guildId,
                    characterName,
                )
            profileCount shouldBe 1L

            val requestCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM simulation_requests WHERE status = ?",
                    Long::class.java,
                    SimulationStatus.PENDING.name,
                )
            requestCount shouldBe 1L
        }

        @Test
        fun `should handle multiple submissions for same character`() {
            // Given
            val guildId = "multi-guild-123"
            val characterName = "MultiChar"
            val entity = createSubmitRequest()

            // When - submit twice
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                entity,
                TestSimulationRequestResponse::class.java,
            )
            val response2 =
                restTemplate.postForEntity(
                    "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                    entity,
                    TestSimulationRequestResponse::class.java,
                )

            // Then - second submission should also succeed
            response2.statusCode shouldBe HttpStatus.ACCEPTED

            // Profile should be reused (only 1 profile)
            val profileCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM simulation_profiles WHERE guild_id = ? AND character_name = ?",
                    Long::class.java,
                    guildId,
                    characterName,
                )
            profileCount shouldBe 1L

            // But 2 requests should exist
            val requestCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM simulation_requests",
                    Long::class.java,
                )
            requestCount shouldBe 2L
        }
    }

    @Nested
    inner class GetSimulationStatus {
        @Test
        fun `should return simulation status when request exists`() {
            // Given - submit a simulation first
            val guildId = "status-guild-123"
            val characterName = "StatusChar"
            val entity = createSubmitRequest()
            val submitResponse =
                restTemplate.postForEntity(
                    "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                    entity,
                    TestSimulationRequestResponse::class.java,
                )
            val requestId = submitResponse.body?.id

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/requests/$requestId",
                    TestSimulationRequestResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.id shouldBe requestId
            response.body?.status shouldBe SimulationStatus.PENDING
        }

        @Test
        fun `should return 404 when request does not exist`() {
            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/requests/999999",
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class GetSimulationResults {
        @Test
        fun `should return empty results when no simulations completed`() {
            // Given - just submit, don't execute
            val guildId = "results-guild-123"
            val characterName = "ResultsChar"
            val characterRealm = "TestRealm"
            val entity = createSubmitRequest(characterRealm = characterRealm)
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                entity,
                TestSimulationRequestResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/guilds/$guildId/characters/$characterName/realms/$characterRealm/results",
                    TestSimulationResultsResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.results?.shouldBeEmpty()
            response.body?.guildId shouldBe guildId
            response.body?.characterName shouldBe characterName
        }

        @Test
        fun `should return results after inserting test data`() {
            // Given - insert test simulation data directly
            val guildId = "direct-results-guild"
            val characterName = "DirectChar"
            val characterRealm = "TestRealm"

            // Insert profile
            jdbcTemplate.update(
                """INSERT INTO simulation_profiles (guild_id, character_name, character_realm, profile_content, created_at)
                   VALUES (?, ?, ?, ?, ?)""",
                guildId,
                characterName,
                characterRealm,
                "warrior=\"$characterName\"",
                Instant.now(),
            )

            val profileId =
                jdbcTemplate.queryForObject(
                    "SELECT id FROM simulation_profiles WHERE guild_id = ? AND character_name = ?",
                    Long::class.java,
                    guildId,
                    characterName,
                )

            // Insert result
            jdbcTemplate.update(
                """INSERT INTO simulation_results (profile_id, item_id, item_name, slot, dps_gain, percent_gain, simulated_at)
                   VALUES (?, ?, ?, ?, ?, ?, ?)""",
                profileId,
                12345L,
                "Test Sword",
                "main_hand",
                1000.0,
                1.5,
                Instant.now(),
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/guilds/$guildId/characters/$characterName/realms/$characterRealm/results",
                    TestSimulationResultsResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.results?.shouldHaveSize(1)
            response.body?.results?.first()?.itemId shouldBe 12345L
            response.body?.results?.first()?.dpsGain shouldBe 1000.0
        }
    }

    @Nested
    inner class GetPendingSimulations {
        @Test
        fun `should return pending simulations for guild`() {
            // Given
            val guildId = "pending-guild-123"
            val entity = createSubmitRequest()
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/Char1",
                entity,
                TestSimulationRequestResponse::class.java,
            )
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/$guildId/characters/Char2",
                entity,
                TestSimulationRequestResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/guilds/$guildId/pending",
                    Array<TestSimulationRequestResponse>::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            response.body?.size shouldBe 2
        }

        @Test
        fun `should filter out other guilds simulations`() {
            // Given
            val entity = createSubmitRequest()
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/guild-A/characters/CharA",
                entity,
                TestSimulationRequestResponse::class.java,
            )
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/guild-B/characters/CharB",
                entity,
                TestSimulationRequestResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/guilds/guild-A/pending",
                    Array<TestSimulationRequestResponse>::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.size shouldBe 1
            response.body?.first()?.guildId shouldBe "guild-A"
        }
    }

    @Nested
    inner class ExecutePendingSimulations {
        @Test
        fun `should return execution summary`() {
            // When - execute with no pending (Docker not available in test)
            val response =
                restTemplate.postForEntity(
                    "/api/v1/simulation/execute-pending",
                    null,
                    TestExecutionSummaryResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body shouldNotBe null
            response.body?.executedAt shouldNotBe null
        }
    }

    @Nested
    inner class GetStatus {
        @Test
        fun `should return service status with endpoints`() {
            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/status",
                    TestSimulationStatusResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.status shouldBe "operational"
            response.body?.endpoints shouldNotBe null
            response.body?.endpoints?.containsKey("Submit Simulation") shouldBe true
        }

        @Test
        fun `should report pending simulation count`() {
            // Given - submit some simulations
            val entity = createSubmitRequest()
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/status-guild/characters/Char1",
                entity,
                TestSimulationRequestResponse::class.java,
            )

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/status",
                    TestSimulationStatusResponse::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            response.body?.pendingSimulations shouldBe 1
        }
    }

    @Nested
    inner class ApiContractVerification {
        @Test
        fun `should return correct JSON structure for submission response`() {
            // Given
            val guildId = "contract-guild"
            val characterName = "ContractChar"
            val entity = createSubmitRequest()

            // When
            val response =
                restTemplate.postForEntity(
                    "/api/v1/simulation/guilds/$guildId/characters/$characterName",
                    entity,
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.ACCEPTED
            val body = response.body!!
            body.contains("\"id\"") shouldBe true
            body.contains("\"characterName\"") shouldBe true
            body.contains("\"guildId\"") shouldBe true
            body.contains("\"status\"") shouldBe true
            body.contains("\"submittedAt\"") shouldBe true
        }

        @Test
        fun `should return correct JSON structure for results response`() {
            // Given
            val guildId = "contract-results-guild"
            val characterName = "ContractResultsChar"
            val characterRealm = "TestRealm"

            // When
            val response =
                restTemplate.getForEntity(
                    "/api/v1/simulation/guilds/$guildId/characters/$characterName/realms/$characterRealm/results",
                    String::class.java,
                )

            // Then
            response.statusCode shouldBe HttpStatus.OK
            val body = response.body!!
            body.contains("\"guildId\"") shouldBe true
            body.contains("\"characterName\"") shouldBe true
            body.contains("\"characterRealm\"") shouldBe true
            body.contains("\"results\"") shouldBe true
            body.contains("\"retrievedAt\"") shouldBe true
        }
    }
}

// Response DTOs for test deserialization (prefixed with Test to avoid collision with main DTOs)
data class TestSimulationRequestResponse(
    val id: Long?,
    val characterName: String,
    val characterRealm: String,
    val guildId: String,
    val status: SimulationStatus,
    val submittedAt: Instant?,
    val completedAt: Instant?,
    val errorMessage: String?,
    val resultCount: Int,
)

data class TestSimulationResultItem(
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val dpsGain: Double,
    val percentGain: Double,
    val isUpgrade: Boolean,
    val normalizedValue: Double,
    val simulatedAt: Instant,
)

data class TestSimulationResultsResponse(
    val guildId: String,
    val characterName: String,
    val characterRealm: String,
    val results: List<TestSimulationResultItem>,
    val retrievedAt: Instant,
)

data class TestExecutionSummaryResponse(
    val executedCount: Int,
    val executedAt: Instant,
)

data class TestSimulationStatusResponse(
    val status: String,
    val pendingSimulations: Int,
    val endpoints: Map<String, String>,
)
