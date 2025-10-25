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
    val selectionsImage: String?,
    val syncedAt: OffsetDateTime
)

