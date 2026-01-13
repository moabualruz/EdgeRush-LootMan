package com.edgerush.lootman.api.simulation

import com.edgerush.datasync.test.base.IntegrationTest
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

/**
 * API Contract Tests for Simulation endpoints.
 *
 * These tests verify:
 * - Response structure matches documented API contract
 * - Field names and types are consistent
 * - Required fields are always present
 * - Backward compatibility is maintained
 * - HTTP status codes are correct for each scenario
 */
class SimulationApiContractTest : IntegrationTest() {

    private val objectMapper = ObjectMapper()

    private fun createSubmitRequest(): HttpEntity<String> {
        val json = """
            {
                "characterRealm": "TestRealm",
                "characterClass": "warrior",
                "characterSpec": "fury",
                "characterLevel": 80,
                "characterRace": "human",
                "iterations": 1000,
                "fightLengthSeconds": 300
            }
        """.trimIndent()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(json, headers)
    }

    @Nested
    inner class SubmitSimulationContract {
        @Test
        fun `POST simulation should return 202 Accepted with correct structure`() {
            // When
            val response = restTemplate.postForEntity(
                "/api/v1/simulation/guilds/contract-guild/characters/ContractChar",
                createSubmitRequest(),
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.ACCEPTED

            val json = objectMapper.readTree(response.body)
            verifySimulationRequestStructure(json)
        }

        @Test
        fun `POST simulation response should include all required fields`() {
            // When
            val response = restTemplate.postForEntity(
                "/api/v1/simulation/guilds/required-fields-guild/characters/RequiredChar",
                createSubmitRequest(),
                String::class.java
            )

            // Then
            val json = objectMapper.readTree(response.body)

            // Required fields
            json.has("id") shouldBe true
            json.has("characterName") shouldBe true
            json.has("characterRealm") shouldBe true
            json.has("guildId") shouldBe true
            json.has("status") shouldBe true
            json.has("submittedAt") shouldBe true
            json.has("resultCount") shouldBe true

            // Optional fields (may be null)
            json.has("completedAt") shouldBe true
            json.has("errorMessage") shouldBe true
        }

        @Test
        fun `POST simulation should accept minimal request body`() {
            // Given - only required fields
            val minimalJson = """
                {
                    "characterRealm": "TestRealm",
                    "characterClass": "warrior",
                    "characterSpec": "fury"
                }
            """.trimIndent()
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            val entity = HttpEntity(minimalJson, headers)

            // When
            val response = restTemplate.postForEntity(
                "/api/v1/simulation/guilds/minimal-guild/characters/MinimalChar",
                entity,
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.ACCEPTED
        }
    }

    @Nested
    inner class GetStatusContract {
        @Test
        fun `GET request status should return correct structure when found`() {
            // Given - create a simulation first
            val submitResponse = restTemplate.postForEntity(
                "/api/v1/simulation/guilds/status-contract-guild/characters/StatusChar",
                createSubmitRequest(),
                String::class.java
            )
            val submitJson = objectMapper.readTree(submitResponse.body)
            val requestId = submitJson.get("id").asLong()

            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/requests/$requestId",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK

            val json = objectMapper.readTree(response.body)
            verifySimulationRequestStructure(json)
        }

        @Test
        fun `GET request status should return 404 for unknown ID`() {
            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/requests/999999999",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.NOT_FOUND
        }
    }

    @Nested
    inner class GetResultsContract {
        @Test
        fun `GET results should return correct structure`() {
            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/results-contract-guild/characters/ResultsChar/realms/TestRealm/results",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK

            val json = objectMapper.readTree(response.body)
            json.has("guildId") shouldBe true
            json.has("characterName") shouldBe true
            json.has("characterRealm") shouldBe true
            json.has("results") shouldBe true
            json.has("retrievedAt") shouldBe true

            json.get("results").isArray shouldBe true
        }

        @Test
        fun `GET results should return empty array when no results`() {
            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/empty-contract-guild/characters/EmptyChar/realms/TestRealm/results",
                String::class.java
            )

            // Then
            val json = objectMapper.readTree(response.body)
            json.get("results").isArray shouldBe true
            json.get("results").size() shouldBe 0
        }
    }

    @Nested
    inner class GetPendingContract {
        @Test
        fun `GET pending should return array structure`() {
            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/pending-contract-guild/pending",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK

            val json = objectMapper.readTree(response.body)
            json.isArray shouldBe true
        }

        @Test
        fun `GET pending should return correct structure for each item`() {
            // Given
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/pending-item-guild/characters/PendingItemChar",
                createSubmitRequest(),
                String::class.java
            )

            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/pending-item-guild/pending",
                String::class.java
            )

            // Then
            val json = objectMapper.readTree(response.body)
            json.size() shouldBe 1
            verifySimulationRequestStructure(json.get(0))
        }
    }

    @Nested
    inner class ExecutePendingContract {
        @Test
        fun `POST execute-pending should return execution summary`() {
            // When
            val response = restTemplate.postForEntity(
                "/api/v1/simulation/execute-pending",
                null,
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK

            val json = objectMapper.readTree(response.body)
            json.has("executedCount") shouldBe true
            json.has("executedAt") shouldBe true

            json.get("executedCount").isInt shouldBe true
        }
    }

    @Nested
    inner class ServiceStatusContract {
        @Test
        fun `GET status should return correct structure`() {
            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/status",
                String::class.java
            )

            // Then
            response.statusCode shouldBe HttpStatus.OK

            val json = objectMapper.readTree(response.body)
            json.has("status") shouldBe true
            json.has("pendingSimulations") shouldBe true
            json.has("endpoints") shouldBe true

            json.get("status").isTextual shouldBe true
            json.get("pendingSimulations").isInt shouldBe true
            json.get("endpoints").isObject shouldBe true
        }

        @Test
        fun `GET status endpoints should document all available endpoints`() {
            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/status",
                String::class.java
            )

            // Then
            val json = objectMapper.readTree(response.body)
            val endpoints = json.get("endpoints")

            endpoints.has("Submit Simulation") shouldBe true
            endpoints.has("Get Status") shouldBe true
            endpoints.has("Get Results") shouldBe true
            endpoints.has("Get Pending") shouldBe true
            endpoints.has("Execute Pending") shouldBe true
        }
    }

    @Nested
    inner class ContentTypeContract {
        @Test
        fun `all endpoints should return application json`() {
            // Given
            val endpoints = listOf(
                "/api/v1/simulation/status",
                "/api/v1/simulation/guilds/content-type-guild/pending",
                "/api/v1/simulation/guilds/content-type-guild/characters/ContentChar/realms/TestRealm/results"
            )

            // When/Then
            endpoints.forEach { endpoint ->
                val response = restTemplate.getForEntity(endpoint, String::class.java)
                response.headers.contentType?.includes(MediaType.APPLICATION_JSON) shouldBe true
            }
        }
    }

    @Nested
    inner class DateTimeFormatContract {
        @Test
        fun `datetime fields should be ISO-8601 format`() {
            // Given
            restTemplate.postForEntity(
                "/api/v1/simulation/guilds/datetime-guild/characters/DateTimeChar",
                createSubmitRequest(),
                String::class.java
            )

            // When
            val response = restTemplate.getForEntity(
                "/api/v1/simulation/guilds/datetime-guild/characters/DateTimeChar/realms/TestRealm/results",
                String::class.java
            )

            // Then
            val json = objectMapper.readTree(response.body)
            val retrievedAt = json.get("retrievedAt").asText()

            // Should match ISO-8601 format
            retrievedAt.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*")) shouldBe true
        }
    }

    private fun verifySimulationRequestStructure(json: JsonNode) {
        json.has("id") shouldBe true
        json.has("characterName") shouldBe true
        json.has("characterRealm") shouldBe true
        json.has("guildId") shouldBe true
        json.has("status") shouldBe true
        json.has("submittedAt") shouldBe true
        json.has("resultCount") shouldBe true

        json.get("characterName").isTextual shouldBe true
        json.get("characterRealm").isTextual shouldBe true
        json.get("guildId").isTextual shouldBe true
        json.get("status").isTextual shouldBe true
        json.get("resultCount").isInt shouldBe true
    }
}
