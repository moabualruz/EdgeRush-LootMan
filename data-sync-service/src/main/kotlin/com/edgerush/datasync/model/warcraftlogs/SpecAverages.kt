package com.edgerush.datasync.model.warcraftlogs

/**
 * Average performance metrics for a specific spec, used for normalization
 */
data class SpecAverages(
    val spec: String,
    val sampleSize: Int,
    val averageDeathsPerAttempt: Double,
    val averageAvoidableDamagePercentage: Double,
    val percentile: Int,
) {
    // Convenience properties for backward compatibility
    val averageDPA: Double get() = averageDeathsPerAttempt
    val averageADT: Double get() = averageAvoidableDamagePercentage
}
