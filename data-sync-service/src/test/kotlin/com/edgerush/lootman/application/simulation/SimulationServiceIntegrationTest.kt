package com.edgerush.lootman.application.simulation

import com.edgerush.datasync.test.base.IntegrationTest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

/**
 * Integration tests for SimulationService.
 *
 * Tests verify:
 * - Service orchestration with real database
 * - Profile generation and persistence
 * - Request lifecycle management
 * - Result retrieval across components
 */
class SimulationServiceIntegrationTest : IntegrationTest() {
    @Autowired
    private lateinit var simulationService: SimulationService

    @Autowired
    private lateinit var simulationRepository: SimulationRepository

    @Nested
    inner class SubmitSimulation {
        @Test
        fun `should create profile and request when submitting simulation`() {
            // When
            val request =
                simulationService.submitSimulation(
                    guildId = "service-guild-123",
                    characterName = "ServiceChar",
                    characterRealm = "ServiceRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                )

            // Then
            request.id shouldNotBe null
            request.status shouldBe SimulationStatus.PENDING
            request.profile.characterName shouldBe "ServiceChar"

            // Verify profile persisted
            val profile =
                simulationRepository.findProfileByCharacter(
                    "service-guild-123",
                    "ServiceChar",
                    "ServiceRealm",
                )
            profile shouldNotBe null
        }

        @Test
        fun `should generate valid SimC profile content`() {
            // When
            val request =
                simulationService.submitSimulation(
                    guildId = "profile-guild-123",
                    characterName = "ProfileChar",
                    characterRealm = "ProfileRealm",
                    characterClass = "mage",
                    characterSpec = "fire",
                    characterLevel = 80,
                    characterRace = "human",
                )

            // Then
            val profileContent = request.profile.profileContent
            profileContent.contains("mage=") shouldBe true
            profileContent.contains("level=80") shouldBe true
            profileContent.contains("race=human") shouldBe true
            profileContent.contains("spec=fire") shouldBe true
        }

        @Test
        fun `should handle custom simulation parameters`() {
            // When
            val request =
                simulationService.submitSimulation(
                    guildId = "params-guild-123",
                    characterName = "ParamsChar",
                    characterRealm = "ParamsRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                    iterations = 5000,
                    fightLengthSeconds = 180,
                )

            // Then
            request.iterations shouldBe 5000
            request.fightLengthSeconds shouldBe 180
        }

        @Test
        fun `should reuse profile for same character`() {
            // Given - submit first simulation
            simulationService.submitSimulation(
                guildId = "reuse-guild-123",
                characterName = "ReuseChar",
                characterRealm = "ReuseRealm",
                characterClass = "warrior",
                characterSpec = "fury",
            )

            // When - submit second simulation for same character
            simulationService.submitSimulation(
                guildId = "reuse-guild-123",
                characterName = "ReuseChar",
                characterRealm = "ReuseRealm",
                characterClass = "warrior",
                characterSpec = "arms", // Different spec
            )

            // Then - should still be one profile
            val profileCount =
                jdbcTemplate.queryForObject(
                    """SELECT COUNT(*) FROM simulation_profiles
                   WHERE guild_id = ? AND character_name = ?""",
                    Long::class.java,
                    "reuse-guild-123",
                    "ReuseChar",
                )
            profileCount shouldBe 1L

            // But two requests
            val requestCount =
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM simulation_requests",
                    Long::class.java,
                )
            requestCount shouldBe 2L
        }
    }

    @Nested
    inner class GetSimulationResults {
        @Test
        fun `should return empty list when no results exist`() {
            // Given
            simulationService.submitSimulation(
                guildId = "empty-results-guild",
                characterName = "EmptyChar",
                characterRealm = "EmptyRealm",
                characterClass = "warrior",
                characterSpec = "fury",
            )

            // When
            val results =
                simulationService.getSimulationResults(
                    guildId = "empty-results-guild",
                    characterName = "EmptyChar",
                    characterRealm = "EmptyRealm",
                )

            // Then
            results.shouldBeEmpty()
        }

        @Test
        fun `should return results when they exist`() {
            // Given - create profile and add results directly
            simulationService.submitSimulation(
                guildId = "with-results-guild",
                characterName = "WithResultsChar",
                characterRealm = "WithResultsRealm",
                characterClass = "warrior",
                characterSpec = "fury",
            )

            val profileId =
                simulationRepository.findProfileIdByCharacter(
                    "with-results-guild",
                    "WithResultsChar",
                    "WithResultsRealm",
                )!!

            simulationRepository.saveResult(
                profileId,
                SimulationResult.create(
                    itemId = 99999L,
                    itemName = "Service Test Item",
                    slot = "chest",
                    dpsGain = 2000.0,
                    percentGain = 2.5,
                    simulatedAt = Instant.now(),
                ),
            )

            // When
            val results =
                simulationService.getSimulationResults(
                    guildId = "with-results-guild",
                    characterName = "WithResultsChar",
                    characterRealm = "WithResultsRealm",
                )

            // Then
            results shouldHaveSize 1
            results.first().itemId shouldBe 99999L
        }

        @Test
        fun `should return empty list when character does not exist`() {
            // When
            val results =
                simulationService.getSimulationResults(
                    guildId = "nonexistent-guild",
                    characterName = "NonexistentChar",
                    characterRealm = "NonexistentRealm",
                )

            // Then
            results.shouldBeEmpty()
        }
    }

    @Nested
    inner class PendingSimulations {
        @Test
        fun `should find all pending simulations`() {
            // Given
            simulationService.submitSimulation(
                guildId = "pending-test-guild",
                characterName = "PendingChar1",
                characterRealm = "PendingRealm",
                characterClass = "warrior",
                characterSpec = "fury",
            )
            simulationService.submitSimulation(
                guildId = "pending-test-guild",
                characterName = "PendingChar2",
                characterRealm = "PendingRealm",
                characterClass = "mage",
                characterSpec = "fire",
            )

            // When
            val pending = simulationRepository.findPendingRequests()

            // Then
            pending shouldHaveSize 2
            pending.all { it.status == SimulationStatus.PENDING } shouldBe true
        }
    }

    @Nested
    inner class ProfileIdManagement {
        @Test
        fun `should correctly track profile IDs across operations`() {
            // Given
            val request =
                simulationService.submitSimulation(
                    guildId = "profile-id-guild",
                    characterName = "ProfileIdChar",
                    characterRealm = "ProfileIdRealm",
                    characterClass = "warrior",
                    characterSpec = "fury",
                )

            // When - find profile ID
            val profileId =
                simulationRepository.findProfileIdByCharacter(
                    "profile-id-guild",
                    "ProfileIdChar",
                    "ProfileIdRealm",
                )

            // Then
            profileId shouldNotBe null

            // And - can find profile by ID
            val profile = simulationRepository.findProfileById(profileId!!)
            profile shouldNotBe null
            profile?.characterName shouldBe "ProfileIdChar"
        }
    }
}
