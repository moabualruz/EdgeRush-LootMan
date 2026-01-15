package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raider_crest_counts")
data class RaiderCrestCountEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    @Column("crest_type")
    val crestType: String,
    @Column("crest_count")
    val crestCount: Int?,
)
