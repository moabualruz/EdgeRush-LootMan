package com.edgerush.datasync.application.flps

import com.edgerush.datasync.domain.flps.model.IpiWeights
import com.edgerush.datasync.domain.flps.model.RmsWeights
import com.edgerush.datasync.domain.flps.repository.*
import org.springframework.stereotype.Service

/**
 * Use case for updating FLPS modifiers and configuration for a guild.
 * This validates the configuration before persisting it.
 */
@Service
class UpdateModifiersUseCase(
    private val flpsModifierRepository: FlpsModifierRepository
) {

    /**
     * Executes the modifier update for the given command.
     *
     * @param command The update parameters
     * @return Result indicating success or validation failure
     */
    fun execute(command: UpdateModifiersCommand): Result<Unit> = runCatching {
        // Validate guild ID
        require(command.guildId.isNotBlank()) { "Guild ID must not be blank" }

        // Validate thresholds (domain models validate themselves)
        validateThresholds(command.thresholds)

        // Validate role multipliers
        validateRoleMultipliers(command.roleMultipliers)

        // Validate recency penalties
        validateRecencyPenalties(command.recencyPenalties)

        // Create configuration (RmsWeights and IpiWeights validate themselves in their constructors)
        val configuration = FlpsConfiguration(
            rmsWeights = command.rmsWeights,
            ipiWeights = command.ipiWeights,
            thresholds = command.thresholds,
            roleMultipliers = command.roleMultipliers,
            recencyPenalties = command.recencyPenalties
        )

        // Save configuration
        flpsModifierRepository.save(command.guildId, configuration)
    }

    private fun validateThresholds(thresholds: FlpsThresholds) {
        require(thresholds.eligibilityAttendance in 0.0..1.0) {
            "Attendance threshold must be between 0.0 and 1.0"
        }
        require(thresholds.eligibilityActivity >= 0.0) {
            "Activity threshold must be non-negative"
        }
    }

    private fun validateRoleMultipliers(multipliers: RoleMultipliers) {
        require(multipliers.tank >= 0.0 && multipliers.healer >= 0.0 && multipliers.dps >= 0.0) {
            "Role multipliers must be non-negative"
        }
    }

    private fun validateRecencyPenalties(penalties: RecencyPenalties) {
        require(penalties.tierA in 0.0..1.0 && penalties.tierB in 0.0..1.0 && penalties.tierC in 0.0..1.0) {
            "Recency penalties must be between 0.0 and 1.0"
        }
        require(penalties.recoveryRate >= 0.0) {
            "Recovery rate must be non-negative"
        }
    }
}

/**
 * Command for updating FLPS modifiers.
 */
data class UpdateModifiersCommand(
    val guildId: String,
    val rmsWeights: RmsWeights,
    val ipiWeights: IpiWeights,
    val thresholds: FlpsThresholds,
    val roleMultipliers: RoleMultipliers,
    val recencyPenalties: RecencyPenalties
)
