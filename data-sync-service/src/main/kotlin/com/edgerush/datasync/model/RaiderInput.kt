package com.edgerush.datasync.model

data class RaiderInput(
    val name: String,
    val role: Role,
    val attendancePercent: Int,
    val deathsPerAttempt: Double,
    val avoidableDamagePct: Double,
    val specAverageDpa: Double,
    val specAverageAdt: Double,
    val vaultSlots: Int,
    val crestUsageRatio: Double,
    val heroicBossesCleared: Int,
    val tierPiecesOwned: Int,
    val simulatedGain: Double,
    val specBaselineOutput: Double,
    val lastAwards: List<LootAward>,
)
