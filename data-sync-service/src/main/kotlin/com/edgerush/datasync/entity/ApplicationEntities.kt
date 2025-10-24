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
    val syncedAt: OffsetDateTime
)

@Table("application_alts")
data class ApplicationAltEntity(
    @Id
    val id: Long? = null,
    val applicationId: Long,
    val name: String?,
    val realm: String?,
    val `class`: String?,
    val role: String?,
    val level: Int?
)

@Table("application_questions")
data class ApplicationQuestionEntity(
    @Id
    val id: Long? = null,
    val applicationId: Long,
    val question: String?,
    val answer: String?,
    val filesJson: String?
)
