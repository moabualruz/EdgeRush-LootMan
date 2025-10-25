package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("raid_signups")
data class RaidSignupEntity(
    @Id
    val id: Long? = null,
    val raidId: Long,
    val characterId: Long?,
    val characterName: String?,
    val characterRealm: String?,
    val characterClass: String?,
    val characterRole: String?,
    val status: String?,
    val comment: String?,
    val selected: Boolean?
)

