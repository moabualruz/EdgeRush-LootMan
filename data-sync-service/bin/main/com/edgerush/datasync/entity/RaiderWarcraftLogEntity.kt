package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raider_warcraft_logs")
data class RaiderWarcraftLogEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    val difficulty: String,
    val score: Int?
)
