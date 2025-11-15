package com.edgerush.lootman.application.flps

import com.edgerush.lootman.domain.flps.model.AttendanceCommitmentScore
import com.edgerush.lootman.domain.flps.model.ExternalPreparationScore
import com.edgerush.lootman.domain.flps.model.FlpsScore
import com.edgerush.lootman.domain.flps.model.ItemPriorityIndex
import com.edgerush.lootman.domain.flps.model.MechanicalAdherenceScore
import com.edgerush.lootman.domain.flps.model.RaiderMeritScore
import com.edgerush.lootman.domain.flps.model.RecencyDecayFactor
import com.edgerush.lootman.domain.flps.model.RoleMultiplier
import com.edgerush.lootman.domain.flps.model.TierBonus
import com.edgerush.lootman.domain.flps.model.UpgradeValue
import com.edgerush.lootman.domain.flps.repository.FlpsModifierRepository
import com.edgerush.lootman.domain.flps.service.FlpsCalculationService
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import com.edgerush.lootman.domain.shared.RaiderId
import org.springframework.stereotype.Service

/**
 * Use case for calculating FLPS score for a raider and item.
 *
 * This use case orchestrates the FLPS calculation by:
 * 1. Retrieving guild-specific modifiers
 * 2. Calculating RMS from component scores
 * 3. Calculating IPI from component values
 * 4. Calculating final FLPS using the domain service
 */
@Service
class CalculateFlpsScoreUseCase(
    private val flpsCalculationService: FlpsCalculationService,
    private val modifierRepository: FlpsModifierRepository,
) {
    /**
     * Executes the FLPS calculation.
     *
     * @param command The calculation command with all required inputs
     * @return Result containing FlpsCalculationResult or error
     */
    fun execute(command: CalculateFlpsScoreCommand): Result<FlpsCalculationResult> =
        runCatching {
            // Retrieve guild-specific modifiers
            val modifiers = modifierRepository.findByGuildId(command.guildId)

            // Calculate RMS using guild-specific weights
            val rms =
                RaiderMeritScore.fromComponents(
                    command.acs,
                    command.mas,
                    command.eps,
                    modifiers.rmsWeights.attendance,
                    modifiers.rmsWeights.mechanical,
                    modifiers.rmsWeights.preparation,
                )

            // Calculate IPI using guild-specific weights
            val ipi =
                ItemPriorityIndex.fromComponents(
                    command.uv,
                    command.tb,
                    command.rm,
                    modifiers.ipiWeights.upgradeValue,
                    modifiers.ipiWeights.tierBonus,
                    modifiers.ipiWeights.roleMultiplier,
                )

            // Calculate final FLPS
            val flps = flpsCalculationService.calculateFlps(rms, ipi, command.rdf)

            // Determine eligibility
            val eligible =
                command.acs.value >= modifiers.thresholds.eligibilityAttendance &&
                    command.mas.value > modifiers.thresholds.eligibilityActivity

            FlpsCalculationResult(
                guildId = command.guildId,
                raiderId = command.raiderId,
                itemId = command.itemId,
                acs = command.acs,
                mas = command.mas,
                eps = command.eps,
                rms = rms,
                uv = command.uv,
                tb = command.tb,
                rm = command.rm,
                ipi = ipi,
                rdf = command.rdf,
                flps = flps,
                eligible = eligible,
            )
        }
}

/**
 * Command for calculating FLPS score.
 */
data class CalculateFlpsScoreCommand(
    val guildId: GuildId,
    val raiderId: RaiderId,
    val itemId: ItemId,
    val acs: AttendanceCommitmentScore,
    val mas: MechanicalAdherenceScore,
    val eps: ExternalPreparationScore,
    val uv: UpgradeValue,
    val tb: TierBonus,
    val rm: RoleMultiplier,
    val rdf: RecencyDecayFactor,
)

/**
 * Result of FLPS calculation.
 */
data class FlpsCalculationResult(
    val guildId: GuildId,
    val raiderId: RaiderId,
    val itemId: ItemId,
    val acs: AttendanceCommitmentScore,
    val mas: MechanicalAdherenceScore,
    val eps: ExternalPreparationScore,
    val rms: RaiderMeritScore,
    val uv: UpgradeValue,
    val tb: TierBonus,
    val rm: RoleMultiplier,
    val ipi: ItemPriorityIndex,
    val rdf: RecencyDecayFactor,
    val flps: FlpsScore,
    val eligible: Boolean,
)
