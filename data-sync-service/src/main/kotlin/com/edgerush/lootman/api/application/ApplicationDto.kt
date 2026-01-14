package com.edgerush.lootman.api.application

import com.edgerush.datasync.entity.ApplicationEntity
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class CreateApplicationRequest(
    @field:NotNull(message = "Application ID is required")
    val applicationId: Long,
    val appliedAt: OffsetDateTime? = null,
    val status: String? = null,
    val role: String? = null,
    val age: Int? = null,
    val country: String? = null,
    val battletag: String? = null,
    val discordId: String? = null,
    val mainCharacterName: String? = null,
    val mainCharacterRealm: String? = null,
    val mainCharacterClass: String? = null,
    val mainCharacterRole: String? = null,
    val mainCharacterRace: String? = null,
    val mainCharacterFaction: String? = null,
    val mainCharacterLevel: Int? = null,
    val mainCharacterRegion: String? = null,
)

data class UpdateApplicationRequest(
    val status: String? = null,
    val role: String? = null,
    val age: Int? = null,
    val country: String? = null,
    val battletag: String? = null,
    val discordId: String? = null,
)

data class ApplicationResponse(
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
) {
    companion object {
        fun from(e: ApplicationEntity) = ApplicationResponse(
            e.applicationId, e.appliedAt, e.status, e.role, e.age, e.country, e.battletag, e.discordId,
            e.mainCharacterName, e.mainCharacterRealm, e.mainCharacterClass, e.mainCharacterRole,
            e.mainCharacterRace, e.mainCharacterFaction, e.mainCharacterLevel, e.mainCharacterRegion, e.syncedAt
        )
    }
}

data class ApplicationExistsResponse(val exists: Boolean)
data class ApplicationCountResponse(val count: Long)
