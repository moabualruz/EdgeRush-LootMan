package com.edgerush.lootman.application.simulation

import com.edgerush.lootman.domain.shared.model.GearSet
import com.edgerush.lootman.domain.simulation.model.SimulationProfile
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Interface for simulation executors.
 * Infrastructure layer provides the Docker implementation.
 */
interface SimulationExecutor {
    suspend fun execute(request: SimulationRequest): Result<List<SimulationResult>>
}

/**
 * Application service for managing simulations.
 *
 * Orchestrates the simulation workflow:
 * 1. Profile generation
 * 2. Request creation and submission
 * 3. Execution via Docker/SimC
 * 4. Result storage
 */
@Service
class SimulationService(
    private val simulationRepository: SimulationRepository,
    private val profileGenerator: ProfileGeneratorService,
    private val raidbotsService: com.edgerush.lootman.infrastructure.external.raidbots.RaidbotsService,
    private val raidbotsConfig: com.edgerush.lootman.infrastructure.external.raidbots.RaidbotsConfig,
    private val simulationExecutor: SimulationExecutor,
) {
    private val logger = LoggerFactory.getLogger(SimulationService::class.java)

    /**
     * Submits a new simulation request for a character.
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @param characterRealm The realm name
     * @param characterClass The character class (e.g., "warrior")
     * @param characterSpec The character spec (e.g., "fury")
     * @param characterLevel The character level
     * @param characterRace The character race
     * @param gear Optional gear set for the simulation
     * @param iterations Number of simulation iterations (default: 10000)
     * @param fightLengthSeconds Fight duration (default: 300)
     * @return The created SimulationRequest
     */
    fun submitSimulation(
        guildId: String,
        characterName: String,
        characterRealm: String,
        characterClass: String,
        characterSpec: String,
        characterLevel: Int = 80,
        characterRace: String = "human",
        gear: GearSet? = null,
        iterations: Int = SimulationRequest.DEFAULT_ITERATIONS,
        fightLengthSeconds: Int = SimulationRequest.DEFAULT_FIGHT_LENGTH_SECONDS,
    ): SimulationRequest {
        logger.info("Submitting simulation for $characterName-$characterRealm")

        // Generate SimC profile
        val profileContent =
            profileGenerator.generateProfile(
                characterName = characterName,
                characterRealm = characterRealm,
                characterClass = characterClass,
                characterSpec = characterSpec,
                characterLevel = characterLevel,
                characterRace = characterRace,
                gear = gear,
            )

        // Create and save profile
        val profile =
            SimulationProfile.create(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
                profileContent = profileContent,
                createdAt = Instant.now(),
            )
        val (profileId, _) = simulationRepository.saveProfile(profile)
        logger.debug("Saved profile with id=$profileId for $characterName-$characterRealm")

        // Create and save request
        val request =
            SimulationRequest.create(
                profile = profile,
                iterations = iterations,
                fightLengthSeconds = fightLengthSeconds,
            )
        val savedRequest = simulationRepository.saveRequest(request)

        logger.info("Simulation request created with id=${savedRequest.id}")
        return savedRequest
    }

    /**
     * Executes all pending simulation requests.
     *
     * @return Number of simulations executed
     */
    fun executePendingSimulations(): Int =
        runBlocking {
            val pendingRequests = simulationRepository.findPendingRequests()
            logger.info("Found ${pendingRequests.size} pending simulations")

            var executedCount = 0
            for (request in pendingRequests) {
                try {
                    executeSimulation(request)
                    executedCount++
                } catch (e: Exception) {
                    logger.error("Failed to execute simulation ${request.id}: ${e.message}", e)
                }
            }

            logger.info("Executed $executedCount simulations")
            return@runBlocking executedCount
        }

    /**
     * Gets simulation results for a character.
     *
     * @param guildId The guild identifier
     * @param characterName The character name
     * @param characterRealm The realm name
     * @return List of simulation results, or empty list if none found
     */
    fun getSimulationResults(
        guildId: String,
        characterName: String,
        characterRealm: String,
    ): List<SimulationResult> {
        val profile =
            simulationRepository.findProfileByCharacter(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
            ) ?: return emptyList()

        // Get profile ID (in production, this would be returned with the profile)
        val profileId = getProfileId(guildId, characterName, characterRealm) ?: return emptyList()

        return simulationRepository.findResultsByProfile(profileId)
    }

    private suspend fun executeSimulation(request: SimulationRequest) {
        logger.info("Executing simulation ${request.id} for ${request.profile.characterIdentifier}")

        // Mark as running
        val runningRequest = request.markRunning()
        simulationRepository.saveRequest(runningRequest)

        // Execute simulation
        // Execute simulation
        if (raidbotsConfig.enabled) {
             try {
                 val simId = raidbotsService.submitSimulation(request.profile.profileContent)
                 logger.info("Submitted Raidbots sim: $simId")
                 
                 // Update request with external ID and mark as running
                 val raidbotsRequest = SimulationRequest.createRaidbots(request.profile, simId)
                 // We need to preserve the ID of the original pending request if this was from one
                 val updatedRequest = if (request.id != null) {
                    raidbotsRequest.withId(request.id).copy(submittedAt = request.submittedAt)
                 } else {
                    raidbotsRequest
                 }
                 simulationRepository.saveRequest(updatedRequest)
             } catch (e: Exception) {
                 throw e
             }
        } else {
             val result = simulationExecutor.execute(runningRequest)
             handleLocalResult(request, runningRequest, result)
        }
    }

    private suspend fun handleLocalResult(
        request: SimulationRequest,
        runningRequest: SimulationRequest,
        result: Result<List<SimulationResult>>
    ) {

        // Handle result
        result.fold(
            onSuccess = { results ->
                logger.info("Simulation ${request.id} completed with ${results.size} results")
                val completedRequest = runningRequest.markCompleted(results)
                simulationRepository.saveRequest(completedRequest)

                // Save individual results
                val profileId =
                    getProfileId(
                        request.profile.guildId,
                        request.profile.characterName,
                        request.profile.characterRealm,
                    )
                if (profileId != null) {
                    results.forEach { result ->
                        simulationRepository.saveResult(profileId, result)
                    }
                }
            },
            onFailure = { error ->
                logger.error("Simulation ${request.id} failed: ${error.message}")
                val failedRequest = runningRequest.markFailed(error.message ?: "Unknown error")
                simulationRepository.saveRequest(failedRequest)
            },
        )
    }

    private fun getProfileId(
        guildId: String,
        characterName: String,
        characterRealm: String,
    ): Long? {
        return simulationRepository.findProfileIdByCharacter(
            guildId = guildId,
            characterName = characterName,
            characterRealm = characterRealm,
        )
    }
}
