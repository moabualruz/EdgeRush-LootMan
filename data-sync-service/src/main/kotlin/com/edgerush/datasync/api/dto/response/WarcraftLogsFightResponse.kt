package com.edgerush.datasync.api.dto.response


data class WarcraftLogsFightResponse(
    val id: Long?,
    val reportId: Long,
    val fightId: Int,
    val encounterId: Int,
    val encounterName: String,
    val difficulty: String,
    val kill: Boolean,
    val startTime: Long,
    val endTime: Long,
    val bossPercentage: Double?
)
