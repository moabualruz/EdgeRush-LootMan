package com.edgerush.datasync.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.OffsetDateTime

@Table("applications")
data class ApplicationEntity(
    @Id
    @Column("application_id")
    val applicationId: Long,
    @Column("applied_at")
    val appliedAt: OffsetDateTime?,
    @Column("status")
    val status: String?,
    @Column("role")
    val role: String?,
    @Column("age")
    val age: Int?,
    @Column("country")
    val country: String?,
    @Column("battletag")
    val battletag: String?,
    @Column("discord_id")
    val discordId: String?,
    @Column("main_character_name")
    val mainCharacterName: String?,
    @Column("main_character_realm")
    val mainCharacterRealm: String?,
    @Column("main_character_class")
    val mainCharacterClass: String?,
    @Column("main_character_role")
    val mainCharacterRole: String?,
    @Column("main_character_race")
    val mainCharacterRace: String?,
    @Column("main_character_faction")
    val mainCharacterFaction: String?,
    @Column("main_character_level")
    val mainCharacterLevel: Int?,
    @Column("main_character_region")
    val mainCharacterRegion: String?,
    @Column("synced_at")
    val syncedAt: OffsetDateTime = OffsetDateTime.now(),
)
