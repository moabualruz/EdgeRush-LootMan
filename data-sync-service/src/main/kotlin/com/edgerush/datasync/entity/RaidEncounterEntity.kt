package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raid_encounters")
data class RaidEncounterEntity(
    @Id
    val id: Long? = null,
    @Column("raid_id")
    val raidId: Long,
    @Column("encounter_id")
    val encounterId: Long?,
    @Column("name")
    val name: String?,
    @Column("enabled")
    val enabled: Boolean?,
    @Column("extra")
    val extra: Boolean?,
    @Column("notes")
    val notes: String?,
)
