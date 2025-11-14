package com.edgerush.datasync.model.warcraftlogs

/**
 * Represents performance data for a character in a specific fight
 */
data class CharacterPerformanceData(
    val name: String,
    val server: String,
    val `class`: String,
    val spec: String,
    val deaths: Int,
    val damageTaken: Long,
    val avoidableDamageTaken: Long,
    val itemLevel: Int,
    val performancePercentile: Double?,
)
