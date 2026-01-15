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
@RequestMapping("/api/v1/simulation")
class SimulationController(
    private val simulationService: SimulationService,
    private val simulationRepository: SimulationRepository,
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
}
