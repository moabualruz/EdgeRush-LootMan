package com.edgerush.datasync.api.dto.response

import java.time.LocalDate
import java.time.OffsetDateTime

data class AttendanceStatResponse(
    val id: Long?,
    val instance: String?,
    val encounter: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val characterId: Long?,
    val characterName: String,
    val characterRealm: String?,
    val characterClass: String?,
    val characterRole: String?,
    val characterRegion: String?,
    val attendedAmountOfRaids: Int?,
    val totalAmountOfRaids: Int?,
    val attendedPercentage: Double?,
    val selectedAmountOfEncounters: Int?,
    val totalAmountOfEncounters: Int?,
    val selectedPercentage: Double?,
    val teamId: Long?,
    val seasonId: Long?,
    val periodId: Long?,
    val syncedAt: OffsetDateTime
)
