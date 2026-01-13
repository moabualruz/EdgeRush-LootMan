package com.edgerush.lootman.api.simulation

import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import java.time.Instant

/**
 * Request body for submitting a new simulation.
 */
data class SubmitSimulationRequest(
    val characterRealm: String,
    val characterClass: String,
    val characterSpec: String,
    val characterLevel: Int? = null,
    val characterRace: String? = null,
    val iterations: Int? = null,
    val fightLengthSeconds: Int? = null
)

/**
 * DTO for simulation request status.
 */
data class SimulationRequestDto(
    val id: Long?,
    val characterName: String,
    val characterRealm: String,
    val guildId: String,
    val status: SimulationStatus,
    val submittedAt: Instant,
    val completedAt: Instant?,
    val errorMessage: String?,
    val resultCount: Int
) {
    companion object {
        fun from(request: SimulationRequest): SimulationRequestDto {
            return SimulationRequestDto(
                id = request.id,
                characterName = request.profile.characterName,
                characterRealm = request.profile.characterRealm,
                guildId = request.profile.guildId,
                status = request.status,
                submittedAt = request.submittedAt,
                completedAt = request.completedAt,
                errorMessage = request.errorMessage,
                resultCount = request.results.size
            )
        }
    }
}

/**
 * DTO for individual simulation result.
 */
data class SimulationResultDto(
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val dpsGain: Double,
    val percentGain: Double,
    val isUpgrade: Boolean,
    val normalizedValue: Double,
    val simulatedAt: Instant
) {
    companion object {
        fun from(result: SimulationResult): SimulationResultDto {
            return SimulationResultDto(
                itemId = result.itemId,
                itemName = result.itemName,
                slot = result.slot,
                dpsGain = result.dpsGain,
                percentGain = result.percentGain,
                isUpgrade = result.isUpgrade,
                normalizedValue = result.normalizedUpgradeValue(),
                simulatedAt = result.simulatedAt
            )
        }
    }
}

/**
 * Response containing simulation results for a character.
 */
data class SimulationResultsResponse(
    val guildId: String,
    val characterName: String,
    val characterRealm: String,
    val results: List<SimulationResultDto>,
    val retrievedAt: Instant
)

/**
 * Summary response after executing pending simulations.
 */
data class ExecutionSummaryResponse(
    val executedCount: Int,
    val executedAt: Instant
)

/**
 * Service status response.
 */
data class SimulationStatusResponse(
    val status: String,
    val pendingSimulations: Int,
    val endpoints: Map<String, String>
)
