package com.edgerush.datasync.api.dto.response

/**
 * Response DTO for FLPS calculation result.
 */
data class FlpsCalculationResponse(
    val flpsScore: Double,
    val raiderMerit: Double,
    val itemPriority: Double,
    val recencyDecay: Double,
    // Component scores for transparency
    val attendanceScore: Double,
    val mechanicalScore: Double,
    val preparationScore: Double,
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double
)
