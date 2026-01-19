package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

@Table("team_raid_days")
data class TeamRaidDayEntity(
    @Id
    val id: Long? = null,
    @Column("team_id")
    val teamId: Long,
    @Column("week_day")
    val weekDay: String?,
    @Column("start_time")
    val startTime: LocalTime?,
    @Column("end_time")
    val endTime: LocalTime?,
    @Column("current_instance")
    val currentInstance: String?,
    @Column("difficulty")
    val difficulty: String?,
    @Column("active_from")
    val activeFrom: LocalDate?,
    @Column("synced_at")
    val syncedAt: OffsetDateTime,
)
