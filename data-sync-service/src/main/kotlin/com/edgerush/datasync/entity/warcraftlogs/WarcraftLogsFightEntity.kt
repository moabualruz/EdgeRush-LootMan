package com.edgerush.datasync.entity.warcraftlogs

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("warcraft_logs_fights")
data class WarcraftLogsFightEntity(
    @Id val id: Long? = null,
    val reportId: Long,
    val fightId: Int,
    val encounterId: Int,
    val encounterName: String,
    val difficulty: String,
    val kill: Boolean,
    val startTime: Long,
    val endTime: Long,
    val bossPercentage: Double?,
)
