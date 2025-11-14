package com.edgerush.datasync.model.warcraftlogs

/**
 * Represents a Warcraft Logs report containing one or more raid encounters
 */
data class WarcraftLogsReport(
    val code: String,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val owner: String,
    val zone: Int,
    val fights: List<WarcraftLogsFight> = emptyList()
)
