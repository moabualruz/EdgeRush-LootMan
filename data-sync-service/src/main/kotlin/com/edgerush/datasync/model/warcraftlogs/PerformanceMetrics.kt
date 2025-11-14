package com.edgerush.datasync.model.warcraftlogs

/**
 * Aggregated performance metrics for a character over multiple fights
 */
data class PerformanceMetrics(
    val characterName: String,
    val characterRealm: String,
    val totalDeaths: Int,
    val totalAttempts: Int,
    val deathsPerAttempt: Double,
    val averageAvoidableDamagePercentage: Double,
    val fightCount: Int,
)
