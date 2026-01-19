package com.edgerush.lootman.api.simulation

import com.edgerush.lootman.application.simulation.SimulationService
import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.repository.SimulationRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * REST controller for SimulationCraft integration.
 *
 * Provides endpoints for:
 * - Submitting simulation requests
 * - Checking simulation status
 * - Retrieving simulation results
 */
@RestController
@RequestMapping("/api/v1/simulations")
class SimulationController(
    private val simulationService: SimulationService,
    private val simulationRepository: SimulationRepository,
    private val getRaiderUseCase: com.edgerush.lootman.application.raider.GetRaiderUseCase,
) {
    /**
     * Submit a new simulation request for a character.
     */
    @PostMapping("/guilds/{guildId}/characters/{characterName}")
    fun submitSimulation(
        @PathVariable guildId: String,
        @PathVariable characterName: String,
        @RequestBody request: SubmitSimulationRequest,
    ): ResponseEntity<SimulationRequestDto> {
        val simulationRequest =
            simulationService.submitSimulation(
                guildId = guildId,
                characterName = characterName,
                characterRealm = request.characterRealm,
                characterClass = request.characterClass,
                characterSpec = request.characterSpec,
                characterLevel = request.characterLevel ?: 80,
                characterRace = request.characterRace ?: "human",
                iterations = request.iterations ?: SimulationRequest.DEFAULT_ITERATIONS,
                fightLengthSeconds = request.fightLengthSeconds ?: SimulationRequest.DEFAULT_FIGHT_LENGTH_SECONDS,
            )

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(SimulationRequestDto.from(simulationRequest))
    }

    /**
     * Get the status of a specific simulation request.
     */
    @GetMapping("/requests/{requestId}")
    fun getSimulationStatus(
        @PathVariable requestId: Long,
    ): ResponseEntity<SimulationRequestDto> {
        val request =
            simulationRepository.findRequestById(requestId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(SimulationRequestDto.from(request))
    }

    /**
     * Get simulation results for a character.
     */
    @GetMapping("/guilds/{guildId}/characters/{characterName}/realms/{characterRealm}/results")
    fun getSimulationResults(
        @PathVariable guildId: String,
        @PathVariable characterName: String,
        @PathVariable characterRealm: String,
    ): ResponseEntity<SimulationResultsResponse> {
        val results =
            simulationService.getSimulationResults(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
            )

        return ResponseEntity.ok(
            SimulationResultsResponse(
                guildId = guildId,
                characterName = characterName,
                characterRealm = characterRealm,
                results = results.map { SimulationResultDto.from(it) },
                retrievedAt = Instant.now(),
            ),
        )
    }

    /**
     * Get pending simulations for a guild.
     */
    @GetMapping("/guilds/{guildId}/pending")
    fun getPendingSimulations(
        @PathVariable guildId: String,
    ): ResponseEntity<List<SimulationRequestDto>> {
        val pending =
            simulationRepository.findPendingRequests()
                .filter { it.profile.guildId == guildId }
                .map { SimulationRequestDto.from(it) }

        return ResponseEntity.ok(pending)
    }

    /**
     * Trigger execution of all pending simulations.
     * This is typically called by a scheduled job, but can be triggered manually.
     */
    @PostMapping("/execute-pending")
    fun executePendingSimulations(): ResponseEntity<ExecutionSummaryResponse> {
        val executedCount = simulationService.executePendingSimulations()

        return ResponseEntity.ok(
            ExecutionSummaryResponse(
                executedCount = executedCount,
                executedAt = Instant.now(),
            ),
        )
    }

    /**
     * Get simulation service status.
     */
    @GetMapping("/status")
    fun getStatus(): ResponseEntity<SimulationStatusResponse> {
        val pendingCount = simulationRepository.findPendingRequests().size

        return ResponseEntity.ok(
            SimulationStatusResponse(
                status = "operational",
                pendingSimulations = pendingCount,
                endpoints =
                    mapOf(
                        "Submit Simulation" to "POST /api/v1/simulation/guilds/{guildId}/characters/{characterName}",
                        "Get Status" to "GET /api/v1/simulation/requests/{requestId}",
                        "Get Results" to "GET /api/v1/simulation/guilds/{guildId}/characters/{characterName}/realms/{characterRealm}/results",
                        "Get Pending" to "GET /api/v1/simulation/guilds/{guildId}/pending",
                        "Execute Pending" to "POST /api/v1/simulation/execute-pending",
                    ),
            ),
        )
    }

    /**
     * Trigger simulation for a raider.
     */
    @PostMapping("/guilds/{guildId}/raiders/{raiderId}/run")
    fun triggerSimulation(
        @PathVariable guildId: String,
        @PathVariable raiderId: Long,
    ): ResponseEntity<SimulationRequestDto> {
        val raider =
            getRaiderUseCase.execute(com.edgerush.lootman.application.raider.GetRaiderQuery(raiderId)).getOrThrow()

        // Ensure guild matches
        if (raider.guildId.value != guildId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val simulationRequest =
            simulationService.submitSimulation(
                guildId = guildId,
                characterName = raider.characterName,
                characterRealm = raider.realm,
                characterClass = raider.characterClass.name.lowercase(),
                characterSpec = "unknown",
            )

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(SimulationRequestDto.from(simulationRequest))
    }

    /**
     * Get simulation status for a raider.
     * Returns IDLE status if no simulation has been run.
     */
    @GetMapping("/guilds/{guildId}/raiders/{raiderId}/status")
    fun getRaiderSimulationStatus(
        @PathVariable guildId: String,
        @PathVariable raiderId: Long,
    ): ResponseEntity<RaiderSimulationStatusResponse> {
        val raider =
            getRaiderUseCase.execute(com.edgerush.lootman.application.raider.GetRaiderQuery(raiderId)).getOrThrow()

        val profileId = simulationRepository.findProfileIdByCharacter(guildId, raider.characterName, raider.realm)

        if (profileId == null) {
            // No simulation profile exists - return IDLE status
            return ResponseEntity.ok(RaiderSimulationStatusResponse.idle(raiderId))
        }

        // Check for pending/running simulation
        val pending = simulationRepository.findPendingRequests().find { it.profile.id == profileId }
        if (pending != null) {
            return ResponseEntity.ok(RaiderSimulationStatusResponse.from(raiderId, pending))
        }

        // Check if there are any completed results - if so, status is COMPLETED
        val results = simulationRepository.findResultsByProfile(profileId)
        if (results.isNotEmpty()) {
            val latestResult = results.maxByOrNull { it.simulatedAt }
            return ResponseEntity.ok(
                RaiderSimulationStatusResponse(
                    raiderId = raiderId,
                    status = "COMPLETED",
                    lastRunAt = latestResult?.simulatedAt,
                    source = "LOCAL",
                ),
            )
        }

        // No simulation found - return IDLE status
        return ResponseEntity.ok(RaiderSimulationStatusResponse.idle(raiderId))
    }
}
