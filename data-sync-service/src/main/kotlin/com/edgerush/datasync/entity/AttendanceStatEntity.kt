package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.OffsetDateTime

@Table("attendance_stats")
data class AttendanceStatEntity(
    @Id
    val id: Long? = null,
    val instance: String?,
    val encounter: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val characterId: Long?,
    val characterName: String,
    val characterRealm: String?,
    val characterClass: String?,
    val characterRole: String?,
    val attendedAmountOfRaids: Int?,
    val totalAmountOfRaids: Int?,
    val attendedPercentage: Double?,
    val selectedAmountOfEncounters: Int?,
    val totalAmountOfEncounters: Int?,
    val selectedPercentage: Double?,
    val syncedAt: OffsetDateTime
)
