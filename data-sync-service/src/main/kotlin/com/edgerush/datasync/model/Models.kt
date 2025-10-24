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
    val lastAwards: List<LootAward>
)

data class LootAward(
    val tier: LootTier,
    val awardedOn: java.time.LocalDate
)

enum class Role { DPS, Tank, Healer }

enum class LootTier { A, B, C }

data class FlpsBreakdown(
    val name: String,
    val role: Role,
    val acs: Double,
    val mas: Double,
    val eps: Double,
    val rms: Double,
    val upgradeValue: Double,
    val tierBonus: Double,
    val roleMultiplier: Double,
    val ipi: Double,
    val rdf: Double,
    val flps: Double,
    val eligible: Boolean
)
