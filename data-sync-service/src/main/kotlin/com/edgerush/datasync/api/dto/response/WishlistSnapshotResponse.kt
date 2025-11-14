package com.edgerush.datasync.api.dto.response

import java.time.OffsetDateTime

data class WishlistSnapshotResponse(
    val id: Long?,
    val raiderId: Long?,
    val characterName: String,
    val characterRealm: String,
    val characterRegion: String?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val rawPayload: String,
    val syncedAt: OffsetDateTime
)
