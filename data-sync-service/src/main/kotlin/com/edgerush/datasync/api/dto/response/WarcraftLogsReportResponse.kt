package com.edgerush.datasync.api.dto.response

import java.time.Instant

data class WarcraftLogsReportResponse(
    val id: Long?,
    val guildId: String,
    val reportCode: String,
    val title: String?,
    val startTime: Instant,
    val endTime: Instant,
    val owner: String?,
    val zone: Int?,
    val syncedAt: Instant,
    val rawMetadata: String?
)
