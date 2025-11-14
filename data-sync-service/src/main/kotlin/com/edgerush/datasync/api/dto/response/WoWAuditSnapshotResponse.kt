package com.edgerush.datasync.api.dto.response

import java.time.OffsetDateTime

data class WoWAuditSnapshotResponse(
    val id: Long?,
    val endpoint: String,
    val rawPayload: String,
    val syncedAt: OffsetDateTime,
)
