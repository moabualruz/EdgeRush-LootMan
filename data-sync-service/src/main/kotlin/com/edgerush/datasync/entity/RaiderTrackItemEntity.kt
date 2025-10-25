package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raider_track_items")
data class RaiderTrackItemEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    val tier: String,
    @Column("item_count")
    val itemCount: Int?
)
