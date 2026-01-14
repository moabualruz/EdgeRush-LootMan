package com.edgerush.lootman.api.flps

import com.edgerush.lootman.domain.flps.repository.FlpsModifiers
import com.edgerush.lootman.domain.flps.repository.IpiWeights
import com.edgerush.lootman.domain.flps.repository.RmsWeights
import com.edgerush.lootman.domain.flps.repository.RoleMultipliers
import com.edgerush.lootman.domain.flps.repository.FlpsThresholds
import com.edgerush.lootman.domain.shared.GuildId

/**
 * Request to preview configuration changes.
 *
 * Contains the proposed configuration changes to simulate.
 * Any null fields will use the current (unchanged) value.
 */
data class ConfigPreviewRequest(
    val rmsWeights: RmsWeightsRequest? = null,
    val ipiWeights: IpiWeightsRequest? = null,
    val roleMultipliers: RoleMultipliersRequest? = null,
    val thresholds: ThresholdsRequest? = null,
)

/**
 * Proposed RMS weight changes.
 */
data class RmsWeightsRequest(
    val attendance: Double? = null,
    val mechanical: Double? = null,
    val preparation: Double? = null,
)

/**
 * Proposed IPI weight changes.
 */
data class IpiWeightsRequest(
    val upgradeValue: Double? = null,
    val tierBonus: Double? = null,
    val roleMultiplier: Double? = null,
)

/**
 * Proposed role multiplier changes.
 */
data class RoleMultipliersRequest(
    val dps: Double? = null,
    val tank: Double? = null,
    val healer: Double? = null,
)

/**
 * Proposed threshold changes.
 */
data class ThresholdsRequest(
    val eligibilityAttendance: Double? = null,
    val eligibilityActivity: Double? = null,
)

/**
 * Response containing the preview results.
 */
data class ConfigPreviewResponse(
    val guildId: String,
    val currentConfig: FlpsConfigSummary,
    val proposedConfig: FlpsConfigSummary,
    val impactSummary: ImpactSummary,
    val raiderImpacts: List<RaiderImpact>,
)

/**
 * Summary of FLPS configuration settings.
 */
data class FlpsConfigSummary(
    val rmsWeights: RmsWeightsResponse,
    val ipiWeights: IpiWeightsResponse,
    val roleMultipliers: RoleMultipliersResponse,
    val thresholds: ThresholdsResponse,
) {
    companion object {
        fun from(modifiers: FlpsModifiers): FlpsConfigSummary = FlpsConfigSummary(
            rmsWeights = RmsWeightsResponse(
                attendance = modifiers.rmsWeights.attendance,
                mechanical = modifiers.rmsWeights.mechanical,
                preparation = modifiers.rmsWeights.preparation,
            ),
            ipiWeights = IpiWeightsResponse(
                upgradeValue = modifiers.ipiWeights.upgradeValue,
                tierBonus = modifiers.ipiWeights.tierBonus,
                roleMultiplier = modifiers.ipiWeights.roleMultiplier,
            ),
            roleMultipliers = RoleMultipliersResponse(
                dps = modifiers.roleMultipliers.dps,
                tank = modifiers.roleMultipliers.tank,
                healer = modifiers.roleMultipliers.healer,
            ),
            thresholds = ThresholdsResponse(
                eligibilityAttendance = modifiers.thresholds.eligibilityAttendance,
                eligibilityActivity = modifiers.thresholds.eligibilityActivity,
            ),
        )
    }
}

/**
 * RMS weight values in response.
 */
data class RmsWeightsResponse(
    val attendance: Double,
    val mechanical: Double,
    val preparation: Double,
)

/**
 * IPI weight values in response.
 */
data class IpiWeightsResponse(
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double,
)

/**
 * Role multiplier values in response.
 */
data class RoleMultipliersResponse(
    val dps: Double,
    val tank: Double,
    val healer: Double,
)

/**
 * Threshold values in response.
 */
data class ThresholdsResponse(
    val eligibilityAttendance: Double,
    val eligibilityActivity: Double,
)

/**
 * Summary of the overall impact of the configuration changes.
 */
data class ImpactSummary(
    val totalRaidersAffected: Int,
    val averageScoreChange: Double,
    val maxScoreIncrease: Double,
    val maxScoreDecrease: Double,
    val eligibilityChanges: EligibilityChanges,
    val rankingChanges: Int,
)

/**
 * Summary of eligibility changes.
 */
data class EligibilityChanges(
    val gained: Int,
    val lost: Int,
    val unchanged: Int,
)

/**
 * Individual raider's score impact from configuration changes.
 */
data class RaiderImpact(
    val raiderId: Long,
    val raiderName: String,
    val currentScore: Double,
    val proposedScore: Double,
    val scoreDelta: Double,
    val currentRank: Int,
    val proposedRank: Int,
    val rankDelta: Int,
    val currentEligible: Boolean,
    val proposedEligible: Boolean,
    val eligibilityChanged: Boolean,
)

/**
 * Helper function to merge proposed changes with current modifiers.
 */
fun FlpsModifiers.mergeWith(request: ConfigPreviewRequest): FlpsModifiers {
    return FlpsModifiers(
        guildId = this.guildId,
        rmsWeights = RmsWeights(
            attendance = request.rmsWeights?.attendance ?: this.rmsWeights.attendance,
            mechanical = request.rmsWeights?.mechanical ?: this.rmsWeights.mechanical,
            preparation = request.rmsWeights?.preparation ?: this.rmsWeights.preparation,
        ),
        ipiWeights = IpiWeights(
            upgradeValue = request.ipiWeights?.upgradeValue ?: this.ipiWeights.upgradeValue,
            tierBonus = request.ipiWeights?.tierBonus ?: this.ipiWeights.tierBonus,
            roleMultiplier = request.ipiWeights?.roleMultiplier ?: this.ipiWeights.roleMultiplier,
        ),
        roleMultipliers = RoleMultipliers(
            dps = request.roleMultipliers?.dps ?: this.roleMultipliers.dps,
            tank = request.roleMultipliers?.tank ?: this.roleMultipliers.tank,
            healer = request.roleMultipliers?.healer ?: this.roleMultipliers.healer,
        ),
        thresholds = FlpsThresholds(
            eligibilityAttendance = request.thresholds?.eligibilityAttendance
                ?: this.thresholds.eligibilityAttendance,
            eligibilityActivity = request.thresholds?.eligibilityActivity
                ?: this.thresholds.eligibilityActivity,
        ),
    )
}
