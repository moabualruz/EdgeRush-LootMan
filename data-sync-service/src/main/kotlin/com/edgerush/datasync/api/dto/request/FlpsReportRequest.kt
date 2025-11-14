package com.edgerush.datasync.api.dto.request

/**
 * Request DTO for generating FLPS report for multiple raiders.
 */
data class FlpsReportRequest(
    val guildId: String,
    val raiders: List<RaiderFlpsDataRequest>
)

/**
 * Input data for a single raider's FLPS calculation in a report.
 */
data class RaiderFlpsDataRequest(
    val raiderId: String,
    val raiderName: String,
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
