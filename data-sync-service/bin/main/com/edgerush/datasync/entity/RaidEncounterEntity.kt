package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

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

