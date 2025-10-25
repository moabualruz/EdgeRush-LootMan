package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.mapping.Column
import java.time.OffsetDateTime

@Table("raiders")
data class RaiderEntity(
    @Id
    val id: Long? = null,
    val characterName: String,
    val realm: String,
    val region: String,
    @Column("wowaudit_id")
    val wowauditId: Long?,
    @Column("class")
    val clazz: String,
    val spec: String,
    val role: String,
    val rank: String?,
    val status: String?,
    val note: String?,
    @Column("blizzard_id")
    val blizzardId: Long?,
    @Column("tracking_since")
    val trackingSince: OffsetDateTime?,
    @Column("join_date")
    val joinDate: OffsetDateTime?,
    @Column("blizzard_last_modified")
    val blizzardLastModified: OffsetDateTime?,
    val lastSync: OffsetDateTime
)
