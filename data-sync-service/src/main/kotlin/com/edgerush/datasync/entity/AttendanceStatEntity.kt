package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.OffsetDateTime

@Table("attendance_stats")
data class AttendanceStatEntity(
    @Id
    val id: Long? = null,
    @Column("instance")
    val instance: String?,
    @Column("encounter")
    val encounter: String?,
    @Column("start_date")
    val startDate: LocalDate?,
    @Column("end_date")
    val endDate: LocalDate?,
    @Column("character_id")
    val characterId: Long?,
    @Column("character_name")
    val characterName: String,
    @Column("character_realm")
    val characterRealm: String?,
    @Column("character_class")
    val characterClass: String?,
    @Column("character_role")
    val characterRole: String?,
    @Column("character_region")
    val characterRegion: String?,
    @Column("attended_amount_of_raids")
    val attendedAmountOfRaids: Int?,
    @Column("total_amount_of_raids")
    val totalAmountOfRaids: Int?,
    @Column("attended_percentage")
    val attendedPercentage: Double?,
    @Column("selected_amount_of_encounters")
    val selectedAmountOfEncounters: Int?,
    @Column("total_amount_of_encounters")
    val totalAmountOfEncounters: Int?,
    @Column("selected_percentage")
    val selectedPercentage: Double?,
    @Column("team_id")
    val teamId: Long?,
    @Column("season_id")
    val seasonId: Long?,
    @Column("period_id")
    val periodId: Long?,
    @Column("synced_at")
    val syncedAt: OffsetDateTime,
)
