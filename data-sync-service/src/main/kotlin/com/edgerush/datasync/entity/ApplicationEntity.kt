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
    val appliedAt: OffsetDateTime?,
    val status: String?,
    val role: String?,
    val age: Int?,
    val country: String?,
    val battletag: String?,
    val discordId: String?,
    val mainCharacterName: String?,
    val mainCharacterRealm: String?,
    val mainCharacterClass: String?,
    val mainCharacterRole: String?,
    val mainCharacterRace: String?,
    val mainCharacterFaction: String?,
    val mainCharacterLevel: Int?,
    val mainCharacterRegion: String?,
    val syncedAt: OffsetDateTime,
)
