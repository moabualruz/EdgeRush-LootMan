package com.edgerush.lootman.api.flps

import com.edgerush.lootman.application.flps.FlpsComponentCalculator
import com.edgerush.lootman.application.flps.FlpsDataAssemblerService
import com.edgerush.lootman.domain.flps.model.ItemPriorityIndex
import com.edgerush.lootman.domain.flps.model.RaiderMeritScore
import com.edgerush.lootman.domain.flps.repository.FlpsModifierRepository
import com.edgerush.lootman.domain.flps.service.FlpsCalculationService
import com.edgerush.lootman.domain.shared.GuildId
import com.edgerush.lootman.domain.shared.ItemId
import org.springframework.stereotype.Service
import kotlin.math.abs

/**
 * Service for previewing FLPS configuration changes.
 *
 * This service allows guild administrators to see how configuration changes
 * would affect FLPS scores before applying them, enabling data-driven
 * decision-making for guild policy adjustments.
 */
@Service
class FlpsConfigPreviewService(
    private val modifierRepository: FlpsModifierRepository,
    private val flpsDataAssembler: FlpsDataAssemblerService,
    private val componentCalculator: FlpsComponentCalculator,
    private val flpsCalculationService: FlpsCalculationService,
) {
    /**
     * Preview the impact of configuration changes on all raiders' FLPS scores.
     *
     * @param guildId The guild to preview changes for
     * @param request The proposed configuration changes
     * @return Preview response with impact analysis
     */
    fun previewConfigChanges(
        guildId: String,
        request: ConfigPreviewRequest,
    ): ConfigPreviewResponse {
        val guildIdObj = GuildId(guildId)

        // Get current configuration
        val currentModifiers = modifierRepository.findByGuildId(guildIdObj)

        // Create proposed configuration by merging changes
        val proposedModifiers = currentModifiers.mergeWith(request)

        // Get all raider data
        val raiderDataList = flpsDataAssembler.assembleFlpsData(guildIdObj)

        // Use a placeholder item for score calculation
        val exampleItemId = ItemId(12345L)

        // Calculate scores with both configurations
        data class ScoreComparison(
            val raiderId: Long,
            val raiderName: String,
            val currentScore: Double,
            val proposedScore: Double,
            val currentEligible: Boolean,
            val proposedEligible: Boolean,
        )

        val comparisons =
            raiderDataList.map { raiderData ->
                // Calculate component scores (these don't change with config)
                val acs = componentCalculator.calculateACS(raiderData.attendance)
                val mas = componentCalculator.calculateMAS()
                val eps = componentCalculator.calculateEPS(raiderData.gear)
                val uv = componentCalculator.calculateUV(raiderData.wishlist, exampleItemId)
                val tb = componentCalculator.calculateTierBonus(raiderData.gear)
                val rm = componentCalculator.calculateRoleMultiplier(raiderData.raider.role)
                val rdf = componentCalculator.calculateRDF(raiderData.lootHistory, raiderData.activeBans)

                // Calculate current score
                val currentRms =
                    RaiderMeritScore.fromComponents(
                        acs,
                        mas,
                        eps,
                        currentModifiers.rmsWeights.attendance,
                        currentModifiers.rmsWeights.mechanical,
                        currentModifiers.rmsWeights.preparation,
                    )
                val currentIpi =
                    ItemPriorityIndex.fromComponents(
                        uv,
                        tb,
                        rm,
                        currentModifiers.ipiWeights.upgradeValue,
                        currentModifiers.ipiWeights.tierBonus,
                        currentModifiers.ipiWeights.roleMultiplier,
                    )
                val currentFlps = flpsCalculationService.calculateFlps(currentRms, currentIpi, rdf)
                val currentEligible =
                    acs.value >= currentModifiers.thresholds.eligibilityAttendance &&
                        mas.value > currentModifiers.thresholds.eligibilityActivity

                // Calculate proposed score
                val proposedRms =
                    RaiderMeritScore.fromComponents(
                        acs,
                        mas,
                        eps,
                        proposedModifiers.rmsWeights.attendance,
                        proposedModifiers.rmsWeights.mechanical,
                        proposedModifiers.rmsWeights.preparation,
                    )
                val proposedIpi =
                    ItemPriorityIndex.fromComponents(
                        uv,
                        tb,
                        rm,
                        proposedModifiers.ipiWeights.upgradeValue,
                        proposedModifiers.ipiWeights.tierBonus,
                        proposedModifiers.ipiWeights.roleMultiplier,
                    )
                val proposedFlps = flpsCalculationService.calculateFlps(proposedRms, proposedIpi, rdf)
                val proposedEligible =
                    acs.value >= proposedModifiers.thresholds.eligibilityAttendance &&
                        mas.value > proposedModifiers.thresholds.eligibilityActivity

                ScoreComparison(
                    raiderId = raiderData.raider.id.value,
                    raiderName = raiderData.raider.characterName,
                    currentScore = currentFlps.value,
                    proposedScore = proposedFlps.value,
                    currentEligible = currentEligible,
                    proposedEligible = proposedEligible,
                )
            }

        // Sort by current score to establish rankings
        val sortedByCurrent = comparisons.sortedByDescending { it.currentScore }
        val sortedByProposed = comparisons.sortedByDescending { it.proposedScore }

        // Create ranking maps
        val currentRankMap = sortedByCurrent.mapIndexed { index, comp -> comp.raiderId to (index + 1) }.toMap()
        val proposedRankMap = sortedByProposed.mapIndexed { index, comp -> comp.raiderId to (index + 1) }.toMap()

        // Build raider impacts
        val raiderImpacts =
            comparisons.map { comp ->
                val currentRank = currentRankMap[comp.raiderId] ?: 0
                val proposedRank = proposedRankMap[comp.raiderId] ?: 0

                RaiderImpact(
                    raiderId = comp.raiderId,
                    raiderName = comp.raiderName,
                    currentScore = comp.currentScore,
                    proposedScore = comp.proposedScore,
                    scoreDelta = comp.proposedScore - comp.currentScore,
                    currentRank = currentRank,
                    proposedRank = proposedRank,
                    rankDelta = currentRank - proposedRank, // Positive means improved rank
                    currentEligible = comp.currentEligible,
                    proposedEligible = comp.proposedEligible,
                    eligibilityChanged = comp.currentEligible != comp.proposedEligible,
                )
            }.sortedByDescending { abs(it.scoreDelta) } // Sort by impact magnitude

        // Calculate impact summary
        val scoreDeltas = raiderImpacts.map { it.scoreDelta }
        val eligibilityGained = raiderImpacts.count { !it.currentEligible && it.proposedEligible }
        val eligibilityLost = raiderImpacts.count { it.currentEligible && !it.proposedEligible }
        val rankingChanges = raiderImpacts.count { it.rankDelta != 0 }

        val impactSummary =
            ImpactSummary(
                totalRaidersAffected = raiderImpacts.count { it.scoreDelta != 0.0 },
                averageScoreChange = if (scoreDeltas.isNotEmpty()) scoreDeltas.average() else 0.0,
                maxScoreIncrease = scoreDeltas.filter { it > 0 }.maxOrNull() ?: 0.0,
                maxScoreDecrease = scoreDeltas.filter { it < 0 }.minOrNull() ?: 0.0,
                eligibilityChanges =
                    EligibilityChanges(
                        gained = eligibilityGained,
                        lost = eligibilityLost,
                        unchanged = raiderImpacts.size - eligibilityGained - eligibilityLost,
                    ),
                rankingChanges = rankingChanges,
            )

        return ConfigPreviewResponse(
            guildId = guildId,
            currentConfig = FlpsConfigSummary.from(currentModifiers),
            proposedConfig = FlpsConfigSummary.from(proposedModifiers),
            impactSummary = impactSummary,
            raiderImpacts = raiderImpacts,
        )
    }

    /**
     * Get the current configuration for a guild.
     *
     * @param guildId The guild ID
     * @return Current configuration summary
     */
    fun getCurrentConfig(guildId: String): FlpsConfigSummary {
        val modifiers = modifierRepository.findByGuildId(GuildId(guildId))
        return FlpsConfigSummary.from(modifiers)
    }
}
