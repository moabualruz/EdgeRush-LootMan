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

@Table("raid_signups")
data class RaidSignupEntity(
    @Id
    val id: Long? = null,
    val raidId: Long,
    val characterId: Long?,
    val characterName: String?,
    val characterRealm: String?,
    val characterClass: String?,
    val characterRole: String?,
    val status: String?,
    val comment: String?,
    val selected: Boolean?
)

@Table("raid_encounters")
data class RaidEncounterEntity(
    @Id
    val id: Long? = null,
    val raidId: Long,
    val encounterId: Long?,
    val name: String?,
    val enabled: Boolean?,
    val extra: Boolean?,
    val notes: String?
)
