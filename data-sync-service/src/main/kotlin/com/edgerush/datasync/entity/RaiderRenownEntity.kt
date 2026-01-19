package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raider_renown")
data class RaiderRenownEntity(
    @Id
    val id: Long? = null,
    @Column("raider_id")
    val raiderId: Long,
    @Column("faction")
    val faction: String,
    @Column("level")
    val level: Int?,
)
