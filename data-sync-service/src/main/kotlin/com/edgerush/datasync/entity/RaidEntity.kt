package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime

@Table("raids")
data class RaidEntity(
    @Id
    @Column("raid_id")
    val raidId: Long,
    @Column("date")
    val date: LocalDate?,
    @Column("start_time")
    val startTime: LocalTime?,
    @Column("end_time")
    val endTime: LocalTime?,
    @Column("instance")
    val instance: String?,
    @Column("difficulty")
    val difficulty: String?,
    @Column("optional")
    val optional: Boolean?,
    @Column("status")
    val status: String?,
    @Column("present_size")
    val presentSize: Int?,
    @Column("total_size")
    val totalSize: Int?,
    @Column("notes")
    val notes: String?,
    @Column("selections_image")
    val selectionsImage: String?,
    @Column("team_id")
    val teamId: Long?,
    @Column("season_id")
    val seasonId: Long?,
    @Column("period_id")
    val periodId: Long?,
    @Column("created_at")
    val createdAt: OffsetDateTime?,
    @Column("updated_at")
    val updatedAt: OffsetDateTime?,
    @Column("synced_at")
    val syncedAt: OffsetDateTime,
)
