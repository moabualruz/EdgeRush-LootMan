package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("user_characters")
data class UserCharacterEntity(
    @Id
    val id: Long? = null,
    @Column("user_id")
    val userId: Long,
    @Column("character_name")
    val characterName: String,
    @Column("realm")
    val realm: String,
    @Column("class_name")
    val className: String?,
    @Column("class_id")
    val classId: Int?,
    @Column("spec_id")
    val specId: Int?,
    @Column("level")
    val level: Int,
    @Column("playable_race")
    val playableRace: String,
    @Column("faction")
    val faction: String,
    @Column("blizzard_id")
    val blizzardId: Long?,
    @Column("guild_name")
    val guildName: String?,
    @Column("guild_realm")
    val guildRealm: String?,
    @Column("guild_id")
    val guildId: String?,
    @Column("last_synced_at")
    val lastSyncedAt: Instant,
)
