package com.edgerush.datasync.api.dto.response

import java.time.OffsetDateTime

data class SyncRunResponse(
    val id: Long?,
    val source: String,
    val status: String,
    val startedAt: java.time.OffsetDateTime,
    val completedAt: java.time.OffsetDateTime?,
    val message: String?
)
