package com.edgerush.datasync.api.dto.request

import jakarta.validation.constraints.*
import java.time.OffsetDateTime

data class CreatePeriodSnapshotRequest(
    val teamId: Long? = null,

    val seasonId: Long? = null,

    val periodId: Long? = null,

    val currentPeriod: Long? = null,

    val fetchedAt: OffsetDateTime? = null
)

data class UpdatePeriodSnapshotRequest(
    val teamId: Long? = null,

    val seasonId: Long? = null,

    val periodId: Long? = null,

    val currentPeriod: Long? = null,

    val fetchedAt: OffsetDateTime? = null
)
