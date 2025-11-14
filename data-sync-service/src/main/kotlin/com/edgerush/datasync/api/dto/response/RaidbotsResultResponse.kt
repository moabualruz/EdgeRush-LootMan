package com.edgerush.datasync.api.dto.response

import java.time.Instant

data class RaidbotsResultResponse(
    val id: Long?,
    val simulationId: Long,
    val itemId: Long,
    val itemName: String,
    val slot: String,
    val dpsGain: Double,
    val percentGain: Double,
    val calculatedAt: Instant,
)
