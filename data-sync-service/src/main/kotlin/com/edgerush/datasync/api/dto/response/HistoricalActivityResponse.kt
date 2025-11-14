package com.edgerush.datasync.api.dto.response

import java.time.OffsetDateTime

data class HistoricalActivityResponse(
    val id: Long?,
    val characterId: Long?,
    val characterName: String,
    val characterRealm: String?,
    val periodId: Long?,
    val teamId: Long?,
    val seasonId: Long?,
    val dataJson: String,
    val syncedAt: OffsetDateTime
)
