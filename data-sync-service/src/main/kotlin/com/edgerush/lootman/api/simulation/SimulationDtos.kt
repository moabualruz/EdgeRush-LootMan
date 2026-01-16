package com.edgerush.lootman.api.simulation

import com.edgerush.lootman.domain.simulation.model.SimulationRequest
import com.edgerush.lootman.domain.simulation.model.SimulationResult
import com.edgerush.lootman.domain.simulation.model.SimulationStatus
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import java.time.Instant

/**
 * Request body for submitting a new simulation.
 */
data class SubmitSimulationRequest(
    @field:NotBlank(message = "Character realm is required")
    val characterRealm: String,
    @field:NotBlank(message = "Character class is required")
    val characterClass: String,
    @field:NotBlank(message = "Character spec is required")
    val characterSpec: String,
    @field:Min(value = 1, message = "Character level must be at least 1")
    @field:Max(value = 80, message = "Character level cannot exceed 80")
    val characterLevel: Int? = null,
    val characterRace: String? = null,
    @field:Min(value = 100, message = "Iterations must be at least 100")
    @field:Max(value = 100000, message = "Iterations cannot exceed 100000")
    val iterations: Int? = null,
    @field:Min(value = 60, message = "Fight length must be at least 60 seconds")
    @field:Max(value = 1800, message = "Fight length cannot exceed 1800 seconds")
    val fightLengthSeconds: Int? = null,
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
    val resultCount: Int,
    val externalId: String? = null,
    val source: String? = "LOCAL",
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
                resultCount = request.results.size,
                externalId = request.externalId,
                source = request.source,
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
    val simulatedAt: Instant,
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
                simulatedAt = result.simulatedAt,
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
    val retrievedAt: Instant,
)

/**
 * Summary response after executing pending simulations.
 */
data class ExecutionSummaryResponse(
    val executedCount: Int,
    val executedAt: Instant,
)

/**
 * Service status response.
 */
data class SimulationStatusResponse(
    val status: String,
    val pendingSimulations: Int,
    val endpoints: Map<String, String>,
)
