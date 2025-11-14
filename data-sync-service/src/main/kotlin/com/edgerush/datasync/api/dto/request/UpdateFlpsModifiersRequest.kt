package com.edgerush.datasync.api.dto.request

/**
 * Request DTO for updating FLPS modifiers and configuration.
 */
data class UpdateFlpsModifiersRequest(
    val rmsWeights: RmsWeightsRequest,
    val ipiWeights: IpiWeightsRequest,
    val thresholds: FlpsThresholdsRequest,
    val roleMultipliers: RoleMultipliersRequest,
    val recencyPenalties: RecencyPenaltiesRequest
)

data class RmsWeightsRequest(
    val attendance: Double,
    val mechanical: Double,
    val preparation: Double
)

data class IpiWeightsRequest(
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double
)

data class FlpsThresholdsRequest(
    val eligibilityAttendance: Double,
    val eligibilityActivity: Double
)

data class RoleMultipliersRequest(
    val tank: Double,
    val healer: Double,
    val dps: Double
)

data class RecencyPenaltiesRequest(
    val tierA: Double,
    val tierB: Double,
    val tierC: Double,
    val recoveryRate: Double
)
