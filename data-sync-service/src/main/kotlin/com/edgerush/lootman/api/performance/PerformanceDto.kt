package com.edgerush.lootman.api.performance

import java.time.Instant

/**
 * Response DTO for performance metrics.
 * Contains DPA (Deaths Per Attempt) and ADT (Avoidable Damage Taken) metrics.
 */
data class PerformanceMetricsResponse(
    val raiderId: Long,
    val characterName: String,
    /** Deaths per attempt (lower is better) */
    val dpa: Double,
    /** Avoidable damage taken percentage (lower is better) */
    val adt: Double,
    /** Spec average for comparison (placeholder) */
    val specAverage: Double,
    /** Performance trend over time */
    val performanceTrend: List<PerformanceDataPoint>,
    /** Last time performance data was updated */
    val lastUpdated: String,
)

/**
 * Single data point in performance trend.
 */
data class PerformanceDataPoint(
    val date: String,
    val dpa: Double,
    val adt: Double,
)
