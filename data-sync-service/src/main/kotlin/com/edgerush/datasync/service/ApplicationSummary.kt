package com.edgerush.datasync.service

import com.edgerush.datasync.entity.ApplicationEntity
import java.time.OffsetDateTime

data class ApplicationSummary(
    val id: Long,
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
    val mainCharacterRole: String?
) {
    fun toEntity(): ApplicationEntity = ApplicationEntity(
        applicationId = id,
        appliedAt = appliedAt,
        status = status,
        role = role,
        age = age,
        country = country,
        battletag = battletag,
        discordId = discordId,
        mainCharacterName = mainCharacterName,
        mainCharacterRealm = mainCharacterRealm,
        mainCharacterClass = mainCharacterClass,
        mainCharacterRole = mainCharacterRole,
        syncedAt = OffsetDateTime.now()
    )
}

