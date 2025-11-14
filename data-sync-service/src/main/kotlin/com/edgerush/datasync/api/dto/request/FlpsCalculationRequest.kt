package com.edgerush.datasync.api.dto.request

/**
 * Request DTO for calculating FLPS score for a single raider and item.
 */
data class FlpsCalculationRequest(
    val guildId: String,
    // RMS inputs
    val attendancePercent: Int,
    val deathsPerAttempt: Double,
    val specAvgDpa: Double,
    val avoidableDamagePct: Double,
    val specAvgAdt: Double,
    val vaultSlots: Int,
    val crestUsageRatio: Double,
    val heroicKills: Int,
    // IPI inputs
    val simulatedGain: Double,
    val specBaseline: Double,
    val tierPiecesOwned: Int,
    val role: String, // "TANK", "HEALER", "DPS"
    // RDF inputs
    val recentLootCount: Int
)
