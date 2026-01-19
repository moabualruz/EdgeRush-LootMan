package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("raiders")
data class RaiderEntity(
    @Id
    val id: Long? = null,
    @Column("character_name")
    val characterName: String,
    @Column("realm")
    val realm: String,
    @Column("region")
    val region: String,
    @Column("guild_id")
    val guildId: String? = null,
    @Column("wowaudit_id")
    val wowauditId: Long?,
    @Column("character_class")
    val clazz: String,
    @Column("spec")
    val spec: String,
    @Column("role")
    val role: String,
    @Column("rank")
    val rank: String?,
    @Column("status")
    val status: String?,
    @Column("note")
    val note: String?,
    @Column("blizzard_id")
    val blizzardId: Long?,
    @Column("tracking_since")
    val trackingSince: OffsetDateTime?,
    @Column("join_date")
    val joinDate: OffsetDateTime?,
    @Column("blizzard_last_modified")
    val blizzardLastModified: OffsetDateTime?,
    @Column("last_sync")
    val lastSync: OffsetDateTime,
    @Column("character_id")
    val characterId: Long? = null,
)
