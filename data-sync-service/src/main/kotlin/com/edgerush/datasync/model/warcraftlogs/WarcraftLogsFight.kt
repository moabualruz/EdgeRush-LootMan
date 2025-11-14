package com.edgerush.datasync.model.warcraftlogs

/**
 * Represents a single boss encounter attempt within a Warcraft Logs report
 */
data class WarcraftLogsFight(
    val id: Int,
    val encounterID: Int,
    val name: String,
    val difficulty: Int,
    val kill: Boolean,
    val startTime: Long,
    val endTime: Long,
    val bossPercentage: Double?
)
