package com.edgerush.datasync.api.dto.response

/**
 * Response DTO for comprehensive FLPS report.
 */
data class FlpsReportResponse(
    val guildId: String,
    val raiderReports: List<RaiderFlpsReportResponse>,
    val configuration: FlpsConfigurationResponse
)

/**
 * Response DTO for a single raider's FLPS report.
 */
data class RaiderFlpsReportResponse(
    val raiderId: String,
    val raiderName: String,
    // Final scores
    val flpsScore: Double,
    val raiderMerit: Double,
    val itemPriority: Double,
    val recencyDecay: Double,
    // Component scores (raw values)
    val attendanceScore: Double,
    val mechanicalScore: Double,
    val preparationScore: Double,
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double,
    // Percentages (relative to perfect scores)
    val attendancePercentage: Double,
    val mechanicalPercentage: Double,
    val preparationPercentage: Double,
    val rmsPercentage: Double,
    val upgradeValuePercentage: Double,
    val tierBonusPercentage: Double,
    val roleMultiplierPercentage: Double,
    val ipiPercentage: Double,
    val recencyDecayPercentage: Double,
    val flpsPercentage: Double
)

/**
 * Response DTO for FLPS configuration.
 */
data class FlpsConfigurationResponse(
    val rmsWeights: RmsWeightsResponse,
    val ipiWeights: IpiWeightsResponse,
    val thresholds: FlpsThresholdsResponse,
    val roleMultipliers: RoleMultipliersResponse,
    val recencyPenalties: RecencyPenaltiesResponse
)

data class RmsWeightsResponse(
    val attendance: Double,
    val mechanical: Double,
    val preparation: Double
)

data class IpiWeightsResponse(
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double
)

data class FlpsThresholdsResponse(
    val eligibilityAttendance: Double,
    val eligibilityActivity: Double
)

data class RoleMultipliersResponse(
    val tank: Double,
    val healer: Double,
    val dps: Double
)

data class RecencyPenaltiesResponse(
    val tierA: Double,
    val tierB: Double,
    val tierC: Double,
    val recoveryRate: Double
)
