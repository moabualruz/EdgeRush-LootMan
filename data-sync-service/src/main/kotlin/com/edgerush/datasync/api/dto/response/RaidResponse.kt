package com.edgerush.datasync.api.dto.response

import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

data class RaidResponse(
    val raidId: Long,
    val date: LocalDate?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val instance: String?,
    val difficulty: String?,
    val optional: Boolean?,
    val status: String?,
    val presentSize: Int?,
    val totalSize: Int?,
    val notes: String?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val syncedAt: OffsetDateTime
)
