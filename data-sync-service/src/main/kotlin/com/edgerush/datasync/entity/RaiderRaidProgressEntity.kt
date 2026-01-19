package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raider_raid_progress")
data class RaiderRaidProgressEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    @Column("raid")
    val raid: String,
    @Column("difficulty")
    val difficulty: String,
    @Column("bosses_defeated")
    val bossesDefeated: Int?,
)
