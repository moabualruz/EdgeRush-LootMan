package com.edgerush.datasync.api.dto.response

import java.time.Instant

data class WarcraftLogsPerformanceResponse(
    val id: Long?,
    val fightId: Long,
    val characterName: String,
    val characterRealm: String,
    val characterClass: String,
    val characterSpec: String,
    val deaths: Int,
    val damageTaken: Long,
    val avoidableDamageTaken: Long,
    val avoidableDamagePercentage: Double,
    val performancePercentile: Double?,
    val itemLevel: Int,
    val calculatedAt: Instant
)
