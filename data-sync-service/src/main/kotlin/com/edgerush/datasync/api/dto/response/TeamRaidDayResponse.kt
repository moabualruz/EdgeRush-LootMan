package com.edgerush.datasync.api.dto.response

import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

data class TeamRaidDayResponse(
    val id: Long?,
    val teamId: Long,
    val weekDay: String?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val currentInstance: String?,
    val difficulty: String?,
    val activeFrom: LocalDate?,
    val syncedAt: OffsetDateTime
)
