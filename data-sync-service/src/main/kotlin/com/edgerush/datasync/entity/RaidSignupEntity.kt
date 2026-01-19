package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("raid_signups")
data class RaidSignupEntity(
    @Id
    val id: Long? = null,
    @Column("raid_id")
    val raidId: Long,
    @Column("character_id")
    val characterId: Long?,
    @Column("character_name")
    val characterName: String?,
    @Column("character_realm")
    val characterRealm: String?,
    @Column("character_region")
    val characterRegion: String?,
    @Column("character_class")
    val characterClass: String?,
    @Column("character_role")
    val characterRole: String?,
    @Column("character_guest")
    val characterGuest: Boolean?,
    @Column("status")
    val status: String?,
    @Column("comment")
    val comment: String?,
    @Column("selected")
    val selected: Boolean?,
)
