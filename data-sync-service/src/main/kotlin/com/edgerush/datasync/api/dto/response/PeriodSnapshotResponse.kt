package com.edgerush.datasync.api.dto.response

import java.time.OffsetDateTime

data class PeriodSnapshotResponse(
    val id: Long?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val currentPeriod: Long?,
    val fetchedAt: OffsetDateTime,
)
