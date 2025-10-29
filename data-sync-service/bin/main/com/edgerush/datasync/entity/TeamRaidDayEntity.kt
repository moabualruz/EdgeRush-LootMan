package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

@Table("team_raid_days")
data class TeamRaidDayEntity(
    @Id
    val id: Long? = null,
    val teamId: Long,
    val weekDay: String?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val currentInstance: String?,
    val difficulty: String?,
    val activeFrom: LocalDate?,
    val syncedAt: OffsetDateTime
)
