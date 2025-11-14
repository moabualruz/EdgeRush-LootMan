package com.edgerush.datasync.entity.warcraftlogs

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("warcraft_logs_performance")
data class WarcraftLogsPerformanceEntity(
    @Id val id: Long? = null,
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
